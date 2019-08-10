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
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth


class MainActivity : AppCompatActivity() {
    private lateinit var mViewPager : ViewPager
    private lateinit var adapter: SectionsPagerAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var toolbar: Toolbar
    private lateinit var auth : FirebaseAuth

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
