package com.androiddeveloper3005.mychat.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.androiddeveloper3005.mychat.R
import com.androiddeveloper3005.mychat.activity.ProfileActivity
import com.androiddeveloper3005.mychat.activity.SettingActivity
import com.androiddeveloper3005.mychat.listener.CustomItemClickListener
import com.androiddeveloper3005.mychat.model.Users
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class UsersAdapter(val user_list: ArrayList<Users> ,val context: Context)  : RecyclerView.Adapter<UsersAdapter.ViewHolder>(){



    override fun getItemCount(): Int {
        return user_list.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsersAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.single_user_layout, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val items = user_list.get(position)
        val user_id = items.uid.toString()
        items.name?.let { holder.setDisplayName(it) }
        items.status?.let { holder.setUserStatus(it) }
        items.image?.let { holder.setUserImage(it,context) }

        holder.setOnCustomItemClickListener(object : CustomItemClickListener{
            override fun onCustomItemClickListener(view: View, position: Int) {
                var profileIntent  = Intent(context,ProfileActivity::class.java)
                profileIntent .putExtra("user_id",user_id)
                profileIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                holder.mView.context.startActivity(profileIntent)
            }

        })

    }


     class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView) , View.OnClickListener {
         var mView : View
         var currentPositin : Int = 0
         var customItemClickListener : CustomItemClickListener?=null

        init{
            mView = itemView
            mView.setOnClickListener(this)

        }

         fun setOnCustomItemClickListener(itemClickListener: CustomItemClickListener){
             this.customItemClickListener = itemClickListener
         }

         override fun onClick(view: View?) {
             this.customItemClickListener!!.onCustomItemClickListener(view!!,adapterPosition)

         }

         fun setDisplayName(name : String) {
            val userNameView : TextView = mView.findViewById(R.id.user_single_name)
            userNameView.setText(name)
        }
        fun setUserStatus(status : String) {
            val userStatusView : TextView = mView.findViewById(R.id.user_single_status)
            userStatusView.setText(status)
        }
        fun setUserImage(thumb_image : String, ctx : Context) {
            val userImageView : CircleImageView = mView.findViewById(R.id.user_single_image)
            Picasso.get().load(thumb_image).placeholder(R.drawable.no_image).into(userImageView)
        }
    }

}


