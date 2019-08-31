package com.androiddeveloper3005.mychat.activity

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.androiddeveloper3005.mychat.R
import com.androiddeveloper3005.mychat.appconstants.Constans
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import java.text.DateFormat
import java.util.*
import kotlin.collections.HashMap
import com.google.firebase.database.DatabaseReference



class ProfileActivity : AppCompatActivity() {
    private lateinit var mProfileImage: ImageView
    private lateinit var mProfileName: TextView
    private lateinit var mProfileStatus:TextView
    private lateinit var mProfileFriendsCount:TextView
    private lateinit var mProfileSendReqBtn: Button
    private lateinit var mDeclineBtn:Button
    private lateinit var mUsersDatabase: DatabaseReference
    private lateinit var mProgressDialog: ProgressDialog
    private lateinit var mFriendReqDatabase:DatabaseReference
    private lateinit var mFriendDatabase:DatabaseReference
    private lateinit var mNotificationDatabase:DatabaseReference
    private lateinit var mRootRef:DatabaseReference
    private lateinit var mCurrent_user: FirebaseUser
    private lateinit var mCurrent_state:String
    private lateinit var user_id:String
    private lateinit var mAuth: FirebaseAuth



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        var intent = intent
        user_id = intent.getStringExtra("user_id")
        mAuth = FirebaseAuth.getInstance()
        mRootRef = FirebaseDatabase.getInstance().getReference()
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.USER_DATABSE_PATH).child(user_id)
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.FRIEND_REQ)
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.FRIENDS)
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.NOTIFICATION)


        mUsersDatabase.keepSynced(true)
        mFriendReqDatabase.keepSynced(true)
        mFriendDatabase.keepSynced(true)
        mNotificationDatabase.keepSynced(true)

        mCurrent_user = FirebaseAuth.getInstance().getCurrentUser()!!
        mProfileImage = findViewById(R.id.message_profile_layout) as ImageView
        mProfileName = findViewById(R.id.profile_displayName) as TextView
        mProfileStatus = findViewById(R.id.profile_status) as TextView
        mProfileFriendsCount = findViewById(R.id.profile_totalFriends) as TextView
        mProfileSendReqBtn = findViewById(R.id.profile_send_req_btn) as Button
        mDeclineBtn = findViewById(R.id.profile_decline_btn) as Button
        mCurrent_state = "not_friends"
        mDeclineBtn.setVisibility(View.INVISIBLE)
        mDeclineBtn.setEnabled(false)
        mProgressDialog = ProgressDialog(this)
        mProgressDialog.setTitle("Loading User Data")
        mProgressDialog.setMessage("Please wait while we load the user data.")
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()

        //get data
        mUsersDatabase.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError?) {
                Log.w("ProfileActivity ::", "loadProfile:onCancelled", databaseError!!.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                val display_name = dataSnapshot!!.child(Constans.NAME).getValue().toString()
                val status = dataSnapshot!!.child(Constans.STATUS).getValue().toString()
                val image = dataSnapshot!!.child(Constans.IMAGE).getValue().toString()
                mProfileName.setText(display_name)
                mProfileStatus.setText(status)
                Picasso.get().load(image).placeholder(R.drawable.no_image).into(mProfileImage)

        //--------------- FRIENDS LIST / REQUEST FEATURE -----
                mFriendReqDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(object:
                    ValueEventListener {
                    override fun onCancelled(databaseError: DatabaseError?) {

                    }

                    override fun onDataChange(dataSnapshot: DataSnapshot?) {
                        if (dataSnapshot!!.hasChild(user_id)){
                            var req_type  : String = dataSnapshot.child(user_id).child("request_type").getValue().toString()

                            if (req_type.equals("received"))
                            {
                                mCurrent_state = "req_received"
                                mProfileSendReqBtn.setText("Accept Friend Request")
                                mDeclineBtn.setVisibility(View.VISIBLE)
                                mDeclineBtn.setEnabled(true)
                            }
                            else if (req_type.equals("sent"))
                            {
                                mCurrent_state = "req_sent"
                                mProfileSendReqBtn.setText("Cancel Friend Request")
                                mDeclineBtn.setVisibility(View.INVISIBLE)
                                mDeclineBtn.setEnabled(false)
                            }

                            mProgressDialog.dismiss()

                        }else{
                            mFriendDatabase.child(mCurrent_user.getUid()).addListenerForSingleValueEvent(object:
                                ValueEventListener {
                                override fun onDataChange(dataSnapshot:DataSnapshot) {
                                    if (dataSnapshot.hasChild(user_id))
                                    {
                                        mCurrent_state = "friends"
                                        mProfileSendReqBtn.setText("Unfriend this Person")
                                        mDeclineBtn.setVisibility(View.INVISIBLE)
                                        mDeclineBtn.setEnabled(false)
                                    }
                                    mProgressDialog.dismiss()
                                }
                                override fun onCancelled(databaseError:DatabaseError) {
                                    mProgressDialog.dismiss()
                                }
                            })

                        }
                    }

                })







            }

        })

        mProfileSendReqBtn.setOnClickListener {
            mProfileSendReqBtn.setEnabled(false)

            // --------------- NOT FRIENDS STATE ------------
            if (mCurrent_state.equals("not_friends")) {
                mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).child("request_type").setValue("sent")
                    .addOnSuccessListener {

                        mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).child("request_type").setValue("received")
                            .addOnSuccessListener {

                                mProfileSendReqBtn.setEnabled(true)
                                mCurrent_state = "req_sent"
                                mProfileSendReqBtn.setText("Cancel Friend Request")

                            }

                    }

                //notification
                val newNotificationref = mRootRef.child("notifications").child(user_id).push()
                val newNotificationId = newNotificationref.key
                mNotificationDatabase.child(user_id).child(newNotificationId).child("from").setValue(mCurrent_user.getUid())
                    .addOnSuccessListener {
                        mNotificationDatabase.child(user_id).child(newNotificationId).child("type").setValue("request")
                            .addOnSuccessListener {

                            }

                    }


            }

            // - -------------- CANCEL REQUEST STATE ------------
            if(mCurrent_state.equals("req_sent")){
                mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener {
                    mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener {
                        mProfileSendReqBtn.setEnabled(true)
                        mCurrent_state = "not_friends"
                        mProfileSendReqBtn.setText("Sent Friend Request")

                    }
                }

            }

            // ------------ REQ RECEIVED STATE ----------

            if (mCurrent_state.equals("req_received"))
            {
                val currentDate = DateFormat.getDateTimeInstance().format(Date())
                val friendsMap = HashMap<String,String>()
                friendsMap.put("Friends/" + mCurrent_user.getUid() + "/" + user_id + "/date", currentDate)
                friendsMap.put("Friends/" + user_id + "/" + mCurrent_user.getUid() + "/date", currentDate)
                mRootRef.updateChildren(friendsMap as Map<String ,Any?>
                ) { databaseError, databaseReference ->
                    if (databaseError == null) {
                        mFriendReqDatabase.child(mCurrent_user.getUid()).child(user_id).removeValue().addOnSuccessListener {
                            mFriendReqDatabase.child(user_id).child(mCurrent_user.getUid()).removeValue().addOnSuccessListener {
                                //remove friends request
                                mProfileSendReqBtn.setEnabled(true)
                                mCurrent_state = "friends"
                                mProfileSendReqBtn.setText("Unfriend this Person")
                                mDeclineBtn.setVisibility(View.INVISIBLE)
                                mDeclineBtn.setEnabled(false)
                            }
                        }

                    } else {
                        val error = databaseError.getMessage()
                        Toast.makeText(this@ProfileActivity, error, Toast.LENGTH_SHORT).show()
                    }
                }

            }

            // ------------ UNFRIENDS ---------

            if (mCurrent_state.equals("friends"))
            {
                val unfriendMap = HashMap<String,Any?>()
                unfriendMap.put("friends/" + mCurrent_user.getUid() + "/" + user_id, null)
                unfriendMap.put("friends/" + user_id + "/" + mCurrent_user.getUid(), null)
                mRootRef.updateChildren(unfriendMap, object: DatabaseReference.CompletionListener {
                    override fun onComplete(databaseError:DatabaseError, databaseReference:DatabaseReference) {
                        if (databaseError == null)
                        {
                            mCurrent_state = "not_friends"
                            mProfileSendReqBtn.setText("Send Friend Request")
                            mDeclineBtn.setVisibility(View.INVISIBLE)
                            mDeclineBtn.setEnabled(false)
                        }
                        else
                        {
                            val error = databaseError.getMessage()
                            Toast.makeText(this@ProfileActivity, error, Toast.LENGTH_SHORT).show()
                        }
                        mProfileSendReqBtn.setEnabled(true)
                    }
                })
            }



        }

    }



    override fun onStart() {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = mAuth.getCurrentUser()
        if (currentUser != null)
        {
            mRootRef.child(Constans.USER_DATABSE_PATH).child(mAuth.currentUser!!.uid).child("online").setValue("true")
        }

    }

    override fun onStop() {
        super.onStop()
        val currentUser = mAuth.getCurrentUser()
        if (currentUser != null)
        {
            mRootRef.child(Constans.USER_DATABSE_PATH).child(mAuth.currentUser!!.uid).child("online").setValue(ServerValue.TIMESTAMP)
        }
    }

}

