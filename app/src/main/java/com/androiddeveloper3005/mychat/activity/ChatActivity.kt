package com.androiddeveloper3005.mychat.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.androiddeveloper3005.mychat.R
import com.androiddeveloper3005.mychat.adapter.MessageAdapter
import com.androiddeveloper3005.mychat.appconstants.Constans
import com.androiddeveloper3005.mychat.model.Messages
import com.androiddeveloper3005.mychat.service.MyService
import com.androiddeveloper3005.mychat.utills.GetTimeAgo
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.ChildEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import id.zelory.compressor.Compressor
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException


class ChatActivity : AppCompatActivity() {
    private lateinit var mChatUser:String
    private lateinit var mChatToolbar: Toolbar
    private lateinit var mRootRef: DatabaseReference
    private lateinit var mTitleView: TextView
    private lateinit var mLastSeenView:TextView
    private lateinit var mProfileImage: CircleImageView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCurrentUserId:String
    private lateinit var mChatAddBtn: ImageButton
    private lateinit var mChatSendBtn:ImageButton
    private lateinit var mChatMessageView: EditText
    private lateinit var mMessagesList: RecyclerView
    private lateinit var mRefreshLayout: SwipeRefreshLayout
    private var messagesList : ArrayList<Messages>  = ArrayList<Messages>()
    private lateinit var mLinearLayout: LinearLayoutManager
    private lateinit var mAdapter: MessageAdapter
    private var TOTAL_ITEMS_TO_LOAD = 10
    private var mCurrentPage = 1
    private var GALLERY_PICK = 1
    // Storage Firebase
    private lateinit var mImageStorage: StorageReference
    //New Solution
    private var itemPos = 0
    private var mLastKey = ""
    private var mPrevKey = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        startService(Intent(applicationContext, MyService::class.java))
        mChatToolbar = findViewById(R.id.chat_app_bar) as Toolbar
        setSupportActionBar(mChatToolbar)
        val actionBar = getSupportActionBar()
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar?.setDisplayShowCustomEnabled(true)
        mRootRef = FirebaseDatabase.getInstance().getReference()
        mAuth = FirebaseAuth.getInstance()
        mCurrentUserId = mAuth.getCurrentUser()!!.getUid()
        mChatUser = getIntent().getStringExtra("user_id")
        var userName = getIntent().getStringExtra("user_name")
        var userImage = getIntent().getStringExtra("user_image")
        //supportActionBar!!.setTitle(userName)
        var inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        var action_bar_view = inflater.inflate(R.layout.chat_custom_bar, null)
        actionBar!!.setCustomView(action_bar_view)

        //recyclerview
        mAdapter = MessageAdapter(messagesList as List<Messages>,applicationContext)
        mMessagesList = findViewById(R.id.messages_list) as RecyclerView

        mMessagesList = findViewById(R.id.messages_list) as RecyclerView
        mRefreshLayout = findViewById(R.id.message_swipe_layout) as SwipeRefreshLayout
        mLinearLayout = LinearLayoutManager(this)
        mMessagesList.setHasFixedSize(true)
        mMessagesList.setLayoutManager(mLinearLayout)
        mMessagesList.setAdapter(mAdapter)

        loadMessages()

        // ---- Custom Action bar Items ----
        mTitleView = findViewById(R.id.custom_bar_title) as TextView
        mLastSeenView = findViewById(R.id.custom_bar_seen) as TextView
        mProfileImage = findViewById(R.id.custom_bar_image) as CircleImageView

        mChatAddBtn = findViewById(R.id.chat_add_btn) as ImageButton
        mChatSendBtn = findViewById(R.id.chat_send_btn) as ImageButton
        mChatMessageView = findViewById(R.id.chat_message_view) as EditText

        mTitleView.setText(userName)
        Picasso.get().load(userImage)
            .placeholder(R.drawable.no_image).into(mProfileImage)
        userImage
        //------- IMAGE STORAGE ---------
        mImageStorage = FirebaseStorage.getInstance().getReference()
        mRootRef.child(Constans.CHAT).child(mCurrentUserId).child(mChatUser).child("seen").setValue(true)



