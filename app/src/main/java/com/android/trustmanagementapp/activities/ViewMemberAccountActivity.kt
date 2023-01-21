package com.android.trustmanagementapp.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.MemberListAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.model.MemberClass
import com.android.trustmanagementapp.model.UserClass
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.MSPTextViewBold
import org.checkerframework.checker.units.qual.A




class ViewMemberAccountActivity : BaseActivity() {
    lateinit var mGroupName: String
    lateinit var mAdminEmail: String
    private lateinit var mMemberList: ArrayList<MemberClass>
    private lateinit var recyclerView: RecyclerView
    lateinit var groupNameText : MSPTextViewBold
    lateinit var groupAdminText : MSPTextViewBold

    lateinit var mResultTestName : String

    private val getMonthDataResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                intent.putExtra(Constants.MEMBER_DELETE_SUCCESS, "")

                val sharedPreferences = getSharedPreferences(
                    Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
                )
                val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
                getAllMemberDetailForGroup(mGroupName, getAdminEmailId)
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_member_account)

        if (intent.hasExtra(Constants.GROUP_NAME)) {
            mGroupName = intent.getStringExtra(Constants.GROUP_NAME)!!
        }
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()

        recyclerView = findViewById(R.id.rcv_view_member_account_detail)
        groupNameText = findViewById(R.id.tv_group_name_member)
        groupAdminText = findViewById(R.id.tv_group_admin_name_member_list)

        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )

        val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
        mAdminEmail = getAdminEmailId!!
        FireStoreClass().getGroupAdmin(this,mAdminEmail)
        getAllMemberDetailForGroup(mGroupName, getAdminEmailId)
    }

    private fun getAllMemberDetailForGroup(groupName: String, adminEmailId: String?) {
        showProgressDialog()
        FireStoreClass().getMemberList(this, groupName, adminEmailId!!)
    }

    fun successAdminList(groupList: ArrayList<UserClass>) {
        groupNameText.text = mGroupName
        for(i in groupList){
            val adminName = i.firstName + " " + i.lastName
            groupAdminText.text = adminName
        }
    }

    fun successMemberListFromFirestore(memberList: ArrayList<MemberClass>){
        mMemberList = memberList

        cancelProgressDialog()
        if(mMemberList.size>0){
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            mMemberList.sortBy { it.memberName
            }
            val viewMemberListAdapter = MemberListAdapter(
                this,mMemberList,this
            )
            recyclerView.adapter = viewMemberListAdapter
        }
    }

    override fun onBackPressed() {
        val i = Intent(this,PreViewMemberActivity::class.java)
        startActivity(i)
    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_view_member)
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