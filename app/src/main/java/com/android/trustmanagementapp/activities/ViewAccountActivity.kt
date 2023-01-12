package com.android.trustmanagementapp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.ViewMasterAccountAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MasterAccountDetail
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.MSPTextViewBold
import java.text.SimpleDateFormat

class ViewAccountActivity : BaseActivity() {
    lateinit var mGroupName: String
    private lateinit var mMasterAccountList: ArrayList<MasterAccountDetail>
    lateinit var mAdminEmail: String
    private lateinit var recyclerView: RecyclerView
    lateinit var linearLayout : LinearLayoutCompat
    lateinit var mTotalAmount : MSPTextViewBold


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_account)

        if (intent.hasExtra(Constants.GROUP_NAME)) {
            mGroupName = intent.getStringExtra(Constants.GROUP_NAME)!!
        }
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        recyclerView = findViewById(R.id.rcv_view_account_detail)
        linearLayout = findViewById(R.id.ll_total_balance)
        mTotalAmount = findViewById(R.id.tv_available_balance)

        setUpSupportActionBar()
        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
        mAdminEmail = getAdminEmailId!!
        getAllMasterDetailForGroup(mGroupName, getAdminEmailId)


    }

    private fun getAllMasterDetailForGroup(groupName: String, adminEmailId: String?) {
        showProgressDialog()
        FireStoreClass().getAllMasterAccountFromFirestore(this, groupName, adminEmailId)
    }

    @SuppressLint("SimpleDateFormat")
    suspend fun successMasterList(masterAccountList: ArrayList<MasterAccountDetail>) {
        mMasterAccountList = masterAccountList
        val previousBalance: Int =
            FireStoreClass().getGroupPreviousBalance(this, mGroupName, mAdminEmail)
        val imageUrl: String =
            FireStoreClass().getMasterImageFromGroupFirestore(this, mGroupName, mAdminEmail)
        cancelProgressDialog()

        if (mMasterAccountList.size > 0) {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            mMasterAccountList.sortBy {
                /* val myMonth: MutableList<Array<Month>> = Arrays.asList(Month.values())
                 Collections.rotate(myMonth,myMonth.size)*/
                val sdf = SimpleDateFormat("MMMM")
                sdf.parse(it.month)
            }
            val viewAccountListAdapter = ViewMasterAccountAdapter(
                this, mMasterAccountList, previousBalance, this, imageUrl
            )
            recyclerView.adapter = viewAccountListAdapter
            checkTotalAmount()
        }
        else{
            linearLayout.visibility = View.GONE
        }


    }

    private suspend fun checkTotalAmount() {
        var previousBalance: Int = 0
        var income: Int = 0
        var expense: Int = 0
        var result : Int = 0

        previousBalance = FireStoreClass().checkPreviousAmountBalanceFromGroup(
            mGroupName,
            mAdminEmail,
            currentYear()
        )

        val mTotalMonth: ArrayList<Int> = FireStoreClass().getFullTotalAmount(mGroupName,mAdminEmail,
            currentYear())
        val mTotalExpense : ArrayList<Int> = FireStoreClass().getFullExpenseAmount(mGroupName,mAdminEmail,
            currentYear())

        income = mTotalMonth.sum()
        expense = mTotalExpense.sum()
        result = income + previousBalance - expense
        mTotalAmount.text = result.toString()

    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_view_account)
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