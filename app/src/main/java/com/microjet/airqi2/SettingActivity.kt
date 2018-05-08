package com.microjet.airqi2

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.Definition.SavePreferences
import kotlinx.android.synthetic.main.activity_setting.*
import com.jaygoo.widget.RangeSeekBar
import com.microjet.airqi2.Definition.Colors
import com.microjet.airqi2.GestureLock.DefaultPatternSettingActivity
import java.text.DecimalFormat
import java.util.logging.SimpleFormatter


/**
 * Created by B00174 on 2017/11/29.
 *
 */
class SettingActivity : AppCompatActivity() {

    private var mPreference: SharedPreferences? = null

    private var swAllowNotifyVal: Boolean = false
    private var swMessageVal: Boolean = false
    private var swViberateVal: Boolean = false
    private var swSoundVal: Boolean = false
    private var swRunInBgVal: Boolean = false
    private var swTotalNotifyVal: Boolean = false
    //20180130
    private var batSoundVal: Boolean = false
    private var swLedPowerVal: Boolean = true
    //20180227
    private var swCloudVal: Boolean = true

    private var tvocSeekBarVal: Int = 660
    private var pm25SeekBarVal: Int = 16

    private var isPrivacy: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        readPreferences()   // 載入設定值
        uiSetListener()
        initActionBar()
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        text_local_uuid.text = MyApplication.getPsuedoUniqueID()
        text_device_ver.text = resources.getString(R.string.text_label_device_version) + MyApplication.getDeviceVersion()
    }

    private fun readPreferences() {
        mPreference = getSharedPreferences(SavePreferences.SETTING_KEY, 0)
        swAllowNotifyVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)
        swMessageVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_MESSAGE, false)
        swViberateVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false)
        swSoundVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false)
        swRunInBgVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_RUN_IN_BG, false)
        swTotalNotifyVal = mPreference!!.getBoolean(SavePreferences.SETTING_TOTAL_POLLUTION_NOTIFY, false)
        //20180206
        batSoundVal = mPreference!!.getBoolean(SavePreferences.SETTING_BATTERY_SOUND, false)

        swLedPowerVal = mPreference!!.getBoolean(SavePreferences.SETTING_LED_SWITCH, true)

        //20180227
        swCloudVal = mPreference!!.getBoolean(SavePreferences.SETTING_CLOUD_FUN, true)

        tvocSeekBarVal = mPreference!!.getInt(SavePreferences.SETTING_TVOC_NOTIFY_VALUE, 660)
        pm25SeekBarVal = mPreference!!.getInt(SavePreferences.SETTING_PM25_NOTIFY_VALUE, 16)

        isPrivacy = mPreference!!.getBoolean(SavePreferences.SETTING_MAP_PRIVACY, false)
        //pm25SeekBarVal = 100


        swAllowNotify.isChecked = swAllowNotifyVal

        if (swAllowNotifyVal) {
            cgMessage.visibility = View.VISIBLE
            cgVibration.visibility = View.VISIBLE
            cgSound.visibility = View.VISIBLE
            cgSeekbar.visibility = View.VISIBLE
            cgLowBatt.visibility = View.VISIBLE
        } else {
            cgMessage.visibility = View.GONE
            cgVibration.visibility = View.GONE
            cgSound.visibility = View.GONE
            cgSeekbar.visibility = View.GONE
            cgLowBatt.visibility = View.GONE
        }

        swMessage.isChecked = swMessageVal
        swVibrate.isChecked = swViberateVal
        swSound.isChecked = swSoundVal
        //20180130
        //swPump.isChecked = swPumpVal
        //20180206
        batSound.isChecked = batSoundVal

        ledPower.isChecked = swLedPowerVal

        swCloudFunc.isChecked = swCloudVal

        tvocSeekBar.setValue(tvocSeekBarVal.toFloat())
        pm25SeekBar.setValue(pm25SeekBarVal.toFloat())

        tvocSeekValue.text = tvocSeekBarVal.toString()
        pm25SeekValue.text = pm25SeekBarVal.toString()

        setSeekBarColor(tvocSeekBar, tvocSeekBarVal.toFloat(), true)
        setSeekBarColor(pm25SeekBar, pm25SeekBarVal.toFloat(), false)

        swAllowPrivacy.isChecked = isPrivacy
    }

    private fun uiSetListener() {

        swAllowNotify.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cgMessage.visibility = View.VISIBLE
                cgVibration.visibility = View.VISIBLE
                cgSound.visibility = View.VISIBLE
                cgSeekbar.visibility = View.VISIBLE
                cgLowBatt.visibility = View.VISIBLE
            } else {
                cgMessage.visibility = View.GONE
                cgVibration.visibility = View.GONE
                cgSound.visibility = View.GONE
                cgSeekbar.visibility = View.GONE
                cgLowBatt.visibility = View.GONE
            }

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_NOTIFY,
                    isChecked).apply()
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

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_MESSAGE,
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

        tvocSeekBar.setOnRangeChangedListener(object : RangeSeekBar.OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar, min: Float, max: Float, isFromUser: Boolean) {
                setSeekBarColor(view, min, true)

                setSeekBarValue(tvocSeekValue, min)

                mPreference!!.edit().putInt(SavePreferences.SETTING_TVOC_NOTIFY_VALUE, min.toInt()).apply()
                Log.e("SeekBar", "Min: $min")
            }

            override fun onStartTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {
                //do what you want!!
            }

            override fun onStopTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {
                //do what you want!!
            }
        })

        pm25SeekBar.setOnRangeChangedListener(object : RangeSeekBar.OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar, min: Float, max: Float, isFromUser: Boolean) {
                setSeekBarColor(view, min, false)

                setSeekBarValue(pm25SeekValue, min)

                mPreference!!.edit().putInt(SavePreferences.SETTING_PM25_NOTIFY_VALUE, min.toInt()).apply()
            }

            override fun onStartTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {
                //do what you want!!
            }

            override fun onStopTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {
                //do what you want!!
            }
        })


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
            val intent: Intent? = Intent(
                    if (isChecked) {
                        text_led_stat!!.text = getString(R.string.text_setting_on)
                        BroadcastActions.INTENT_KEY_LED_ON
                    } else {
                        text_led_stat.text = getString(R.string.text_setting_off)
                        BroadcastActions.INTENT_KEY_LED_OFF
                    }
            )

            sendBroadcast(intent)

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_LED_SWITCH,
                    isChecked).apply()
        }

        //20180227  CloudFun
        swCloudFunc.setOnCheckedChangeListener { _, isChecked ->

            val intent: Intent? = Intent(BroadcastIntents.PRIMARY)

            if (isChecked) {
                text_clouud_stat!!.text = getString(R.string.text_setting_on)
                intent!!.putExtra("status", BroadcastActions.INTENT_KEY_CLOUD_ON)
            } else {
                text_clouud_stat.text = getString(R.string.text_setting_off)
                intent!!.putExtra("status", BroadcastActions.INTENT_KEY_CLOUD_OFF)
            }

            sendBroadcast(intent)

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_CLOUD_FUN,
                    isChecked).apply()

        }

        swAllowPrivacy.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                DefaultPatternSettingActivity.startAction(this@SettingActivity)
            } else {

            }
        }

    }

    private fun setSeekBarColor(view: RangeSeekBar, min: Float, isTVOC: Boolean) {
        if(isTVOC) {
            view.setLineColor(R.color.colorSeekBarDefault, when (min) {
                in 0..219 -> Colors.tvocCO2Colors[0]
                in 220..659 -> Colors.tvocCO2Colors[1]
                in 660..2199 -> Colors.tvocCO2Colors[2]
                in 2200..5499 -> Colors.tvocCO2Colors[3]
                in 5500..20000 -> Colors.tvocCO2Colors[4]
                else -> Colors.tvocCO2Colors[5]
            })
        } else {
            view.setLineColor(R.color.colorSeekBarDefault, when (min) {
                in 0..15 -> Colors.tvocCO2Colors[0]
                in 16..34 -> Colors.tvocCO2Colors[1]
                in 35..54 -> Colors.tvocCO2Colors[2]
                in 55..150 -> Colors.tvocCO2Colors[3]
                in 151..250 -> Colors.tvocCO2Colors[4]
                else -> Colors.tvocCO2Colors[5]
            })
        }
    }

    private fun setSeekBarValue(view: TextView, min: Float) {
        val format = DecimalFormat("###")
        view.text = format.format(min).toString()
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
