package com.android.trustmanagementapp.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.MemberClass
import com.android.trustmanagementapp.utils.*
import kotlinx.coroutines.launch
import java.io.IOException

class GuestProfileActivity : BaseActivity() {
    lateinit var etMemberName: MSPEditText
    lateinit var etMemberEmail: MSPEditText
    lateinit var etMemberPhone: MSPEditText
    lateinit var btnSaveMember: MSPButton


    private var mSelectedProfileImageUri: Uri? = null
    private var mSelectedImageCloudUriString: String? = null

    private lateinit var mUserAdminEmail: String

    lateinit var mUserMemberEmail: String
    lateinit var ivUserIcon: ImageView

    private lateinit var toolbarLabel: MSPTextViewBold
    lateinit var mMemberList: ArrayList<MemberClass>
    lateinit var mGroupName: String

    lateinit var consMemberProfile : String
    lateinit var consMemberPhone : String
    lateinit var consUpdatedMemberEmail : String
    lateinit var consAdminEmail : String

    private lateinit var consUserAdminUID: String

    private val getPhotoActionResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                onPhotoResult(result.resultCode, data)
            }
        }

    private val getDataResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                finish()
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_guest_profile)

        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()

        if (intent.hasExtra(Constants.GROUP_NAME)) {
            mGroupName = intent.getStringExtra(Constants.GROUP_NAME)!!
        }

        if(intent.hasExtra(Constants.STORE_MEMBER_EMAIL_ID)){
            mUserMemberEmail = intent.getStringExtra(Constants.STORE_MEMBER_EMAIL_ID)!!
        }
        Log.e("mUserMemberEmail ch", mUserMemberEmail)
        getCurrentMemberDetail(mUserMemberEmail, mGroupName)


        etMemberName = findViewById(R.id.et_member_name_add_mem)
        etMemberEmail = findViewById(R.id.et_member_email_add_mem)
        etMemberPhone = findViewById(R.id.et_member_phone_add_mem)
        btnSaveMember = findViewById(R.id.btn_add_mem)
        toolbarLabel = findViewById(R.id.edit_toolbar_member)
        ivUserIcon = findViewById(R.id.iv_user_profile_icon)

        ivUserIcon.setOnClickListener {
            permissionForPhoto()
        }

        btnSaveMember.setOnClickListener {
            lifecycleScope.launch {
                updateUserProfile()
            }

        }

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun updateUserProfile() {

        val imageStringCheck: String = mSelectedProfileImageUri.toString()

        if (imageStringCheck.isNotEmpty() && imageStringCheck != "null") {
            uploadUserImage()
        } else {
            if(intent.hasExtra(Constants.STORE_MEMBER_PROFILE_ID)){
                val profileImage = intent.getStringExtra(Constants.STORE_MEMBER_PROFILE_ID)
                updateMember(profileImage)
            }

        }
    }

    private suspend fun updateMember(profileImage: String?) {
        if (validateRegisterMemberField()) {
            showProgressDialog()
            val memberName = etMemberName.text.toString().trim { it <= ' ' }
            val memberEmail = etMemberEmail.text.toString().trim { it <= ' ' }
            val memberPhone = etMemberPhone.text.toString().trim { it <= ' ' }
            val memberClass = MemberClass(
                mGroupName,
                memberName,
                memberEmail,
                memberPhone,
                consUserAdminUID,
                mUserAdminEmail,
                profileImage!!
            )
            FireStoreClass().updateMemberProfile(
                this, memberClass, mGroupName, mUserAdminEmail, mUserMemberEmail
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadUserImage() {
        showProgressDialog()
        if (validateRegisterMemberField()) {
            FireStoreClass().uploadImageToCloudStorage(
                this, mSelectedProfileImageUri!!,
                Constants.USER_IMAGE, ""
            )
        }


    }

    private fun validateRegisterMemberField(): Boolean {
        return when {
            TextUtils.isEmpty(etMemberName.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(resources.getString(R.string.err_msg_enter_member_name), true)
                false
            }
            TextUtils.isEmpty(etMemberEmail.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(resources.getString(R.string.err_msg_enter_email), true)
                false
            }
            TextUtils.isEmpty(etMemberPhone.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(resources.getString(R.string.err_msg_mobile_not_valid), true)
                false
            }

            else -> {
                true
            }
        }
    }

    private fun getCurrentMemberDetail(memberEmail: String?, mCurrentGroupName: String) {
        showProgressDialog()
        FireStoreClass().getCurrentGuestMemberDetailFromFirestore(
            this,
            memberEmail,
            mCurrentGroupName
        )
    }

    private fun permissionForPhoto() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            val galleryIntent = Intent(
                Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            getPhotoActionResult.launch(galleryIntent)
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                Constants.READ_STORAGE_PERMISSION_CODE
            )
        }
    }

    private fun onPhotoResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    // The uri of selected image from phone storage.
                    mSelectedProfileImageUri = data.data!!
                    GlideLoaderClass(this).loadGroupIcon(mSelectedProfileImageUri!!, ivUserIcon)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@GuestProfileActivity,
                        "Image selection failed",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            Log.e("Request Cancelled", "Image selection cancelled")
        }
    }

    fun successGettingMemberList(memberList: ArrayList<MemberClass>) {
        cancelProgressDialog()
        for (i in memberList) {
            etMemberName.setText(i.memberName)
            etMemberEmail.setText(i.memberEmail)
            etMemberPhone.setText(i.memberPhone)
            GlideLoaderClass(this).loadGuestProfilePictures(
                i.profileImage, ivUserIcon
            )
            mUserAdminEmail = i.memberAdminEmail
            consUserAdminUID = i.memberAdminUID
            consUpdatedMemberEmail = i.memberEmail

        }
    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_add_member)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    suspend fun successGroupIconUpload(imageUrl: Uri?) {
        mSelectedImageCloudUriString = imageUrl.toString()

        val memberName = etMemberName.text.toString().trim { it <= ' ' }
        val memberEmail = etMemberEmail.text.toString().trim { it <= ' ' }
        val memberPhone = etMemberPhone.text.toString().trim { it <= ' ' }

        val memberClass = MemberClass(
            mGroupName,
            memberName,
            memberEmail,
            memberPhone,
            consUserAdminUID,
            mUserAdminEmail,
            imageUrl.toString()
        )

        FireStoreClass().updateMemberProfile(
            this, memberClass, mGroupName, mUserAdminEmail, mUserMemberEmail
        )

    }

    suspend fun successUpdatedMemberList(
        memberList: ArrayList<MemberClass>,
        memberEmail: String,
        mUserAdminEmail: String,
        mUserGroupName: String
    ) {

        mMemberList = memberList
        cancelProgressDialog()

        for(i in mMemberList){
            consUpdatedMemberEmail = i.memberEmail
            consMemberPhone = i.memberPhone
            consMemberProfile = i.profileImage
            consAdminEmail = i.memberAdminEmail

            Log.e("before act ", consUpdatedMemberEmail)
        }

        val haspMap: HashMap<String, String> = HashMap()
        haspMap[Constants.MEMBER_EMAIL] = etMemberEmail.text.toString()
        val checkMasterAccountAvailable: ArrayList<String> =
            FireStoreClass().getMemberAccountFromFirestore(
                mUserAdminEmail,
                mUserGroupName,
                mUserMemberEmail
            )
        if (checkMasterAccountAvailable.size > 0){
            FireStoreClass().updateMemberAccount(
                this,
                memberEmail,
                mUserAdminEmail,
                mUserGroupName,
                haspMap
            )
        }else{

            val updatedList: ArrayList<MemberClass>
            val memberEmails = etMemberEmail.text.toString().trim { it <= ' ' }
            updatedList = FireStoreClass().getCurrentUpdatedMemberDetail(memberEmails, mGroupName)
            for(i in updatedList){
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra(Constants.PROFILE_IMAGE, consMemberProfile)
                intent.putExtra(Constants.MEMBER_PHONE, consMemberPhone)
                intent.putExtra(Constants.ADMIN_EMAIL, consAdminEmail)
                intent.putExtra(Constants.MEMBER_EMAIL, consUpdatedMemberEmail)
                intent.putExtra(Constants.GROUP_NAME, mGroupName)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                getDataResult.launch(intent)
                finish()
            }

        }

    }

    suspend fun successUpdateMemberAccount() {

        val updatedList: ArrayList<MemberClass>
        val memberEmail = etMemberEmail.text.toString().trim { it <= ' ' }
        updatedList = FireStoreClass().getCurrentUpdatedMemberDetail(memberEmail, mGroupName)
        Log.e("size 1", updatedList.toString())

        for(i in updatedList){
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(Constants.PROFILE_IMAGE, i.profileImage)
            intent.putExtra(Constants.MEMBER_PHONE, i.memberAdminEmail)
            intent.putExtra(Constants.ADMIN_EMAIL, i.memberAdminEmail)
            intent.putExtra(Constants.MEMBER_EMAIL, i.memberEmail)
            intent.putExtra(Constants.GROUP_NAME, i.groupName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            getDataResult.launch(intent)
            finish()
        }

    }


}