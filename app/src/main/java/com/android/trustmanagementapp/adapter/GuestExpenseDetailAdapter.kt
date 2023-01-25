package com.android.trustmanagementapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.activities.AddExpenseActivity
import com.android.trustmanagementapp.firestore.FireStoreClass

import com.android.trustmanagementapp.model.MonthExpense
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPEditText
import com.android.trustmanagementapp.utils.MSPTextViewBold

class GuestExpenseDetailAdapter(
    private val context: Context,
    private val expenseList: ArrayList<MonthExpense>,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_guest_expense_view_rc_layout,
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n", "CutPasteId")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = expenseList[position]
        if (holder is MyViewHolder) {

            GlideLoaderClass(context).loadGroupPictures(
                model.groupImage,
                holder.itemView.findViewById(R.id.cv_trust_image_exp)
            )

            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_account_group_name_exp).text =
                model.groupName
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_month_name_exp).text =
                "${model.month} Expense"
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_month_amount_exp).text =
                model.expenseAmount.toString()


            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_expense_detail)
                .setOnClickListener {
                    val llLayout: LinearLayout =
                        holder.itemView.findViewById(R.id.ll_expense_detail)

                    if (!llLayout.isVisible) {
                        llLayout.visibility = View.VISIBLE
                        holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_brief_expense).text =
                            model.detail

                    } else {
                        llLayout.visibility = View.GONE
                    }

                }
        }
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
}