package com.android.trustmanagementapp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
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

class ViewAccountActivity : BaseActivity() {
    lateinit var mGroupName: String
    private lateinit var mMasterAccountList: ArrayList<MasterAccountDetail>
    lateinit var mAdminEmail: String
    private lateinit var recyclerView: RecyclerView
    lateinit var linearLayout: LinearLayoutCompat
    lateinit var mTotalAmount: MSPTextViewBold


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
        lifecycleScope.launch {
            compareMasterAccountAndMemberAccount(mGroupName, getAdminEmailId)

        }


    }

    private suspend fun compareMasterAccountAndMemberAccount(
        mGroupName: String,
        getAdminEmailId: String
    ) {
        showProgressDialog()
        val mMasterMonthList: ArrayList<String> =
            FireStoreClass().getMasterAccountMonth(this, mGroupName, getAdminEmailId, currentYear())
        Log.e("master month list", mMasterMonthList.size.toString())
        if (mMasterMonthList.size > 0) {
            Log.e("master month detail", mMasterMonthList.toString())
            for (month in mMasterMonthList) {
                if (month == "January") {
                    val memberAccountMonthCheck: ArrayList<String> =
                        FireStoreClass().memberAccountMonthCheckFromFirestore(
                            month,
                            mGroupName,
                            getAdminEmailId,
                            currentYear()
                        )
                    if (memberAccountMonthCheck.size > 0) {
                        Log.e("Member month available", "no update needed")
                    } else if ((memberAccountMonthCheck.size == 0)) {
                        val finalAmount = 0
                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = finalAmount
                        FireStoreClass().memberDeleteUpdateMasterAccount(
                            this,
                            getAdminEmailId,
                            mGroupName,
                            month,
                            currentYear(),
                            userHashMap
                        )
                    }
                    val memberAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().memberAccountAmountForCurrentMonthFirestore(
                            month, mGroupName, getAdminEmailId, currentYear()
                        )
                    val masterAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().masterAccountAmountForCurrentMonthFireStore(
                            mGroupName, getAdminEmailId, month, currentYear()
                        )
                    val memberAccountAmount: Int = memberAccountAmountForCurrentMonth.sum()
                    val masterAccountAmount: Int = masterAccountAmountForCurrentMonth.sum()

                    if (memberAccountAmount != masterAccountAmount) {

                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = memberAccountAmount

                        FireStoreClass().compareFinalMasterAccountUpdate(
                            this,
                            getAdminEmailId, mGroupName, month, currentYear(), userHashMap
                        )
                    }
                }

                if (month == "February") {
                    val memberAccountMonthCheck: ArrayList<String> =
                        FireStoreClass().memberAccountMonthCheckFromFirestore(
                            month,
                            mGroupName,
                            getAdminEmailId,
                            currentYear()
                        )
                    if (memberAccountMonthCheck.size > 0) {
                        Log.e("Member month available", "no update needed")
                    } else if ((memberAccountMonthCheck.size == 0)) {

                        val finalAmount = 0
                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = finalAmount
                        FireStoreClass().memberDeleteUpdateMasterAccount(
                            this,
                            getAdminEmailId,
                            mGroupName,
                            month,
                            currentYear(),
                            userHashMap
                        )
                    }
                    val memberAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().memberAccountAmountForCurrentMonthFirestore(
                            month, mGroupName, getAdminEmailId, currentYear()
                        )
                    val masterAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().masterAccountAmountForCurrentMonthFireStore(
                            mGroupName, getAdminEmailId, month, currentYear()
                        )
                    val memberAccountAmount: Int = memberAccountAmountForCurrentMonth.sum()
                    val masterAccountAmount: Int = masterAccountAmountForCurrentMonth.sum()

                    if (memberAccountAmount != masterAccountAmount) {

                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = memberAccountAmount

                        FireStoreClass().compareFinalMasterAccountUpdate(
                            this,
                            getAdminEmailId, mGroupName, month, currentYear(), userHashMap
                        )
                    }
                }

                if (month == "March") {
                    val memberAccountMonthCheck: ArrayList<String> =
                        FireStoreClass().memberAccountMonthCheckFromFirestore(
                            month,
                            mGroupName,
                            getAdminEmailId,
                            currentYear()
                        )
                    if (memberAccountMonthCheck.size > 0) {
                        Log.e("Member month available", "no update needed")
                    } else if ((memberAccountMonthCheck.size == 0)) {

                        val finalAmount = 0
                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = finalAmount
                        FireStoreClass().memberDeleteUpdateMasterAccount(
                            this,
                            getAdminEmailId,
                            mGroupName,
                            month,
                            currentYear(),
                            userHashMap
                        )
                    }
                    val memberAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().memberAccountAmountForCurrentMonthFirestore(
                            month, mGroupName, getAdminEmailId, currentYear()
                        )
                    val masterAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().masterAccountAmountForCurrentMonthFireStore(
                            mGroupName, getAdminEmailId, month, currentYear()
                        )
                    val memberAccountAmount: Int = memberAccountAmountForCurrentMonth.sum()
                    val masterAccountAmount: Int = masterAccountAmountForCurrentMonth.sum()

                    if (memberAccountAmount != masterAccountAmount) {

                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = memberAccountAmount

                        FireStoreClass().compareFinalMasterAccountUpdate(
                            this,
                            getAdminEmailId, mGroupName, month, currentYear(), userHashMap
                        )
                    }
                }

                if (month == "April") {
                    val memberAccountMonthCheck: ArrayList<String> =
                        FireStoreClass().memberAccountMonthCheckFromFirestore(
                            month,
                            mGroupName,
                            getAdminEmailId,
                            currentYear()
                        )
                    if (memberAccountMonthCheck.size > 0) {
                        Log.e("Member month available", "no update needed")
                    } else if ((memberAccountMonthCheck.size == 0)) {

                        val finalAmount = 0
                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = finalAmount
                        FireStoreClass().memberDeleteUpdateMasterAccount(
                            this,
                            getAdminEmailId,
                            mGroupName,
                            month,
                            currentYear(),
                            userHashMap
                        )
                    }
                    val memberAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().memberAccountAmountForCurrentMonthFirestore(
                            month, mGroupName, getAdminEmailId, currentYear()
                        )
                    val masterAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().masterAccountAmountForCurrentMonthFireStore(
                            mGroupName, getAdminEmailId, month, currentYear()
                        )
                    val memberAccountAmount: Int = memberAccountAmountForCurrentMonth.sum()
                    val masterAccountAmount: Int = masterAccountAmountForCurrentMonth.sum()

                    if (memberAccountAmount != masterAccountAmount) {

                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = memberAccountAmount

                        FireStoreClass().compareFinalMasterAccountUpdate(
                            this,
                            getAdminEmailId, mGroupName, month, currentYear(), userHashMap
                        )
                    }
                }

                if (month == "May") {
                    val memberAccountMonthCheck: ArrayList<String> =
                        FireStoreClass().memberAccountMonthCheckFromFirestore(
                            month,
                            mGroupName,
                            getAdminEmailId,
                            currentYear()
                        )
                    if (memberAccountMonthCheck.size > 0) {
                        Log.e("Member month available", "no update needed")
                    } else if ((memberAccountMonthCheck.size == 0)) {

                        val finalAmount = 0
                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = finalAmount
                        FireStoreClass().memberDeleteUpdateMasterAccount(
                            this,
                            getAdminEmailId,
                            mGroupName,
                            month,
                            currentYear(),
                            userHashMap
                        )
                    }
                    val memberAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().memberAccountAmountForCurrentMonthFirestore(
                            month, mGroupName, getAdminEmailId, currentYear()
                        )
                    val masterAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().masterAccountAmountForCurrentMonthFireStore(
                            mGroupName, getAdminEmailId, month, currentYear()
                        )
                    val memberAccountAmount: Int = memberAccountAmountForCurrentMonth.sum()
                    val masterAccountAmount: Int = masterAccountAmountForCurrentMonth.sum()

                    if (memberAccountAmount != masterAccountAmount) {

                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = memberAccountAmount

                        FireStoreClass().compareFinalMasterAccountUpdate(
                            this,
                            getAdminEmailId, mGroupName, month, currentYear(), userHashMap
                        )
                    }
                }

                if (month == "June") {
                    val memberAccountMonthCheck: ArrayList<String> =
                        FireStoreClass().memberAccountMonthCheckFromFirestore(
                            month,
                            mGroupName,
                            getAdminEmailId,
                            currentYear()
                        )
                    if (memberAccountMonthCheck.size > 0) {
                        Log.e("Member month available", "no update needed")
                    } else if ((memberAccountMonthCheck.size == 0)) {

                        val finalAmount = 0
                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = finalAmount
                        FireStoreClass().memberDeleteUpdateMasterAccount(
                            this,
                            getAdminEmailId,
                            mGroupName,
                            month,
                            currentYear(),
                            userHashMap
                        )
                    }
                    val memberAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().memberAccountAmountForCurrentMonthFirestore(
                            month, mGroupName, getAdminEmailId, currentYear()
                        )
                    val masterAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().masterAccountAmountForCurrentMonthFireStore(
                            mGroupName, getAdminEmailId, month, currentYear()
                        )
                    val memberAccountAmount: Int = memberAccountAmountForCurrentMonth.sum()
                    val masterAccountAmount: Int = masterAccountAmountForCurrentMonth.sum()

                    if (memberAccountAmount != masterAccountAmount) {

                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = memberAccountAmount

                        FireStoreClass().compareFinalMasterAccountUpdate(
                            this,
                            getAdminEmailId, mGroupName, month, currentYear(), userHashMap
                        )
                    }
                }

                if (month == "July") {
                    val memberAccountMonthCheck: ArrayList<String> =
                        FireStoreClass().memberAccountMonthCheckFromFirestore(
                            month,
                            mGroupName,
                            getAdminEmailId,
                            currentYear()
                        )
                    if (memberAccountMonthCheck.size > 0) {
                        Log.e("Member month available", "no update needed")
                    } else if ((memberAccountMonthCheck.size == 0)) {

                        val finalAmount = 0
                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = finalAmount
                        FireStoreClass().memberDeleteUpdateMasterAccount(
                            this,
                            getAdminEmailId,
                            mGroupName,
                            month,
                            currentYear(),
                            userHashMap
                        )
                    }
                    val memberAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().memberAccountAmountForCurrentMonthFirestore(
                            month, mGroupName, getAdminEmailId, currentYear()
                        )
                    val masterAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().masterAccountAmountForCurrentMonthFireStore(
                            mGroupName, getAdminEmailId, month, currentYear()
                        )
                    val memberAccountAmount: Int = memberAccountAmountForCurrentMonth.sum()
                    val masterAccountAmount: Int = masterAccountAmountForCurrentMonth.sum()

                    if (memberAccountAmount != masterAccountAmount) {

                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = memberAccountAmount

                        FireStoreClass().compareFinalMasterAccountUpdate(
                            this,
                            getAdminEmailId, mGroupName, month, currentYear(), userHashMap
                        )
                    }
                }

                if (month == "August") {
                    val memberAccountMonthCheck: ArrayList<String> =
                        FireStoreClass().memberAccountMonthCheckFromFirestore(
                            month,
                            mGroupName,
                            getAdminEmailId,
                            currentYear()
                        )
                    if (memberAccountMonthCheck.size > 0) {
                        Log.e("Member month available", "no update needed")
                    } else if ((memberAccountMonthCheck.size == 0)) {

                        val finalAmount = 0
                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = finalAmount
                        FireStoreClass().memberDeleteUpdateMasterAccount(
                            this,
                            getAdminEmailId,
                            mGroupName,
                            month,
                            currentYear(),
                            userHashMap
                        )
                    }
                    val memberAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().memberAccountAmountForCurrentMonthFirestore(
                            month, mGroupName, getAdminEmailId, currentYear()
                        )
                    val masterAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().masterAccountAmountForCurrentMonthFireStore(
                            mGroupName, getAdminEmailId, month, currentYear()
                        )
                    val memberAccountAmount: Int = memberAccountAmountForCurrentMonth.sum()
                    val masterAccountAmount: Int = masterAccountAmountForCurrentMonth.sum()

                    if (memberAccountAmount != masterAccountAmount) {

                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = memberAccountAmount

                        FireStoreClass().compareFinalMasterAccountUpdate(
                            this,
                            getAdminEmailId, mGroupName, month, currentYear(), userHashMap
                        )
                    }
                }

                if (month == "September") {
                    val memberAccountMonthCheck: ArrayList<String> =
                        FireStoreClass().memberAccountMonthCheckFromFirestore(
                            month,
                            mGroupName,
                            getAdminEmailId,
                            currentYear()
                        )
                    if (memberAccountMonthCheck.size > 0) {
                        Log.e("Member month available", "no update needed")
                    } else if ((memberAccountMonthCheck.size == 0)) {

                        val finalAmount = 0
                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = finalAmount
                        FireStoreClass().memberDeleteUpdateMasterAccount(
                            this,
                            getAdminEmailId,
                            mGroupName,
                            month,
                            currentYear(),
                            userHashMap
                        )
                    }
                    val memberAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().memberAccountAmountForCurrentMonthFirestore(
                            month, mGroupName, getAdminEmailId, currentYear()
                        )
                    val masterAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().masterAccountAmountForCurrentMonthFireStore(
                            mGroupName, getAdminEmailId, month, currentYear()
                        )
                    val memberAccountAmount: Int = memberAccountAmountForCurrentMonth.sum()
                    val masterAccountAmount: Int = masterAccountAmountForCurrentMonth.sum()

                    if (memberAccountAmount != masterAccountAmount) {

                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = memberAccountAmount

                        FireStoreClass().compareFinalMasterAccountUpdate(
                            this,
                            getAdminEmailId, mGroupName, month, currentYear(), userHashMap
                        )
                    }
                }

                if (month == "October") {
                    val memberAccountMonthCheck: ArrayList<String> =
                        FireStoreClass().memberAccountMonthCheckFromFirestore(
                            month,
                            mGroupName,
                            getAdminEmailId,
                            currentYear()
                        )
                    if (memberAccountMonthCheck.size > 0) {
                        Log.e("Member month available", "no update needed")
                    } else if ((memberAccountMonthCheck.size == 0)) {

                        val finalAmount = 0
                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = finalAmount
                        FireStoreClass().memberDeleteUpdateMasterAccount(
                            this,
                            getAdminEmailId,
                            mGroupName,
                            month,
                            currentYear(),
                            userHashMap
                        )
                    }
                    val memberAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().memberAccountAmountForCurrentMonthFirestore(
                            month, mGroupName, getAdminEmailId, currentYear()
                        )
                    val masterAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().masterAccountAmountForCurrentMonthFireStore(
                            mGroupName, getAdminEmailId, month, currentYear()
                        )
                    val memberAccountAmount: Int = memberAccountAmountForCurrentMonth.sum()
                    val masterAccountAmount: Int = masterAccountAmountForCurrentMonth.sum()

                    if (memberAccountAmount != masterAccountAmount) {

                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = memberAccountAmount

                        FireStoreClass().compareFinalMasterAccountUpdate(
                            this,
                            getAdminEmailId, mGroupName, month, currentYear(), userHashMap
                        )
                    }
                }

                if (month == "November") {
                    val memberAccountMonthCheck: ArrayList<String> =
                        FireStoreClass().memberAccountMonthCheckFromFirestore(
                            month,
                            mGroupName,
                            getAdminEmailId,
                            currentYear()
                        )
                    if (memberAccountMonthCheck.size > 0) {
                        Log.e("Member month available", "no update needed")
                    } else if ((memberAccountMonthCheck.size == 0)) {

                        val finalAmount = 0
                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = finalAmount
                        FireStoreClass().memberDeleteUpdateMasterAccount(
                            this,
                            getAdminEmailId,
                            mGroupName,
                            month,
                            currentYear(),
                            userHashMap
                        )
                    }
                    val memberAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().memberAccountAmountForCurrentMonthFirestore(
                            month, mGroupName, getAdminEmailId, currentYear()
                        )
                    val masterAccountAmountForCurrentMonth: ArrayList<Int> =
                        FireStoreClass().masterAccountAmountForCurrentMonthFireStore(
                            mGroupName, getAdminEmailId, month, currentYear()
                        )
                    val memberAccountAmount: Int = memberAccountAmountForCurrentMonth.sum()
                    val masterAccountAmount: Int = masterAccountAmountForCurrentMonth.sum()

                    if (memberAccountAmount != masterAccountAmount) {

                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = memberAccountAmount

                        FireStoreClass().compareFinalMasterAccountUpdate(
                            this,
                            getAdminEmailId, mGroupName, month, currentYear(), userHashMap
                        )
                    }
                }

                if (month == "December") {
                    val memberAccountMonthCheck: ArrayList<String> =
                        FireStoreClass().memberAccountMonthCheckFromFirestore(
                            month,
                            mGroupName,
                            getAdminEmailId,
                            currentYear()
                        )
                    if (memberAccountMonthCheck.size > 0) {
                        Log.e("Member month available", "no update needed")
                    } else if ((memberAccountMonthCheck.size == 0)) {

                        val finalAmount = 0
                        val userHashMap = HashMap<String, Any>()
                        userHashMap[Constants.INCOME] = finalAmount
                        FireStoreClass().memberDeleteUpdateMasterAccount(
                            this,
                            getAdminEmailId,
                            mGroupName,
                            month,
                            currentYear(),
                            userHashMap
                        )
                    }
                }
                val memberAccountAmountForCurrentMonth: ArrayList<Int> =
                    FireStoreClass().memberAccountAmountForCurrentMonthFirestore(
                        month, mGroupName, getAdminEmailId, currentYear()
                    )
                val masterAccountAmountForCurrentMonth: ArrayList<Int> =
                    FireStoreClass().masterAccountAmountForCurrentMonthFireStore(
                        mGroupName, getAdminEmailId, month, currentYear()
                    )
                val memberAccountAmount: Int = memberAccountAmountForCurrentMonth.sum()
                val masterAccountAmount: Int = masterAccountAmountForCurrentMonth.sum()

                if (memberAccountAmount != masterAccountAmount) {

                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.INCOME] = memberAccountAmount

                    FireStoreClass().compareFinalMasterAccountUpdate(
                        this,
                        getAdminEmailId, mGroupName, month, currentYear(), userHashMap
                    )
                }
            }
        }
        getAllMasterDetailForGroup(mGroupName, getAdminEmailId, currentYear())

    }

    private fun getAllMasterDetailForGroup(
        groupName: String,
        adminEmailId: String?,
        currentYear: Int
    ) {

        FireStoreClass().getAllMasterAccountFromFirestore(
            this,
            groupName,
            adminEmailId,
            currentYear
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

        } else {
            linearLayout.visibility = View.GONE
        }

    }

    private suspend fun checkTotalAmount() {
        showProgressDialog()
        var previousBalance: Int = 0
        var income: Int = 0
        var expense: Int = 0
        var result: Int = 0

        previousBalance = FireStoreClass().checkPreviousAmountBalanceFromGroup(
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

    fun updateMasterAccountZeroSuccess() {
        Log.e("Master Account update", "Zero value updated to non member account month")
        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
        getAllMasterDetailForGroup(mGroupName, getAdminEmailId, currentYear())
    }

    fun updateMasterAmountSuccess() {
        Log.e("update success", "update completed")
    }


}