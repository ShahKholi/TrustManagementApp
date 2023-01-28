package com.android.trustmanagementapp.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.model.MemberAccountDetail
import com.android.trustmanagementapp.utils.MSPTextViewBold

class MonthWiseDetailAdapter(
    private val context: Context,
    private val memberAccountList: ArrayList<MemberAccountDetail>,
    private val activity: Activity
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
       return MyViewHolder(
           LayoutInflater.from(context).inflate(
               R.layout.item_month_wise_detail_view_rc_layout,
               parent,
               false
           )
       )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = memberAccountList[position]

        if (holder is MyViewHolder){
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_month_wise_member_list).text =
                model.memberName
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_month_amount_detail).text =
                model.currentAmount.toString()
        }
    }

    override fun getItemCount(): Int {
        return  memberAccountList.size
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
}