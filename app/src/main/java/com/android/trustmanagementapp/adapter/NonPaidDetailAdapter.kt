package com.android.trustmanagementapp.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.utils.MSPTextViewBold

class NonPaidDetailAdapter(
    private val context: Context,
    private val memberList: ArrayList<String>,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_non_paid_month_wise_detail_view_rc_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        for (memberName in memberList) {
            val model = memberList[position]
            if (holder is MyViewHolder) {
                holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_month_wise_member_list).text =
                    model
            }
        }
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
}