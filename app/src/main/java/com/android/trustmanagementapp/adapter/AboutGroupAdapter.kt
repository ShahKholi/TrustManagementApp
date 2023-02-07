package com.android.trustmanagementapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.model.AboutGroup
import com.android.trustmanagementapp.model.MonthExpense
import com.android.trustmanagementapp.utils.MSPTextViewBold
import android.view.MotionEvent
import android.view.View.OnTouchListener
import android.widget.ImageView
import com.android.trustmanagementapp.activities.AdminAboutGroupActivity
import com.android.trustmanagementapp.firestore.FireStoreClass


class AboutGroupAdapter(
    private val context: Context,
    private val aboutGroupList: ArrayList<AboutGroup>,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_about_group_rc_layout,
                parent,
                false
            )
        )
    }

    @SuppressLint("CutPasteId")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = aboutGroupList[position]
        if (holder is MyViewHolder) {
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_group_name_about).text =
                model.groupName
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_about_detail).text =
                model.fullDetail

            holder.itemView.findViewById<ImageView>(R.id.iv_delete_about_group).setOnClickListener {
                val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                val customerLayout: View =
                    LayoutInflater.from(context)
                        .inflate(R.layout.custom_dilog_box_delete, null)
                builder.setView(customerLayout)
                builder.setTitle("DELETE")
                when(activity){
                    is AdminAboutGroupActivity -> {

                        builder.setPositiveButton("YES") { dialogInterface, _ ->
                            activity.showProgressDialog()
                            FireStoreClass().deleteAboutGroup(activity,
                            model.id)
                        }
                        builder.setNegativeButton("NO") { dialogInterface, _ ->
                            dialogInterface.dismiss()
                        }
                        val alertDialog: AlertDialog? = builder.create()
                        // Set other dialog properties
                        alertDialog!!.setCancelable(false)
                        alertDialog.show()
                    }
                }
            }

        }
    }

    override fun getItemCount(): Int {
        return aboutGroupList.size
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
}