package com.android.trustmanagementapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.MSPTextViewBold
import kotlinx.coroutines.launch

class GuestInformationActivity : BaseActivity() {
    private lateinit var mGroupName: String
    private lateinit var mAdmin: String
    private lateinit var mMemberEmail: String

    private lateinit var tvAdminCode : MSPTextViewBold
    private lateinit var tvTotalAmount : MSPTextViewBold
    private lateinit var tvOrginateDate : MSPTextViewBold
    private lateinit var tvAdminEmail : MSPTextViewBold
    private lateinit var tvGroupName : MSPTextViewBold


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_information)

        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()

        tvAdminCode = findViewById(R.id.tv_code_view)
        tvTotalAmount = findViewById(R.id.tv_till_paid)
        tvOrginateDate = findViewById(R.id.tv_originate_date)
        tvAdminEmail = findViewById(R.id.tv_your_admin_email)
        tvGroupName = findViewById(R.id.tv_your_group)

        if(intent.hasExtra(Constants.GROUP_NAME)){
            mGroupName = intent.getStringExtra(Constants.GROUP_NAME)!!
            tvGroupName.text = mGroupName
        }
        if(intent.hasExtra(Constants.ADMIN_EMAIL)){
            mAdmin = intent.getStringExtra(Constants.ADMIN_EMAIL)!!
            tvAdminEmail.text = mAdmin

        }
        if(intent.hasExtra(Constants.MEMBER_EMAIL)){
            mMemberEmail = intent.getStringExtra(Constants.MEMBER_EMAIL)!!
        }

        lifecycleScope.launch {
            getAdminCode()
        }
        lifecycleScope.launch {
            getFullAmountForCurrentUser()
        }

        lifecycleScope.launch {
            getGroupDetail()
        }


    }

    private suspend fun getGroupDetail() {
       val groupDate : String = FireStoreClass().getGroupDate(mAdmin,mGroupName)
        tvOrginateDate.text = groupDate
    }

    private suspend fun getFullAmountForCurrentUser() {
        showProgressDialog()
        val totalAmount : ArrayList<Int> = FireStoreClass().memberAccountTotalAmountFirestore(
            mGroupName,mAdmin,mMemberEmail
        )
        val finalAmount = totalAmount.sum()
        tvTotalAmount.text = finalAmount.toString()
        cancelProgressDialog()

    }

    private suspend fun getAdminCode() {
        val adminCode = FireStoreClass().getAdminCodeFromUserFireStore(mAdmin)
        tvAdminCode.text = adminCode
    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_info)
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