        mRootRef.child(Constans.USER_DATABSE_PATH).child(mChatUser).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val online = dataSnapshot.child(Constans.ONLINE).getValue().toString()
                val image = dataSnapshot.child(Constans.IMAGE).getValue().toString()
                if (online == "true")
                {
                    mLastSeenView.setText("Online")
                }
                else
                {
                    var getTimeAgo = GetTimeAgo()
                    var lastTime = java.lang.Long.parseLong(online)
                    var lastSeenTime = getTimeAgo.getTimeAgo(lastTime, applicationContext)
                    mLastSeenView.setText(""+lastSeenTime)
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {
            }
        })


        mRootRef.child(Constans.CHAT).child(mCurrentUserId).addValueEventListener(object:ValueEventListener {
           override fun onDataChange(dataSnapshot:DataSnapshot) {
                if (!dataSnapshot.hasChild(mChatUser))
                {
                    val chatAddMap = HashMap<String,Any?>()
                    chatAddMap.put("seen", false)
                    chatAddMap.put("timestamp", ServerValue.TIMESTAMP)

                    val chatUserMap = HashMap<String,Any?>()
                    chatUserMap.put("chat/" + mCurrentUserId + "/" + mChatUser, chatAddMap)
                    chatUserMap.put("chat/" + mChatUser + "/" + mCurrentUserId, chatAddMap)

                    mRootRef.updateChildren(chatUserMap, object:DatabaseReference.CompletionListener {

                       override fun onComplete(databaseError:DatabaseError?, databaseReference:DatabaseReference) {
                            if (databaseError != null)
                            {
                                Log.d("CHAT_LOG", databaseError.getMessage().toString())
                            }
                        }
                    })
                }
            }
           override fun onCancelled(databaseError:DatabaseError) {
            }
        })

        mChatSendBtn.setOnClickListener(object: View.OnClickListener {
           override fun onClick(view:View) {
                sendMessage()
            }
        })

        mRefreshLayout.setOnRefreshListener(object: SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                mCurrentPage++
                itemPos = 0
                loadMoreMessages()
            }

        })
        mChatAddBtn.setOnClickListener(object:View.OnClickListener {
           override fun onClick(view:View) {
                val galleryIntent = Intent()
                galleryIntent.setType("image/*")
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT)
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK)
            }
        })



    }

    private fun loadMoreMessages() {
        val messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser)
        val messageQuery = messageRef.orderByKey().endAt(mLastKey).limitToLast(10)
        messageQuery.addChildEventListener(object:ChildEventListener {
          override  fun onChildAdded(dataSnapshot:DataSnapshot, s:String?) {
                val message = dataSnapshot.getValue(Messages::class.java)
                val messageKey = dataSnapshot.getKey()
                if (!mPrevKey.equals(messageKey))
                {
                    messagesList.add(itemPos++, message!!)
                }
                else
                {
                    mPrevKey = mLastKey
                }
                if (itemPos === 1)
                {
                    mLastKey = messageKey
                }
                Log.d("TOTALKEYS", "Last Key : " + mLastKey + " | Prev Key : " + mPrevKey + " | Message Key : " + messageKey)
                mAdapter.notifyDataSetChanged()
                mRefreshLayout.setRefreshing(false)
                mLinearLayout.scrollToPositionWithOffset(10, 0)
            }
            override fun onChildChanged(dataSnapshot:DataSnapshot, s:String) {
            }
            override fun onChildRemoved(dataSnapshot:DataSnapshot) {
            }
            override fun onChildMoved(dataSnapshot:DataSnapshot, s:String) {
            }
            override fun onCancelled(databaseError:DatabaseError) {
            }
        })
    }



    private fun loadMessages() {
        val messageRef = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser)
        val messageQuery = messageRef.limitToLast(mCurrentPage * TOTAL_ITEMS_TO_LOAD)

        messageQuery.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

                val message = dataSnapshot.getValue(Messages::class.java)

                itemPos++

                if (itemPos === 1) {

                    val messageKey = dataSnapshot.key

                    mLastKey = messageKey
                    mPrevKey = messageKey

                }

                messagesList.add(message!!)
                mAdapter.notifyDataSetChanged()

                mMessagesList.scrollToPosition(messagesList.size - 1)

                mRefreshLayout.isRefreshing = false

            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    

    private fun sendMessage() {
        val message = mChatMessageView.getText().toString()
        if (!TextUtils.isEmpty(message))
        {
            val current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser
            val chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId
            val user_message_push = mRootRef.child("messages")
                .child(mCurrentUserId).child(mChatUser).push()
            val push_id = user_message_push.getKey()

            val messageMap = HashMap<String,Any?>()
            messageMap.put("message", message)
            messageMap.put("seen", false)
            messageMap.put("type", "text")
            messageMap.put("timestamp", ServerValue.TIMESTAMP)
            messageMap.put("from", mCurrentUserId)

            val messageUserMap = HashMap<String,Any?>()
            messageUserMap.put(current_user_ref + "/" + push_id, messageMap)
            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap)
            mChatMessageView.setText("")

            mRootRef.child(Constans.CHAT).child(mCurrentUserId).child(mChatUser).child("seen").setValue(true)
            mRootRef.child(Constans.CHAT).child(mCurrentUserId).child(mChatUser).child("timestamp").setValue(ServerValue.TIMESTAMP)
            mRootRef.child(Constans.CHAT).child(mChatUser).child(mCurrentUserId).child("seen").setValue(false)
            mRootRef.child(Constans.CHAT).child(mChatUser).child(mCurrentUserId).child("timestamp").setValue(ServerValue.TIMESTAMP)

            mRootRef.updateChildren(messageUserMap, object:DatabaseReference.CompletionListener {

               override fun onComplete(databaseError:DatabaseError?, databaseReference:DatabaseReference) {

                    if (databaseError != null)
                    {
                        Log.d("CHAT_LOG", databaseError.getMessage().toString())
                    }
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode === GALLERY_PICK && resultCode === RESULT_OK && data != null)
        {
           var  mImageUri : Uri? = data!!.getData()
            try
            {
               var bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri)
            }
            catch (e: IOException) {
                e.printStackTrace()
            }
            CropImage.activity(mImageUri)
                .start(this)
        }
        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            val result = CropImage.getActivityResult(data)
            if (resultCode === RESULT_OK)
            {
               var  resultUri : Uri = result.getUri()
                var thump_filepath : File = File(resultUri.getPath())
                try
                {
                    //bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageUri);
                   var thumb_bitmap = Compressor(this).compressToBitmap(thump_filepath)
                    val byteArrayOutputStream = ByteArrayOutputStream()
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream)
                    val thumb_byte = byteArrayOutputStream.toByteArray()
                    val current_user_ref = "messages/" + mCurrentUserId + "/" + mChatUser
                    val chat_user_ref = "messages/" + mChatUser + "/" + mCurrentUserId
                    val user_message_push = mRootRef.child("messages").child(mCurrentUserId).child(mChatUser).push()
                    val push_id = user_message_push.getKey()
                    val filepath = mImageStorage.child("message_images").child(mCurrentUserId).child(push_id)
                    val uploadTask = filepath.putBytes(thumb_byte)
                    uploadTask.addOnSuccessListener(object: OnSuccessListener<UploadTask.TaskSnapshot> {
                      override  fun onSuccess(taskSnapshot:UploadTask.TaskSnapshot) {
                            val downloadUri = taskSnapshot.getDownloadUrl().toString()
                            val messageMap = HashMap<String,Any?>()
                            messageMap.put("message", downloadUri)
                            messageMap.put("seen", false)
                            messageMap.put("type", "image")
                            messageMap.put("time", ServerValue.TIMESTAMP)
                            messageMap.put("from", mCurrentUserId)
                            val messageUserMap = HashMap<String,Any?>()
                            messageUserMap.put(current_user_ref + "/" + push_id, messageMap)
                            messageUserMap.put(chat_user_ref + "/" + push_id, messageMap)
                            mChatMessageView.setText("")
                            mRootRef.updateChildren(messageUserMap, object:DatabaseReference.CompletionListener {
                               override fun onComplete(databaseError:DatabaseError?, databaseReference:DatabaseReference) {
                                    if (databaseError != null) {
                                        Log.d("CHAT_LOG", databaseError.getMessage().toString())
                                    }
                                }
                            })
                        }
                    })
                }
                catch (e:IOException) {
                    e.printStackTrace()
                }
            }
            else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                val error = result.getError()
            }
        }
    }
}
