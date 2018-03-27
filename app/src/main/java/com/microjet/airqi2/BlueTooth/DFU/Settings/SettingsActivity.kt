package com.microjet.airqi2.BlueTooth.DFU.Settings

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import com.microjet.airqi2.R

/**
 * Created by B00055 on 2018/3/26.
 */

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_actionbar)
     //   setSupportActionBar(toolbar)
     //   supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        // Display the fragment as the main content.
        fragmentManager.beginTransaction().replace(R.id.content, SettingFragment()).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
