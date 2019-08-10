package com.androiddeveloper3005.mychat.activity

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.androiddeveloper3005.mychat.R
import com.androiddeveloper3005.mychat.adapter.UsersAdapter
import com.androiddeveloper3005.mychat.appconstants.Constans
import com.androiddeveloper3005.mychat.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class AllUserActivity : AppCompatActivity() {
    private lateinit var toolBar : Toolbar
    private lateinit var user_list_recyclerview : RecyclerView
    private lateinit var mUsersDatabase : DatabaseReference
    private lateinit var mLayoutManager : LinearLayoutManager
    private  var list : ArrayList<Users>  = ArrayList<Users>()
    private  var users  = Users()
    private lateinit var name : String
    private lateinit var status : String
    private lateinit var imageuri : String
    private lateinit var uid : String
    private lateinit var adapter : UsersAdapter
    private lateinit var loadingView: LinearLayout
    private lateinit var noDataView:LinearLayout
    private lateinit var mAuth : FirebaseAuth




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_user)
        toolBar = findViewById(R.id.toolBar)
        setSupportActionBar(toolBar)
        supportActionBar!!.setTitle("All User")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        initLoader()
        mAuth = FirebaseAuth.getInstance()

        adapter = UsersAdapter(list,applicationContext)
        user_list_recyclerview = findViewById(R.id.all_user_recycler_view)
        mLayoutManager = LinearLayoutManager(this);
        user_list_recyclerview.layoutManager = mLayoutManager


        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child(Constans.USER_DATABSE_PATH);
        //get data
        mUsersDatabase.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError?) {
                Log.w("All User ::", "loadPost:onCancelled", databaseError?.toException())
            }

            override fun onDataChange(dataSnapshot: DataSnapshot?) {
                for (data in dataSnapshot!!.children  ){
                    name = data.child(Constans.NAME).getValue().toString()
                    status = data.child(Constans.STATUS).getValue().toString()
                    imageuri = data.child(Constans.IMAGE).getValue().toString()
                    uid = data.child(Constans.UID).getValue().toString()
                    users = Users(name,status,imageuri,uid)
                    if (!uid.equals(mAuth.currentUser!!.uid)){
                        list.add(users)

                    }
                    if (!list.isEmpty()){
                        user_list_recyclerview.adapter = adapter
                        adapter.notifyDataSetChanged()
                        hideLoader()

                    }else{
                        hideLoader()
                        showEmptyView()

                    }



                }
            }

        })



    }

    fun initLoader() {
        loadingView = findViewById(R.id.loadingView) as LinearLayout
        noDataView = findViewById(R.id.noDataView) as LinearLayout
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
        adapter.notifyDataSetChanged()
    }



}
