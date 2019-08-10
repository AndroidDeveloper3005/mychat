package com.androiddeveloper3005.mychat.activity

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.androiddeveloper3005.mychat.R
import com.google.android.material.navigation.NavigationView

open class BaseActivity : AppCompatActivity() {
    private lateinit var mContext: Context
    private lateinit var mActivity: Activity
    private lateinit var mToolbar:Toolbar
    private lateinit var mDrawerLayout: DrawerLayout
    private lateinit var mNavigationView: NavigationView
    private lateinit var loadingView: LinearLayout
    private lateinit var noDataView:LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initVariable()
    }

    private fun initVariable() {
        mContext = getApplicationContext()
        mActivity = this@BaseActivity
    }
    fun initToolbar() {
        mToolbar = findViewById(R.id.toolBar) as Toolbar
        setSupportActionBar(mToolbar)
    }
    fun enableBackButton() {
        if (getSupportActionBar() != null)
        {
            getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true)
            getSupportActionBar()!!.setDisplayShowHomeEnabled(true)
        }
    }
    fun getToolbar(title:String) : Toolbar {
        if (mToolbar == null)
        {
            mToolbar = findViewById(R.id.toolBar) as Toolbar
            setSupportActionBar(mToolbar)
        }
        return mToolbar
    }
    fun setToolbarTitle(title:String) {
        if (getSupportActionBar() != null)
        {
            getSupportActionBar()!!.setTitle(title)
        }
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
}
