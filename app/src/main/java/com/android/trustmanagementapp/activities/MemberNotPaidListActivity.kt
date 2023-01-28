package com.android.trustmanagementapp.activities

import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.NonPaidDetailAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MemberAccountDetail
import com.android.trustmanagementapp.model.MemberClass
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPTextViewBold
import de.hdodenhof.circleimageview.CircleImageView

class MemberNotPaidListActivity : BaseActivity() {
    lateinit var mGroupName: String
    lateinit var mMonthName: String
    private lateinit var mAdminEmail: String
    private lateinit var mGroupImageString : String

    private lateinit var mPaidMemberList : ArrayList<MemberAccountDetail>
    private lateinit var mAllMemberList : ArrayList<MemberClass>

    lateinit var mGroupImage : CircleImageView
    lateinit var fGroupName : MSPTextViewBold
    lateinit var fMonth : MSPTextViewBold
    lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_not_paid_list)

        if (intent.hasExtra(Constants.GROUP_NAME)) {
            mGroupName = intent.getStringExtra(Constants.GROUP_NAME)!!
        }
        if (intent.hasExtra(Constants.MONTH)) {
            mMonthName = intent.getStringExtra(Constants.MONTH)!!
        }
        if (intent.hasExtra(Constants.MEMBER_ADMIN_EMAIL)) {
            mAdminEmail = intent.getStringExtra(Constants.MEMBER_ADMIN_EMAIL)!!
        }

        if (intent.hasExtra(Constants.GROUP_IMAGE)) {
            mGroupImageString = intent.getStringExtra(Constants.GROUP_IMAGE)!!
        }
        getMemberList()

        mGroupImage = findViewById(R.id.cv_group_image)
        fGroupName = findViewById(R.id.tv_group_name)
        fMonth = findViewById(R.id.tv_month)
        recyclerView = findViewById(R.id.rcv_month_wise_detail)
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

    fun successMemberList(memberAccountList: ArrayList<MemberAccountDetail>) {
        mPaidMemberList = memberAccountList
        allMemberList()

    }

    private fun allMemberList() {
        FireStoreClass().getMemberList(this,mGroupName,mAdminEmail)
    }

    fun successMemberListFromFirestore(memberList: ArrayList<MemberClass>) {
        cancelProgressDialog()

        GlideLoaderClass(this).loadGroupPictures(
            mGroupImageString,
            mGroupImage
        )
        fGroupName.text = mGroupName
        fMonth.text = mMonthName
        mAllMemberList = memberList
        val allMemberNameList: ArrayList<String> = ArrayList()
        val paidMemberNameList: ArrayList<String> = ArrayList()
        val finalNonPaidList: ArrayList<String> = ArrayList()

        var finalLastName: String

        for (allMember in mAllMemberList) {
            allMemberNameList.add(allMember.memberName)
        }
        for (paidMember in mPaidMemberList) {
            paidMemberNameList.add(paidMember.memberName)
        }

        for (element in paidMemberNameList) {
            allMemberNameList.remove(element)
        }
        for (name in allMemberNameList) {
            finalLastName = name
            finalNonPaidList.add(finalLastName)
        }
        finalNonPaidList.add(allMemberNameList.toString())

        finalNonPaidList.removeAt(finalNonPaidList.size - 1)

        if (finalNonPaidList.size > 0) {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            finalNonPaidList.sortBy {
                it
            }
            val localNonPaidListActivity = NonPaidDetailAdapter(
                this, finalNonPaidList, this
            )
            recyclerView.adapter = localNonPaidListActivity
        }


    }
}