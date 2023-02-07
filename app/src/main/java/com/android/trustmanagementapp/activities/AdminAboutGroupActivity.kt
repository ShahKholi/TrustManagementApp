package com.android.trustmanagementapp.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
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
import com.android.trustmanagementapp.adapter.AboutGroupAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.AboutGroup
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.MSPButton
import com.android.trustmanagementapp.utils.MSPEditText
import kotlinx.coroutines.launch


class AdminAboutGroupActivity : BaseActivity() {

    private lateinit var autoCompleteGroupName: AutoCompleteTextView
    private lateinit var adapterGroupItems: ArrayAdapter<String>
    private lateinit var btnAboutGroup: MSPButton
    private lateinit var tvAddressField : MSPEditText
    private lateinit var tvAboutDetail : MSPEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var currentGroupNameSelect: String

    private lateinit var aboutGroupList : ArrayList<AboutGroup>



    private val getAboutDataResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_about_group)

        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        btnAboutGroup = findViewById(R.id.btn_about_group)
        tvAddressField = findViewById(R.id.et_address)
        tvAboutDetail = findViewById(R.id.et_description)
        autoCompleteGroupName = findViewById(R.id.auto_complete_expense_group)
        recyclerView = findViewById(R.id.rcv_expense_group_detail)

        setUpSupportActionBar()

        getGroupNameFromFireStore()
        loadExpenseDetailFromFireStore()

        btnAboutGroup.setOnClickListener {
            lifecycleScope.launch {
                createAboutGroup()
            }

        }

    }

    private fun loadExpenseDetailFromFireStore() {
        showProgressDialog()
        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")!!
        FireStoreClass().getAboutDetail(this, getAdminEmailId)
    }

    private suspend fun createAboutGroup() {
        if(validateAboutGroupField()){


            val sharedPreferences = getSharedPreferences(
                Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
            )
            val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")!!
            val groupName = autoCompleteGroupName.text.toString().trim { it <= ' ' }
            val address = tvAddressField.text.toString().trim { it <= ' ' }
            val detail = tvAboutDetail.text.toString().trim { it <= ' ' }

            val checkAboutGroupAvailable : String = FireStoreClass().checkAboutGroupAvailableinFirestore(
                getAdminEmailId,groupName
            )
            if(checkAboutGroupAvailable.isNotEmpty() && checkAboutGroupAvailable != "null"){
                Toast.makeText(this, "About Group detail already added in your $groupName",
                Toast.LENGTH_LONG).show()
            }else{
                showProgressDialog()
                val aboutGroup = AboutGroup(
                    groupName,
                    getAdminEmailId,
                    address,
                    detail
                )
                FireStoreClass().createAboutGroup(this, aboutGroup)
            }


        }
    }

    private fun validateAboutGroupField(): Boolean {
        return when {
            TextUtils.isEmpty(autoCompleteGroupName.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(
                    resources.getString(R.string.please_select_group_dropdown),
                    true
                )
                false
            }

            TextUtils.isEmpty(tvAddressField.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(
                    resources.getString(R.string.please_enter_address),
                    true
                )
                false
            }

            TextUtils.isEmpty(tvAboutDetail.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(
                    resources.getString(R.string.please_enter_about),
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

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_about_group)
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

    fun successCreateAboutGroup(aboutGroup: AboutGroup) {
        cancelProgressDialog()
        val intent = intent
        getAboutDataResult.launch(intent)
        finish()
    }

    fun successLoadAboutList(aboutList: ArrayList<AboutGroup>) {
        aboutGroupList = aboutList
        cancelProgressDialog()
        if(aboutGroupList.size > 0){
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            aboutGroupList.sortBy {
                it.groupName
            }


            val cartListAdapter = AboutGroupAdapter(
                this, aboutGroupList,
                this@AdminAboutGroupActivity
            )
            recyclerView.adapter = cartListAdapter
        }
    }

    fun deletionSuccess() {
        cancelProgressDialog()
        val intent = intent
        getAboutDataResult.launch(intent)
        finish()
    }

}