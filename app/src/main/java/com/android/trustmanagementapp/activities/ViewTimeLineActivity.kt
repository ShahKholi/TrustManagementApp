package com.android.trustmanagementapp.activities

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.AllTimelineAdapter
import com.android.trustmanagementapp.adapter.TimelineAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.Timeline
import com.android.trustmanagementapp.utils.Constants
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class ViewTimeLineActivity : BaseActivity() {
    private lateinit var mTimelineList: ArrayList<Timeline>
    private lateinit var recyclerView: RecyclerView

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_time_line)

        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()
        recyclerView = findViewById(R.id.rcv_your_timeline_detail)

        loadTimeLineDetailFromFireStore()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadTimeLineDetailFromFireStore() {
        showProgressDialog()
        FireStoreClass().loadAllTimelineDetail(this)
    }

    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_view_timeline_view)
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

    @RequiresApi(Build.VERSION_CODES.O)
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
            val cartListAdapter = AllTimelineAdapter(
                this, mTimelineList, this
            )
            recyclerView.adapter = cartListAdapter
        }
    }
}