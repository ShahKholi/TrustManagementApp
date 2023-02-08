package com.android.trustmanagementapp.activities


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.AboutGroup
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPTextViewBold

class AboutGroupFullViewActivity : BaseActivity() {
    lateinit var storeAdminEmail: String
    lateinit var storeGroupName: String

    private lateinit var ivGroupImage: ImageView
    private lateinit var tvGroupOriginateDate: MSPTextViewBold
    private lateinit var tvGroupAddress: MSPTextViewBold
    private lateinit var tvGroupFullDetail: MSPTextViewBold
    lateinit var toolbarGroupName: MSPTextViewBold


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_group_full_view)

        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()

        if (intent.hasExtra(Constants.ABOUT_GROUP_NAME)) {
            storeGroupName = intent.getStringExtra(Constants.ABOUT_GROUP_NAME)!!
            toolbarGroupName = findViewById(R.id.toolbar_about_group_view_text)
            toolbarGroupName.text = storeGroupName
        }
        if (intent.hasExtra(Constants.ABOUT_GROUP_ADMIN_EMAIL)) {
            storeAdminEmail = intent.getStringExtra(Constants.ABOUT_GROUP_ADMIN_EMAIL)!!
        }

        ivGroupImage = findViewById(R.id.iv_about_group_image)
        tvGroupOriginateDate = findViewById(R.id.tv_originate_date_view)
        tvGroupAddress = findViewById(R.id.tv_address)
        tvGroupFullDetail = findViewById(R.id.tv_about_full_detail)

        loadGroupDetail()

    }

    private fun loadGroupDetail() {
        showProgressDialog()
        FireStoreClass().getGroupDetail(this, storeAdminEmail, storeGroupName)
    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_about_group_view)
        setSupportActionBar(toolbar)


        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    @SuppressLint("SetTextI18n")
    fun successGroupDetail(groupList: ArrayList<GroupNameClass>) {

        if (groupList.size > 0) {
            for (i in groupList) {
                if (i.groupImage.isNotEmpty() && i.groupImage != "null") {
                    GlideLoaderClass(this).loadGroupIcon(i.groupImage, ivGroupImage)
                } else {
                    GlideLoaderClass(this).loadGroupIcon(
                        R.drawable.ic_user_placeholder,
                        ivGroupImage
                    )
                }
                tvGroupOriginateDate.text = "Group Originated Date : ${i.groupCreatedDate}"

            }
            FireStoreClass().getFullAboutDetail(this,storeAdminEmail,storeGroupName)

        } else {
            cancelProgressDialog()
        }
    }

    @SuppressLint("SetTextI18n")
    fun successLoadAboutList(aboutGroupList: ArrayList<AboutGroup>) {

        if(aboutGroupList.size > 0){
            for (i in aboutGroupList){
                tvGroupAddress.text = "Office Address : ${i.address}"
                tvGroupFullDetail.text = i.fullDetail
                cancelProgressDialog()

            }
        }else{
            cancelProgressDialog()
        }

    }
}