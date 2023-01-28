package com.android.trustmanagementapp.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.activities.AddTimelineActivity
import com.android.trustmanagementapp.activities.ViewTimeLineActivity
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MonthExpense
import com.android.trustmanagementapp.model.Timeline
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPTextViewBold
import kotlinx.coroutines.launch

class AllTimelineAdapter(
    private val context: Context,
    private val timelineList: ArrayList<Timeline>,
    private val activity: Activity

) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_timeline_all_view_rc_layout,
                parent,
                false
            )
        )
    }

    @SuppressLint("InflateParams")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = timelineList[position]
        val liked : Boolean
        if (holder is MyViewHolder) {


            GlideLoaderClass(context).loadGroupPictures(
                model.groupImage,
                holder.itemView.findViewById(R.id.iv_timeline_group_image_view)
            )
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_timeline_group_name_view).text =
                model.groupName
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_timeline_date_time_view).text =
                model.dateTime
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_timeline_group_detail_view).text =
                model.detail
            GlideLoaderClass(context).loadGroupPictures(
                model.timeLineImage,
                holder.itemView.findViewById(R.id.iv_timeline_detail_image_view)
            )
            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_like_count_view).text =
                model.like.toString()

            holder.itemView.findViewById<ImageView>(R.id.iv_like_image_view)
                .setOnClickListener {
                    when (activity) {
                        is ViewTimeLineActivity -> {
                            activity.lifecycleScope.launch {
                                val getLikeCount: Int = FireStoreClass().getLikeCount(model.id)

                                val finalCount = getLikeCount + 1
                                holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_like_count_view).text =
                                    finalCount.toString()
                            }

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