package com.microjet.airqi2

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import com.microjet.airqi2.Definition.SavePreferences
import android.widget.AdapterView


/**
 * Created by B00174 on 2017/11/29.
 */
class SettingActivity : AppCompatActivity() {

    var spCycle: Spinner? = null
    var swMessage: SwitchCompat? = null
    var swViberate: SwitchCompat? = null
    var swSound: SwitchCompat? = null
    var swRunInBg: SwitchCompat? = null
    var swTotalNotify: SwitchCompat? = null
    var text_msg_stat: TextView? = null
    var text_vibe_stat: TextView? = null
    var text_sound_stat: TextView? = null
    var text_run_bg_stat: TextView? = null
    var text_total_notify_stat: TextView? = null
    var btn_clean: Button? = null
    var btn_export: Button? = null

    var mPreference: SharedPreferences? = null

    var spCycleVal: Int = 0
    var swMessageVal: Boolean = false
    var swViberateVal: Boolean = false
    var swSoundVal: Boolean = false
    var swRunInBgVal: Boolean = false
    var swTotalNotifyVal: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        uiFindViewById()
        uiSetListener()

        val mCycleAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
                this, R.array.pickCycle, android.R.layout.simple_spinner_item)
        mCycleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCycle!!.adapter = mCycleAdapter

        mPreference = getSharedPreferences(SavePreferences.SETTING_KEY,0)
        //20171202   Andy ACtivity載入時讀取偏好設定並設定資料庫相關參數Time

        //測試資料庫讀是否成功
        /*
        var realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        query.equalTo("TVOCValue", "16")
        val result1 = query.findAllAsync()
        Toast.makeText(this,result1.first()?.created_time.toString(),Toast.LENGTH_SHORT).show()
        */

        initActionBar()


    }

    override fun onResume() {
        super.onResume()
        readPreferences()   // 當Activity onResume時載入設定值
    }

    private fun readPreferences() {
        spCycleVal = mPreference!!.getInt(SavePreferences.SETTING_TEST_CYCLE, 0)
        swMessageVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)
        swViberateVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false)
        swSoundVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false)
        swRunInBgVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_RUN_IN_BG, false)
        swTotalNotifyVal = mPreference!!.getBoolean(SavePreferences.SETTING_TOTAL_POLLUTION_NOTIFY, false)

        spCycle!!.setSelection(spCycleVal)

        swMessage!!.isChecked = swMessageVal
        swViberate!!.isChecked = swViberateVal
        swSound!!.isChecked = swSoundVal
        swRunInBg!!.isChecked = swRunInBgVal
        swTotalNotify!!.isChecked = swTotalNotifyVal

    }

    private fun uiFindViewById() {
        spCycle = findViewById(R.id.spCycle)

        swMessage = findViewById(R.id.swMessage)
        swViberate = findViewById(R.id.swViberate)
        swSound = findViewById(R.id.swSound)
        swRunInBg = findViewById(R.id.swRunInBg)
        swTotalNotify = findViewById(R.id.swTotalNotify)

        text_msg_stat = findViewById(R.id.text_msg_stat)
        text_vibe_stat = findViewById(R.id.text_vibe_stat)
        text_sound_stat = findViewById(R.id.text_sound_stat)
        text_run_bg_stat = findViewById(R.id.text_run_bg_stat)
        text_total_notify_stat = findViewById(R.id.text_total_notify_stat)

        btn_clean = findViewById(R.id.btn_clean)
        btn_export = findViewById(R.id.btn_export)
    }

    private fun uiSetListener() {
        spCycle!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPreference!!.edit().putInt(SavePreferences.SETTING_TEST_CYCLE,
                        position).apply()
            }






            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        swMessage!!.setOnClickListener {
            if (swMessage!!.isChecked) {
                text_msg_stat!!.text = getString(R.string.text_setting_on)
                val mainintent = Intent("Main")
                mainintent.putExtra("status", "message")
                sendBroadcast(mainintent)
                Log.d("message","messageSETTING")
            } else {
                text_msg_stat!!.text = getString(R.string.text_setting_off)
            }

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_NOTIFY,
                    swMessage!!.isChecked).apply()
        }

        swViberate!!.setOnClickListener {
            if (swViberate!!.isChecked) {
                text_vibe_stat!!.text = getString(R.string.text_setting_on)
            } else {
                text_vibe_stat!!.text = getString(R.string.text_setting_off)
            }

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_VIBERATION,
                    swViberate!!.isChecked).apply()
        }

        swSound!!.setOnClickListener {
            if (swSound!!.isChecked) {
                text_sound_stat!!.text = getString(R.string.text_setting_on)
            } else {
                text_sound_stat!!.text = getString(R.string.text_setting_off)
            }

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_SOUND,
                    swSound!!.isChecked).apply()
        }

        swRunInBg!!.setOnClickListener {
            if (swRunInBg!!.isChecked) {
                text_run_bg_stat!!.text = getString(R.string.text_setting_on)
            } else {
                text_run_bg_stat!!.text = getString(R.string.text_setting_off)
            }

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_RUN_IN_BG,
                    swRunInBg!!.isChecked).apply()
        }

        swTotalNotify!!.setOnClickListener {
            if (swTotalNotify!!.isChecked) {
                text_total_notify_stat!!.text = getString(R.string.text_setting_on)
            } else {
                text_total_notify_stat!!.text = getString(R.string.text_setting_off)
            }

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_TOTAL_POLLUTION_NOTIFY,
                    swTotalNotify!!.isChecked).apply()
        }
    }


    override fun finish() {
        //val intent = Intent()
        //intent.putExtra("choseCycle", mPreference?.getInt(SavePreferences.SETTING_TEST_CYCLE, 0))
        //setResult(2, intent)
        super.finish()
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
