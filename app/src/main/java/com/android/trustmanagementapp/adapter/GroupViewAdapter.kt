package com.android.trustmanagementapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPTextViewBold

class GroupViewAdapter(
    private val context: Context,
    private val groupList: ArrayList<GroupNameClass>,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_group_rc_layout,
                parent,
                false
            )
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = groupList[position]
        if (holder is MyViewHolder) {
            GlideLoaderClass(context).loadGroupPictures(
                model.groupImage,
                holder.itemView.findViewById(R.id.iv_group_item_image)
            )
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_item_group_name).text =
                "GROUP NAME : ${model.groupName}"
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_group_create_date).text =
                "GROUP ORIGINATED DATE : ${model.groupCreatedDate}"
        }
    }

    override fun getItemCount(): Int {
        return groupList.size
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
}