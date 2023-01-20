package com.android.trustmanagementapp.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.model.MonthExpense
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPTextViewBold

class PreExpenseImageListAdapter(
    private val context: Context,
    private val expenseList: ArrayList<MonthExpense>,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return PreExpenseImageListAdapter.MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_pre_expense_view_rc_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = expenseList[position]
        if (holder is MyViewHolder) {
            GlideLoaderClass(context).loadGroupPictures(
                model.spendImage,
                holder.itemView.findViewById(R.id.iv_pre_expense_image)
            )

            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_pre_expense_detail).text =
                model.detail

            holder.itemView.setOnClickListener {
                val llLayout: LinearLayout =
                    holder.itemView.findViewById(R.id.ll_exp_detail_text)
                if (!llLayout.isVisible) {
                    llLayout.visibility = View.VISIBLE
                    holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_pre_expense_detail).text =
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