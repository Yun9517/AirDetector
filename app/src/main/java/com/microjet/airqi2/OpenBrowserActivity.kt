package com.microjet.airqi2

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

/**
 * Created by B00190 on 2018/6/27.
 */
class OpenBrowserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val TAG = OpenBrowserActivity::class.java.simpleName
        Log.e(TAG, "onCreate")
        //副總杯杯要的網頁
        if (getIntent().hasExtra("fromNotification")) {
            getIntent().removeExtra("fromNotification")
            val intentActitity = Intent(this, MainActivity::class.java)
            val url = Uri.parse(TvocNoseData.scrollingList[0]["url"].toString())
            val intenturl = Intent(Intent.ACTION_VIEW, url)
            val intents = arrayOfNulls<Intent>(2)
            intents[0] = intentActitity
            intents[1] = intenturl
            startActivities(intents)
        }
        this.finish()
    }
}