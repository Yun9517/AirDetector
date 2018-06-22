package com.microjet.airqi2

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
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
import com.microjet.airqi2.CustomAPI.CSVWriter
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.Definition.Colors
import com.microjet.airqi2.GestureLock.DefaultPatternCheckingActivity
import com.microjet.airqi2.GestureLock.DefaultPatternSettingActivity
import com.microjet.airqi2.TvocNoseData.calObject
import com.microjet.airqi2.URL.AirActionTask
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_setting.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by B00174 on 2017/11/29.
 *
 */
class SettingActivity : AppCompatActivity() {

    private var swAllowNotifyVal: Boolean = false
    private var swMessageVal: Boolean = false
    private var swViberateVal: Boolean = false
    private var swSoundVal: Boolean = false
    //20180130
    private var batSoundVal: Boolean = false
    private var swLedPowerVal: Boolean = true
    private var swLedOffLinePowerVal: Boolean = true

    //20180227
    private var swCloudVal: Boolean = false
    private var swCloud3GVal: Boolean = false

    private var tvocSeekBarVal: Int = 660
    private var pm25SeekBarVal: Int = 16

    private var isPrivacy: Boolean = false

    //20180515
    private var swCloudNotifyVal: Boolean = false

    //20180517
    private var cloudTime: Int = TvocNoseData.firebaseNotiftime
    private var cloudPM25: Int = TvocNoseData.firebaseNotifPM25     //停留本頁暫存用變數
    private var cloudTVOC: Int = TvocNoseData.firebaseNotifTVOC    //停留本頁暫存用變數

    private lateinit var myPref: PrefObjects

    private lateinit var realm: Realm
    private lateinit var result: RealmResults<AsmDataModel>
    private lateinit var listener: RealmChangeListener<RealmResults<AsmDataModel>>
    private lateinit var filter: List<AsmDataModel>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        myPref = PrefObjects(this)

        readPreferences()   // 載入設定值
        uiSetListener()
        initActionBar()
        //20180516 by 白~~~~~~~~~~~~~~~~~~~告
        setFCMSettingView()

        if (intent.getBooleanExtra("CONN", false)) {
            cgDeviceControl.visibility = View.VISIBLE
        } else {
            cgDeviceControl.visibility = View.GONE
        }

        // 2018/05/22 Depend on the device status, change the button name (Update or Fix) - Part one
        val deviceName = myPref.getSharePreferenceName()
        if (deviceName == "DfuTarg") {
            btnCheckFW?.text = getString(R.string.dfu_update_failure)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()

        //text_device_ver.text = String.format(resources.getString(R.string.text_label_device_version), MyApplication.getDeviceVersion())
        text_device_ver_detail.text = String.format(resources.getString(R.string.text_label_device_version_detail), MyApplication.getDeviceVersion(), " v", MyApplication.getDeviceSerial())
        text_app_ver.text = String.format(resources.getString(R.string.show_app_version), BuildConfig.VERSION_NAME)

        getPrivacySettings()

        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    private fun readPreferences() {
        getNotificationSettings()
        getCloudSettings()
        getPrivacySettings()
        getDeviceLedSettings()
        getFCMSettings()
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


        //20180206
        batSound.setOnCheckedChangeListener { _, isChecked ->

            myPref.setSharePreferenceAllowNotifyLowBattery(isChecked)
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

            myPref.setSharePreferenceLedOn(isChecked)
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

            myPref.setSharePreferenceDisconnectLedOn(isChecked)
        }

        //20180227  CloudFun
        swCloudFunc.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                cgAllow3G.visibility = View.VISIBLE

                showEnable3GDialog()

                swCloud3GVal = myPref.getSharePreferenceCloudUpload3GStat()

                /*if(swCloud3GVal) {
                    swAllow3G.isChecked = swCloud3GVal
                }*/
            } else {
                cgAllow3G.visibility = View.GONE
            }

            myPref.setSharePreferenceCloudUploadStat(isChecked)
        }

        swAllow3G.setOnCheckedChangeListener { _, isChecked ->

            myPref.setSharePreferenceCloudUpload3GStat(isChecked)
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

                    myPref.setSharePreferenceAllowNotifyTvocValue(value.toInt())
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

                    myPref.setSharePreferenceAllowNotifyPM25Value(value.toInt())
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
                updateCloudSetting(25, 35, 660)
            }

            myPref.setSharePreferenceFirebase(isChecked)
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

        dataExport.setOnClickListener {
            val cal = Calendar.getInstance()
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(year, month, dayOfMonth)
                calObject.set(year, month, dayOfMonth)
                checkPermissions()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dpd.setMessage("請選擇日期")
            dpd.show()
        }

