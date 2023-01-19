package com.android.trustmanagementapp.activities

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MemberAccountDetail
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.model.MasterAccountDetail
import com.android.trustmanagementapp.model.MemberClass
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.MSPButton
import com.android.trustmanagementapp.utils.MSPEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AddAccountActivity : BaseActivity() {
    private var currentFirebaseUser = FirebaseAuth.getInstance().currentUser
    private lateinit var adapterGroupItems: ArrayAdapter<String>
    private lateinit var adapterMemberItems: ArrayAdapter<String>
    private lateinit var autoCompleteGroupName: AutoCompleteTextView
    private lateinit var adapterMonthItems: ArrayAdapter<String>
    private lateinit var autoCompleteMemberName: AutoCompleteTextView
    private lateinit var autoCompleteMonthView: AutoCompleteTextView
    private lateinit var etAddAmount: MSPEditText
    private lateinit var btnAddAmount: MSPButton

    private lateinit var mMemberList: ArrayList<MemberClass>

    private lateinit var mCurrentMemberList: ArrayList<MemberClass>

    private lateinit var mMemberAccountList: ArrayList<MemberAccountDetail>

    private lateinit var mMasterAccountList: ArrayList<MasterAccountDetail>
    lateinit var mAdminEmail: String

    private lateinit var mCurrentMonthTotal: ArrayList<Int>


    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_account)
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        autoCompleteGroupName = findViewById(R.id.auto_complete_text_group)
        autoCompleteMemberName = findViewById(R.id.auto_complete_text_member)
        autoCompleteMonthView = findViewById(R.id.auto_complete_text_month)
        etAddAmount = findViewById(R.id.et_add_amount)
        btnAddAmount = findViewById(R.id.btn_add_account)

        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
        mAdminEmail = getAdminEmailId!!

        setUpSupportActionBar()
        getGroupNameFromFireStore()

        getDateList()

        btnAddAmount.setOnClickListener {
            registerAmountDetail()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun registerAmountDetail() {
        if (validateRegisterAmountField()) {
            showProgressDialog()
            val sharedPreferences = getSharedPreferences(
                Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
            )
            val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
            val groupName = autoCompleteGroupName.text.toString()
            val memberName = autoCompleteMemberName.text.toString()


            FireStoreClass().getMemberEmailFromFireStore(
                this,
                getAdminEmailId.toString(),
                groupName,
                memberName
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun successMemberEmail(memberList: ArrayList<MemberClass>) {

        val groupName = autoCompleteGroupName.text.toString()
        val month = autoCompleteMonthView.text.toString()
        mCurrentMemberList = memberList

        for (i in mCurrentMemberList) {
            FireStoreClass().checkMonthAvailableForThisMember(
                this,
                groupName,
                i.memberName,
                i.memberEmail,
                month
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun updateMemberMonthAmount(previousAmount: Int) {
        val memberList = mCurrentMemberList
        val month = autoCompleteMonthView.text.toString()
        for (i in memberList) {
            val groupName = i.groupName
            val memberEmail = i.memberEmail
            val memberName = i.memberName
            val adminEmail = i.memberAdminEmail


            val amount = etAddAmount.text.toString().trim { it <= ' ' }
            val totalAmount = amount.toInt() + previousAmount
            val userHashMap = HashMap<String, Any>()
            userHashMap[Constants.CURRENT_AMOUNT] = totalAmount
            FireStoreClass().updateCurrentAmountMember(
                this, userHashMap,
                groupName,
                month,
                memberEmail,
                memberName,
                adminEmail
            )
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun createMemberAccount() {
        val memberList = mCurrentMemberList
        val month = autoCompleteMonthView.text.toString()
        val amount = etAddAmount.text.toString().trim { it <= ' ' }
        for (i in memberList) {
            val accountDetail = MemberAccountDetail(
                currentYear().toString(),
                i.groupName,
                i.memberName,
                i.memberEmail,
                i.memberAdminEmail,
                month,
                amount.toInt()
            )
            FireStoreClass().createAccountDetailToFirestore(this, accountDetail)
        }

    }


    private fun validateRegisterAmountField(): Boolean {
        return when {
            TextUtils.isEmpty(autoCompleteGroupName.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(
                    resources.getString(R.string.please_select_group_dropdown),
                    true
                )
                false
            }
            TextUtils.isEmpty(autoCompleteMemberName.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(
                    resources.getString(R.string.please_select_member_dropdown),
                    true
                )
                false
            }
            TextUtils.isEmpty(autoCompleteMonthView.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(
                    resources.getString(R.string.please_select_date_dropdown),
                    true
                )
                false
            }
            TextUtils.isEmpty(etAddAmount.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(
                    resources.getString(R.string.please_enter_amount),
                    true
                )
                false
            }
            else -> {
                true
            }
        }
    }

    private fun getDateList() {
        val result = dateList()

        adapterMonthItems = ArrayAdapter<String>(
            this,
            R.layout.support_simple_spinner_dropdown_item, result
        )
        adapterMonthItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        autoCompleteMonthView.setAdapter(adapterMonthItems)
        autoCompleteMonthView.onItemClickListener =
            (AdapterView.OnItemClickListener() { adapterView: AdapterView<*>, view1: View,
                                                 position: Int, l: Long ->
                val item = adapterView.getItemAtPosition(position).toString()

            })
    }

    private fun getMemberNameFromFireStore(groupName: String) {
        FireStoreClass().getMemberList(this, groupName, mAdminEmail)
    }

    private fun getGroupNameFromFireStore() {
        FireStoreClass().getGroupList(this)
    }

    fun successMemberListFromFirestore(memberList: ArrayList<MemberClass>) {
        mMemberList = memberList
        val result = ArrayList<String>()
        for (i in memberList) {
            result.sort()
            result.add(i.memberName)
            adapterMemberItems = ArrayAdapter<String>(
                this,
                R.layout.support_simple_spinner_dropdown_item, result
            )

            // Specify the layout to use when the list of choices appear
            adapterMemberItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            autoCompleteMemberName.setAdapter(adapterMemberItems)
            autoCompleteMemberName.onItemClickListener =
                (AdapterView.OnItemClickListener() { adapterView: AdapterView<*>, view1: View,
                                                     position: Int, l: Long ->
                    val item = adapterView.getItemAtPosition(position).toString()

                })
        }
    }

    fun successGroupListFromFirestore(groupList: ArrayList<GroupNameClass>) {
        val result = ArrayList<String>()
        for (i in groupList) {
            result.add(i.groupName)
            adapterGroupItems = ArrayAdapter<String>(
                this,
                R.layout.support_simple_spinner_dropdown_item, result
            )

            // Specify the layout to use when the list of choices appear
            adapterGroupItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            autoCompleteGroupName.setAdapter(adapterGroupItems)
            autoCompleteGroupName.onItemClickListener =
                (AdapterView.OnItemClickListener() { adapterView: AdapterView<*>, view1: View,
                                                     position: Int, l: Long ->
                    val item = adapterView.getItemAtPosition(position).toString()
                    getMemberNameFromFireStore(autoCompleteGroupName.text.toString())
                })

        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun accountRegistrationSuccess(member: MemberAccountDetail) {
        cancelProgressDialog()
        Toast.makeText(
            this, "${member.memberName} detail added",
            Toast.LENGTH_LONG
        ).show()

        lifecycleScope.launch {
            getAllMonthAvailableInFireStore(member.groupName, member.adminEmail)
        }

    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_add_account)
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

    @RequiresApi(Build.VERSION_CODES.N)
    private suspend fun getAllMonthAvailableInFireStore(
        groupName: String,
        getAdminEmailIdText: String?
    ) {

        mMemberAccountList = FireStoreClass().allAvailableMonthList(groupName, getAdminEmailIdText)

        val groupNameMT = autoCompleteGroupName.text.toString()
        val amountMT = etAddAmount.text.toString().trim { it <= ' ' }
        val monthMt = autoCompleteMonthView.text.toString()

        val masterAccountDetail = MasterAccountDetail(
            "",
            groupNameMT,
            getAdminEmailIdText!!,
            currentYear(),
            monthMt,
            amountMT.toInt()
        )

        val checkMasterAccountExist: ArrayList<MasterAccountDetail> =
            FireStoreClass().getAllMasterAccount(groupNameMT, getAdminEmailIdText, monthMt)

        if (checkMasterAccountExist.size > 0) {
            mCurrentMonthTotal = FireStoreClass().getTotalAmount(
                groupName, getAdminEmailIdText,
                monthMt
            )
            val finalAmount = mCurrentMonthTotal.sum()

            val userHashMap = HashMap<String, Any>()
            userHashMap[Constants.INCOME] = finalAmount
            FireStoreClass().updateMasterAccount(
                this,
                getAdminEmailIdText, groupName, monthMt, currentYear(), userHashMap
            )

        } else {
            FireStoreClass().registerMasterAccount(this, masterAccountDetail)
        }

    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun amountUpdateSuccess() {

        Toast.makeText(
            this, "member detail added",
            Toast.LENGTH_LONG
        ).show()

        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")


        lifecycleScope.launch {
            getAllMonthAvailableInFireStore(autoCompleteGroupName.text.toString(), getAdminEmailId)
        }

    }

    fun masterAccountRegisterSuccess() {
        cancelProgressDialog()
        // autoCompleteGroupName.setText("")
        autoCompleteMemberName.setText("")
        autoCompleteMonthView.setText("")
        etAddAmount.setText("")

    }

    fun updateMasterAmountSuccess() {
        cancelProgressDialog()
        //  autoCompleteGroupName.setText("")
        autoCompleteMemberName.setText("")
        autoCompleteMonthView.setText("")
        etAddAmount.setText("")
    }


}