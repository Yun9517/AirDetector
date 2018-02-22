package com.microjet.airqi2

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.Definition.SavePreferences
import kotlinx.android.synthetic.main.activity_setting.*

/**
 * Created by B00174 on 2017/11/29.
 * 
 */
class SettingActivity : AppCompatActivity() {

    private var mPreference: SharedPreferences? = null

    private var spCycleVal: Int = 0
    private var swMessageVal: Boolean = false
    private var swViberateVal: Boolean = false
    private var swSoundVal: Boolean = false
    private var swRunInBgVal: Boolean = false
    private var swTotalNotifyVal: Boolean = false
    //20180130
    private var batSoundVal: Boolean = false
    private var swLedPowerVal: Boolean = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        uiSetListener()

        val mCycleAdapter: ArrayAdapter<CharSequence> = ArrayAdapter.createFromResource(
                this, R.array.pickCycle, android.R.layout.simple_spinner_item)
        mCycleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCycle.adapter = mCycleAdapter

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
        //20180130
        //swPumpVal = mPreference!!.getBoolean(SavePreferences.SETTING_PUMP_MUNUAL, false)
        //20180206
        batSoundVal = mPreference!!.getBoolean(SavePreferences.SETTING_BATTERY_SOUND, false)

        swLedPowerVal = mPreference!!.getBoolean(SavePreferences.SETTING_LED_SWITCH, true)

        spCycle.setSelection(spCycleVal)

        swMessage.isChecked = swMessageVal
        swVibrate.isChecked = swViberateVal
        swSound.isChecked = swSoundVal
        swRunInBg.isChecked = swRunInBgVal
        swTotalNotify.isChecked = swTotalNotifyVal
        //20180130
        //swPump.isChecked = swPumpVal
        //20180206
        batSound.isChecked = batSoundVal

        ledPower.isChecked = swLedPowerVal

        //** 2017/12/27 Not the Best Solution to Fix Toggle button **//

        /*swViberate?.setOnTouchListener{ v, event -> event.actionMasked == MotionEvent.ACTION_MOVE }
        if (swViberate.isChecked) {
            text_vibe_stat?.text = getString(R.string.text_setting_on)
        } else {
            text_vibe_stat?.text = getString(R.string.text_setting_off)
        }

        swSound?.setOnTouchListener{ v, event -> event.actionMasked == MotionEvent.ACTION_MOVE }
        if (swSound.isChecked) {
            text_sound_stat.text = getString(R.string.text_setting_on)
        } else {
            text_sound_stat.text = getString(R.string.text_setting_off)
        }*/

    }

    private fun uiSetListener() {
        spCycle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                mPreference!!.edit().putInt(SavePreferences.SETTING_TEST_CYCLE,
                        position).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        swMessage.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                text_msg_stat.text = getString(R.string.text_setting_on)
                val mainintent = Intent(BroadcastIntents.PRIMARY)
                mainintent.putExtra("status", "message")
                sendBroadcast(mainintent)

                Log.d("message", "messageSETTING")
            } else {
                text_msg_stat.text = getString(R.string.text_setting_off)
            }

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_NOTIFY,
                    isChecked).apply()
        }

        swVibrate.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                text_vibrate_stat.text = getString(R.string.text_setting_on)
            } else {
                text_vibrate_stat.text = getString(R.string.text_setting_off)
            }

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_VIBERATION,
                    isChecked).apply()
        }

        swSound.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                text_sound_stat.text = getString(R.string.text_setting_on)
            } else {
                text_sound_stat.text = getString(R.string.text_setting_off)
            }

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_SOUND,
                    isChecked).apply()
        }

        swRunInBg.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                text_run_bg_stat.text = getString(R.string.text_setting_on)
            } else {
                text_run_bg_stat.text = getString(R.string.text_setting_off)
            }

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_RUN_IN_BG,
                    isChecked).apply()
        }

        swTotalNotify.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                text_total_notify_stat.text = getString(R.string.text_setting_on)
            } else {
                text_total_notify_stat.text = getString(R.string.text_setting_off)
            }

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_TOTAL_POLLUTION_NOTIFY,
                    isChecked).apply()
        }
       //20180129
//        swPump.setOnCheckedChangeListener { _, isChecked ->
//            if (isChecked) {
//                text_pump_stat!!.text = getString(R.string.text_setting_on)
//                //20180130
//                //************************************************************************************************************************************
//                val intent: Intent? = Intent(BroadcastIntents.PRIMARY)
//                intent!!.putExtra("status", BroadcastActions.INTENT_KEY_PUMP_ON)
//                sendBroadcast(intent)
//                Log.i("幹我按下了!!",text_pump_stat!!.text.toString())
//                //************************************************************************************************************************************
//            } else {
//                text_pump_stat!!.text = getString(R.string.text_setting_off)
//                //************************************************************************************************************************************
//                val intent: Intent? = Intent(BroadcastIntents.PRIMARY)
//                intent!!.putExtra("status", BroadcastActions.INTENT_KEY_PUMP_OFF)
//                sendBroadcast(intent)
//                Log.i("幹我不按了!!",text_pump_stat!!.text.toString())
//                //************************************************************************************************************************************
//            }
//
//            mPreference!!.edit().putBoolean(SavePreferences.SETTING_PUMP_MUNUAL,
//                    isChecked).apply()
//        }
        //20180206
        batSound.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                text_bat_stat!!.text = getString(R.string.text_setting_on)
            } else {
                text_bat_stat!!.text = getString(R.string.text_setting_off)
            }
            mPreference!!.edit().putBoolean(SavePreferences.SETTING_BATTERY_SOUND,
                    isChecked).apply()
        }

        ledPower.setOnCheckedChangeListener { _, isChecked ->

            val intent: Intent? = Intent(BroadcastIntents.PRIMARY)

            if (isChecked) {
                text_led_stat!!.text = getString(R.string.text_setting_on)
                intent!!.putExtra("status", BroadcastActions.INTENT_KEY_LED_ON)
            } else {
                text_led_stat.text = getString(R.string.text_setting_off)
                intent!!.putExtra("status", BroadcastActions.INTENT_KEY_LED_OFF)
            }

            sendBroadcast(intent)

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_LED_SWITCH,
                    isChecked).apply()
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
