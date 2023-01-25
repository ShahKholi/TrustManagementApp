package com.android.trustmanagementapp.adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.activities.GuestAllMemberDetailedActivity
import com.android.trustmanagementapp.activities.MemberDetailedActivity
import com.android.trustmanagementapp.model.MasterAccountDetail
import com.android.trustmanagementapp.model.MemberClass
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPTextViewBold

class GuestAllMemberListAdapter(
    private val context: Context,
    private val memberList: ArrayList<MemberClass>,
    private val activity: Activity
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context).inflate(
                R.layout.item_guest_member_list_view_rc_layout,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = memberList[position]
        if(holder is MyViewHolder){
            if(model.profileImage.isNotEmpty()&& model.profileImage != "null"){
                GlideLoaderClass(context).loadGroupPictures(
                    model.profileImage,
                    holder.itemView.findViewById(R.id.cv_profile_member_list)
                )
            }else{
                GlideLoaderClass(context).loadGroupPictures(
                    R.drawable.ic_user_placeholder,
                    holder.itemView.findViewById(R.id.cv_profile_member_list)
                )
            }

            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_user_name_member).text =
                model.memberName

            holder.itemView.findViewById<MSPTextViewBold>(R.id.tv_contact_number_member).text =
                model.memberPhone

            holder.itemView.setOnClickListener {
                val intent =Intent(context, GuestAllMemberDetailedActivity::class.java)
                intent.putExtra(Constants.GROUP_NAME, model.groupName)
                intent.putExtra(Constants.MEMBER_ADMIN_EMAIL, model.memberAdminEmail)
                intent.putExtra(Constants.MEMBER_EMAIL, model.memberEmail)
                intent.putExtra(Constants.MEMBER_NAME, model.memberName)
                intent.putExtra(Constants.MEMBER_PHONE, model.memberPhone)
                intent.putExtra(Constants.PROFILE_IMAGE, model.profileImage)
                context.startActivity(intent)
            }

        }
    }

    override fun getItemCount(): Int {
        return memberList.size
    }

    class MyViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

    }
}