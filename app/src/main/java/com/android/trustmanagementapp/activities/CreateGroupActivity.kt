package com.android.trustmanagementapp.activities

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.utils.*
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import java.io.IOException


class CreateGroupActivity : BaseActivity() {
    lateinit var originateDate: MSPTextViewBold
    lateinit var groupName: MSPEditText
    lateinit var btnCreateGroup: MSPButton
    var currentFirebaseUser = FirebaseAuth.getInstance().currentUser
    lateinit var ivGroupIcon: ImageView
    lateinit var etPreviousBalance : MSPEditText

    private var mSelectedGroupImageUri: Uri? = null
    private var mSelectedImageCloudUriString: String? = null


    private val getPhotoActionResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                onPhotoResult(result.resultCode, data)
            }
        }
    private val getGroupDataResult : ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()

        originateDate = findViewById(R.id.tv_select_group_date)
        groupName = findViewById(R.id.et_create_group_name)
        btnCreateGroup = findViewById(R.id.btn_create_group)
        ivGroupIcon = findViewById(R.id.iv_group_icon)
        etPreviousBalance = findViewById(R.id.et_group_previous_balance)

        originateDate.setOnClickListener {
            originDateSelect()
        }
        ivGroupIcon.setOnClickListener {
            permissionForPhoto()

        }
        btnCreateGroup.setOnClickListener {
            val imageStringCheck: String = mSelectedGroupImageUri.toString()
            if (imageStringCheck.isNotEmpty() && imageStringCheck != "null") {
                uploadUserImage()
            } else {
                registerGroupNoPhoto()
            }
        }
    }

    private fun uploadUserImage() {
        if (validateGroupDetails()) {
            showProgressDialog()
            FireStoreClass().uploadImageToCloudStorage(
                this, mSelectedGroupImageUri!!,
                Constants.GROUP_IMAGE
            )
        }
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.READ_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val galleryIntent = Intent(
                    Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                getPhotoActionResult.launch(galleryIntent)
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun groupRegistrationSuccess(groupName: String) {
        cancelProgressDialog()
        Toast.makeText(
            this,
            "your group $groupName created",
            Toast.LENGTH_LONG
        ).show()
        finish()
        val intent = Intent(this,AdminScreenActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        getGroupDataResult.launch(intent)
    }

    private fun onPhotoResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    // The uri of selected image from phone storage.
                    mSelectedGroupImageUri = data.data!!
                    GlideLoaderClass(this).loadGroupIcon(mSelectedGroupImageUri!!, ivGroupIcon)
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@CreateGroupActivity,
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

    private fun registerGroupNoPhoto() {
        if (validateGroupDetails()) {
            showProgressDialog()

            val sharedPreferences = getSharedPreferences(
                Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
            )
            val getEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
            val mGroupName: String = groupName.text.toString().trim { it <= ' ' }
            val mPreviousBalance : String = etPreviousBalance.text.toString().trim { it <= ' ' }
            val mSelectedDate: String = originateDate.text.toString().trim { it <= ' ' }
            val createGroup = GroupNameClass(
                mGroupName,
                mPreviousBalance.toInt(),
                mSelectedDate,
                getEmailId!!,
                "",
                currentFirebaseUser!!.uid,
            )
            FireStoreClass().createGroup(this, createGroup)
        }
    }

    fun successGroupIconUpload(imageUrl: Uri) {
        mSelectedImageCloudUriString = imageUrl.toString()
        cancelProgressDialog()

        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
        val mGroupName: String = groupName.text.toString().trim { it <= ' ' }
        val mSelectedDate: String = originateDate.text.toString().trim { it <= ' ' }
        val mPreviousBalance : String = etPreviousBalance.text.toString().trim { it <= ' ' }
        val createGroup = GroupNameClass(
            mGroupName,
            mPreviousBalance.toInt(),
            mSelectedDate,
            getEmailId!!,
            imageUrl.toString(),
            currentFirebaseUser!!.uid,
        )
        FireStoreClass().createGroup(this, createGroup)

    }

    private fun validateGroupDetails(): Boolean {
        return when {
            TextUtils.isEmpty(groupName.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(resources.getString(R.string.err_msg_enter_group_name), true)
                false
            }

            TextUtils.isEmpty(etPreviousBalance.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(resources.getString(R.string.err_msg_previous_balance), true)
                false
            }

            TextUtils.isEmpty(originateDate.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(resources.getString(R.string.err_msg_select_date), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun originDateSelect() {
        val myCalender = Calendar.getInstance()
        val year = myCalender.get(Calendar.YEAR)
        val month = myCalender.get(Calendar.MONTH)
        val day = myCalender.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(
            this,
            { _, SelecteddayYear, SelecteddayMonth, SelecteddayDay ->
                val selectedDate = "$SelecteddayDay/${SelecteddayMonth + 1}/$SelecteddayYear"
                originateDate.text = selectedDate
            },
            year, month, day
        )
        dpd.datePicker.maxDate = System.currentTimeMillis() - 8640000
        dpd.show()
    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_create_group)
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

}