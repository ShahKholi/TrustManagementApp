package com.android.trustmanagementapp.activities

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.model.MemberClass
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPButton
import com.android.trustmanagementapp.utils.MSPEditText
import com.google.firebase.auth.FirebaseAuth
import java.io.IOException

class AddMemberActivity : BaseActivity() {
    lateinit var etMemberName: MSPEditText
    lateinit var etMemberEmail: MSPEditText
    lateinit var etMemberPhone: MSPEditText
    lateinit var btnSaveMember: MSPButton
    private var currentFirebaseUser = FirebaseAuth.getInstance().currentUser

    lateinit var autoComplete: AutoCompleteTextView
    lateinit var adapterItems: ArrayAdapter<String>

    lateinit var ivUserIcon: ImageView

    private var mSelectedProfileImageUri: Uri? = null
    private var mSelectedImageCloudUriString: String? = null

    private val getPhotoActionResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                onPhotoResult(result.resultCode, data)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_member)

        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()

        getGroupNameFromFireStore()

        etMemberName = findViewById(R.id.et_member_name_add_mem)
        etMemberEmail = findViewById(R.id.et_member_email_add_mem)
        etMemberPhone = findViewById(R.id.et_member_phone_add_mem)
        btnSaveMember = findViewById(R.id.btn_add_mem)
        autoComplete = findViewById(R.id.auto_complete_text)

        ivUserIcon = findViewById(R.id.iv_user_profile_icon)

        btnSaveMember.setOnClickListener {
            val imageStringCheck: String = mSelectedProfileImageUri.toString()
            if (imageStringCheck.isNotEmpty() && imageStringCheck != "null") {
                uploadUserImage()
            } else {
                registerMember()
            }
        }
        ivUserIcon.setOnClickListener {
            permissionForPhoto()
        }

    }

    private fun uploadUserImage() {
        if (validateRegisterMemberField()) {
            showProgressDialog()
            FireStoreClass().uploadImageToCloudStorage(
                this, mSelectedProfileImageUri!!,
                Constants.USER_IMAGE
            )
        }
    }

    fun successGroupIconUpload(imageUrl: Uri) {
        mSelectedImageCloudUriString = imageUrl.toString()

        val memberName = etMemberName.text.toString().trim { it <= ' ' }
        val memberEmail = etMemberEmail.text.toString().trim { it <= ' ' }
        val memberPhone = etMemberPhone.text.toString().trim { it <= ' ' }
        val groupName = autoComplete.text.toString().trim { it <= ' ' }
        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
        val createMember = MemberClass(
            groupName,
            memberName,
            memberEmail,
            memberPhone,
            currentFirebaseUser!!.uid,
            getAdminEmailId!!,
            imageUrl.toString()
        )
        FireStoreClass().createMemberToFirestore(this, createMember)
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
                        this@AddMemberActivity,
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

    private fun getGroupNameFromFireStore() {
        FireStoreClass().getGroupList(this)
    }

    private fun registerMember() {
        if (validateRegisterMemberField()) {
            showProgressDialog()
            val memberName = etMemberName.text.toString().trim { it <= ' ' }
            val memberEmail = etMemberEmail.text.toString().trim { it <= ' ' }
            val memberPhone = etMemberPhone.text.toString().trim { it <= ' ' }
            val groupName = autoComplete.text.toString().trim { it <= ' ' }
            val sharedPreferences = getSharedPreferences(
                Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
            )
            val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
            val createMember = MemberClass(
                groupName,
                memberName,
                memberEmail,
                memberPhone,
                currentFirebaseUser!!.uid,
                getAdminEmailId!!
            )
            FireStoreClass().createMemberToFirestore(this, createMember)
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
            TextUtils.isEmpty(autoComplete.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(
                    resources.getString(R.string.please_select_group_dropdown),
                    true
                )
                false
            }

            else -> {
                true
            }
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

    fun groupRegistrationSuccess(memberName: String) {
        cancelProgressDialog()
        Toast.makeText(
            this,
            "your member $memberName created in your group",
            Toast.LENGTH_LONG
        ).show()
        finish()
        val intent = Intent(this, AdminScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun successGroupListFromFirestore(groupList: ArrayList<GroupNameClass>) {
        val result = ArrayList<String>()
        for (i in groupList) {
            result.add(i.groupName)
            adapterItems = ArrayAdapter<String>(
                this,
                R.layout.support_simple_spinner_dropdown_item, result
            )

            // Specify the layout to use when the list of choices appear
            adapterItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            autoComplete.setAdapter(adapterItems)
            autoComplete.onItemClickListener =
                (AdapterView.OnItemClickListener() { adapterView: AdapterView<*>, view1: View, position: Int, l: Long ->
                    val item = adapterView.getItemAtPosition(position).toString()
                    /* Toast.makeText(this, "Item is $item", Toast.LENGTH_LONG).show()*/
                })
        }

    }

}