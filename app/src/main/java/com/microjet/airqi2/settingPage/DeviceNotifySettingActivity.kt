package com.microjet.airqi2.settingPage

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.jaygoo.widget.RangeSeekBar
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.Definition.Colors
import com.microjet.airqi2.PrefObjects
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.activity_setting2.*
import java.text.DecimalFormat

/**
 * Created by B00174 on 2017/11/29.
 *
 */

class DeviceNotifySettingActivity : AppCompatActivity() {

    private var swAllowNotifyVal: Boolean = false
    private var swMessageVal: Boolean = false
    private var swVibrateVal: Boolean = false
    private var swSoundVal: Boolean = false

    private var tvocSeekBarVal: Int = 660
    private var pm25SeekBarVal: Int = 16

    private lateinit var myPref: PrefObjects
    var assignNumber = 0
    var value:String = "0"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting2)

        myPref = PrefObjects(this)

        readPreferences()   // 載入設定值
        uiSetListener()
        initActionBar()
    }

    private fun readPreferences() {
        getNotificationSettings()
    }

    private fun uiSetListener() {
        swAllowNotify.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cgMessage.visibility = View.VISIBLE
                cgVibration.visibility = View.VISIBLE
                cgSound.visibility = View.VISIBLE
                cgSeekbar.visibility = View.VISIBLE
                //cgLowBatt.visibility = View.VISIBLE
            } else {
                cgMessage.visibility = View.GONE
                cgVibration.visibility = View.GONE
                cgSound.visibility = View.GONE
                cgSeekbar.visibility = View.GONE
                //cgLowBatt.visibility = View.GONE
            }

            myPref.setSharePreferenceAllowNotify(isChecked)
        }

        swMessage.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                val mainintent = Intent(BroadcastIntents.PRIMARY)
                mainintent.putExtra("status", "message")
                sendBroadcast(mainintent)

                Log.d("message", "messageSETTING")
            }

            myPref.setSharePreferenceAllowNotifyMessage(isChecked)
        }

        swVibrate.setOnCheckedChangeListener { _, isChecked ->

            myPref.setSharePreferenceAllowNotifyVibrate(isChecked)
        }

        swSound.setOnCheckedChangeListener { _, isChecked ->

            myPref.setSharePreferenceAllowNotifySound(isChecked)
        }

        tvocSeekBar.setOnRangeChangedListener(object : RangeSeekBar.OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar, min: Float, max: Float, isFromUser: Boolean) {
                if (isFromUser) {
                    setSeekBarColor(view, min, true)
                    setSeekBarValue(tvocSeekValue, min)
                    myPref.setSharePreferenceAllowNotifyTvocValue(min.toInt())
                }
                Log.e("SeekBar", "Min: $min, IsFromUser: $isFromUser")
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
                if (isFromUser) {
                    setSeekBarColor(view, min, false)
                    setSeekBarValue(pm25SeekValue, min)
                    myPref.setSharePreferenceAllowNotifyPM25Value(min.toInt())
                }
                Log.e("SeekBar", "Min: $min, IsFromUser: $isFromUser")
            }

            override fun onStartTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {
                //do what you want!!
            }

            override fun onStopTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {
                //do what you want!!
            }
        })

        tvocSeekValue.setOnClickListener {
            val editText = EditText(this)
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            editText.textSize = 40f
            editText.textAlignment = EditText.TEXT_ALIGNMENT_CENTER

            val dialog = AlertDialog.Builder(this)

            dialog.setTitle(resources.getString(R.string.text_setting_tvoc_value))
            dialog.setView(editText)
            dialog.setPositiveButton(getString(android.R.string.ok), { _, _ ->
            value = editText.text.toString()
                /*if (value.isNotEmpty() && value.toInt() in 220..2200) {
                    tvocSeekBar.setValue(value.toFloat())
                    setSeekBarColor(tvocSeekBar, value.toFloat(), true)
                    setSeekBarValue(tvocSeekValue, value.toFloat())

                    myPref.setSharePreferenceAllowNotifyTvocValue(value.toInt())
                } else {
                    Utils.toastMakeTextAndShow(this, "Value Over Range", Toast.LENGTH_SHORT)
                }*/
                if (value.isNotEmpty()) {
                    if (value.toDouble() > 2221) {
                        assignNumber = 2222
                        value = "2221"
                    }else{
                        assignNumber = 0
                        value = editText.text.toString()
                    }
                        when (value.toInt()) {

                            in 220..2200 -> assignNumber = value.toInt()
                            in 0..219 -> assignNumber = 220
                            else -> assignNumber = 2200
                        }
                        tvocSeekBar.setValue(assignNumber.toFloat())
                        setSeekBarColor(tvocSeekBar, assignNumber.toFloat(), true)
                        setSeekBarValue(tvocSeekValue, assignNumber.toFloat())
                        myPref.setSharePreferenceAllowNotifyTvocValue(assignNumber)

                }
            })

            dialog.setNegativeButton(getString(android.R.string.cancel), null)
            dialog.show()
        }

        pm25SeekValue.setOnClickListener {
            val editText = EditText(this)
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            editText.textSize = 40f
            editText.textAlignment = EditText.TEXT_ALIGNMENT_CENTER

            val dialog = AlertDialog.Builder(this)

            dialog.setTitle(resources.getString(R.string.text_setting_pm25_value))
            dialog.setView(editText)
            dialog.setPositiveButton(getString(android.R.string.ok), { _, _ ->
            value = editText.text.toString()

                /*if (value.isNotEmpty() && value.toInt() in 16..150) {
                    pm25SeekBar.setValue(value.toFloat())
                    setSeekBarColor(pm25SeekBar, value.toFloat(), false)
                    setSeekBarValue(pm25SeekValue, value.toFloat())

                    myPref.setSharePreferenceAllowNotifyPM25Value(value.toInt())
                } else {
                    Utils.toastMakeTextAndShow(this, "Value Over Range", Toast.LENGTH_SHORT)
                }*/
                if (value.isNotEmpty()) {
                    if (value.toDouble() > 2221) {
                        assignNumber = 2222
                        value = "2221"
                    }else{
                        assignNumber = 0
                        value = editText.text.toString()
                    }

                    when(value.toInt()) {
                        in 16..150 -> assignNumber = value.toInt()
                        in 0..15 -> assignNumber = 15
                        else -> assignNumber = 150
                    }
                    pm25SeekBar.setValue(assignNumber.toFloat())
                    setSeekBarColor(pm25SeekBar, assignNumber.toFloat(), false)
                    setSeekBarValue(pm25SeekValue, assignNumber.toFloat())
                    myPref.setSharePreferenceAllowNotifyPM25Value(assignNumber)
                }
            })

            dialog.setNegativeButton(getString(android.R.string.cancel), null)
            dialog.show()
        }
    }

    private fun setSeekBarColor(view: RangeSeekBar, min: Float, isTVOC: Boolean) {
        if (isTVOC) {
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
                in 55..149 -> Colors.tvocCO2Colors[3]
                in 150..250 -> Colors.tvocCO2Colors[4]
                else -> Colors.tvocCO2Colors[5]
            })
        }
    }

    private fun setSeekBarValue(view: TextView, min: Float) {
        val format = DecimalFormat("###")
        view.text = format.format(min).toString()
    }

    private fun getNotificationSettings() {
        swAllowNotifyVal = myPref.getSharePreferenceAllowNotify()
        swMessageVal = myPref.getSharePreferenceAllowNotifyMessage()
        swVibrateVal = myPref.getSharePreferenceAllowNotifyVibrate()
        swSoundVal = myPref.getSharePreferenceAllowNotifySound()

        tvocSeekBarVal = myPref.getSharePreferenceAllowNotifyTvocValue()
        pm25SeekBarVal = myPref.getSharePreferenceAllowNotifyPM25Value()

        swAllowNotify.isChecked = swAllowNotifyVal

        if (swAllowNotifyVal) {
            cgMessage.visibility = View.VISIBLE
            cgVibration.visibility = View.VISIBLE
            cgSound.visibility = View.VISIBLE
            cgSeekbar.visibility = View.VISIBLE
            //cgLowBatt.visibility = View.VISIBLE
        } else {
            cgMessage.visibility = View.GONE
            cgVibration.visibility = View.GONE
            cgSound.visibility = View.GONE
            cgSeekbar.visibility = View.GONE
            //cgLowBatt.visibility = View.GONE
        }

        swMessage.isChecked = swMessageVal
        swVibrate.isChecked = swVibrateVal
        swSound.isChecked = swSoundVal

        tvocSeekBar.setValue(tvocSeekBarVal.toFloat())
        pm25SeekBar.setValue(pm25SeekBarVal.toFloat())

        tvocSeekValue.text = tvocSeekBarVal.toString()
        pm25SeekValue.text = pm25SeekBarVal.toString()

        setSeekBarColor(tvocSeekBar, tvocSeekBarVal.toFloat(), true)
        setSeekBarColor(pm25SeekBar, pm25SeekBarVal.toFloat(), false)
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
