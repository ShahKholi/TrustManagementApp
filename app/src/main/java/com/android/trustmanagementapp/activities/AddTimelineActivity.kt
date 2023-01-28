package com.android.trustmanagementapp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.TimelineAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.model.MonthExpense
import com.android.trustmanagementapp.model.Timeline
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.GlideLoaderClass
import com.android.trustmanagementapp.utils.MSPButton
import com.android.trustmanagementapp.utils.MSPEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class AddTimelineActivity : BaseActivity() {
    private lateinit var autoCompleteGroupName: AutoCompleteTextView
    private lateinit var adapterGroupItems: ArrayAdapter<String>
    private lateinit var currentGroupNameSelect: String
    private lateinit var imageTimelineView: ImageView
    private lateinit var btnPost: MSPButton
    private lateinit var etTimelineDetail: MSPEditText
    var currentFirebaseUser = FirebaseAuth.getInstance().currentUser
    private lateinit var mTimelineList: ArrayList<Timeline>
    private lateinit var recyclerView: RecyclerView

    private var mSelectedGroupImageUri: Uri? = null
    private var mSelectedImageCloudUriString: String? = null

    private val getGroupDataResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                finish()
            }
        }

    private val getPhotoActionResult: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                onPhotoResult(result.resultCode, data)
            }
        }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_timeline)
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()

        autoCompleteGroupName = findViewById(R.id.auto_complete_expense_group)
        imageTimelineView = findViewById(R.id.iv_timeline_image)
        btnPost = findViewById(R.id.btn_post_timeline)
        etTimelineDetail = findViewById(R.id.et_timeline_detail)
        recyclerView = findViewById(R.id.rcv_your_timeline_detail)


        getGroupNameFromFireStore()
        loadTimeLineDetailFromFireStore()

        imageTimelineView.setOnClickListener {
            permissionForPhoto()
        }

        btnPost.setOnClickListener {
            val grpName = autoCompleteGroupName.text.toString()

            if (validateTimeLineField()) {
                val sharedPreferences = getSharedPreferences(
                    Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
                )
                val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
                val timelineDetail = etTimelineDetail.text.toString().trim { it <= ' ' }

                val imageStringCheck: String = mSelectedGroupImageUri.toString()
                if (imageStringCheck.isNotEmpty() && imageStringCheck != "null") {
                    uploadTimeLineImage()
                } else {
                    lifecycleScope.launch {
                        uploadTimeLineWithNoImage()
                    }

                }

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadTimeLineDetailFromFireStore() {
        showProgressDialog()
        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")

        FireStoreClass().loadTimelineDetail(this, getAdminEmailId!!)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun uploadTimeLineWithNoImage() {
        if (validateTimeLineField()) {
            showProgressDialog()
            val sharedPreferences = getSharedPreferences(
                Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
            )
            val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
            val groupName = autoCompleteGroupName.text.toString()
            val groupImage =
                FireStoreClass().getGroupImageFromFirestore(groupName, getAdminEmailId!!)
            val etTimelineDetail = etTimelineDetail.text.toString().trim { it <= ' ' }

            val createTimeline = Timeline(
                groupName,
                getAdminEmailId,
                etTimelineDetail,
                "",
                currentDateAndTime(),
                groupImage,
                ""
            )
            FireStoreClass().saveDocumentID(this, createTimeline)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun successGroupIconUpload(imageUrl: Uri?) {
        mSelectedImageCloudUriString = imageUrl.toString()
        cancelProgressDialog()
        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
        val groupName = autoCompleteGroupName.text.toString()
        val etTimelineDetail = etTimelineDetail.text.toString().trim { it <= ' ' }
        val groupImage = FireStoreClass().getGroupImageFromFirestore(groupName, getAdminEmailId!!)
        val createTimeline = Timeline(
            groupName,
            getAdminEmailId,
            etTimelineDetail,
            imageUrl.toString(),
            currentDateAndTime(),
            groupImage,
            ""
        )
        FireStoreClass().saveDocumentID(this, createTimeline)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun uploadTimeLineImage() {
        if (validateTimeLineField()) {
            showProgressDialog()
            FireStoreClass().uploadImageToCloudStorage(
                this, mSelectedGroupImageUri!!,
                Constants.TIMELINE_IMAGE, ""
            )
        }
    }

    private fun validateTimeLineField(): Boolean {
        return when {
            TextUtils.isEmpty(autoCompleteGroupName.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(
                    resources.getString(R.string.please_select_group_dropdown),
                    true
                )
                false
            }

            TextUtils.isEmpty(etTimelineDetail.text.toString().trim { it <= ' ' }) -> {
                showSnackBarMessage(
                    resources.getString(R.string.please_enter_detail),
                    true
                )
                false
            }

            else -> {
                true
            }
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

    private fun onPhotoResult(resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    // The uri of selected image from phone storage.
                    mSelectedGroupImageUri = data.data!!
                    GlideLoaderClass(this).loadGroupIcon(
                        mSelectedGroupImageUri!!,
                        imageTimelineView
                    )
                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this@AddTimelineActivity,
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
        val sharedPreferences = getSharedPreferences(
            Constants.STORE_EMAIL_ID, Context.MODE_PRIVATE
        )
        val getAdminEmailId = sharedPreferences.getString(Constants.STORE_EMAIL_ID, "")
        FireStoreClass().getGroupList(this, getAdminEmailId!!)
    }

    fun successGroupListFromFirestore(groupList: ArrayList<GroupNameClass>) {

        val result = ArrayList<String>()
        for (i in groupList) {
            result.add(i.groupName)
            adapterGroupItems = ArrayAdapter<String>(
                this,
                R.layout.support_simple_spinner_dropdown_item, result
            )

            // Specify the layout to use when the list of choices appear
            adapterGroupItems.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            autoCompleteGroupName.setAdapter(adapterGroupItems)
            autoCompleteGroupName.onItemClickListener =
                (AdapterView.OnItemClickListener() { adapterView: AdapterView<*>, view1: View,
                                                     position: Int, l: Long ->
                    val item = adapterView.getItemAtPosition(position).toString()
                    currentGroupNameSelect = item

                })
        }
    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_add_timeline)
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

    fun successSavedDocumentID(timeline: Timeline) {
        cancelProgressDialog()
        Toast.makeText(
            this,
            "your timeline added into ${timeline.groupName} timeline",
            Toast.LENGTH_LONG
        ).show()
        getGroupDataResult.launch(intent)
        finish()

    }
    fun deletionSuccess() {
        cancelProgressDialog()
        Toast.makeText(
            this,
            "your timeline is deleted",
            Toast.LENGTH_LONG
        ).show()
        getGroupDataResult.launch(intent)
        finish()
    }



    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SimpleDateFormat")
    fun successTimelineListFromFirestore(timelineList: ArrayList<Timeline>) {
        cancelProgressDialog()
        mTimelineList = timelineList
        if (mTimelineList.size > 0) {
            recyclerView.layoutManager = LinearLayoutManager(this)
            recyclerView.setHasFixedSize(true)
            mTimelineList.sortByDescending {
                // val sdf = SimpleDateFormat("MMMM")
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                //sdf.parse(it.dateTime)
                it.dateTime.format(formatter)
            }
            val cartListAdapter = TimelineAdapter(
                this, mTimelineList, this
            )
            recyclerView.adapter = cartListAdapter
        }
    }



}