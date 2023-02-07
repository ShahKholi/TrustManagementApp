package com.android.trustmanagementapp.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences

import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.GroupViewAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.model.UserClass
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.MSPButton
import com.google.firebase.auth.FirebaseAuth

class AdminScreenActivity : BaseActivity() {
    lateinit var createGroupBtn: MSPButton
    lateinit var btnAddMemberActivity: MSPButton
    lateinit var btnAddAccountDetail: MSPButton
    lateinit var btnPreViewActivity: MSPButton
    lateinit var btnAddExpenseActivity: MSPButton
    lateinit var btnPreViewMemberDetail: MSPButton
    lateinit var btnAddTimeline: MSPButton
    lateinit var btnViewTimeLine: MSPButton
    lateinit var btnLogout: MSPButton
    lateinit var btnAboutGroup: MSPButton

    private lateinit var mGroupList: ArrayList<GroupNameClass>
    private lateinit var recyclerView: RecyclerView

    private lateinit var assignedAdminEmail: String

    private lateinit var mAdminUserList: ArrayList<UserClass>

    private val getGroupDataResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                finish()
            }
        }


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
        btnAddTimeline = findViewById(R.id.btn_add_timeline)
        btnViewTimeLine = findViewById(R.id.btn_view_timeline)
        btnLogout = findViewById(R.id.logout_admin)
        btnAboutGroup = findViewById(R.id.btn_add_about_group)

        getAdminCode()

        btnAboutGroup.setOnClickListener {
            val intent = Intent(this, AdminAboutGroupActivity::class.java)
            startActivity(intent)
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            finish()
            FirebaseAuth.getInstance().signOut()
        }

        btnViewTimeLine.setOnClickListener {
            val intent = Intent(this, ViewTimeLineActivity::class.java)
            startActivity(intent)
        }

        btnAddTimeline.setOnClickListener {
            val intent = Intent(this, AddTimelineActivity::class.java)
            startActivity(intent)
        }

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

    }

    private fun getAdminCode() {
        val currentUID = FireStoreClass().getCurrentUserID()
        FireStoreClass().getAdminDetail(this, currentUID)
        FireStoreClass().getAssignedGroupAdmin(this, currentUID)
    }

    override fun onBackPressed() {
        doubleBackToExit()
    }

    fun successAdminList(adminEmail: String) {
        assignedAdminEmail = adminEmail
        Log.e("assignedAdminEmail", assignedAdminEmail)

        getGroupListFromFireStore()
    }

    private fun getGroupListFromFireStore() {
        showProgressDialog()

        if (assignedAdminEmail.isNotEmpty() && assignedAdminEmail != "null") {
            val sharedPreferences = getSharedPreferences(
                Constants.STORE_ASSIGNED_ADMIN_EMAIL_ID, Context.MODE_PRIVATE
            )
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(
                Constants.STORE_ASSIGNED_ADMIN_EMAIL_ID, assignedAdminEmail
            )
            editor.apply()

            FireStoreClass().getAssignedGroupList(this, assignedAdminEmail)

        } else {
            val sharedPreferences = getSharedPreferences(
                Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
            )
            val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")

            FireStoreClass().getGroupList(this, getAdminEmailId!!)

        }

    }

    fun successGroupListFromFirestore(groupList: ArrayList<GroupNameClass>) {
        cancelProgressDialog()
        mGroupList = groupList

        if (mGroupList.size > 0) {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            groupList.sortBy { it.groupName }
            val cartListAdapter = GroupViewAdapter(
                this, mGroupList,
                this@AdminScreenActivity
            )
            recyclerView.adapter = cartListAdapter
        }
    }

    suspend fun successGroupDeleted(groupName: String, email: String) {
        Log.e("group", "current group was deleted successfully")

        val checkMemberGroupName: ArrayList<String> =
            FireStoreClass().checkGroupAvailableinMemberFirestore(
                email,
                groupName
            )
        if (checkMemberGroupName.size > 0) {
            FireStoreClass().deleteMemberDetail(email, groupName)
        }

        val checkMemberAccountList: ArrayList<String> =
            FireStoreClass().checkGroupAvailableinMemberAccountFirestore(
                email,
                groupName
            )
        if (checkMemberAccountList.size > 0) {
            FireStoreClass().deleteMemberAccount(email, groupName)
        }
        val expenseAccountList: ArrayList<String> =
            FireStoreClass().checkExpenseGroupAvailableInFirestore(
                email,
                groupName
            )
        if (expenseAccountList.size > 0) {
            FireStoreClass().deleteExpenseAccount(email, groupName)
        }
        val masterAccountList: ArrayList<String> =
            FireStoreClass().checkMasterGroupAvailableInFirestore(
                email,
                groupName
            )
        if (masterAccountList.size > 0) {
            FireStoreClass().deleteMasterAccount(email, groupName)
        }

        val checkAboutGroupDetail: String = FireStoreClass().checkAboutGroupAvailableinFirestore(
            email, groupName
        )
        if (checkAboutGroupDetail.isNotEmpty() && checkAboutGroupDetail != "null") {
            FireStoreClass().deleteMasterAboutGroup(email, groupName)
        }

        Log.e("All Data", "All the data deleted successfully")
        cancelProgressDialog()
        val intent = intent
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        getGroupDataResult.launch(intent)
    }

    fun successAdminUserList(userList: ArrayList<UserClass>) {
        mAdminUserList = userList
        for (i in mAdminUserList) {
            val sharedPreferences = getSharedPreferences(
                Constants.STORE_TIMELINE_MEMBER_NAME, Context.MODE_PRIVATE
            )
            val editor: SharedPreferences.Editor = sharedPreferences.edit()
            editor.putString(
                Constants.STORE_TIMELINE_MEMBER_NAME, i.firstName + " " + i.lastName
            )
            editor.apply()
        }
    }


}