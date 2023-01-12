package com.android.trustmanagementapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MasterAccountDetail
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPTextViewBold
import androidx.lifecycle.lifecycleScope

class ViewMasterAccountAdapter(
    private val context: Context,
    private val masterAccountList: ArrayList<MasterAccountDetail>,
    private val previousBalance : Int,
    private val activity: Activity,
    private val imageUrl : String,

) : RecyclerView.Adapter<RecyclerView.ViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_account_view_rc_layout,
                parent,
                false
            )
        )
    }

    @SuppressLint( "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        val model = masterAccountList[position]


        if (holder is MyViewHolder){
            GlideLoaderClass(context).loadGroupPictures(
                imageUrl,
                holder.itemView.findViewById(R.id.cv_trust_image)
            )
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_account_group_name).text =
                model.groupName
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_month_name).text =
                model.month + " Income"
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_month_amount).text =
                model.income.toString()
            holder.itemView.findViewById<MSPTextViewBold>(R.id.et_view_account_expense_amount).text=
                model.expenseAmount.toString()
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_view_account_previous_balance).text =
                previousBalance.toString()

        }
    }

    override fun getItemCount(): Int {
        return masterAccountList.size
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
}