package com.android.trustmanagementapp.activities

import android.content.Intent

import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.GroupViewAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.utils.MSPButton

class AdminScreenActivity : BaseActivity() {
    lateinit var createGroupBtn: MSPButton
    lateinit var btnAddMemberActivity: MSPButton
    lateinit var btnAddAccountDetail : MSPButton
    lateinit var btnPreViewActivity : MSPButton
    lateinit var btnAddExpenseActivity : MSPButton
    lateinit var btnPreViewMemberDetail : MSPButton

    private lateinit var mGroupList: ArrayList<GroupNameClass>
    private lateinit var recyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_screen)

        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        recyclerView = findViewById(R.id.rcv_admin_group_detail)
        createGroupBtn = findViewById(R.id.btn_create_group_admin_screen)
        btnAddMemberActivity = findViewById(R.id.btn_add_member_admin_screen)
        btnAddAccountDetail = findViewById(R.id.btn_add_account_admin_screen)
        btnPreViewActivity = findViewById(R.id.btn_view_account_detail_admin_screen)
        btnAddExpenseActivity = findViewById(R.id.btn_add_expense_admin_screen)
        btnPreViewMemberDetail = findViewById(R.id.btn_view_member_admin_screen)

        btnPreViewMemberDetail.setOnClickListener {
            val intent = Intent(this, PreViewMemberActivity::class.java)
            startActivity(intent)
        }

        btnAddExpenseActivity.setOnClickListener {
            val intent = Intent(this, AddExpenseActivity::class.java)
            startActivity(intent)
        }

        btnPreViewActivity.setOnClickListener {
            val intent = Intent(this, PreViewAccountActivity::class.java)
            startActivity(intent)
        }

        btnAddAccountDetail.setOnClickListener {
            val intent = Intent(this, AddAccountActivity::class.java)
            startActivity(intent)
        }

        createGroupBtn.setOnClickListener {
            val intent = Intent(this, CreateGroupActivity::class.java)
            startActivity(intent)
        }

        btnAddMemberActivity.setOnClickListener {
            val intent = Intent(this, AddMemberActivity::class.java)
            startActivity(intent)
        }

        getGroupListFromFireStore()
    }

    private fun getGroupListFromFireStore() {
        showProgressDialog()
        FireStoreClass().getGroupList(this)
    }

    fun successGroupListFromFirestore(groupList: ArrayList<GroupNameClass>) {
        cancelProgressDialog()
        mGroupList = groupList
        if (mGroupList.size > 0) {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            groupList.sortBy { it.groupName }
            val cartListAdapter = GroupViewAdapter(this, mGroupList,
                this@AdminScreenActivity)
            recyclerView.adapter = cartListAdapter
        }
    }

}