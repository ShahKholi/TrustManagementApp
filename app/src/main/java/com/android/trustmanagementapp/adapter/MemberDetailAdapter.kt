package com.android.trustmanagementapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi

import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.activities.MemberDetailedActivity
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MemberAccountDetail
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.MSPEditText
import com.android.trustmanagementapp.utils.MSPTextViewBold

class MemberDetailAdapter(
    private val context: Context,
    private val memberDetailAccountList: ArrayList<MemberAccountDetail>,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_member_detail_view_rc_layout,
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("SetTextI18n", "CutPasteId")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = memberDetailAccountList[position]
        if (holder is MyViewHolder) {
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_month_name_detail).text =
                model.month + " Received Amount"
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_month_amount_detail).text =
                model.currentAmount.toString()

            holder.itemView.findViewById<ImageView>(R.id.iv_delete_month_member)
                .setOnClickListener {
                    val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
                    builder.setIcon(android.R.drawable.ic_dialog_alert)
                    val customerLayout: View =
                        LayoutInflater.from(context)
                            .inflate(R.layout.custom_dilog_box_delete, null)
                    builder.setView(customerLayout)
                    builder.setTitle("DELETE")
                    when(activity){
                        is MemberDetailedActivity -> {
                            builder.setPositiveButton("YES") { dialogInterface, _ ->

                                FireStoreClass().getDeleteItemDocumentID(activity,model.month,
                                    model.groupName,model.adminEmail, model.memberEmail,model.year,model.currentAmount)

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

            holder.itemView.findViewById<ImageView>(R.id.iv_edit_amount).setOnClickListener {

                val builder = AlertDialog.Builder(
                    context,
                    R.style.AlertDialogTheme
                )
                val defaultAmount =
                    holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_month_amount_detail)
                        .text.toString()

                val customerLayout: View =
                    LayoutInflater.from(context)
                        .inflate(R.layout.custom_dilog_box_edit_text, null)
                builder.setView(customerLayout)
                builder.setTitle("Edit Amount")
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                val editText: MSPEditText =
                    customerLayout.findViewById(R.id.et_user_amount_edit)
                editText.setText(defaultAmount)

                when (activity) {
                    is MemberDetailedActivity -> {
                        builder.setPositiveButton("SAVE") { dialogInterface, _ ->

                            val amount = editText.text.toString()
                            val memberName = model.memberName
                            val adminEmail = model.adminEmail
                            val memberEmail = model.memberEmail
                            val groupName = model.groupName
                            val month = model.month
                            val totalAmount = amount.toInt()
                            val userHashMap = HashMap<String, Any>()
                            userHashMap[Constants.CURRENT_AMOUNT] = totalAmount
                            FireStoreClass().updateCurrentAmountMember(
                                activity,
                                userHashMap,
                                groupName,
                                month,
                                memberEmail,
                                memberName,
                                adminEmail
                            )
                        }
                        builder.setNegativeButton("CANCEL") { dialogInterface, _ ->
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
        return memberDetailAccountList.size

    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
}