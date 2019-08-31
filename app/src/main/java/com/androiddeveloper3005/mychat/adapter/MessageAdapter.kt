package com.androiddeveloper3005.mychat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.androiddeveloper3005.mychat.R
import com.androiddeveloper3005.mychat.appconstants.Constans
import com.androiddeveloper3005.mychat.model.Messages
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView


class MessageAdapter(val mMessageList: List<Messages>, val mContext: Context):RecyclerView.Adapter<MessageAdapter.MessageViewHolder>(){
    private lateinit var mUserDatabase: DatabaseReference
    private lateinit var  mAuth: FirebaseAuth
    val MSG_TYPE_LEFT = 0
    val MSG_TYPE_RIGHT = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        if (viewType == MSG_TYPE_RIGHT) {
            val view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_right, parent, false)
            return MessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(mContext).inflate(R.layout.chat_item_left, parent, false)
            return MessageViewHolder(view)
        }
    }

    override fun getItemCount(): Int {
       return mMessageList.size
    }

    override fun getItemViewType(position: Int): Int {
        mAuth = FirebaseAuth.getInstance()
        return if (mMessageList[position].from.equals(mAuth.currentUser!!.uid)) {
            MSG_TYPE_RIGHT
        } else {
            MSG_TYPE_LEFT
        }
    }

    override fun onBindViewHolder(viewHolder: MessageViewHolder, position: Int) {
        var c = mMessageList.get(position)
        var from_user = c.from
        var message_type = c.type
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.USER_DATABSE_PATH).child(from_user);

        mUserDatabase.addValueEventListener(object: ValueEventListener {
           override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.child(Constans.NAME).getValue().toString()
                val image = dataSnapshot.child(Constans.IMAGE).getValue().toString()
                //viewHolder.displayName.setText(name)
                Picasso.get().load(image)
                    .placeholder(R.drawable.no_image).into(viewHolder.profileImage)
            }
           override fun onCancelled(databaseError: DatabaseError) {
            }
        })
        if (message_type.equals("text"))
        {
            viewHolder.messageText.setVisibility(View.VISIBLE)
            viewHolder.messageText.setText(c.message)
        }
        else
        {
            viewHolder.messageImage.setVisibility(View.VISIBLE)
            Picasso.get().load(c.message)
                .placeholder(R.drawable.no_image).into(viewHolder.messageImage)
        }
    }


    class MessageViewHolder(view: View): RecyclerView.ViewHolder(view) {
        var messageText: TextView
        var profileImage: CircleImageView
        //var displayName:TextView
        var messageImage: ImageView
        init{
            messageText = view.findViewById(R.id.message_text_layout) as TextView
            profileImage = view.findViewById(R.id.message_profile_layout) as CircleImageView
            //displayName = view.findViewById(R.id.name_text_layout) as TextView
            messageImage = view.findViewById(R.id.message_image_layout) as ImageView
        }

}



}