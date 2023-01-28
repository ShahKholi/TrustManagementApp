package com.android.trustmanagementapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.activities.AddTimelineActivity
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MonthExpense
import com.android.trustmanagementapp.model.Timeline
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPTextViewBold

class TimelineAdapter(
    private val context: Context,
    private val timelineList: ArrayList<Timeline>,
    private val activity: Activity

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_timeline_your_view_rc_layout,
                parent,
                false
            )
        )
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = timelineList[position]
        if (holder is MyViewHolder) {

            GlideLoaderClass(context).loadGroupPictures(
                model.groupImage,
                holder.itemView.findViewById(R.id.iv_timeline_group_image)
            )
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_timeline_group_name).text =
                model.groupName
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_timeline_date_time).text =
                model.dateTime
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_timeline_group_detail).text =
                model.detail
            GlideLoaderClass(context).loadGroupPictures(
                model.timeLineImage,
                holder.itemView.findViewById(R.id.iv_timeline_detail_image)
            )
            holder.itemView.findViewById<ImageView>(R.id.iv_delete_timeline).setOnClickListener {
                val builder = AlertDialog.Builder(
                    context,
                    R.style.AlertDialogTheme
                )
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                val customerLayout: View =
                    LayoutInflater.from(context)
                        .inflate(R.layout.custom_dilog_box_delete, null)
                builder.setView(customerLayout)
                builder.setTitle("DELETE")
                when(activity){
                    is AddTimelineActivity -> {
                        builder.setPositiveButton("YES") { dialogInterface, _ ->
                            FireStoreClass().deleteCurrentTimeline(activity,model.id)
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
        return timelineList.size
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
}