package com.microjet.airqi2.settingPage

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.microjet.airqi2.GestureLock.DefaultPatternCheckingActivity
import com.microjet.airqi2.GestureLock.DefaultPatternSettingActivity
import com.microjet.airqi2.PrefObjects
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.activity_setting1.*

/**
 * Created by B00174 on 2017/11/29.
 *
 */

class PrivacySettingActivity : AppCompatActivity() {

    private var isPrivacy: Boolean = false

    private lateinit var myPref: PrefObjects

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting1)

        myPref = PrefObjects(this)

        readPreferences()   // 載入設定值
        uiSetListener()
        initActionBar()
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        getPrivacySettings()
    }

    private fun readPreferences() {
        getPrivacySettings()
    }

    private fun uiSetListener() {

        swAllowPrivacy.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                DefaultPatternSettingActivity.startAction(this@PrivacySettingActivity)
            } else {
                DefaultPatternCheckingActivity.startAction(this@PrivacySettingActivity,
                        DefaultPatternCheckingActivity.START_ACTION_MODE_DISABLE)
            }
        }

        btnChangePassword.setOnClickListener {
            DefaultPatternCheckingActivity.startAction(this@PrivacySettingActivity,
                    DefaultPatternCheckingActivity.START_ACTION_MODE_CHANGE_PASSWOPRD)
        }
    }

    private fun getPrivacySettings() {
        isPrivacy = myPref.getSharePreferencePrivacy()

        swAllowPrivacy.isChecked = isPrivacy

        if (isPrivacy) {
            cgPrivacy.visibility = View.VISIBLE
        } else {
            cgPrivacy.visibility = View.GONE
        }
    }

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home //對用戶按home icon的處理，本例只需關閉activity，就可返回上一activity，即主activity。
            -> {
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
