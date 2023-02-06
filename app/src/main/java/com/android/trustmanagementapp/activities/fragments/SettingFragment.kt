package com.android.trustmanagementapp.activities.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.activities.GuestInformationActivity
import com.android.trustmanagementapp.activities.GuestProfileActivity
import com.android.trustmanagementapp.activities.GuestViewGroupExpenseActivity
import com.android.trustmanagementapp.activities.LoginActivity
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.MSPButton
import com.android.trustmanagementapp.utils.MSPTextViewBold

class SettingFragment : BlankFragment(), View.OnClickListener {
    private lateinit var mAdmin: String
    private lateinit var mGroupName: String
    private lateinit var mMemberEmail: String

    private lateinit var mYourProfileInfo: MSPTextViewBold
    private lateinit var mLogout: MSPButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(
            R.layout.fragment_setting,
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


        mYourProfileInfo = view.findViewById(R.id.tv_fragment_your_profile)
        mYourProfileInfo.setOnClickListener(this)

        mLogout = view.findViewById(R.id.logout_guest)
        mLogout.setOnClickListener(this)

        return view
    }

    override fun onClick(id: View?) {
        when (id?.id) {
            R.id.tv_fragment_your_profile -> {
                goToInformationActivity()
            }
            R.id.logout_guest-> {
                goToLoginScreen()
            }
        }
    }

    private fun goToLoginScreen() {
        val intent =
            Intent(this.requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

    }

    private fun goToInformationActivity() {
        val intent =
            Intent(this.requireContext(), GuestInformationActivity::class.java)
        intent.putExtra(Constants.GROUP_NAME, mGroupName)
        intent.putExtra(Constants.ADMIN_EMAIL, mAdmin)
        intent.putExtra(Constants.MEMBER_EMAIL, mMemberEmail)
        startActivity(intent)
    }


}