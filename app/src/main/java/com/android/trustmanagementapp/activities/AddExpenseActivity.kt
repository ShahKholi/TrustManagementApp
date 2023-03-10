package com.android.trustmanagementapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.ExpenseDetailAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.model.MasterAccountDetail
import com.android.trustmanagementapp.model.MonthExpense
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPButton
import com.android.trustmanagementapp.utils.MSPEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.IOException
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
    private val hashMapUp: HashMap<String, Any> = HashMap()
    private lateinit var mCurrentMonthTotal: ArrayList<Int>
    lateinit var mCurrentDeleteDocumentID: ArrayList<MonthExpense>

    private lateinit var mExpenseList: ArrayList<MonthExpense>
    private lateinit var recyclerView: RecyclerView
    private lateinit var mCurrentAmount: ArrayList<Int>
    private lateinit var imageExpenseView : ImageView

    private var mSelectedGroupImageUri: Uri? = null
    private var mSelectedImageCloudUriString: String? = null

    private val getExpenseDataResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                finish()
            }
        }

    private val getPhotoActionResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                onPhotoResult(result.resultCode, data)
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
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
        imageExpenseView = findViewById(R.id.iv_expense_image)

        getGroupNameFromFireStore()
        getDateList()
        loadExpenseDetailFromFireStore()

        imageExpenseView.setOnClickListener {
            permissionForPhoto()
        }


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


    private fun permissionForPhoto() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            val galleryIntent = Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            getPhotoActionResult.launch(galleryIntent)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                Constants.READ_STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val galleryIntent = Intent(
                    Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                getPhotoActionResult.launch(galleryIntent)
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun onPhotoResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    // The uri of selected image from phone storage.
                    mSelectedGroupImageUri = data.data!!
                    GlideLoaderClass(this).loadGroupIcon(mSelectedGroupImageUri!!, imageExpenseView)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@AddExpenseActivity,
                        "Image selection failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun getGroupImage(groupName: String) {
        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")

        val groupImage: String =
            FireStoreClass().getGroupImageFromFirestore( groupName,getAdminEmailId!!)
        val imageStringCheck: String = mSelectedGroupImageUri.toString()
        if (imageStringCheck.isNotEmpty() && imageStringCheck != "null"){
            uploadUserImage(groupImage)
        }else {
            registerExpenseAmountDetail(groupImage)
        }


    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadUserImage(groupImage: String) {
        if (validateRegisterExpenseAmountField()) {
            showProgressDialog()
            FireStoreClass().uploadImageToCloudStorage(
                this, mSelectedGroupImageUri!!,
                Constants.EXPENSE_IMAGE,groupImage
            )
        }
    }

    suspend fun successGroupIconUpload(uri: Uri, groupImage: String) {
        mSelectedImageCloudUriString = uri.toString()

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
            groupImage,
            "",
            currentYear(),
            mSelectedImageCloudUriString!!
        )
        cancelProgressDialog()
        FireStoreClass().registerExpenseDetail(this, monthExpenseAmount)


    }

    private fun loadExpenseDetailFromFireStore() {
        showProgressDialog()
        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
        FireStoreClass().loadExpenseDetail(this,getAdminEmailId!!)
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
                groupImageString,
                "",
                currentYear()
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
        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
        FireStoreClass().getGroupList(this,getAdminEmailId!!)
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
        val previousExpenseCurrentMonth: Int =
            FireStoreClass().checkPreviousAmountForCurrentMonthInMaster(
                monthExpenseAmount.groupName,
                monthExpenseAmount.memberAdminEmail, monthExpenseAmount.month, currentYear()
            )
        Log.e("check value", monthExpenseAmount.toString())

        updateExpenseAmountToMaster(
            monthExpenseAmount.groupName,
            monthExpenseAmount.memberAdminEmail,
            monthExpenseAmount.expenseAmount + previousExpenseCurrentMonth,

            monthExpenseAmount.month
        )
        successCreationToMaster()
    }

    private fun successCreationToMaster() {
        Toast.makeText(this, "Expense Amount added", Toast.LENGTH_SHORT).show()
        val intent = intent
        getExpenseDataResult.launch(intent)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        cancelProgressDialog()
    }

    private fun updateExpenseAmountToMaster(
        groupName: String,
        memberAdminEmail: String,
        expenseAmount: Int, month: String
    ) {
        hashMapUp["monthExpense"] = month
        hashMapUp["expenseAmount"] = expenseAmount
        FireStoreClass().createExpenseAmountToMasterAccount(
            this,
            groupName, memberAdminEmail, hashMapUp, month, currentYear()
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

    suspend fun expenseUpdateSuccess(monthExpenseList: ArrayList<MonthExpense>) {

        for (i in monthExpenseList) {
            mCurrentMonthTotal = FireStoreClass().getAllExpenseDetailForCurrentMonth(
                i.memberAdminEmail,
                i.month, i.groupName, i.year
            )
            Log.e("mCurrentMonthTotal sum", mCurrentMonthTotal.toString())
            if (mCurrentMonthTotal.sum() != 0) {
                val finalAmount = mCurrentMonthTotal.sum()
                val userHashMap: HashMap<String, Any> = HashMap()
                userHashMap[Constants.EXPENSE_AMOUNT] = finalAmount
                FireStoreClass().createExpenseAmountToMasterAccount(
                    this,
                    i.groupName, i.memberAdminEmail, userHashMap, i.month, currentYear()
                )
            }else{
                cancelProgressDialog()
            }

        }
    }

    fun successUpdateToMaster() {
        cancelProgressDialog()
        val intent = intent
        getExpenseDataResult.launch(intent)
        finish()
    }

    fun successGettingDocumentID(expenseList: ArrayList<MonthExpense>,
                                 expenseAmount: Int, month: String) {
        mCurrentDeleteDocumentID = expenseList
        for (i in mCurrentDeleteDocumentID){
            FireStoreClass().deleteExpenseMonthWise(this, i.id, expenseAmount, month)
        }

    }

    suspend fun deletionSuccess(expenseAmount: Int, month: String) {
        for(i in mCurrentDeleteDocumentID){
            mCurrentAmount = FireStoreClass().getAllExpenseDetailForCurrentMonth(
                i.memberAdminEmail,month,i.groupName,currentYear()
            )
            val finalAmount = mCurrentAmount.sum()
            val userHashMap = HashMap<String, Any>()
            userHashMap[Constants.EXPENSE_AMOUNT] = finalAmount
            FireStoreClass().updateMasterAccount(
                this, i.memberAdminEmail,
                i.groupName, month, currentYear(), userHashMap
            )
        }

    }
    fun updateMasterAmountSuccess() {
        cancelProgressDialog()
        val intent = intent
        getExpenseDataResult.launch(intent)
        finish()

    }

}


