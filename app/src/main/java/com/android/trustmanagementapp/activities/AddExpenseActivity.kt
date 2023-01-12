package com.android.trustmanagementapp.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.ExpenseDetailAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.model.MasterAccountDetail
import com.android.trustmanagementapp.model.MemberAccountDetail
import com.android.trustmanagementapp.model.MonthExpense
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.MSPButton
import com.android.trustmanagementapp.utils.MSPEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AddExpenseActivity : BaseActivity() {
    private var currentFirebaseUser = FirebaseAuth.getInstance().currentUser
    private lateinit var autoCompleteGroupName: AutoCompleteTextView
    private lateinit var adapterGroupItems: ArrayAdapter<String>
    private lateinit var adapterMonthItems: ArrayAdapter<String>
    private lateinit var autoCompleteMonthView: AutoCompleteTextView
    private lateinit var etExpenseAmount: MSPEditText
    private lateinit var etExpenseDetail: MSPEditText
    private lateinit var btnExpense: MSPButton
    private lateinit var currentGroupNameSelect: String
    private lateinit var mGroupList: ArrayList<GroupNameClass>
    private val hashMapUp: HashMap<String, Any> = HashMap()

    private lateinit var mExpenseList: ArrayList<MonthExpense>
    private lateinit var recyclerView: RecyclerView

    private val getExpenseDataResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                finish()

            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()

        autoCompleteGroupName = findViewById(R.id.auto_complete_expense_group)
        autoCompleteMonthView = findViewById(R.id.auto_complete_expense_month)
        etExpenseAmount = findViewById(R.id.et_expense_amount)
        etExpenseDetail = findViewById(R.id.et_expense_detail)
        btnExpense = findViewById(R.id.btn_expense_account)
        recyclerView = findViewById(R.id.rcv_expense_group_detail)

        getGroupNameFromFireStore()
        getDateList()
        loadExpenseDetailFromFireStore()


        btnExpense.setOnClickListener {
            val grpName = autoCompleteGroupName.text.toString()
            val month = autoCompleteMonthView.text.toString()

            lifecycleScope.launch {
                if (validateRegisterExpenseAmountField()) {
                    val sharedPreferences = getSharedPreferences(
                        Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
                    )
                    val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")

                    val masterAccountList: ArrayList<MasterAccountDetail> =
                        FireStoreClass().checkMasterAccountForSameMonth(
                            grpName, getAdminEmailId, month
                        )
                    if (masterAccountList.size > 0) {
                        getGroupImage(currentGroupNameSelect)
                    } else {
                        showSnackBarMessage(
                            "Please add First Account detail for this $month",
                            false
                        )
                    }

                }
            }
        }

    }

    private suspend fun getGroupImage(groupName: String) {
        val imageUrl: ArrayList<GroupNameClass> =
            FireStoreClass().getGroupImageFromFirestore(this, groupName)

        mGroupList = imageUrl
        for (i in mGroupList) {
            val groupImageString = i.groupImage
            lifecycleScope.launch {
                registerExpenseAmountDetail(groupImageString)
            }
        }
    }

    private fun loadExpenseDetailFromFireStore() {
        showProgressDialog()
        FireStoreClass().loadExpenseDetail(this)
    }

    private suspend fun registerExpenseAmountDetail(groupImageString: String) {
        if (validateRegisterExpenseAmountField()) {
            showProgressDialog()
            val sharedPreferences = getSharedPreferences(
                Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
            )
            val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")

            val adminUid = currentFirebaseUser!!.uid
            val groupName = autoCompleteGroupName.text.toString()
            val month = autoCompleteMonthView.text.toString()
            val expAmount: String = etExpenseAmount.text.toString().trim { it <= ' ' }
            val detail: String = etExpenseDetail.text.toString().trim { it <= ' ' }

            val monthExpenseAmount = MonthExpense(
                groupName,
                adminUid,
                getAdminEmailId!!,
                expAmount.toInt(),
                month,
                detail,
                groupImageString
            )
            FireStoreClass().registerExpenseDetail(this, monthExpenseAmount)
        }
    }

    private fun validateRegisterExpenseAmountField(): Boolean {
        return when {
            TextUtils.isEmpty(autoCompleteGroupName.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(
                    resources.getString(R.string.please_select_group_dropdown),
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
            TextUtils.isEmpty(etExpenseAmount.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(
                    resources.getString(R.string.please_enter_amount),
                    true
                )
                false
            }

            TextUtils.isEmpty(etExpenseDetail.text.toString().trim { it <= ' ' }) -> {
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

    private fun getGroupNameFromFireStore() {
        FireStoreClass().getGroupList(this)
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
                    currentGroupNameSelect = item

                })
        }
    }

    suspend fun groupExpenseDetailSuccess(monthExpenseAmount: MonthExpense) {
        cancelProgressDialog()
        Toast.makeText(
            this, "Expense Amount ${monthExpenseAmount.expenseAmount}", Toast.LENGTH_LONG
        ).show()

        val previousExpenseCurrentMonth: Int =
            FireStoreClass().checkPreviousAmountForCurrentMonthInMaster(
                monthExpenseAmount.groupName,
                monthExpenseAmount.memberAdminEmail, monthExpenseAmount.month, currentYear()
            )
        Log.e("check value", previousExpenseCurrentMonth.toString())



        updateExpenseAmountToMaster(
            monthExpenseAmount.groupName,
            monthExpenseAmount.memberAdminEmail,
            monthExpenseAmount.expenseAmount + previousExpenseCurrentMonth,
            monthExpenseAmount.month
        )
        val intent = intent
        getExpenseDataResult.launch(intent)
        finish()
    }

    private fun updateExpenseAmountToMaster(
        groupName: String,
        memberAdminEmail: String,
        expenseAmount: Int, month: String
    ) {
        hashMapUp["monthExpense"] = month
        hashMapUp["expenseAmount"] = expenseAmount
        FireStoreClass().createExpenseAmountToMasterAccount(
            groupName, memberAdminEmail, hashMapUp, month
        )

    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_add_expense)
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
    fun successExpenseListFromFirestore(expenseList: ArrayList<MonthExpense>) {
        cancelProgressDialog()
        mExpenseList = expenseList
        if (mExpenseList.size > 0) {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            //mExpenseList.sortBy { it.month }
            mExpenseList.sortBy {
                /* val myMonth: MutableList<Array<Month>> = Arrays.asList(Month.values())
                 Collections.rotate(myMonth,myMonth.size)*/
                val sdf = SimpleDateFormat("MMMM")
                sdf.parse(it.month)
            }

            val cartListAdapter = ExpenseDetailAdapter(
                this, mExpenseList,
                this@AddExpenseActivity
            )
            recyclerView.adapter = cartListAdapter

        }

    }


}


