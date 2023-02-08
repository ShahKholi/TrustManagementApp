package com.android.trustmanagementapp.activities

import android.annotation.SuppressLint
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.SyncStateContract
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.GuestMemberTransactionAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MemberAccountDetail
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPTextViewBold
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat

class GuestMemberTransactionDetailActivity : BaseActivity() {

    private lateinit var mMemberAccountList : ArrayList<MemberAccountDetail>
    private lateinit var mGroupName: String
    private lateinit var mAdmin: String
    private lateinit var mMemberEmail: String
    private lateinit var mProfileImage : String
    private lateinit var mMemberPhone : String


    private lateinit var tvProfileImage : CircleImageView
    private lateinit var tvMemberName : MSPTextViewBold
    private lateinit var tvMemberPhone : MSPTextViewBold
    private lateinit var tvMemberEmail : MSPTextViewBold
    private lateinit var recyclerView: RecyclerView
    lateinit var mTotalAmount: MSPTextViewBold
    lateinit var llBalanceScreen: LinearLayoutCompat
    lateinit var llHeader : LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_member_transaction_detail)

        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()

        tvProfileImage = findViewById(R.id.cv_profile_member_detail)
        tvMemberPhone = findViewById(R.id.tv_contact_number_member)
        if(intent.hasExtra(Constants.GROUP_NAME)){
            mGroupName = intent.getStringExtra(Constants.GROUP_NAME)!!
        }
        if(intent.hasExtra(Constants.MEMBER_EMAIL)){
            mMemberEmail = intent.getStringExtra(Constants.MEMBER_EMAIL)!!
        }
        if(intent.hasExtra(Constants.ADMIN_EMAIL)){
            mAdmin = intent.getStringExtra(Constants.ADMIN_EMAIL)!!
        }
        if(intent.hasExtra(Constants.PROFILE_IMAGE)){
            mProfileImage = intent.getStringExtra(Constants.PROFILE_IMAGE)!!
            if(mProfileImage.isNotEmpty() && mProfileImage != "null"){
                GlideLoaderClass(this).loadGuestProfilePictures(
                    mProfileImage, tvProfileImage
                )
            }else {
                GlideLoaderClass(this).loadGuestProfilePictures(
                    R.drawable.ic_user_placeholder,tvProfileImage
                )
            }
        }
        if(intent.hasExtra(Constants.MEMBER_PHONE)){
            mMemberPhone = intent.getStringExtra(Constants.MEMBER_PHONE)!!
            tvMemberPhone.text = mMemberPhone
        }

        tvMemberName = findViewById(R.id.tv_user_name_member_detail)
        tvMemberEmail = findViewById(R.id.tv_email_member_detail)
        recyclerView = findViewById(R.id.rcv_view_account_detail_guest)
        mTotalAmount = findViewById(R.id.tv_total_amount_detail)
        llBalanceScreen = findViewById(R.id.ll_total_balance_detail)
        llHeader = findViewById(R.id.ll_header)

        getCurrentMemberTransactionList()

    }

    private fun getCurrentMemberTransactionList() {
        showProgressDialog()
        FireStoreClass().guestGetIncomeDetailFromFirestore(
          this,
          mGroupName,
          mAdmin,
          mMemberEmail,
          currentYear()
      )
    }

    @SuppressLint("SimpleDateFormat")
    fun successMemberAccount(monthList: ArrayList<MemberAccountDetail>) {
        mMemberAccountList = monthList
            cancelProgressDialog()
        val amountList: ArrayList<Int> = ArrayList()

        if(mMemberAccountList.size>0){
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            mMemberAccountList.sortBy {
                val sdf = SimpleDateFormat("MMMM")
                sdf.parse(it.month)
            }
            val guestMemberDetailAdapter = GuestMemberTransactionAdapter(
                this,mMemberAccountList,this
            )
            recyclerView.adapter = guestMemberDetailAdapter
            for (i in mMemberAccountList){
                tvMemberName.text = i.memberName
                tvMemberEmail.text = i.memberEmail
                amountList.add(i.currentAmount)
                amountList.sum()
            }
            mTotalAmount.text = amountList.sum().toString()
        }else{
            llBalanceScreen.visibility = View.GONE
            llHeader.visibility = View.GONE

            val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            builder.setIcon(android.R.drawable.ic_dialog_info)
            val customerLayout: View =
                LayoutInflater.from(this)
                    .inflate(R.layout.custom_dilog_box_no_data_found, null)
            builder.setView(customerLayout)
            builder.setTitle("INFO")
            builder.setPositiveButton("OK"){dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            val alertDialog: AlertDialog? = builder.create()
            // Set other dialog properties
            alertDialog!!.setCancelable(true)
            alertDialog.show()
        }
    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_view_member_detail)
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