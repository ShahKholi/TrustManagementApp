package com.android.trustmanagementapp.activities.fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.AllTimelineAdapter
import com.android.trustmanagementapp.firestore.FireStoreClass
import com.android.trustmanagementapp.model.Timeline
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


class TimelineFragment : BlankFragment(), View.OnClickListener {
    lateinit var recyclerView: RecyclerView
    private lateinit var mTimelineList: ArrayList<Timeline>

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(
            R.layout.fragment_timeline,
            container,
            false
        )

        recyclerView = view.findViewById(R.id.rcv_timeline_fragment)

        loadTimeLineDetailFromFireStore()

        return view

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadTimeLineDetailFromFireStore() {
        showProgressDialog()
        FireStoreClass().loadAllTimelineDetailFragment(this)
    }

    override fun onClick(p0: View?) {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun successTimelineListFromFirestore(timelineList: ArrayList<Timeline>) {
        cancelProgressDialog()
        mTimelineList = timelineList
        if (mTimelineList.size > 0) {
            recyclerView.layoutManager = LinearLayoutManager(this.requireContext())
            recyclerView.setHasFixedSize(true)
            mTimelineList.sortByDescending {
                // val sdf = SimpleDateFormat("MMMM")
                val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)
                //sdf.parse(it.dateTime)
                it.dateTime.format(formatter)
            }
            val cartListAdapter = AllTimelineAdapter(
                this.requireContext(), mTimelineList, this.requireActivity()
            )
            recyclerView.adapter = cartListAdapter
        }
    }

}