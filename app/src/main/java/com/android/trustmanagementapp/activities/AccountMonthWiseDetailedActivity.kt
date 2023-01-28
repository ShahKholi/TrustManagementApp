package com.android.trustmanagementapp.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.MonthWiseDetailAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MasterAccountDetail
import com.android.trustmanagementapp.model.MemberAccountDetail
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPButton
import com.android.trustmanagementapp.utils.MSPTextViewBold
import de.hdodenhof.circleimageview.CircleImageView

class AccountMonthWiseDetailedActivity : BaseActivity() {

    lateinit var mGroupName: String
    lateinit var mMonthName: String
    private lateinit var mAdminEmail: String
    lateinit var mIncome: String

    lateinit var mGroupImageString : String

    lateinit var mGroupImage : CircleImageView
    lateinit var fGroupName : MSPTextViewBold
    lateinit var fMonth : MSPTextViewBold
    lateinit var fAmount : MSPTextViewBold
    lateinit var recyclerView: RecyclerView
    lateinit var fBtnNotPaidList : MSPButton

    private lateinit var mMemberAccountList: ArrayList<MemberAccountDetail>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_month_wise_detailed)
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()

        if (intent.hasExtra(Constants.GROUP_NAME)) {
            mGroupName = intent.getStringExtra(Constants.GROUP_NAME)!!
        }
        if (intent.hasExtra(Constants.MONTH)) {
            mMonthName = intent.getStringExtra(Constants.MONTH)!!
        }
        if (intent.hasExtra(Constants.MEMBER_ADMIN_EMAIL)) {
            mAdminEmail = intent.getStringExtra(Constants.MEMBER_ADMIN_EMAIL)!!
        }
        if (intent.hasExtra(Constants.INCOME)) {
            mIncome = intent.getStringExtra(Constants.INCOME)!!
        }
        mGroupImage = findViewById(R.id.cv_group_image)
        fGroupName = findViewById(R.id.tv_group_name)
        fMonth = findViewById(R.id.tv_month)
        fAmount = findViewById(R.id.tv_amount)
        recyclerView = findViewById(R.id.rcv_month_wise_detail)
        fBtnNotPaidList = findViewById(R.id.tv_amount_not_receive)

        getMemberList()

        fBtnNotPaidList.setOnClickListener {
            val intent = Intent(this, MemberNotPaidListActivity::class.java)
            intent.putExtra(Constants.GROUP_NAME, mGroupName)
            intent.putExtra(Constants.MONTH, mMonthName)
            intent.putExtra(Constants.MEMBER_ADMIN_EMAIL, mAdminEmail)
            intent.putExtra(Constants.GROUP_IMAGE, mGroupImageString)
            startActivity(intent)
        }

    }

    private fun getMemberList() {
        showProgressDialog()
        FireStoreClass().monthWiseMemberAccountDetail(
            this,
            mGroupName,
            mMonthName,
            mAdminEmail,
            currentYear()
        )
    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_month_wise_detail)
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

    suspend fun successMemberList(memberAccountList: ArrayList<MemberAccountDetail>) {
        val getGroupImage = FireStoreClass().getGroupImageFromFirestore(
            mGroupName,mAdminEmail
        )
        mGroupImageString = getGroupImage
        GlideLoaderClass(this).loadGroupPictures(
            getGroupImage,
            mGroupImage
        )
        fGroupName.text = mGroupName
        fMonth.text = mMonthName
        fAmount.text = mIncome
        cancelProgressDialog()
        mMemberAccountList = memberAccountList
        if(mMemberAccountList.size > 0){
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            mMemberAccountList.sortBy{ it.memberName
            }
            val localMemberAccountList = MonthWiseDetailAdapter(
                this,mMemberAccountList,this
            )
            recyclerView.adapter = localMemberAccountList
        }

    }
}