package com.android.trustmanagementapp.activities

import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.PreExpenseImageListAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MonthExpense
import com.android.trustmanagementapp.utils.Constants

lateinit var mGroupName: String
lateinit var mMonthName: String
lateinit var mAdminEmail: String
private lateinit var mExpenseList: ArrayList<MonthExpense>
private lateinit var recyclerView: RecyclerView

class PreExpenseImageActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_expense_image)

        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()
        if(intent.hasExtra(Constants.GROUP_NAME)){
            mGroupName = intent.getStringExtra(Constants.GROUP_NAME)!!
        }
        if(intent.hasExtra(Constants.MONTH)){
            mMonthName = intent.getStringExtra(Constants.MONTH)!!
        }
        if(intent.hasExtra(Constants.MEMBER_ADMIN_EMAIL)){
            mAdminEmail = intent.getStringExtra(Constants.MEMBER_ADMIN_EMAIL)!!
        }
        recyclerView = findViewById(R.id.rcv_pre_expense)

        getExpenseImageDetail()

    }

    private fun getExpenseImageDetail() {
        showProgressDialog()
        FireStoreClass().getExpenseImageDetail(this,mGroupName,mMonthName,mAdminEmail)
    }


    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_expense_image_pre)
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

    fun expenseImageSuccess(expenseList: ArrayList<MonthExpense>) {
            cancelProgressDialog()
        mExpenseList = expenseList
        if(mExpenseList.size > 0){
            recyclerView.layoutManager = GridLayoutManager(this,2)
            recyclerView.setHasFixedSize(true)

            val cartListAdapter = PreExpenseImageListAdapter(
                this, mExpenseList,this
            )
            recyclerView.adapter = cartListAdapter
        }
    }
}