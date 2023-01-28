package com.android.trustmanagementapp.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.GuestCurrentMemberDetailAdapter
import com.android.trustmanagementapp.adapter.MemberDetailAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MemberAccountDetail
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPButton
import com.android.trustmanagementapp.utils.MSPTextViewBold
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat

class GuestAllMemberDetailedActivity : BaseActivity() {

    private lateinit var mUserGroupName: String
    lateinit var mUserAdminEmail: String
    lateinit var mUserMemberEmail: String
    lateinit var mUserMemberName: String
    lateinit var mUserMemberPhone: String
    lateinit var mUserProfileImage: String
    private lateinit var mMemberDetailAccountList: ArrayList<MemberAccountDetail>
    private lateinit var recyclerView: RecyclerView
    lateinit var mProfileImage: CircleImageView
    private lateinit var mUserName: MSPTextViewBold
    lateinit var mUserPhone: MSPTextViewBold
    lateinit var mUserEmail: MSPTextViewBold
    lateinit var llBalanceScreen: LinearLayoutCompat
    lateinit var mTotalAmount: MSPTextViewBold


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_all_member_detailed)

        if (intent.hasExtra(Constants.GROUP_NAME)) {
            mUserGroupName = intent.getStringExtra(Constants.GROUP_NAME)!!
        }
        if (intent.hasExtra(Constants.MEMBER_ADMIN_EMAIL)) {
            mUserAdminEmail = intent.getStringExtra(Constants.MEMBER_ADMIN_EMAIL)!!
        }
        if (intent.hasExtra(Constants.MEMBER_EMAIL)) {
            mUserMemberEmail = intent.getStringExtra(Constants.MEMBER_EMAIL)!!
        }
        if (intent.hasExtra(Constants.MEMBER_NAME)) {
            mUserMemberName = intent.getStringExtra(Constants.MEMBER_NAME)!!
        }
        if (intent.hasExtra(Constants.MEMBER_PHONE)) {
            mUserMemberPhone = intent.getStringExtra(Constants.MEMBER_PHONE)!!
        }
        if (intent.hasExtra(Constants.PROFILE_IMAGE)) {
            mUserProfileImage = intent.getStringExtra(Constants.PROFILE_IMAGE)!!
        }

        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )

        recyclerView = findViewById(R.id.rcv_view_account_detail)
        mProfileImage = findViewById(R.id.cv_profile_member_detail)
        mUserName = findViewById(R.id.tv_user_name_member_detail)
        mUserPhone = findViewById(R.id.tv_contact_number_member)
        mUserEmail = findViewById(R.id.tv_email_member_detail)
        llBalanceScreen = findViewById(R.id.ll_total_balance_detail)
        mTotalAmount = findViewById(R.id.tv_total_amount_detail)

        GlideLoaderClass(this).loadGroupPictures(
            mUserProfileImage,
            mProfileImage
        )
        mUserName.text = mUserMemberName
        mUserPhone.text = mUserMemberPhone
        mUserEmail.text = mUserMemberEmail

        setUpSupportActionBar()
        getMemberIncomeDetail()

    }
    private fun getMemberIncomeDetail() {
        showProgressDialog()
        FireStoreClass().getIncomeDetailFromFirestore(
            this, mUserGroupName, mUserAdminEmail, mUserMemberEmail, mUserMemberName,
            currentYear()
        )

    }

    @SuppressLint("SimpleDateFormat")
    fun successIncomeDetail(monthList: ArrayList<MemberAccountDetail>) {
        cancelProgressDialog()
        mMemberDetailAccountList = monthList
        val amountList: ArrayList<Int> = ArrayList()

        if (mMemberDetailAccountList.size > 0) {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            mMemberDetailAccountList.sortBy {
                val sdf = SimpleDateFormat("MMMM")
                sdf.parse(it.month)
            }
            val memberDetailAdapter = GuestCurrentMemberDetailAdapter(
                this, mMemberDetailAccountList, this
            )
            recyclerView.adapter = memberDetailAdapter
            for (i in mMemberDetailAccountList) {
                amountList.add(i.currentAmount)
                amountList.sum()
            }
            mTotalAmount.text = amountList.sum().toString()

        } else {
            llBalanceScreen.visibility = View.GONE
        }

    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_view_member_detail)
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
}