package com.android.trustmanagementapp.activities.fragments

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.android.trustmanagementapp.R
import com.google.android.material.snackbar.Snackbar
import java.util.*


open class BlankFragment : Fragment() {

    private lateinit var mProgressDialog: Dialog
    private var doubleBackToExitPressedOnce = false
    fun showProgressDialog(){
        mProgressDialog = Dialog(requireActivity())
        mProgressDialog.setContentView(R.layout.dialog_progress)
        mProgressDialog.setCancelable(false)
        mProgressDialog.setCanceledOnTouchOutside(false)
        mProgressDialog.show()
    }
    fun cancelProgressDialog(){
        mProgressDialog.dismiss()
    }

    fun currentYear(): Int {
        val calendar: Calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }

   /* fun doubleBackToExit() {

        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }

        this.doubleBackToExitPressedOnce = true

        Toast.makeText(
            this@Context,
            resources.getString(R.string.please_click_back_again_to_exit),
            Toast.LENGTH_SHORT
        ).show()


        Handler(Looper.getMainLooper()).postDelayed({
            doubleBackToExitPressedOnce = false
        }, 2000)
    }*/


}