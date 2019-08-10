package com.androiddeveloper3005.mychat.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.androiddeveloper3005.mychat.fragment.ChatsFragment
import com.androiddeveloper3005.mychat.fragment.FriendsFragment
import com.androiddeveloper3005.mychat.fragment.RequestFragment

class SectionsPagerAdapter(fm : FragmentManager) : FragmentPagerAdapter(fm) {


    override fun getItem(position: Int): Fragment {
        return when(position){
            0 ->{
                RequestFragment()
            }
            1->{
                ChatsFragment()
            }
            else -> {
                return FriendsFragment()
            }
        }

    }

    override fun getCount(): Int {
        return 3
    }


    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Requests"
            1 -> "Chats"
            else -> {
                return "Friends"
            }
        }
    }

}