        // 2018/05/22 Depend on the device status, change the button name (Update or Fix) - Part two
        btnCheckFW.setOnClickListener {
            if (btnCheckFW.text == getString(R.string.dfu_update_failure)) {
                EventBus.getDefault().post(BleEvent("Download Success"))
            } else {
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
        swAllowNotifyVal = myPref.getSharePreferenceAllowNotify()
        swMessageVal = myPref.getSharePreferenceAllowNotifyMessage()
        swViberateVal = myPref.getSharePreferenceAllowNotifyVibrate()
        swSoundVal = myPref.getSharePreferenceAllowNotifySound()
        batSoundVal = myPref.getSharePreferenceAllowNotifyLowBattery()

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
        isPrivacy = myPref.getSharePreferencePrivacy()

        swAllowPrivacy.isChecked = isPrivacy

        if (isPrivacy) {
            cgPrivacy.visibility = View.VISIBLE
        } else {
            cgPrivacy.visibility = View.GONE
        }
    }

    private fun getCloudSettings() {
        swCloudVal = myPref.getSharePreferenceCloudUploadStat()
        swCloud3GVal = myPref.getSharePreferenceCloudUpload3GStat()

        swCloudFunc.isChecked = swCloudVal

        if (swCloudVal) {
            cgAllow3G.visibility = View.VISIBLE

            if (swCloud3GVal) {
                swAllow3G.isChecked = swCloud3GVal
            }
        } else {
            cgAllow3G.visibility = View.GONE
        }

    }

    private fun getFCMSettings() {
        swCloudNotifyVal = myPref.getSharePreferenceFirebase()
        swAllowCloudNotify?.isChecked = swCloudNotifyVal
        if (swCloudNotifyVal) {
            cgCloudNotify.visibility = View.VISIBLE
            cgCloudSeekbar.visibility = View.VISIBLE
        } else {
            cgCloudNotify.visibility = View.GONE
            cgCloudSeekbar.visibility = View.GONE
        }
    }

    private fun getDeviceLedSettings() {
        swLedPowerVal = myPref.getSharePreferenceLedOn()
        swLedOffLinePowerVal = myPref.getSharePreferenceDisconnectLedOn()

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
                val mDeviceAddress = myPref.getSharePreferenceMAC()
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
        /*val myResponse = */aat.execute("postFWVersion")
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

            //val fwVer = ""

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

    // 2018/05/30 show enable 3G dialog
    private fun showEnable3GDialog() {
        val Dialog = android.app.AlertDialog.Builder(this).create()
        Dialog.setTitle(getString(R.string.allow_3G))
        Dialog.setMessage(getString(R.string.text_Enable3GDialog))
        Dialog.setCancelable(false)//讓返回鍵與空白無效
        //Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")

        Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.text_close))//否
        { dialog, _ ->
            swAllow3G.isChecked = false
            dialog.dismiss()
        }
        Dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.text_open))//是
        { dialog, _ ->
            swAllow3G.isChecked = true
            dialog.dismiss()
        }
        Dialog.show()
    }

    //2018515 by 白~~~~~~~~~~~~~~~~告

    @SuppressLint("SetTextI18n")
    private fun setFCMSettingView() {
        when (TvocNoseData.firebaseNotiftime) {
            in 0..9 -> {
                btnCloudNotify.text = "0${TvocNoseData.firebaseNotiftime}:00"
            }
            25 -> {
                btnCloudNotify.text = "00:00"
            }
            else -> {
                btnCloudNotify.text = "${TvocNoseData.firebaseNotiftime}:00"
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
                        btnCloudNotify.text = "0$cloudTime:00"
                    } else {
                        btnCloudNotify.text = "$cloudTime:00"
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
        FirebaseNotifSettingTask().execute(myToken, argTime.toString(), argPm25.toString(), argTvoc.toString())
        setFCMSettingView()
    }

    // 查詢資料庫
    private fun runRealmQueryData() {
        realm = Realm.getDefaultInstance()

        //現在時間實體毫秒
        val touchTime = if (calObject.get(Calendar.HOUR_OF_DAY) >= 8) calObject.timeInMillis else calObject.timeInMillis + calObject.timeZone.rawOffset
        //將日期設為今天日子加一天減1秒
        val startTime = touchTime / (3600000 * 24) * (3600000 * 24) - calObject.timeZone.rawOffset
        val endTime = startTime + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)

        listener = RealmChangeListener {
            filter = it.filter { it.macAddress == myPref.getSharePreferenceMAC() }

            parseDataToCsv(filter)
            Log.e("Realm Listener", "Update Database...")
        }

        result = realm.where(AsmDataModel::class.java)
                .between("Created_time", startTime, endTime)
                .sort("Created_time", Sort.ASCENDING).findAllAsync()

        result.addChangeListener(listener)
    }

    @SuppressLint("SimpleDateFormat")
    private fun parseDataToCsv(results: List<AsmDataModel>) {
        if (results.isNotEmpty()) {
            val foldeName = "ADDWII Mobile Nose"
            val date = SimpleDateFormat("yyyyMMdd")
            val fileName = "${date.format(calObject.timeInMillis)}_Mobile_Nose"

            val writeCSV = CSVWriter(foldeName, fileName, CSVWriter.COMMA_SEPARATOR)

            val timeFormat = SimpleDateFormat("HH:mm")

            val header = arrayOf("id", "Time", "TVOC", "eCO2", "Temperature", "Humidity", "PM2.5")

            writeCSV.writeLine(header)

            for (i in 0 until results.size) {
                val time = results[i].created_time
                val tvocVal = if (results[i].tvocValue == "65538") "No Data" else "${results[i].tvocValue} ppb"
                val eco2Val = if (results[i].ecO2Value == "65538") "No Data" else "${results[i].ecO2Value} ppm"
                val tempVal = if (results[i].tempValue == "65538") "No Data" else "${results[i].tempValue} °C"
                val humiVal = if (results[i].humiValue == "65538") "No Data" else "${results[i].humiValue} %"
                val pm25Val = if (results[i].pM25Value == "65538") "No Data" else "${results[i].pM25Value} μg/m³"

                val textCSV = arrayOf((i + 1).toString(), timeFormat.format(time), tvocVal, eco2Val, tempVal, humiVal, pm25Val)

                writeCSV.writeLine(textCSV).toString()
            }

            writeCSV.close()
            result.removeAllChangeListeners()

            Utils.toastMakeTextAndShow(this@SettingActivity, getString(R.string.text_export_success_msg), Toast.LENGTH_SHORT)
        }
    }

    private fun checkPermissions() {

        if (ActivityCompat.checkSelfPermission(this@SettingActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this@SettingActivity,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 2)
        } else {
            Log.e("ChectPerm", "Permission Granted. Starting export data...")
            runRealmQueryData()
        }
    }
}
