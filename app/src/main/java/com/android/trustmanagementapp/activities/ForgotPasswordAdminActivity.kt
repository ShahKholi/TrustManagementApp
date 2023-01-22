package com.android.trustmanagementapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.utils.MSPButton
import com.android.trustmanagementapp.utils.MSPEditText
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordAdminActivity : BaseActivity() {

    lateinit var etPasswordText : MSPEditText
    lateinit var btnResetPassword : MSPButton
    lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password_admin)
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )

        setUpSupportActionBar()

        etPasswordText = findViewById(R.id.et_forgot_password)
        btnResetPassword = findViewById(R.id.btn_forgot_password)
        auth = FirebaseAuth.getInstance()

        btnResetPassword.setOnClickListener {
            showProgressDialog()
            val sPassword = etPasswordText.text.toString().trim { it <= ' ' }
            auth.sendPasswordResetEmail(sPassword)
                .addOnSuccessListener {
                    cancelProgressDialog()
                    Toast.makeText(this,"Password sent to email, Please check"
                    ,Toast.LENGTH_LONG).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    startActivity(intent)
                }.addOnFailureListener {
                    cancelProgressDialog()
                    Toast.makeText(this,it.toString()
                        ,Toast.LENGTH_LONG).show()
                }
        }

    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.tool_forgot_bar_login)
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