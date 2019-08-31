package com.androiddeveloper3005.mychat.fragment


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.androiddeveloper3005.mychat.R
import com.androiddeveloper3005.mychat.appconstants.Constans
import com.androiddeveloper3005.mychat.model.FriendsRequest
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView
import java.text.DateFormat
import java.util.*
import kotlin.collections.HashMap


class RequestFragment : Fragment() {
    private lateinit var mRequestList: RecyclerView
    private lateinit var mRequestDatabase: DatabaseReference
    private lateinit var mRootRef : DatabaseReference
    private lateinit var mUsersDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mCurrent_user_id:String
    private lateinit var mCurrent_state:String
    private lateinit var mMainView:View
    private lateinit var loadingView: LinearLayout
    private lateinit var noDataView: LinearLayout
    private var list_user_id :String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mMainView = inflater.inflate(R.layout.fragment_request, container, false)
        mRequestList = mMainView.findViewById(R.id.request_list) as RecyclerView
        mAuth = FirebaseAuth.getInstance()
        mCurrent_user_id = mAuth.getCurrentUser()!!.getUid()
        mRootRef = FirebaseDatabase.getInstance().getReference()
        mRequestDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.FRIEND_REQ).child(mCurrent_user_id)
        mRequestDatabase.keepSynced(true)
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.USER_DATABSE_PATH)
        mUsersDatabase.keepSynced(true)
        mRequestList.setHasFixedSize(true)
        mRequestList.setLayoutManager(LinearLayoutManager(context))
        mCurrent_state = "not_friends"


        return mMainView
    }


    override fun onStart() {
        super.onStart()
        var requestAdapter = object: FirebaseRecyclerAdapter<FriendsRequest, RequestViewHolder>(
            FriendsRequest::class.java
            ,R.layout.single_request_layout
            ,RequestViewHolder::class.java
            ,mRequestDatabase
        ){
            override fun populateViewHolder(viewHolder: RequestViewHolder, model: FriendsRequest?, position: Int) {
                val request_type = model!!.request_type.toString()
                list_user_id = getRef(position).getKey()
                if (request_type == "received") {
                    viewHolder!!.accept_btn.setVisibility(View.VISIBLE)
                    viewHolder!!.reject_btn.setVisibility(View.VISIBLE)
                } else {
                    viewHolder!!.reject_btn.setVisibility(View.VISIBLE)
                }

                mUsersDatabase.child(list_user_id).addValueEventListener(object: ValueEventListener {
                   override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val userName = dataSnapshot.child(Constans.NAME).getValue().toString()
                        val userImage = dataSnapshot.child(Constans.IMAGE).getValue().toString()
                        val status = dataSnapshot.child(Constans.STATUS).getValue().toString()
                        viewHolder.setName(userName)
                        activity?.let { viewHolder.setUserImage(userImage, it) }
                        //viewHolder.setStatus(status)
                    }
                   override fun onCancelled(databaseError: DatabaseError) {
                    }
                })


                viewHolder.accept_btn.setOnClickListener {
                    val currentDate = DateFormat.getDateTimeInstance().format(Date())
                    val friendsMap = HashMap<String,Any?>()
                    friendsMap.put("friends/" + mCurrent_user_id + "/" + list_user_id + "/date", currentDate)
                    friendsMap.put("friends/" + list_user_id + "/" + mCurrent_user_id + "/date", currentDate)
                    friendsMap.put("friend_req/" + mCurrent_user_id + "/" + list_user_id, null)
                    friendsMap.put("friend_req/" + list_user_id + "/" + mCurrent_user_id, null)
                    mRootRef.updateChildren(friendsMap, object:DatabaseReference.CompletionListener {

                        override fun onComplete(databaseError:DatabaseError?, databaseReference:DatabaseReference) {
                            if (databaseError == null)
                            {

                            }
                            else
                            {
                                val error = databaseError.getMessage()
                                Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show()
                            }
                        }
                    })
                }


                viewHolder.reject_btn.setOnClickListener {

                        mRootRef.child(Constans.FRIEND_REQ).child(mCurrent_user_id).child(list_user_id).removeValue().addOnSuccessListener(
                            object: OnSuccessListener<Void> {
                                override fun onSuccess(aVoid:Void?) {
                                    mRootRef.child(Constans.FRIEND_REQ).child(list_user_id).child(mCurrent_user_id).removeValue().addOnSuccessListener(

                                        object:OnSuccessListener<Void> {

                                            override fun onSuccess(aVoid:Void?) {

                                            }
                                        }
                                    )
                                }
                            })


                }


            }

        }
        mRequestList.setAdapter(requestAdapter)

    }

    class RequestViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        internal var mView:View
        var accept_btn: Button
        var reject_btn:Button
        init{
            mView = itemView
            accept_btn = mView.findViewById(R.id.request_user_accept_btn)
            reject_btn = mView.findViewById(R.id.request_user_reject_btn)
        }
        fun setName(name:String) {
            val userNameView = mView.findViewById(R.id.request_user_name) as TextView
            userNameView.setText(name)
        }
        fun setUserImage(thumb_image:String, ctx: Context) {
            val userImageView = mView.findViewById(R.id.request_user_image) as ImageView
            Picasso.get().load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE).into(userImageView, object: Callback {
               override fun onSuccess() {
                }
               override fun onError(e:Exception) {
                    Picasso.get().load(thumb_image).into(userImageView)
                }
            })
        }

    }


}
