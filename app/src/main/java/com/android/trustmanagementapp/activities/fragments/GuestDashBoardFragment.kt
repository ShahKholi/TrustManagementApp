package com.android.trustmanagementapp.activities.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.trustmanagementapp.R

import com.android.trustmanagementapp.utils.Constants

import android.widget.ImageView

import androidx.lifecycle.lifecycleScope
import com.android.trustmanagementapp.activities.GuestAllMemberListDetailActivity
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPTextViewBold
import kotlinx.coroutines.launch

import com.android.trustmanagementapp.activities.GuestMemberTransactionDetailActivity
import com.android.trustmanagementapp.activities.GuestViewAccountActivity
import com.android.trustmanagementapp.model.MemberAccountDetail


class GuestDashBoardFragment : BlankFragment(), View.OnClickListener {

    private lateinit var mGroupName: String
    private lateinit var mAdmin: String
    private lateinit var mMemberEmail: String
    private lateinit var mMemberPhone: String
    private lateinit var mMemberProfileImage: String
    private lateinit var ivProfileIcon: ImageView

    private lateinit var mYourTransactionDetail: MSPTextViewBold
    private lateinit var mGroupTransactionDetail : MSPTextViewBold
    private lateinit var mAllGroupMemberTransactionList : MSPTextViewBold

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view: View = inflater.inflate(
            R.layout.fragment_guest_dash_board,
            container,
            false
        )

        val sharedPreferencesAdminEmail = requireActivity().getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getAdminEmailId = sharedPreferencesAdminEmail.getString(Constants.STORE_EMAIL_ID, "")
        mAdmin = getAdminEmailId!!

        val sharedPreferencesGroupName = requireActivity().getSharedPreferences(
            Constants.STORE_GROUP_NAME_ID, Context.MODE_PRIVATE
        )
        val getGroupEmailId =
            sharedPreferencesGroupName.getString(Constants.STORE_GROUP_NAME_ID, "")
        mGroupName = getGroupEmailId!!

        val sharedPreferencesMemberEmail = requireActivity().getSharedPreferences(
            Constants.STORE_MEMBER_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getMemberEmailId =
            sharedPreferencesMemberEmail.getString(Constants.STORE_MEMBER_EMAIL_ID, "")
        mMemberEmail = getMemberEmailId!!

        val sharedPreferencesMemberPhone = requireActivity().getSharedPreferences(
            Constants.STORE_MEMBER_PHONE_ID, Context.MODE_PRIVATE
        )
        val getMemberPhoneId =
            sharedPreferencesMemberPhone.getString(Constants.STORE_MEMBER_PHONE_ID, "")
        mMemberPhone = getMemberPhoneId!!

        val sharedPreferencesMemberProfileImage = requireActivity().getSharedPreferences(
            Constants.STORE_MEMBER_PROFILE_ID, Context.MODE_PRIVATE
        )
        val getMemberProfileId =
            sharedPreferencesMemberProfileImage.getString(Constants.STORE_MEMBER_PROFILE_ID, "")
        mMemberProfileImage = getMemberProfileId!!

        lifecycleScope.launch {
            showProgressDialog()
            getGroupImage()
        }

        mYourTransactionDetail = view.findViewById(R.id.tv_fragment_transaction)
        mYourTransactionDetail.setOnClickListener(this)

        mGroupTransactionDetail = view.findViewById(R.id.tv_fragment_group_accounts)
        mGroupTransactionDetail.setOnClickListener(this)

        mAllGroupMemberTransactionList = view.findViewById(R.id.tv_fragment_member_accounts)
        mAllGroupMemberTransactionList.setOnClickListener(this)

        return view
    }


    private suspend fun getGroupImage() {
        val imageString = FireStoreClass().getGroupImageFromFirestore(mGroupName, mAdmin)
        cancelProgressDialog()
        ivProfileIcon = requireActivity().findViewById(R.id.iv_fragment_group_image)
        GlideLoaderClass(requireContext()).loadGroupPictures(
            imageString,
            ivProfileIcon
        )

    }

    override fun onClick(id: View?) {
        when (id?.id) {
            R.id.tv_fragment_transaction -> {
                /* //val fragment = SettingFragment()
                 val fr: FragmentTransaction = requireActivity().supportFragmentManager.beginTransaction()
                 fr.replace(R.id.guest_member_transaction_layout, )
                 fr.commit()*/
                /*val intent =
                    Intent(this.requireContext(), GuestMemberTransactionDetailActivity::class.java)
                startActivity(intent)*/
                getCurrentMemberTransaction()
            }
            R.id.tv_fragment_group_accounts -> {
                getAllMemberAccountList()
            }
            R.id.tv_fragment_member_accounts-> {
                getAllTransactionAccountList()
            }
            else -> {
            }
        }
    }

    private fun getAllTransactionAccountList() {
        val intent =
            Intent(this.requireContext(), GuestAllMemberListDetailActivity::class.java)
        intent.putExtra(Constants.GROUP_NAME, mGroupName)
        intent.putExtra(Constants.ADMIN_EMAIL, mAdmin)
        startActivity(intent)
    }

    private fun getAllMemberAccountList() {
        val intent =
            Intent(this.requireContext(), GuestViewAccountActivity::class.java)
        intent.putExtra(Constants.GROUP_NAME, mGroupName)
        intent.putExtra(Constants.ADMIN_EMAIL, mAdmin)
        startActivity(intent)
    }

    private fun getCurrentMemberTransaction() {

        val intent =
            Intent(this.requireContext(), GuestMemberTransactionDetailActivity::class.java)
        intent.putExtra(Constants.GROUP_NAME, mGroupName)
        intent.putExtra(Constants.MEMBER_EMAIL, mMemberEmail)
        intent.putExtra(Constants.ADMIN_EMAIL, mAdmin)
        intent.putExtra(Constants.PROFILE_IMAGE,mMemberProfileImage)
        intent.putExtra(Constants.MEMBER_PHONE, mMemberPhone)
        startActivity(intent)

    }



}