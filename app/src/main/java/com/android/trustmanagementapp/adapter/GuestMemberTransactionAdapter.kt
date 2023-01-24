package com.android.trustmanagementapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.model.MemberAccountDetail
import com.android.trustmanagementapp.utils.MSPTextViewBold

class GuestMemberTransactionAdapter(
    private val context: Context,
    private val memberDetailAccountList: ArrayList<MemberAccountDetail>,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_guest_member_transaction_detail_view_rc_layout,
                parent,
                false
            )
        )

    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = memberDetailAccountList[position]
        if(holder is MyViewHolder){
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_month_name_detail).text =
                model.month + " Received Amount"
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_month_amount_detail).text =
                model.currentAmount.toString()
        }
    }

    override fun getItemCount(): Int {
        return memberDetailAccountList.size
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
}