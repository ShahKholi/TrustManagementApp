package com.android.trustmanagementapp.activities

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.trustmanagementapp.R
import com.google.android.material.snackbar.Snackbar
import java.util.*

open class BaseActivity : AppCompatActivity() {
    lateinit var mProgressDialog : Dialog
    private var doubleBackToExitPressedOnce = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_base)
    }

    fun showSnackBarMessage(message : String, errorMessage : Boolean){
        val snackBar = Snackbar.make(findViewById(android.R.id.content),message,
            Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view

        /*val params = snackBarView.layoutParams as FrameLayout.LayoutParams
        params.gravity = Gravity.TOP
        snackBarView.setBackgroundColor(
            ContextCompat.getColor(this,
            R.color.snack_bar_error))*/

        if(errorMessage){
            snackBarView.setBackgroundColor(
                ContextCompat.getColor(this,
                    R.color.snack_bar_error))
        }else{
            snackBarView.setBackgroundColor(
                ContextCompat.getColor(this,
                    R.color.snack_bar_success))
        }
        snackBar.show()
    }

    fun currentYear(): Int {
        val calendar: Calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }

    fun dateList(): ArrayList<String> {
        val result = ArrayList<String>()
        result.add("January")
        result.add("February")
        result.add("March")
        result.add("April")
        result.add("May")
        result.add("June")
        result.add("July")
        result.add("August")
        result.add("September")
        result.add("October")
        result.add("November")
        result.add("December")
        return result

    }

    fun showProgressDialog(){
        mProgressDialog = Dialog(this)
        mProgressDialog.setContentView(R.layout.dialog_progress)
        mProgressDialog.setCancelable(false)
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()
    }
    fun cancelProgressDialog(){
        mProgressDialog.dismiss()
    }

    fun doubleBackToExit() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true

        Toast.makeText(
            this,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()

        @Suppress("DEPRECATION")
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
}