package com.android.trustmanagementapp.activities

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.activities.fragments.GuestDashBoardFragment
import com.android.trustmanagementapp.adapter.FragmentMyAdapter
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPTextViewBold
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {


    private lateinit var container : ViewPager2
    private lateinit var tabs : TabLayout

    private lateinit var mGroupName : String
    private lateinit var mAdminEmail : String
    private lateinit var mMemberEmail  : String
    private lateinit var mMemberPhone : String
    private lateinit var mMemberProfileImage : String
    private lateinit var mToolbarText : MSPTextViewBold

    private lateinit var mSetMemberPhoto : CircleImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()

        container = findViewById(R.id.container)
        tabs = findViewById(R.id.tabs)

        val adapter = FragmentMyAdapter(supportFragmentManager,lifecycle)
        container.adapter = adapter

        mSetMemberPhoto = findViewById(R.id.iv_member_image_frag)
        mToolbarText = findViewById(R.id.toolbar_text_guest_main)

        if(intent.hasExtra(Constants.PROFILE_IMAGE)){
            mMemberProfileImage = intent.getStringExtra(Constants.PROFILE_IMAGE)!!
           if(mMemberProfileImage.isNotEmpty() && mMemberProfileImage !="null"){

               GlideLoaderClass(this).loadGuestProfilePictures(
                   mMemberProfileImage, mSetMemberPhoto
               )
           }else{
               GlideLoaderClass(this).loadGuestProfilePictures(
                   R.drawable.ic_user_placeholder,mSetMemberPhoto
               )
           }


            val sharedPreferences = getSharedPreferences(
                Constants.STORE_MEMBER_PROFILE_ID, Context.MODE_PRIVATE
            )
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(
                Constants.STORE_MEMBER_PROFILE_ID, mMemberProfileImage
            )
            editor.apply()
        }

        if(intent.hasExtra(Constants.MEMBER_PHONE)){
            mMemberPhone = intent.getStringExtra(Constants.MEMBER_PHONE)!!
            val sharedPreferences = getSharedPreferences(
                Constants.STORE_MEMBER_PHONE_ID, Context.MODE_PRIVATE
            )
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(
                Constants.STORE_MEMBER_PHONE_ID, mMemberPhone
            )
            editor.apply()
        }

        if(intent.hasExtra(Constants.MEMBER_EMAIL)){
            mMemberEmail = intent.getStringExtra(Constants.MEMBER_EMAIL)!!
            val sharedPreferences = getSharedPreferences(
                Constants.STORE_MEMBER_EMAIL_ID, Context.MODE_PRIVATE
            )
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(
                Constants.STORE_MEMBER_EMAIL_ID, mMemberEmail
            )
            editor.apply()
        }

        if(intent.hasExtra(Constants.GROUP_NAME)){
            mGroupName = intent.getStringExtra(Constants.GROUP_NAME)!!
            mToolbarText.text = mGroupName

            val sharedPreferences = getSharedPreferences(
                Constants.STORE_GROUP_NAME_ID, Context.MODE_PRIVATE
            )
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(
                Constants.STORE_GROUP_NAME_ID, mGroupName
            )
            editor.apply()

        }

        if(intent.hasExtra(Constants.ADMIN_EMAIL)){
            mAdminEmail = intent.getStringExtra(Constants.ADMIN_EMAIL)!!

            val sharedPreferences = getSharedPreferences(
                Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
            )
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(
                Constants.STORE_EMAIL_ID, mAdminEmail
            )
            editor.apply()

        }

        TabLayoutMediator(tabs,container) { tab, position ->
            when(position){
                0 -> {
                   // tab.setIcon(R.drawable.camera + position)
                    tab.text = "TIMELINE"

                }
                1 -> {
                    tab.text = "YOUR DASHBOARD"

                }
                2 -> {
                    tab.text = "SETTING"

                }
            }
        }.attach()
        resizeTab()
        tabs.getTabAt(1)?.select()
    }

    private fun resizeTab(){
        val layout = (tabs.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.6F
        layout.layoutParams = layoutParams
    }



    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_main_fragment)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
           // actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
           // actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}