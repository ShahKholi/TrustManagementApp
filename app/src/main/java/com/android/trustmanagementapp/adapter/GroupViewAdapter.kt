package com.android.trustmanagementapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.activities.AdminScreenActivity
import com.android.trustmanagementapp.activities.CreateGroupActivity
import com.android.trustmanagementapp.activities.ViewAccountActivity
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.utils.Constants
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

            holder.itemView.findViewById<ImageView>(R.id.iv_edit_group).setOnClickListener {
                val intent = Intent(context,CreateGroupActivity::class.java)
                intent.putExtra(Constants.GROUP_NAME, model.groupName)
                intent.putExtra(Constants.FINISH, "finish")
                intent.putExtra(Constants.ADMIN_EMAIL, model.email)
                intent.putExtra(Constants.CREATED_DATE , model.groupCreatedDate)
                intent.putExtra(Constants.GROUP_IMAGE, model.groupImage)
                intent.putExtra(Constants.GROUP_PREVIOUS_BALANCE.toString(), model.groupPreviousBalance)
                context.startActivity(intent)
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(context,ViewAccountActivity::class.java)
                intent.putExtra(Constants.GROUP_NAME, model.groupName)
                context.startActivity(intent)
            }

            holder.itemView.findViewById<ImageView>(R.id.iv_delete_group).setOnClickListener {
                val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                val customerLayout: View =
                    LayoutInflater.from(context)
                        .inflate(R.layout.custom_dilog_box_group_delete, null)
                builder.setView(customerLayout)
                builder.setTitle("DELETE")
                when(activity){
                    is AdminScreenActivity -> {
                        builder.setPositiveButton("YES") {dialogInterface, _ ->
                            activity.showProgressDialog()
                            FireStoreClass().deleteCurrentGroup(context, model.groupName,model.email)
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
        return groupList.size
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
}