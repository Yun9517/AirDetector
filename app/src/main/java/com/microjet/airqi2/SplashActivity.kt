package com.microjet.airqi2

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.activity_splash.*
import android.content.Intent
import android.view.WindowManager


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var myPref: PrefObjects

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_splash)

        myPref = PrefObjects(this)

        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN)

        val view = window.decorView
        view.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        Handler().postDelayed(Runnable /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */

        {
            // This method will be executed once the timer is over
            // Start your app main activity
            if (myPref.getIsFirstUsed()) {
                val i = Intent(this@SplashActivity, TourActivity::class.java) //放你想跳過去的頁面
                startActivity(i)
                finish()
            } else {
                //val i = Intent(this@SplashActivity, MainActivity::class.java) //放你想跳過去的頁面
                val i = Intent(this@SplashActivity, MainActivity::class.java) //放你想跳過去的頁面
                startActivity(i)
                finish()
            }
            // close this activity
        }, AUTO_HIDE_DELAY_MILLIS.toLong())
    }

    companion object {
        private const val AUTO_HIDE_DELAY_MILLIS = 1000
    }
}
