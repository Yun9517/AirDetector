package com.microjet.airqi2

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Environment
import android.support.v4.content.FileProvider
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import com.jaygoo.widget.RangeSeekBar
import com.microjet.airqi2.BlueTooth.DFU.DFUProcessClass
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.Definition.Colors
import com.microjet.airqi2.Definition.SavePreferences
import com.microjet.airqi2.GestureLock.DefaultPatternCheckingActivity
import com.microjet.airqi2.GestureLock.DefaultPatternSettingActivity
import com.microjet.airqi2.TvocNoseData.calObject
import com.microjet.airqi2.URL.AirActionTask
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_setting.*
import kotlinx.android.synthetic.main.drawer_header.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


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
    //20180130
    private var batSoundVal: Boolean = false
    private var swLedPowerVal: Boolean = true
    private var swLedOffLinePowerVal: Boolean = true

    //20180227
    private var swCloudVal: Boolean = true
    private var swCloud3GVal: Boolean = true

    private var tvocSeekBarVal: Int = 660
    private var pm25SeekBarVal: Int = 16

    private var isPrivacy: Boolean = false

    //20180515
    private var swCloudNotifyVal: Boolean = false

    //20180517
    private var cloudTVOC: Int = TvocNoseData.firebaseNotifTVOC    //停留本頁暫存用變數
    private var cloudPM25: Int = TvocNoseData.firebaseNotifPM25     //停留本頁暫存用變數
    private var cloudTime: Int = TvocNoseData.firebaseNotiftime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        readPreferences()   // 載入設定值
        uiSetListener()
        initActionBar()

        if(intent.getBooleanExtra("CONN", false)) {
            cgDeviceControl.visibility = View.VISIBLE
        } else {
            cgDeviceControl.visibility = View.GONE
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        text_device_ver.text = resources.getString(R.string.text_label_device_version) + MyApplication.getDeviceVersion()

        getPrivacySettings()

        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    private fun readPreferences() {
        mPreference = getSharedPreferences(SavePreferences.SETTING_KEY, 0)
        getNotificationSettings()
        getCloudSettings()
        getPrivacySettings()
        getDeviceLedSettings()
        //20180516 by 白~~~~~~~~~~~~~~~~~~~告
        getFirebaseNotifSettings()
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
                val mainintent = Intent(BroadcastIntents.PRIMARY)
                mainintent.putExtra("status", "message")
                sendBroadcast(mainintent)

                Log.d("message", "messageSETTING")
            }

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_MESSAGE,
                    isChecked).apply()
        }

        swVibrate.setOnCheckedChangeListener { _, isChecked ->

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_VIBERATION,
                    isChecked).apply()
        }

        swSound.setOnCheckedChangeListener { _, isChecked ->

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_SOUND,
                    isChecked).apply()
        }

        tvocSeekBar.setOnRangeChangedListener(object : RangeSeekBar.OnRangeChangedListener {
            override fun onRangeChanged(view: RangeSeekBar, min: Float, max: Float, isFromUser: Boolean) {
                if (isFromUser) {
                    setSeekBarColor(view, min, true)
                    setSeekBarValue(tvocSeekValue, min)
                    mPreference!!.edit().putInt(SavePreferences.SETTING_TVOC_NOTIFY_VALUE, min.toInt()).apply()
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
                    mPreference!!.edit().putInt(SavePreferences.SETTING_PM25_NOTIFY_VALUE, min.toInt()).apply()
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


        //20180206
        batSound.setOnCheckedChangeListener { _, isChecked ->

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_BATTERY_SOUND,
                    isChecked).apply()
        }

        ledPower.setOnCheckedChangeListener { _, isChecked ->

            val intent: Intent? = Intent(
                    if (isChecked) {
                        BroadcastActions.INTENT_KEY_ONLINE_LED_ON
                    } else {
                        BroadcastActions.INTENT_KEY_ONLINE_LED_OFF
                    }
            )

            sendBroadcast(intent)

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_LED_SWITCH,
                    isChecked).apply()
        }

        ledDisconnectPower.setOnCheckedChangeListener { _, isChecked ->

            val intent: Intent? = Intent(
                    if (isChecked) {
                        BroadcastActions.INTENT_KEY_OFFLINE_LED_ON
                    } else {
                        BroadcastActions.INTENT_KEY_OFFLINE_LED_OFF
                    }
            )

            sendBroadcast(intent)

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_LED_SWITCH_OFFLINE,
                    isChecked).apply()
        }

        //20180227  CloudFun
        swCloudFunc.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                cgAllow3G.visibility = View.VISIBLE

                swCloud3GVal = MyApplication.getSharePreferenceCloudUpload3GStat()

                if(swCloud3GVal) {
                    swAllow3G.isChecked = swCloud3GVal
                }
            } else {
                cgAllow3G.visibility = View.GONE
            }

            MyApplication.setSharePreferenceCloudUploadStat(isChecked)
        }

        swAllow3G.setOnCheckedChangeListener { _, isChecked ->

            MyApplication.setSharePreferenceCloudUpload3GStat(isChecked)
        }

        swAllowPrivacy.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                DefaultPatternSettingActivity.startAction(this@SettingActivity)
            } else {
                DefaultPatternCheckingActivity.startAction(this@SettingActivity,
                        DefaultPatternCheckingActivity.START_ACTION_MODE_DISABLE)
            }
        }

        btnChangePassword.setOnClickListener {
            DefaultPatternCheckingActivity.startAction(this@SettingActivity,
                    DefaultPatternCheckingActivity.START_ACTION_MODE_CHANGE_PASSWOPRD)
        }

        tvocSeekValue.setOnClickListener {
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
                    tvocSeekBar.setValue(value.toFloat())
                    setSeekBarColor(tvocSeekBar, value.toFloat(), true)
                    setSeekBarValue(tvocSeekValue, value.toFloat())

                    mPreference!!.edit().putInt(SavePreferences.SETTING_TVOC_NOTIFY_VALUE, value.toInt()).apply()
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
                val value = editText.text.toString()

                if (value.isNotEmpty() && value.toInt() in 16..150) {
                    pm25SeekBar.setValue(value.toFloat())
                    setSeekBarColor(pm25SeekBar, value.toFloat(), false)
                    setSeekBarValue(pm25SeekValue, value.toFloat())

                    mPreference!!.edit().putInt(SavePreferences.SETTING_PM25_NOTIFY_VALUE, value.toInt()).apply()
                }
            })

            dialog.setNegativeButton(getString(android.R.string.cancel), null)
            dialog.show()
        }

        swAllowCloudNotify.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cgCloudNotify.visibility = View.VISIBLE
                cgCloudSeekbar.visibility = View.VISIBLE
            } else {
                cgCloudNotify.visibility = View.GONE
                cgCloudSeekbar.visibility = View.GONE
            }

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_CLOUD_NOTIFY, isChecked).apply()
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
                    setSeekBarColor(cloudTvocSeekBar, value.toFloat(), true)
                    setSeekBarValue(cloudTvocSeekValue, value.toFloat())
                    TvocNoseData.firebaseNotifTVOC = value.toInt()

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
                if(value.isNotEmpty() && value.toInt() in 16..150) {
                    cloudPM25SeekBar.setValue(value.toFloat())
                    setSeekBarColor( cloudPM25SeekBar, value.toFloat(), false)
                    setSeekBarValue( cloudPM25SeekValue, value.toFloat())
                    TvocNoseData.firebaseNotifPM25 = value.toInt()

                }
            })

            dialog.setNegativeButton(getString(android.R.string.cancel), null)
            dialog.show()
        }

        btnCloudNotify.setOnClickListener {
            numberPickerDialog()
        }

        btnSaveCloudSetting.setOnClickListener {
            updataSetting()
        }

        dataExport.setOnClickListener {
            val cal = Calendar.getInstance()
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(year, month, dayOfMonth)
                calObject.set(year, month, dayOfMonth)
                updateDateInView()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dpd.setMessage("請選擇日期")
            dpd.show()
        }

        // 2018/05/22 Depend on the device status, change the button name (Update or Fix) - start
        val share = getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE)
        val deviceName = share.getString("name", "")
        if (deviceName == "DfuTarg") {
            btnCheckFW?.text = getString(R.string.dfu_update_failure)
            btnCheckFW.setOnClickListener {
                EventBus.getDefault().post(BleEvent("Download Success"))
            }
            // 2018/05/22 Depend on the device status, change the button name (Update or Fix) - end
        } else {
            btnCheckFW.setOnClickListener {
                if (MyApplication.getDeviceChargeStatus()) {
                    val fwVer = MyApplication.getDeviceVersion()
                    val fwSerial = MyApplication.getDeviceSerial()
                    val fwType = MyApplication.getDeviceType()
                    //checkFwVersion("20$fwVer$fwSerial", "00$fwType")
                    checkFwVersion("20$fwVer$fwSerial", fwType)
                } else {
                    showNotChargingDialog()
                }
            }
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

    private fun getNotificationSettings() {
        swAllowNotifyVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)
        swMessageVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_MESSAGE, false)
        swViberateVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false)
        swSoundVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false)
        batSoundVal = mPreference!!.getBoolean(SavePreferences.SETTING_BATTERY_SOUND, false)

        tvocSeekBarVal = mPreference!!.getInt(SavePreferences.SETTING_TVOC_NOTIFY_VALUE, 660)
        pm25SeekBarVal = mPreference!!.getInt(SavePreferences.SETTING_PM25_NOTIFY_VALUE, 16)

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

        batSound.isChecked = batSoundVal

        tvocSeekBar.setValue(tvocSeekBarVal.toFloat())
        pm25SeekBar.setValue(pm25SeekBarVal.toFloat())

        tvocSeekValue.text = tvocSeekBarVal.toString()
        pm25SeekValue.text = pm25SeekBarVal.toString()

        setSeekBarColor(tvocSeekBar, tvocSeekBarVal.toFloat(), true)
        setSeekBarColor(pm25SeekBar, pm25SeekBarVal.toFloat(), false)
    }

    private fun getPrivacySettings() {
        isPrivacy = mPreference!!.getBoolean(SavePreferences.SETTING_MAP_PRIVACY, false)

        swAllowPrivacy.isChecked = isPrivacy

        if (isPrivacy) {
            cgPrivacy.visibility = View.VISIBLE
        } else {
            cgPrivacy.visibility = View.GONE
        }
    }

    private fun getCloudSettings() {
        swCloudVal = MyApplication.getSharePreferenceCloudUploadStat()
        swCloud3GVal = MyApplication.getSharePreferenceCloudUpload3GStat()

        swCloudFunc.isChecked = swCloudVal

        if(swCloudVal) {
            cgAllow3G.visibility = View.VISIBLE

            if(swCloud3GVal) {
                swAllow3G.isChecked = swCloud3GVal
            }
        } else {
            cgAllow3G.visibility = View.GONE
        }

        swAllowCloudNotify.isChecked = swCloudNotifyVal
    }

    private fun getDeviceLedSettings() {
        swLedPowerVal = mPreference!!.getBoolean(SavePreferences.SETTING_LED_SWITCH, true)
        swLedOffLinePowerVal = mPreference!!.getBoolean(SavePreferences.SETTING_LED_SWITCH_OFFLINE, true)

        ledPower.isChecked = swLedPowerVal
        ledDisconnectPower.isChecked = swLedOffLinePowerVal
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

    @Subscribe
    fun onEvent(bleEvent: BleEvent) {
        /* 處理事件 */
        Log.d("AirAction", bleEvent.message)
        when (bleEvent.message) {
            "version latest" -> {
                showFwLatestDialog()
            }
            "New FW Arrival " -> {
                showDownloadDialog(bleEvent.message!!)
            }
            "Download Success" -> {
                /*
                val intent = Intent()
                intent.putExtra("ADDRESS",show_Dev_address?.text.toString())
                intent.putExtra("DEVICE_NAME",show_Device_Name?.text.toString())
                intent.setClass(this, DFUActivity::class.java)
                startActivity(intent)*/
                val dfup = DFUProcessClass(this)
                val share = getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE)
                val mDeviceAddress = share.getString("mac", "noValue")
                if (mDeviceAddress != "noValue") {
                    dfup.DFUAction("", mDeviceAddress)
                }
            }
            "dfu complete" -> {
                showDfuCompleteDialog()
            }
        }
    }

    private fun checkFwVersion(Version: String, DeviceType: String) {
        //if (batValue > 100) {
        val aat = AirActionTask(this@SettingActivity, Version, DeviceType)
        val myResponse = aat.execute("postFWVersion")
        Log.v("AirActionTask", "OVER")
        //}
    }

    private fun showDownloadDialog(msg: String) {
        val Dialog = android.app.AlertDialog.Builder(this).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        Dialog.setTitle(getString(R.string.remind))
        Dialog.setMessage("$msg\t請確定裝置與電源連接正常，手機儘量接近裝置，以利FW更新。")
        Dialog.setCancelable(false)//讓返回鍵與空白無效
        //Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")

        Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.Reject))//否
        { dialog, _ ->
            dialog.dismiss()
        }
        Dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Accept))//是
        { dialog, _ ->

            dialog.dismiss()

            val fwVer = ""

            val aat = AirActionTask(this@SettingActivity)
            aat.execute("downloadFWFile")
        }
        Dialog.show()
    }

    private fun showFwLatestDialog() {
        val Dialog = android.app.AlertDialog.Builder(this).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        Dialog.setTitle(getString(R.string.remind))
        Dialog.setMessage("您的Mobile Nose已是最新版本。")
        Dialog.setCancelable(false)//讓返回鍵與空白無效
        Dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Accept))//是
        { dialog, _ ->

            dialog.dismiss()
        }
        Dialog.show()
    }

    private fun showNotChargingDialog() {
        val Dialog = android.app.AlertDialog.Builder(this).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        Dialog.setTitle(getString(R.string.remind))
        Dialog.setMessage("請將您的Mobile Nose插上充電線。")
        Dialog.setCancelable(false)//讓返回鍵與空白無效
        Dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Accept))//是
        { dialog, _ ->

            dialog.dismiss()
        }
        Dialog.show()
    }

    private fun showDfuCompleteDialog() {
        val Dialog = android.app.AlertDialog.Builder(this).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        Dialog.setTitle(getString(R.string.remind))
        Dialog.setMessage("Mobile Nose已更新完成，請將您的Mobile Nose重新開機。\n按下Yes將返回到主畫面。")
        Dialog.setCancelable(false)//讓返回鍵與空白無效
        Dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Accept))//是
        { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        Dialog.show()
    }

    //2018515 by 白~~~~~~~~~~~~~~~~告

    @SuppressLint("SetTextI18n")
    private fun getFirebaseNotifSettings() {

        swCloudNotifyVal = mPreference!!.getBoolean(SavePreferences.SETTING_CLOUD_NOTIFY, true)
        if (swCloudNotifyVal) {
            cgCloudNotify.visibility = View.VISIBLE
            cgCloudSeekbar.visibility = View.VISIBLE
        } else {
            cgCloudNotify.visibility = View.GONE
            cgCloudSeekbar.visibility = View.GONE
        }

        if (TvocNoseData.firebaseNotiftime < 10) {
            btnCloudNotify.text = "0${TvocNoseData.firebaseNotiftime}:00"
        } else {
            btnCloudNotify.text = "${TvocNoseData.firebaseNotiftime}:00"
        }
        cloudTvocSeekValue.text = TvocNoseData.firebaseNotifTVOC.toString()
        cloudTvocSeekBar.setValue(TvocNoseData.firebaseNotifTVOC.toFloat())
        cloudPM25SeekValue.text = TvocNoseData.firebaseNotifPM25.toString()
        cloudPM25SeekBar.setValue(TvocNoseData.firebaseNotifPM25.toFloat())

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
                        btnCloudNotify.text = "0$cloudTime:00"
                    } else {
                        btnCloudNotify.text = "$cloudTime:00"
                    }
                    Log.e("TvocNoseData", TvocNoseData.firebaseNotiftime.toString())
                }.setTitle(getString(R.string.text_cloud_notify_time))

        alertBuilder.show()
    }

    private fun updataSetting() {
        val shareToken = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        val myToken = shareToken.getString("token", "")
        TvocNoseData.firebaseNotiftime = cloudTime
        TvocNoseData.firebaseNotifTVOC = cloudTVOC
        TvocNoseData.firebaseNotifPM25 = cloudPM25
        FirebaseNotifTask().execute(myToken, TvocNoseData.firebaseNotiftime.toString(), TvocNoseData.firebaseNotifPM25.toString(), TvocNoseData.firebaseNotifTVOC.toString())

    }


    private fun updateDateInView() {
        dbData2CVSAsyncTasks()//sdf)
        fileProvider()
    }

    private fun dbData2CVSAsyncTasks() {//TS: TvocNoseData) {
        try {
            val allData = getDbData(Date().day, Date().day)//Date().day, Date().day)
            writeDataToFile(allData, this@SettingActivity)
        } catch (e: Exception) {
            Log.e("return_body_erro", e.toString())
        }
    }

    private fun fileProvider() {
        var mSDFile: File? = null
        //mSDFile = this.getFilesDir()
        mSDFile = this@SettingActivity.getFileStreamPath("BLE_Data.csv")
        val uri = FileProvider.getUriForFile(this, packageName, mSDFile)

        Log.e("#抓取檔案路徑為:", uri.path + "#packageName=" + packageName + "#mSDFile=" + mSDFile)
        val intent = Intent(Intent.ACTION_SEND)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        intent.data = uri
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.type = "csv/plain"
        intent.type = "Application/csv"
        val chooser = Intent.createChooser(intent, title)

        //給目錄臨時的權限
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        // Verify the intent will resolve to at least one activity
        if (intent.resolveActivity(packageManager) != null) {
            startActivityForResult(chooser, 0)
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDbData(startTimeZone: Int, EntTime: Int): ArrayList<String> {
        val dataArrayListOnee = ArrayList<String>()
        val touchTime = if (calObject.get(Calendar.HOUR) >= 8) calObject.timeInMillis else calObject.timeInMillis + calObject.timeZone.rawOffset
        //val touchTime = calObject.timeInMillis// + calObject.timeZone.rawOffset
        val endDay = touchTime / (3600000 * 24) * (3600000 * 24) - calObject.timeZone.rawOffset
        val endDayLast = endDay + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        //一天共有2880筆
        val dataCount = (endDayLast - endDay) / (60 * 1000)
        query.between("Created_time", endDay, endDayLast).sort("Created_time", Sort.ASCENDING)
        val result1 = query.findAll()
        Log.e("資料筆數", result1.size.toString())
        Log.e("所有資料筆數", result1.toString())
        try {
            if (result1.size > 0) {
                val dateLabelFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                for (i in result1.indices) {
                    Log.i("text", "i=$i\n")
                    dataArrayListOnee.add(result1[i]?.tempValue.toString() + ",")
                    dataArrayListOnee.add(result1[i]?.humiValue.toString() + ",")
                    dataArrayListOnee.add(result1[i]?.tvocValue.toString() + ",")
                    dataArrayListOnee.add(result1[i]?.ecO2Value.toString() + ",")
                    dataArrayListOnee.add(result1[i]?.pM25Value.toString() + ",")
                    dataArrayListOnee.add(result1[i]?.longitude!!.toString() + ",")
                    dataArrayListOnee.add(result1[i]?.latitude!!.toString() + ",")
                    val date = dateLabelFormat.format(result1[i]?.created_time!!.toLong())
                    dataArrayListOnee.add(date + "\r\n")
                }
            } else {
                Log.e("未上傳資料筆數", result1.size.toString())
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        realm.close()
        return dataArrayListOnee
    }

    private fun writeDataToFile(data: ArrayList<String>, context: Context) {
        try {
            var mSDFile: File? = null
            //檢查有沒有SD卡裝置
            if (Environment.getExternalStorageState() == Environment.MEDIA_REMOVED) {
                Toast.makeText(applicationContext, "沒有SD卡!!!", Toast.LENGTH_SHORT).show()
                return
            } else {
                //取得SD卡儲存路徑
                mSDFile = Environment.getExternalStorageDirectory()
                mSDFile = context.getFileStreamPath("BLE_Data.csv")
                mSDFile.delete()
            }
            val mFileWriter = FileWriter(mSDFile!!, true)
            mFileWriter.write("tempValue,humiValue,tvocValue,ecO2Value,pM25Value,longitude,latitude,created_time \r\n")
            for (l in 0..data.size) {
                mFileWriter.write(data[l])//data[l])
                mFileWriter.flush()
            }
            Log.e("全給我進去!!", data.last())
            mFileWriter.close()
            Log.e("Excel檔完成!!路徑為:", mSDFile.path)
        } catch (e: IOException) {
            Log.e("Exception", "File write failed: " + e.toString())
        }
    }
}
