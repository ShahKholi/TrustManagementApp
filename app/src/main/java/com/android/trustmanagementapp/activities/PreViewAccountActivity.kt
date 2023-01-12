package com.android.trustmanagementapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.GroupNameClass
import com.android.trustmanagementapp.utils.Constants
import com.android.trustmanagementapp.utils.MSPButton

class PreViewAccountActivity : BaseActivity() {

    private lateinit var autoCompleteGroupName: AutoCompleteTextView
    private lateinit var adapterGroupItems: ArrayAdapter<String>
    private lateinit var btnPreAccount : MSPButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pre_view_account)

        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()

        autoCompleteGroupName = findViewById(R.id.auto_complete_text_group_pre)
        btnPreAccount = findViewById(R.id.btn_pre_account)
        getGroupNameFromFireStore()

        btnPreAccount.setOnClickListener {
            val intent = Intent(this, ViewAccountActivity::class.java)
            intent.putExtra(Constants.GROUP_NAME,autoCompleteGroupName.text.toString())
            startActivity(intent)
        }
    }

    private fun getGroupNameFromFireStore() {
        FireStoreClass().getGroupList(this)
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

                })
        }
    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_pre_view_account)
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