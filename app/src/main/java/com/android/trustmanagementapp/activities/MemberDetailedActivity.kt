package com.android.trustmanagementapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.MemberDetailAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MasterAccountDetail
import com.android.trustmanagementapp.model.MemberAccountDetail
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPButton
import com.android.trustmanagementapp.utils.MSPTextViewBold
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class MemberDetailedActivity : BaseActivity() {
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
    lateinit var mCurrentDeleteDocumentID: ArrayList<MemberAccountDetail>
    lateinit var btnEdit: MSPButton
    lateinit var btnDeleteMember: MSPButton

    private lateinit var mCurrentMonthTotal: ArrayList<Int>
    private lateinit var mCurrentAmount: ArrayList<Int>

    private lateinit var mAvailableMonthList: ArrayList<MemberAccountDetail>


    private val getMonthDataResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                getMemberIncomeDetail()
                finish()
            }
        }

    private val getMemberUserDeleteResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_member_detailed)


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
        btnEdit = findViewById(R.id.btn_edit_member)
        btnDeleteMember = findViewById(R.id.btn_delete_member)

        btnDeleteMember.setOnClickListener {
            deleteMemberActivity()
        }

        btnEdit.setOnClickListener {
            goToEditScreenActivity()
        }

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

    private fun deleteMemberActivity() {

        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setIcon(android.R.drawable.ic_dialog_alert)
        val customerLayout: View =
            LayoutInflater.from(this)
                .inflate(R.layout.custom_dilog_box_delete, null)
        builder.setView(customerLayout)
        builder.setTitle("DELETE")
        builder.setPositiveButton("YES") { _, _ ->
            Log.e("dialog", "Accepted")
            lifecycleScope.launch {
                showProgressDialog()
                startDeleteActivity()
            }

        }
        builder.setNegativeButton("NO") { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        val alertDialog: AlertDialog? = builder.create()
        // Set other dialog properties
        alertDialog!!.setCancelable(false)
        alertDialog.show()


    }

    private suspend fun startDeleteActivity() {
        val checkAccountAvailableInAccount: ArrayList<String> =
            FireStoreClass().checkAccountAvailableFromFirestore(
                this,
                mUserAdminEmail,
                mUserGroupName,
                mUserMemberEmail
            )
        Log.e("Member Account Size", checkAccountAvailableInAccount.size.toString())

        if (checkAccountAvailableInAccount.size > 0) {
            FireStoreClass().deleteMemberAccountDetail(
                this,
                mUserAdminEmail,
                mUserGroupName,
                mUserMemberEmail
            )
        } else {
            successDeleteFromMemberAccount()
        }

    }

    private fun goToEditScreenActivity() {
        val intent = Intent(this, AddMemberActivity::class.java)
        intent.putExtra(Constants.GROUP_NAME, mUserGroupName)
        intent.putExtra(Constants.MEMBER_ADMIN_EMAIL, mUserAdminEmail)
        intent.putExtra(Constants.MEMBER_EMAIL, mUserMemberEmail)
        intent.putExtra(Constants.MEMBER_NAME, mUserMemberName)
        intent.putExtra(Constants.MEMBER_PHONE, mUserMemberPhone)
        intent.putExtra(Constants.PROFILE_IMAGE, mUserProfileImage)
        startActivity(intent)
    }

    private fun getMemberIncomeDetail() {
        showProgressDialog()
        FireStoreClass().getIncomeDetailFromFirestore(
            this, mUserGroupName, mUserAdminEmail, mUserMemberEmail, mUserMemberName,
            currentYear()
        )

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
            val memberDetailAdapter = MemberDetailAdapter(
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

    @RequiresApi(Build.VERSION_CODES.N)
    suspend fun amountUpdateSuccess(memberList: ArrayList<MemberAccountDetail>) {
        for (i in memberList) {
            mCurrentMonthTotal =
                FireStoreClass().checkAmountMasterAccountForSameMonth(
                    i.groupName,
                    i.adminEmail,
                    i.month,
                    currentYear().toString()
                )
            Log.e("mCurrentMonthTotal", mCurrentMonthTotal.toString())
            val month = i.month
            val totalAmount = mCurrentMonthTotal.sum()
            val userHashMap = HashMap<String, Any>()
            userHashMap[Constants.INCOME] = totalAmount

            FireStoreClass().updateMasterAccount(
                this, i.adminEmail,
                i.groupName, month, currentYear(), userHashMap
            )
        }

    }

    fun updateMemberMasterAmountSuccess() {
        val intent = Intent(this@MemberDetailedActivity,
            ViewMemberAccountActivity::class.java)
        intent.putExtra(Constants.GROUP_NAME, mUserGroupName)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
        Log.e("updateMemberMaster", "delete only")
    }

    fun updateMasterAmountSuccess() {
        cancelProgressDialog()
        val intent = intent
        intent.putExtra(Constants.GROUP_NAME, mUserGroupName)
        intent.putExtra(Constants.FINISH, "finish")
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK and Intent.FLAG_ACTIVITY_NEW_TASK
        getMonthDataResult.launch(intent)
        finish()
        Log.e("updateMasterAmount", "updated only")


    }

    fun successGettingDocumentID(
        memberList: ArrayList<MemberAccountDetail>,
        currentAmount: Int,
        month: String
    ) {
        mCurrentDeleteDocumentID = memberList
        for (i in mCurrentDeleteDocumentID) {
            FireStoreClass().deleteMonthWiseMemberAccountDetail(this, i.id, currentAmount, month)
        }
    }

    suspend fun deletionSuccess(currentAmount: Int, month: String) {

        for (i in mCurrentDeleteDocumentID) {
            mCurrentAmount =
                FireStoreClass().checkAmountMasterAccountForSameMonth(
                    i.groupName,
                    i.adminEmail,
                    month,
                    currentYear().toString()
                )


            if (mCurrentAmount.sum() == 0) {
                val userHashMap = HashMap<String, Any>()
                userHashMap[Constants.INCOME] = mCurrentAmount.sum()
                FireStoreClass().updateMasterAccount(
                    this, i.adminEmail,
                    i.groupName, month, currentYear(), userHashMap
                )

            } else {
                val userHashMap = HashMap<String, Any>()
                val totalAmount = mCurrentAmount.sum()
                val finalAmount = currentAmount - totalAmount
                userHashMap[Constants.INCOME] = finalAmount
                FireStoreClass().updateMasterAccount(
                    this, i.adminEmail,
                    i.groupName, month, currentYear(), userHashMap
                )
            }


        }
    }

    fun successDeleteFromMemberAccount() {
        lifecycleScope.launch {
            FireStoreClass().deleteMemberFromMemberList(
                this@MemberDetailedActivity,
                mUserAdminEmail,
                mUserGroupName,
                mUserMemberEmail
            )
        }

    }

    suspend fun successDeleteFromMember() {
        Log.e("successDeleteFromMember", "memberDeleted start updating Master")
        val amountListJan: ArrayList<Int> = ArrayList()
        val amountListFeb: ArrayList<Int> = ArrayList()
        val amountListMar: ArrayList<Int> = ArrayList()
        val amountListApr: ArrayList<Int> = ArrayList()
        val amountListMay: ArrayList<Int> = ArrayList()
        val amountListJun: ArrayList<Int> = ArrayList()
        val amountListJul: ArrayList<Int> = ArrayList()
        val amountListAug: ArrayList<Int> = ArrayList()
        val amountListSep: ArrayList<Int> = ArrayList()
        val amountListOct: ArrayList<Int> = ArrayList()
        val amountListNov: ArrayList<Int> = ArrayList()
        val amountListDec: ArrayList<Int> = ArrayList()
        mAvailableMonthList = FireStoreClass().beforeDeleteGetMemberAccountDetailFromFirestore(
            mUserAdminEmail, mUserGroupName
        )
        Log.e("mAvailableMonthList", mAvailableMonthList.toString())
        if (mAvailableMonthList.size > 0) {
            for (i in mAvailableMonthList) {
                if (i.month == "January") {
                    amountListJan.add(i.currentAmount)
                    val finalAmount: Int = amountListJan.sum()
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.INCOME] = finalAmount
                    FireStoreClass().memberDeleteUpdateMasterAccount(
                        this,
                        mUserAdminEmail,
                        mUserGroupName,
                        i.month,
                        currentYear(),
                        userHashMap
                    )
                }
                if (i.month == "February") {
                    amountListFeb.add(i.currentAmount)
                    val finalAmount: Int = amountListFeb.sum()
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.INCOME] = finalAmount
                    FireStoreClass().memberDeleteUpdateMasterAccount(
                        this,
                        mUserAdminEmail,
                        mUserGroupName,
                        i.month,
                        currentYear(),
                        userHashMap
                    )
                }
                if (i.month == "March") {

                    amountListMar.add(i.currentAmount)
                    val finalAmount: Int = amountListMar.sum()
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.INCOME] = finalAmount
                    FireStoreClass().memberDeleteUpdateMasterAccount(
                        this,
                        mUserAdminEmail,
                        mUserGroupName,
                        i.month,
                        currentYear(),
                        userHashMap
                    )
                }
                if (i.month == "April") {

                    amountListApr.add(i.currentAmount)
                    val finalAmount: Int = amountListApr.sum()
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.INCOME] = finalAmount
                    FireStoreClass().memberDeleteUpdateMasterAccount(
                        this,
                        mUserAdminEmail,
                        mUserGroupName,
                        i.month,
                        currentYear(),
                        userHashMap
                    )
                }
                if (i.month == "May") {

                    amountListMay.add(i.currentAmount)
                    val finalAmount: Int = amountListMay.sum()
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.INCOME] = finalAmount
                    FireStoreClass().memberDeleteUpdateMasterAccount(
                        this,
                        mUserAdminEmail,
                        mUserGroupName,
                        i.month,
                        currentYear(),
                        userHashMap
                    )
                }
                if (i.month == "June") {

                    amountListJun.add(i.currentAmount)
                    val finalAmount: Int = amountListJun.sum()
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.INCOME] = finalAmount
                    FireStoreClass().memberDeleteUpdateMasterAccount(
                        this,
                        mUserAdminEmail,
                        mUserGroupName,
                        i.month,
                        currentYear(),
                        userHashMap
                    )
                }
                if (i.month == "July") {

                    amountListJul.add(i.currentAmount)
                    val finalAmount: Int = amountListJul.sum()
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.INCOME] = finalAmount
                    FireStoreClass().memberDeleteUpdateMasterAccount(
                        this,
                        mUserAdminEmail,
                        mUserGroupName,
                        i.month,
                        currentYear(),
                        userHashMap
                    )
                }
                if (i.month == "August") {

                    amountListAug.add(i.currentAmount)
                    val finalAmount: Int = amountListAug.sum()
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.INCOME] = finalAmount
                    FireStoreClass().memberDeleteUpdateMasterAccount(
                        this,
                        mUserAdminEmail,
                        mUserGroupName,
                        i.month,
                        currentYear(),
                        userHashMap
                    )
                }
                if (i.month == "September") {

                    amountListSep.add(i.currentAmount)
                    val finalAmount: Int = amountListSep.sum()
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.INCOME] = finalAmount
                    FireStoreClass().memberDeleteUpdateMasterAccount(
                        this,
                        mUserAdminEmail,
                        mUserGroupName,
                        i.month,
                        currentYear(),
                        userHashMap
                    )
                }
                if (i.month == "October") {

                    amountListOct.add(i.currentAmount)
                    val finalAmount: Int = amountListOct.sum()
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.INCOME] = finalAmount
                    FireStoreClass().memberDeleteUpdateMasterAccount(
                        this,
                        mUserAdminEmail,
                        mUserGroupName,
                        i.month,
                        currentYear(),
                        userHashMap
                    )
                }
                if (i.month == "November") {

                    amountListNov.add(i.currentAmount)
                    val finalAmount: Int = amountListNov.sum()
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.INCOME] = finalAmount
                    FireStoreClass().memberDeleteUpdateMasterAccount(
                        this,
                        mUserAdminEmail,
                        mUserGroupName,
                        i.month,
                        currentYear(),
                        userHashMap
                    )
                }
                if (i.month == "December") {

                    amountListDec.add(i.currentAmount)
                    val finalAmount: Int = amountListDec.sum()
                    val userHashMap = HashMap<String, Any>()
                    userHashMap[Constants.INCOME] = finalAmount
                    FireStoreClass().memberDeleteUpdateMasterAccount(
                        this,
                        mUserAdminEmail,
                        mUserGroupName,
                        i.month,
                        currentYear(),
                        userHashMap
                    )
                }
            }
        } else {
            updateMemberMasterAmountSuccess()
        }

        cancelProgressDialog()
    }


}