package com.androiddeveloper3005.mychat.fragment


import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.androiddeveloper3005.mychat.R
import com.androiddeveloper3005.mychat.activity.ChatActivity
import com.androiddeveloper3005.mychat.activity.ProfileActivity
import com.androiddeveloper3005.mychat.appconstants.Constans
import com.androiddeveloper3005.mychat.model.Friends
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class FriendsFragment : Fragment() {
    private lateinit var mFriendsList: RecyclerView
    private lateinit var mFriendsDatabase:DatabaseReference
    private lateinit var mUsersDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCurrent_user_id:String
    private lateinit var mMainView:View
    private lateinit var loadingView: LinearLayout
    private lateinit var noDataView: LinearLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_friends, container, false)
        mFriendsList = mMainView.findViewById(R.id.friends_list) as RecyclerView
        mAuth = FirebaseAuth.getInstance()
        mCurrent_user_id = mAuth.getCurrentUser()!!.getUid().toString()
        mFriendsDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.FRIENDS).child(mCurrent_user_id)
        mFriendsDatabase.keepSynced(true)
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.USER_DATABSE_PATH)
        mUsersDatabase.keepSynced(true)
        mFriendsList.setHasFixedSize(true)
        mFriendsList.setLayoutManager(LinearLayoutManager(getContext()))
        loadingView = mMainView.findViewById(R.id.loadingView) as LinearLayout
        noDataView = mMainView.findViewById(R.id.noDataView) as LinearLayout
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


        var friendsRecyclerViewAdapter = object: FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
            Friends::class.java,
            R.layout.single_user_layout,
            FriendsViewHolder::class.java,
            mFriendsDatabase
        ) {
           override protected fun populateViewHolder(friendsViewHolder:FriendsViewHolder, friends: Friends, position:Int) {
               friends!!.date?.let { friendsViewHolder.setDate(it) }
                val list_user_id = getRef(position).getKey()
                mUsersDatabase.child(list_user_id).addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val userName = dataSnapshot.child(Constans.NAME).getValue().toString()
                        val userThumb = dataSnapshot.child(Constans.IMAGE).getValue().toString()
                        if (dataSnapshot.hasChild("online"))
                        {
                            val userOnline = dataSnapshot.child("online").getValue().toString()
                            friendsViewHolder.setUserOnline(userOnline)
                        }
                        friendsViewHolder.setName(userName)
                        friendsViewHolder.setUserImage(userThumb,context!!)
                        friendsViewHolder.mView.setOnClickListener(object:View.OnClickListener {
                           override fun onClick(view:View) {
                                val options = arrayOf<CharSequence>("Open Profile", "Send message")
                                val builder = AlertDialog.Builder(context!!)
                                builder.setTitle("Select Options")
                                builder.setItems(options, object: DialogInterface.OnClickListener {
                                  override  fun onClick(dialogInterface:DialogInterface, i:Int) {
                                        //Click Event for each item.
                                        if (i == 0)
                                        {
                                            val profileIntent = Intent(getContext(), ProfileActivity::class.java)
                                            profileIntent.putExtra("user_id", list_user_id)
                                            startActivity(profileIntent)
                                        }
                                        if (i == 1)
                                        {
                                            val chatIntent = Intent(getContext(), ChatActivity::class.java)
                                            chatIntent.putExtra("user_id", list_user_id)
                                            chatIntent.putExtra("user_name", userName)
                                            chatIntent.putExtra("user_image", userThumb)
                                            startActivity(chatIntent)
                                        }
                                    }
                                })
                                builder.show()
                            }
                        })
                    }
                   override fun onCancelled(databaseError: DatabaseError) {
                    }
                })
            }
        }

        hideLoader()
        mFriendsList.setAdapter(friendsRecyclerViewAdapter)




    }


}

class FriendsViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
    internal var mView:View
    init{
        mView = itemView
    }
    fun setDate(date:String) {
        val userStatusView = mView.findViewById(R.id.user_single_status) as TextView
        userStatusView.setText(date)
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
