package com.androiddeveloper3005.mychat.fragment


import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddeveloper3005.mychat.R
import com.androiddeveloper3005.mychat.activity.ChatActivity
import com.androiddeveloper3005.mychat.appconstants.Constans
import com.androiddeveloper3005.mychat.model.Conv
import com.androiddeveloper3005.mychat.utills.GetTimeAgo
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView



class ChatsFragment : Fragment() {
    private lateinit var mConvList:RecyclerView
    private lateinit var mConvDatabase: DatabaseReference
    private lateinit var mMessageDatabase:DatabaseReference
    private lateinit var mUsersDatabase:DatabaseReference
    private lateinit var mAuth:FirebaseAuth
    private lateinit var mCurrent_user_id:String
    private lateinit var mMainView:View
    private lateinit var loadingView: LinearLayout
    private lateinit var noDataView: LinearLayout


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_chats, container, false)
        loadingView = mMainView.findViewById(R.id.loadingView) as LinearLayout
        noDataView = mMainView.findViewById(R.id.noDataView) as LinearLayout
        mConvList = mMainView.findViewById(R.id.conv_list) as RecyclerView
        mAuth = FirebaseAuth.getInstance()
        mCurrent_user_id = mAuth.getCurrentUser()!!.getUid()
        mConvDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.CHAT).child(mCurrent_user_id)
        mConvDatabase.keepSynced(true)
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.USER_DATABSE_PATH)
        mMessageDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.MESSAGES).child(mCurrent_user_id)
        mUsersDatabase.keepSynced(true)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.setReverseLayout(true)
        linearLayoutManager.setStackFromEnd(true)
        mConvList.setHasFixedSize(true)
        mConvList.setLayoutManager(linearLayoutManager)

        return mMainView
    }



    fun showLoader() {
        if (loadingView != null)
        {
            loadingView.setVisibility(View.VISIBLE)
        }
        if (noDataView != null)
        {
            noDataView.setVisibility(View.GONE)
        }
    }
    fun hideLoader() {
        if (loadingView != null)
        {
            loadingView.setVisibility(View.GONE)
        }
        if (noDataView != null)
        {
            noDataView.setVisibility(View.GONE)
        }
    }
    fun showEmptyView() {
        if (loadingView != null)
        {
            loadingView.setVisibility(View.GONE)
        }
        if (noDataView != null)
        {
            noDataView.setVisibility(View.VISIBLE)
        }
    }

    override fun onStart() {
        super.onStart()
        var conversationQuery : Query = mConvDatabase.orderByChild(Constans.TIMESTAMP)
        var firebaseConvAdapter  = object: FirebaseRecyclerAdapter<Conv, ConvViewHolder>(
            Conv::class.java,
            R.layout.single_user_layout,
            ConvViewHolder::class.java,
            conversationQuery

        ){
            override fun populateViewHolder(convViewHolder: ConvViewHolder?, conv: Conv?, position: Int) {
                val list_user_id = getRef(position).getKey()
                val lastMessageQuery = mMessageDatabase.child(list_user_id).limitToLast(1)

                lastMessageQuery.addChildEventListener(object: ChildEventListener {

                   override fun onChildAdded(dataSnapshot: DataSnapshot, s:String?) {


                       val data = dataSnapshot.child("message").getValue().toString()
                       val type = dataSnapshot.child("type").value.toString()

                       if (type.equals("image"))
                       {
                           convViewHolder?.setMessage(" A photo", conv?.isSeen)
                       }
                       else
                       {
                           convViewHolder?.setMessage(data, conv?.isSeen)
                       }

                    }

                   override fun onChildChanged(dataSnapshot:DataSnapshot, s:String?) {

                    }
                   override fun onChildRemoved(dataSnapshot:DataSnapshot) {

                    }
                   override fun onChildMoved(dataSnapshot:DataSnapshot, s:String?) {

                    }
                   override fun onCancelled(databaseError:DatabaseError) {

                    }
                })


                mUsersDatabase.child(list_user_id).addValueEventListener(object:ValueEventListener {
                    override fun onDataChange(dataSnapshot:DataSnapshot) {
                        val userName = dataSnapshot.child(Constans.NAME).getValue().toString()
                        val userThumb = dataSnapshot.child(Constans.IMAGE).getValue().toString()
                        if (dataSnapshot.hasChild(Constans.ONLINE))
                        {
                            val userOnline = dataSnapshot.child(Constans.ONLINE).getValue().toString()
                            convViewHolder?.setUserOnline(userOnline)
                        }
                        convViewHolder?.setName(userName)
                        context?.let { convViewHolder?.setUserImage(userThumb, it) }
                        convViewHolder?.mView?.setOnClickListener(object:View.OnClickListener {
                            override  fun onClick(view:View) {
                                val chatIntent = Intent(context, ChatActivity::class.java)
                                chatIntent.putExtra("user_id", list_user_id)
                                chatIntent.putExtra("user_name", userName)
                                chatIntent.putExtra("user_image", userThumb)
                                startActivity(chatIntent)
                            }
                        })
                    }
                    override fun onCancelled(databaseError:DatabaseError) {
                    }
                })

            }

        }
        hideLoader()
        mConvList.setAdapter(firebaseConvAdapter);
    }


}


class ConvViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    internal var mView:View
    init{
        mView = itemView
    }

    fun setMessage(message:String, isSeen:Boolean?) {
        val userStatusView = mView.findViewById(R.id.user_single_status) as TextView
        userStatusView.setText(message)
        if (!isSeen!!)
        {
            userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.BOLD)
        }
        else
        {
            userStatusView.setTypeface(userStatusView.getTypeface(), Typeface.NORMAL)
        }
    }
    fun setName(name:String) {
        val userNameView = mView.findViewById(R.id.user_single_name) as TextView
        userNameView.setText(name)
    }
    fun setUserImage(thumb_image:String, ctx: Context) {
        val userImageView = mView.findViewById(R.id.user_single_image) as CircleImageView
        Picasso.get().load(thumb_image).placeholder(R.drawable.no_image).into(userImageView)
    }
    fun setUserOnline(online_status:String) {
        val userOnlineView = mView.findViewById(R.id.user_single_online_icon) as ImageView
        if (online_status == "true")
        {
            userOnlineView.setVisibility(View.VISIBLE)
        }
        else
        {
            userOnlineView.setVisibility(View.INVISIBLE)
        }
    }
}
