package com.androiddeveloper3005.mychat.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.viewpager.widget.ViewPager
import com.androiddeveloper3005.mychat.R
import com.androiddeveloper3005.mychat.adapter.SectionsPagerAdapter
import com.androiddeveloper3005.mychat.appconstants.Constans
import com.androiddeveloper3005.mychat.service.MyService
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.iid.FirebaseInstanceId


class MainActivity : AppCompatActivity() {
    private lateinit var mViewPager : ViewPager
    private lateinit var adapter: SectionsPagerAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var toolbar: Toolbar
    private lateinit var auth : FirebaseAuth
    private lateinit var mUsersDatabase: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        toolbar = findViewById(R.id.toolBar)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle("Chat Me")
        //view pager
        mViewPager = findViewById(R.id.main_tab_pager)
        adapter = SectionsPagerAdapter(supportFragmentManager)
        //setAdapter
        mViewPager.adapter  = adapter

        tabLayout = findViewById(R.id.main_tabs)
        tabLayout.setupWithViewPager(mViewPager)
        auth = FirebaseAuth.getInstance()
        var deviceToken = FirebaseInstanceId.getInstance().token
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.USER_DATABSE_PATH).child(auth.currentUser!!.uid)
        mUsersDatabase.child(Constans.DEVICETOKEN).setValue(deviceToken).addOnSuccessListener {

        }
        startService(Intent(applicationContext,MyService::class.java))


    }

    override fun onStart() {
        super.onStart()
        var deviceToken = FirebaseInstanceId.getInstance().token
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.USER_DATABSE_PATH).child(auth.currentUser!!.uid)
        mUsersDatabase.child(Constans.DEVICETOKEN).setValue(deviceToken).addOnSuccessListener {

        }

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.getCurrentUser()
        if (currentUser != null)
        {
            mUsersDatabase.child("online").setValue("true")
        }

    }

    override fun onStop() {
        super.onStop()
        val currentUser = auth.getCurrentUser()
        if (currentUser != null)
        {
            mUsersDatabase.child("online").setValue(ServerValue.TIMESTAMP)
        }
    }
    

   override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.account_setting, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.logout_setting){
            FirebaseAuth.getInstance().signOut()
            sendToStart()
        }

        if (item.itemId == R.id.my_account){
            var sendToSetting = Intent(this,SettingActivity::class.java)
            sendToSetting.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK )
            startActivity(sendToSetting)

        }

        if(item.itemId == R.id.all_user){
            var sendToAllUser = Intent(this,AllUserActivity::class.java)
            sendToAllUser.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK )
            startActivity(sendToAllUser)

        }
        return super.onOptionsItemSelected(item)
    }

    private fun sendToStart() {
        var sendToStart = Intent(this,LoginActivity::class.java)
        sendToStart.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK )
        startActivity(sendToStart)

    }


}
