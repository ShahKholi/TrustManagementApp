package com.android.trustmanagementapp.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.android.trustmanagementapp.activities.fragments.GuestDashBoardFragment
import com.android.trustmanagementapp.activities.fragments.SettingFragment
import com.android.trustmanagementapp.activities.fragments.TimelineFragment

class FragmentMyAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {
    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
       return when(position){
            0 ->{
                TimelineFragment()
            }
            1 ->{
                GuestDashBoardFragment()
            }
            2 ->{
                SettingFragment()
            }
            else->{
                Fragment()
            }

        }
    }
}