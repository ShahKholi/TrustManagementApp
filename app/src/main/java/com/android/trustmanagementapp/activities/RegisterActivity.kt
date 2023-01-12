package com.android.trustmanagementapp.activities

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MasterAccountDetail
import com.android.trustmanagementapp.model.UserClass
import com.android.trustmanagementapp.utils.MSPButton
import com.android.trustmanagementapp.utils.MSPEditText
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class RegisterActivity : BaseActivity() {
    lateinit var registerScreenLayout: LinearLayoutCompat
    private lateinit var btnRegister: MSPButton
    lateinit var mFirebaseAuth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
             window.decorView.windowInsetsController!!.hide(
                 android.view.WindowInsets.Type.statusBars()
             )
         }*/
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )

        setUpSupportActionBar()
        mFirebaseAuth = FirebaseAuth.getInstance()
        registerScreenLayout = findViewById(R.id.ll_register_screen)
        btnRegister = findViewById(R.id.btn_reg)


        btnRegister.setOnClickListener {
            registerUser()
        }

    }

    private fun validateRegisterDetails(): Boolean {
        val etFirstName: MSPEditText = findViewById(R.id.et_first_name_reg)
        val etLastName: MSPEditText = findViewById(R.id.et_last_name_reg)
        val etEmail: MSPEditText = findViewById(R.id.et_email_reg)
        val etPassword: MSPEditText = findViewById(R.id.et_password_reg)
        val etConfirmPassword: MSPEditText = findViewById(R.id.et_confirm_password_reg)
        val termAndCondition: AppCompatCheckBox = findViewById(R.id.cb_terms_and_condition)
        val etMobile: MSPEditText = findViewById(R.id.et_mob_reg)
        val etCode: MSPEditText = findViewById(R.id.et_code_reg)


        return when {
            TextUtils.isEmpty(etFirstName.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(resources.getString(R.string.err_msg_enter_first_name), true)
                false
            }

            TextUtils.isEmpty(etLastName.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(resources.getString(R.string.err_msg_enter_last_name), true)
                false
            }
            TextUtils.isEmpty(etEmail.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(resources.getString(R.string.err_msg_enter_email), true)
                false
            }

            TextUtils.isEmpty(etMobile.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(resources.getString(R.string.err_msg_mobile_not_valid), true)
                false
            }

            TextUtils.isEmpty(etCode.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(resources.getString(R.string.code_error), true)
                false
            }

            TextUtils.isEmpty(etPassword.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(resources.getString(R.string.err_msg_enter_password), true)
                false
            }

            TextUtils.isEmpty(etConfirmPassword.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(
                    resources.getString(R.string.err_msg_enter_confirm_password),
                    true
                )
                false
            }

            etPassword.text.toString().trim { it <= ' ' } != etConfirmPassword.text.toString()
                .trim { it <= ' ' } -> {
                showSnackBarMessage(
                    resources.getString(R.string.err_msg_password_and_confirm_password_mismatch),
                    true
                )
                false
            }
            !termAndCondition.isChecked -> {
                showSnackBarMessage(
                    resources.getString(R.string.err_msg_agree_terms_and_condition),
                    true
                )
                false
            }

            else -> {
                true
            }
        }
    }

    private fun registerUser() {

        // Check with validate function if the entries are valid or not.
        if (validateRegisterDetails()) {
            showProgressDialog()

            val mEmail: MSPEditText = findViewById(R.id.et_email_reg)
            val mFirstName: MSPEditText = findViewById(R.id.et_first_name_reg)
            val mLastName: MSPEditText = findViewById(R.id.et_last_name_reg)
            val passwordString: MSPEditText = findViewById(R.id.et_password_reg)
            val mMobileNum: MSPEditText = findViewById(R.id.et_mob_reg)
            val etCode: MSPEditText = findViewById(R.id.et_code_reg)

            val email: String = mEmail.text.toString().trim { it <= ' ' }
            val password: String = passwordString.text.toString().trim { it <= ' ' }
            val firstName: String = mFirstName.text.toString().trim { it <= ' ' }
            val lastName: String = mLastName.text.toString().trim { it <= ' ' }
            val mobile: String = mMobileNum.text.toString().trim { it <= ' ' }
            val code: String = etCode.text.toString().trim { it <= ' ' }

            lifecycleScope.launch {
                val masterAccountList: ArrayList<UserClass> =
                    FireStoreClass().validateCode(this@RegisterActivity, code)
                Log.e("check code size", masterAccountList.size.toString())
                if (masterAccountList.size > 0) {
                    cancelProgressDialog()
                    showSnackBarMessage(
                        "Given code is already available in different code." +
                                " Please provide unique code",
                        false
                    )
                }else{
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->

                            // If the registration is successfully done
                            if (task.isSuccessful) {

                                // Firebase registered user
                                val firebaseUser: FirebaseUser = task.result!!.user!!

                                val user = UserClass(
                                    firebaseUser.uid,
                                    firstName,
                                    lastName,
                                    email,
                                    mobile,
                                    code
                                )
                                FireStoreClass().registerUser(this@RegisterActivity, user)

                                //  FirebaseAuth.getInstance().signOut()

                            } else {
                                // If the registering is not successful then show error message.
                                cancelProgressDialog()
                                showSnackBarMessage(task.exception!!.message.toString(), true)
                                Log.e("check error msg ", task.exception.toString())
                            }
                        }.addOnFailureListener { e ->
                            Log.e("check error msg ", e.message.toString())
                            Toast.makeText(
                                this@RegisterActivity,
                                "Error of register Activity",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
            }
            // Create an instance and create a register a user with email and password.

        }
    }

    fun userRegistrationSuccess(firstName: String) {
        cancelProgressDialog()
        Toast.makeText(
            this,
            "You are registered successfully.",
            Toast.LENGTH_LONG
        ).show()
        loginActivity()
    }

    private fun loginActivity() {
        startActivity(
            Intent(this, LoginActivity::class.java)
        )
        finish()
    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_register)
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