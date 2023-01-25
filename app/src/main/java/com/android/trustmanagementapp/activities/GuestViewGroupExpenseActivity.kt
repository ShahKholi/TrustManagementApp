package com.android.trustmanagementapp.activities

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.ExpenseDetailAdapter
import com.android.trustmanagementapp.adapter.GuestExpenseDetailAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MonthExpense
import java.text.SimpleDateFormat

class GuestViewGroupExpenseActivity : BaseActivity() {
    private lateinit var mExpenseList: ArrayList<MonthExpense>
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_view_group_expense)
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        recyclerView = findViewById(R.id.rcv_expense_group_detail)
        setUpSupportActionBar()
        loadExpenseDetailFromFireStore()
    }

    private fun loadExpenseDetailFromFireStore() {
        showProgressDialog()
        FireStoreClass().loadExpenseDetail(this)
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

            val cartListAdapter = GuestExpenseDetailAdapter(
                this, mExpenseList,
                this@GuestViewGroupExpenseActivity
            )
            recyclerView.adapter = cartListAdapter

        }

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
}