package com.android.trustmanagementapp.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MemberClass
import com.android.trustmanagementapp.model.UserClass
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.MSPButton
import com.android.trustmanagementapp.utils.MSPEditText
import com.android.trustmanagementapp.utils.MSPTextViewBold
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : BaseActivity() {
    private lateinit var registrationLink: MSPTextViewBold
    lateinit var etEmail: MSPEditText
    private lateinit var llEmailScreen: LinearLayoutCompat
    lateinit var llPasswordScreen: LinearLayoutCompat
    lateinit var llCodeScreen: LinearLayoutCompat
    lateinit var btnPasswordLogin: MSPButton
    lateinit var btnCodeLogin: MSPButton
    lateinit var btnLogin: MSPButton
    private lateinit var etPasswordText: MSPEditText
    private lateinit var etCodeLogin: MSPEditText
    private lateinit var forgotPassword: MSPTextViewBold

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.decorView.windowInsetsController!!.hide(
                android.view.WindowInsets.Type.statusBars()
            )
        }*/
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        llEmailScreen = findViewById(R.id.ll_email_screen)
        llPasswordScreen = findViewById(R.id.ll_password_screen)
        llCodeScreen = findViewById(R.id.ll_code_screen)
        registrationLink = findViewById(R.id.reg_text_link)
        btnPasswordLogin = findViewById(R.id.btn_password_login)
        btnCodeLogin = findViewById(R.id.btn_code_login)
        btnLogin = findViewById(R.id.btn_login)
        etPasswordText = findViewById(R.id.et_password_log)
        etCodeLogin = findViewById(R.id.et_code_log)
        forgotPassword = findViewById(R.id.forgot_password_link)

        forgotPassword.setOnClickListener {
            val intent = Intent(this, ForgotPasswordAdminActivity::class.java)
            startActivity(intent)
        }

        registrationLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnLogin.setOnClickListener {
            loginRegisterUser()
        }

    }

    private fun loginRegisterUser() {
        if (validateEmailPage()) {
            showProgressDialog()
            val email = etEmail.text.toString().trim { it <= ' ' }
            FireStoreClass().validateUserAdminORNonAdmin(this, email)

        }
    }

    private fun validateEmailPage(): Boolean {
        etEmail = findViewById(R.id.et_email_log)
        return when {
            TextUtils.isEmpty(etEmail.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            else -> {
                true
            }
        }
    }

    @SuppressLint("CommitPrefEdits")
    fun userValueReturn(adminUser: Boolean) {

        if (adminUser) {
            setUpSupportActionBar()
            llEmailScreen.visibility = View.GONE
            llPasswordScreen.visibility = View.VISIBLE
            btnPasswordLogin.visibility = View.VISIBLE
            btnLogin.visibility = View.GONE
            forgotPassword.visibility = View.GONE

            val email = etEmail.text.toString().trim { it <= ' ' }
            btnPasswordLogin.setOnClickListener {
                val password = etPasswordText.text.toString().trim { it <= ' ' }
                if (password.isNotEmpty()) {
                    showProgressDialog()
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            cancelProgressDialog()
                            if (task.isSuccessful) {
                                Toast.makeText(
                                    this, "Admin user login Success",
                                    Toast.LENGTH_LONG
                                ).show()

                                val sharedPreferences = getSharedPreferences(
                                    Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
                                )
                                val editor: SharedPreferences.Editor = sharedPreferences.edit()
                                editor.putString(
                                    Constants.STORE_EMAIL_ID, email
                                )
                                editor.apply()
                                val intent = Intent(this, AdminScreenActivity::class.java)
                                startActivity(intent)
                            }
                        }
                        .addOnFailureListener { e ->
                            cancelProgressDialog()
                            Toast.makeText(
                                this, e.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                            Log.e("Exception Occur login", e.message.toString())
                        }
                }
            }
        } else {
            setUpSupportActionBar()
            llEmailScreen.visibility = View.GONE
            llCodeScreen.visibility = View.VISIBLE
            btnLogin.visibility = View.GONE
            btnPasswordLogin.visibility = View.GONE
            btnCodeLogin.visibility = View.VISIBLE
            forgotPassword.visibility = View.GONE

            val email = etEmail.text.toString().trim { it <= ' ' }
            btnCodeLogin.setOnClickListener {
                if (etCodeLogin.text.toString().isNotEmpty()) {
                    showProgressDialog()
                    FireStoreClass().codeValidateUserDetail(
                        this,
                        etCodeLogin.text.toString(),
                        email
                    )
                }
            }
        }
    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.tool_bar_login)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbar.setNavigationOnClickListener {
            llEmailScreen.visibility = View.VISIBLE
            llPasswordScreen.visibility = View.GONE
            llCodeScreen.visibility = View.GONE
            btnLogin.visibility = View.VISIBLE
            btnPasswordLogin.visibility = View.GONE
            btnCodeLogin.visibility = View.GONE
            forgotPassword.visibility = View.VISIBLE
        }
    }

    fun successMemberLogin(userEmailList: ArrayList<UserClass>, email: String) {

        for (i in userEmailList) {
            FireStoreClass().checkCurrentEmailMemberAvailableFirestore(this, email, i.email)

        }
    }

    fun failureMemberLogin() {
        cancelProgressDialog()
        Toast.makeText(
            this, "Code doesn't match.. get correct code from Admin",
            Toast.LENGTH_LONG
        ).show()
    }

    fun emailNotAvailable() {
        cancelProgressDialog()
        /*Toast.makeText(
            this, "your email is not added to group. please contact group admin",
            Toast.LENGTH_LONG
        ).show()*/
    }

    fun userAvailableInMember(memberList: ArrayList<MemberClass>) {
        cancelProgressDialog()
        for (i in memberList){
            val intent = Intent(this,MainActivity::class.java)
            intent.putExtra(Constants.MEMBER_NAME, i.memberName)
            intent.putExtra(Constants.GROUP_NAME, i.groupName)
            intent.putExtra(Constants.ADMIN_EMAIL, i.memberAdminEmail)
            intent.putExtra(Constants.PROFILE_IMAGE, i.profileImage)
            intent.putExtra(Constants.MEMBER_PHONE, i.memberPhone)
            intent.putExtra(Constants.MEMBER_EMAIL, i.memberEmail)
            startActivity(intent)

        }
    }

}