package com.microjet.airqi2.settingPage

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import com.jaygoo.widget.RangeSeekBar
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.Definition.Colors
import com.microjet.airqi2.FirebaseNotifTask
import com.microjet.airqi2.PrefObjects
import com.microjet.airqi2.R
import com.microjet.airqi2.TvocNoseData
import kotlinx.android.synthetic.main.activity_setting3.*
import java.text.DecimalFormat

/**
 * Created by B00174 on 2017/11/29.
 *
 */

class CloudNotifySettingActivity : AppCompatActivity() {

    //20180515
    private var swCloudNotifyVal: Boolean = false
    //2018/07/04
    private var swMessageVal: Boolean = false
    private var swVibrateVal: Boolean = false
    private var swSoundVal: Boolean = false

    //20180517
    private var cloudTime: Int = TvocNoseData.firebaseNotiftime
    private var cloudPM25: Int = TvocNoseData.firebaseNotifPM25     //停留本頁暫存用變數
    private var cloudTVOC: Int = TvocNoseData.firebaseNotifTVOC    //停留本頁暫存用變數

    private lateinit var myPref: PrefObjects

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting3)

        myPref = PrefObjects(this)

        readPreferences()   // 載入設定值
        uiSetListener()
        initActionBar()
        //20180516 by 白~~~~~~~~~~~~~~~~~~~告
        setFCMSettingView()
    }

    private fun readPreferences() {
        getFCMSettings()
    }

    private fun uiSetListener() {
        swAllowCloudNotify.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cgCloudNotify.visibility = View.VISIBLE
                cgCloudSeekbar.visibility = View.VISIBLE
                // 2018/07/04
                cg_cloud_Message.visibility = View.VISIBLE
                cg_cloud_Vibration.visibility = View.VISIBLE
                cg_cloud_Sound.visibility = View.VISIBLE
                indexTitleGroup.visibility = View.VISIBLE
            } else {
                cgCloudNotify.visibility = View.GONE
                cgCloudSeekbar.visibility = View.GONE
                updateCloudSetting(25, 35, 660)
                // 2018/07/04
                cg_cloud_Message.visibility = View.GONE
                cg_cloud_Vibration.visibility = View.GONE
                cg_cloud_Sound.visibility = View.GONE
                indexTitleGroup.visibility = View.GONE
            }

            myPref.setSharePreferenceFirebase(isChecked)
        }

        sw_cloud_Message.setOnCheckedChangeListener { _, isChecked ->
            myPref.setSharePreferenceAllowBroadcastMessage(isChecked)
        }

        sw_cloud_Vibrate.setOnCheckedChangeListener { _, isChecked ->
            myPref.setSharePreferenceAllowBroadcastVibrate(isChecked)
        }

        sw_cloud_Sound.setOnCheckedChangeListener { _, isChecked ->
            myPref.setSharePreferenceAllowBroadcastSound(isChecked)
        }

        //20180516 BY 白~~~~~~~~~~~~~~告
        cloudTvocSeekBar.setOnRangeChangedListener(object : RangeSeekBar.OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar, min: Float, max: Float, isFromUser: Boolean) {
                if (isFromUser) {
                    setSeekBarColor(view, min, true)
                    setSeekBarValue(cloudTvocSeekValue, min)
                    cloudTVOC = min.toInt()
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

        cloudPM25SeekBar.setOnRangeChangedListener(object : RangeSeekBar.OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar, min: Float, max: Float, isFromUser: Boolean) {
                if (isFromUser) {
                    setSeekBarColor(view, min, false)
                    setSeekBarValue(cloudPM25SeekValue, min)
                    cloudPM25 = min.toInt()
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

        cloudTvocSeekValue.setOnClickListener {
            val editText = EditText(this)
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            editText.textSize = 40f
            editText.textAlignment = EditText.TEXT_ALIGNMENT_CENTER

            val dialog = AlertDialog.Builder(this)

            dialog.setTitle(resources.getString(R.string.text_setting_tvoc_value))
            dialog.setView(editText)
            dialog.setPositiveButton(getString(android.R.string.ok), { _, _ ->
                val value = editText.text.toString()

                if (value.isNotEmpty() && value.toInt() in 220..2200) {
                    cloudTvocSeekBar.setValue(value.toFloat())
                    cloudTVOC = value.toInt()
                    setSeekBarColor(cloudTvocSeekBar, value.toFloat(), true)
                    setSeekBarValue(cloudTvocSeekValue, value.toFloat())


                }
            })

            dialog.setNegativeButton(getString(android.R.string.cancel), null)
            dialog.show()
        }

        cloudPM25SeekValue.setOnClickListener {
            val editText = EditText(this)
            editText.inputType = InputType.TYPE_CLASS_NUMBER
            editText.textSize = 40f
            editText.textAlignment = EditText.TEXT_ALIGNMENT_CENTER

            val dialog = AlertDialog.Builder(this)

            dialog.setTitle(resources.getString(R.string.text_setting_pm25_value))
            dialog.setView(editText)
            dialog.setPositiveButton(getString(android.R.string.ok), { _, _ ->
                val value = editText.text.toString()
                if (value.isNotEmpty() && value.toInt() in 16..150) {
                    cloudPM25SeekBar.setValue(value.toFloat())
                    cloudPM25 = value.toInt()
                    setSeekBarColor(cloudPM25SeekBar, value.toFloat(), false)
                    setSeekBarValue(cloudPM25SeekValue, value.toFloat())


                }
            })

            dialog.setNegativeButton(getString(android.R.string.cancel), null)
            dialog.show()
        }

        btnCloudNotify.setOnClickListener {
            numberPickerDialog()
        }

        btnSaveCloudSetting.setOnClickListener {
            updateCloudSetting(cloudTime, cloudPM25, cloudTVOC)
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

    private fun getFCMSettings() {
        swCloudNotifyVal = myPref.getSharePreferenceFirebase()
        swMessageVal = myPref.getSharePreferenceAllowBroadcastMessage()
        swVibrateVal = myPref.getSharePreferenceAllowBroadcastVibrate()
        swSoundVal = myPref.getSharePreferenceAllowBroadcastSound()

        swAllowCloudNotify?.isChecked = swCloudNotifyVal
        if (swCloudNotifyVal) {
            cgCloudNotify.visibility = View.VISIBLE
            cgCloudSeekbar.visibility = View.VISIBLE
            // 2018/07/04 Add toggle Button: sw_cloud_Message, sw_cloud_Vibrate, sw_cloud_Sound
            cg_cloud_Message.visibility = View.VISIBLE
            cg_cloud_Vibration.visibility = View.VISIBLE
            cg_cloud_Sound.visibility = View.VISIBLE
            indexTitleGroup.visibility = View.VISIBLE
        } else {
            cgCloudNotify.visibility = View.GONE
            cgCloudSeekbar.visibility = View.GONE
            // 2018/07/04 Add toggle Button: sw_cloud_Message, sw_cloud_Vibrate, sw_cloud_Sound
            cg_cloud_Message.visibility = View.GONE
            cg_cloud_Vibration.visibility = View.GONE
            cg_cloud_Sound.visibility = View.GONE
            indexTitleGroup.visibility = View.GONE
        }
        // 2018/07/04 Add toggle Button: sw_cloud_Message, sw_cloud_Vibrate, sw_cloud_Sound
        sw_cloud_Message.isChecked = swMessageVal
        sw_cloud_Vibrate.isChecked = swVibrateVal
        sw_cloud_Sound.isChecked = swSoundVal
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

    //2018515 by 白~~~~~~~~~~~~~~~~告
    @SuppressLint("SetTextI18n")
    private fun setFCMSettingView() {
        when (TvocNoseData.firebaseNotiftime) {
            in 0..9 -> {
                btnCloudNotify.text = "0${TvocNoseData.firebaseNotiftime}"
            }
            25 -> {
                btnCloudNotify.text = "00"
            }
            else -> {
                btnCloudNotify.text = "${TvocNoseData.firebaseNotiftime}"
            }
        }
        //TVOC TEXTVIEW VALUE
        cloudTvocSeekValue.text = TvocNoseData.firebaseNotifTVOC.toString()
        cloudTvocSeekBar.setValue(TvocNoseData.firebaseNotifTVOC.toFloat())
        //PM25 TEXTVIEW VALUE
        cloudPM25SeekValue.text = TvocNoseData.firebaseNotifPM25.toString()
        cloudPM25SeekBar.setValue(TvocNoseData.firebaseNotifPM25.toFloat())
        //SEEKBARCOLOR
        setSeekBarColor(cloudTvocSeekBar, TvocNoseData.firebaseNotifTVOC.toFloat(), true)
        setSeekBarColor(cloudPM25SeekBar, TvocNoseData.firebaseNotifPM25.toFloat(), false)
    }

    @SuppressLint("SetTextI18n")
    private fun numberPickerDialog() {
        val myHourPicker = NumberPicker(this)
        myHourPicker.maxValue = 23
        myHourPicker.minValue = 0
        myHourPicker.value = TvocNoseData.firebaseNotiftime
        val alertBuilder = AlertDialog.Builder(this).setView(myHourPicker)
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    cloudTime = myHourPicker.value
                    if (cloudTime < 10) {
                        btnCloudNotify.text = "0$cloudTime"
                    } else {
                        btnCloudNotify.text = "$cloudTime"
                    }
                    Log.e("TvocNoseData", TvocNoseData.firebaseNotiftime.toString())
                }.setTitle(getString(R.string.text_cloud_notify_time))

        alertBuilder.show()
    }

    private fun updateCloudSetting(argTime: Int, argPm25: Int, argTvoc: Int) {
        TvocNoseData.firebaseNotiftime = argTime
        TvocNoseData.firebaseNotifPM25 = argPm25
        TvocNoseData.firebaseNotifTVOC = argTvoc
        val shareToken = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        val myToken = shareToken.getString("token", "")
        FirebaseNotifTask().execute(myToken, argTime.toString(), argPm25.toString(), argTvoc.toString())
        setFCMSettingView()
    }
}
