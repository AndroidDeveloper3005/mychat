package com.androiddeveloper3005.mychat.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.androiddeveloper3005.mychat.appconstants.Constans
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import javax.xml.datatype.DatatypeConstants.DAYS
import java.util.*
import java.util.concurrent.TimeUnit
import com.google.firebase.database.DataSnapshot
import com.google.firebase.storage.FirebaseStorage


class MyService : Service() {
    private lateinit var messageRef : DatabaseReference
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mCurrentUID :String


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onCreate() {
        super.onCreate()

        mAuth = FirebaseAuth.getInstance()
        mCurrentUID = mAuth.currentUser?.uid.toString()
        messageRef = FirebaseDatabase.getInstance().reference.child(Constans.MESSAGES).child(mCurrentUID)
        val cutoff =( Date().getTime() - TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES)).toString()
        val oldData : Query =messageRef.orderByChild(Constans.TIMESTAMP).endAt(cutoff)

        oldData.addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(snapshot: DataSnapshot?) {
                for (itemSnapshot in snapshot!!.getChildren()) {
                    val key = itemSnapshot.getRef().key
                    var storageRef = FirebaseStorage.getInstance().getReference().child(Constans.MESSAGES_IMAGE).child(mCurrentUID).child(key)
                    storageRef.delete().addOnSuccessListener {
                        Toast.makeText(applicationContext, "Data Deleted.", Toast.LENGTH_LONG).show()
                    }
                    itemSnapshot.getRef().removeValue()


                }
            }

        })

    }
}
