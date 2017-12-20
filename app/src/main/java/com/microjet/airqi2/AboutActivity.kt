package com.microjet.airqi2

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView

/**
 * Created by B00174 on 2017/11/27.
 */
class AboutActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        val textView : TextView?= findViewById(R.id.aboutAppVersion)
        val string : String? = getString(R.string.show_app_version, BuildConfig.VERSION_NAME)
        textView!!.text = string
    }
}