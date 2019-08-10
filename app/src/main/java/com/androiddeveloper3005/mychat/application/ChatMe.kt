package com.androiddeveloper3005.mychat.application

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import com.squareup.picasso.Picasso.*

class ChatMe: Application() {
    private lateinit var mUserDatabase: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    override fun onCreate() {
        super.onCreate()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        /* Picasso */
        val builder = Builder(this)
        builder.downloader(OkHttp3Downloader(this!!, Integer.MAX_VALUE as Long))
        val built = builder.build()
        built.setIndicatorsEnabled(true)
        built.setLoggingEnabled(true)
        setSingletonInstance(built)
        mAuth = FirebaseAuth.getInstance()
        if (mAuth.getCurrentUser() != null)
        {
            mUserDatabase = FirebaseDatabase.getInstance()
                .getReference().child("Users").child(mAuth.getCurrentUser()!!.getUid())
            mUserDatabase.addValueEventListener(object: ValueEventListener {
               override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot != null)
                    {
                        mUserDatabase.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP)
                    }
                }
              override  fun onCancelled(databaseError:DatabaseError) {
                }
            })
        }
    }
}