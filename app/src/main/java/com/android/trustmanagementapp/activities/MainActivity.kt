package com.android.trustmanagementapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.android.trustmanagementapp.R
import com.android.trustmanagementapp.adapter.FragmentMyAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class MainActivity : AppCompatActivity() {


    private lateinit var container : ViewPager2
    private lateinit var tabs : TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.statusBarColor = ContextCompat.getColor(
            this,
            R.color.bar_color
        )
        setUpSupportActionBar()

        container = findViewById(R.id.container)
        tabs = findViewById(R.id.tabs)

        val adapter = FragmentMyAdapter(supportFragmentManager,lifecycle)
        container.adapter = adapter

        TabLayoutMediator(tabs,container) { tab, position ->
            when(position){
                0 -> {
                   // tab.setIcon(R.drawable.camera + position)
                    tab.text = "TIMELINE"

                }
                1 -> {
                    tab.text = "YOUR DASHBOARD"

                }
                2 -> {
                    tab.text = "SETTING"

                }
            }
        }.attach()
        resizeTab()
        tabs.getTabAt(1)?.select()
    }

    private fun resizeTab(){
        val layout = (tabs.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.6F
        layout.layoutParams = layoutParams
    }



    private fun setUpSupportActionBar() {
        val toolbar: Toolbar = findViewById(R.id.toolbar_main_fragment)
        setSupportActionBar(toolbar)

        val actionBar = supportActionBar
        if (actionBar != null) {
           // actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_ios_24)
           // actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setDisplayShowHomeEnabled(true)
            actionBar.setDisplayShowTitleEnabled(false)
        }
        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }
    }
}