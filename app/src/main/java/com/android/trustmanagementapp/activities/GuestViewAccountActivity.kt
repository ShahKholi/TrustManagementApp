package com.android.trustmanagementapp.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.ViewMasterAccountAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MasterAccountDetail
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.MSPTextViewBold
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class GuestViewAccountActivity : BaseActivity() {
    private lateinit var mGroupName: String
    private lateinit var mAdminEmail: String
    private lateinit var mMasterAccountList: ArrayList<MasterAccountDetail>
    private lateinit var recyclerView: RecyclerView
    lateinit var linearLayout: LinearLayoutCompat
    lateinit var mTotalAmount: MSPTextViewBold

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_view_account)
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )

        if (intent.hasExtra(Constants.GROUP_NAME)) {
            mGroupName = intent.getStringExtra(Constants.GROUP_NAME)!!
        }
        if (intent.hasExtra(Constants.ADMIN_EMAIL)) {
            mAdminEmail = intent.getStringExtra(Constants.ADMIN_EMAIL)!!
        }
        recyclerView = findViewById(R.id.rcv_view_account_detail)
        linearLayout = findViewById(R.id.ll_total_balance)
        mTotalAmount = findViewById(R.id.tv_available_balance)

        getAllMasterDetailForGroup()

    }

    private fun getAllMasterDetailForGroup() {
        showProgressDialog()
        FireStoreClass().getAllMasterAccountFromFirestore(
            this,
            mGroupName,
            mAdminEmail,
            currentYear()
        )
    }

    @SuppressLint("SimpleDateFormat")
    suspend fun successMasterList(masterAccountList: ArrayList<MasterAccountDetail>) {
        mMasterAccountList = masterAccountList
        val previousBalance: Int =
            FireStoreClass().getGroupPreviousBalance(this, mGroupName, mAdminEmail)
        val imageUrl: String =
            FireStoreClass().getMasterImageFromGroupFirestore(this, mGroupName, mAdminEmail)
        cancelProgressDialog()
        Log.e("check guest master size",mMasterAccountList.size.toString())
        if (mMasterAccountList.size > 0) {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            mMasterAccountList.sortBy{
                val sdf = SimpleDateFormat("MMMM")
                sdf.parse(it.month)
            }
            val viewAccountListAdapter = ViewMasterAccountAdapter(
                this, mMasterAccountList, previousBalance, this, imageUrl
            )
            recyclerView.adapter = viewAccountListAdapter
            checkTotalAmount()
        }
        else {
            linearLayout.visibility = View.GONE
        }
    }

    private suspend fun checkTotalAmount() {
        showProgressDialog()
        var income: Int = 0
        var expense: Int = 0
        var result: Int = 0

        val previousBalance : Int = FireStoreClass().checkPreviousAmountBalanceFromGroup(
            mGroupName,
            mAdminEmail,
            currentYear()
        )

        val mTotalMonth: ArrayList<Int> = FireStoreClass().getFullTotalAmount(
            mGroupName, mAdminEmail,
            currentYear()
        )
        val mTotalExpense: ArrayList<Int> = FireStoreClass().getFullExpenseAmount(
            mGroupName, mAdminEmail,
            currentYear()
        )

        Handler(Looper.getMainLooper()).postDelayed({
            lifecycleScope.launch {
                income = mTotalMonth.sum()
                expense = mTotalExpense.sum()
                result = income + previousBalance - expense
                mTotalAmount.text = result.toString()
                cancelProgressDialog()
            }
        }, 2000)


    }
}