package com.microjet.airqi2.settingPage

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.microjet.airqi2.*
import com.microjet.airqi2.BlueTooth.DFU.DFUProcessClass
import com.microjet.airqi2.CustomAPI.CSVWriter
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.TvocNoseData.calObject
import com.microjet.airqi2.URL.AirActionTask
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_setting.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by B00174 on 2017/11/29.
 *
 */

class SettingActivity : AppCompatActivity() {

    private var swLedPowerVal: Boolean = true
    private var swLedOffLinePowerVal: Boolean = true

    private var isRunInForeground: Boolean = false

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

        if (intent.getBooleanExtra("CONN", false)) {
            deviceControl1.visibility = View.VISIBLE
            deviceControl2.visibility = View.VISIBLE
            deviceControl3.visibility = View.VISIBLE

            mainDivider6.visibility = View.VISIBLE
            mainDivider7.visibility = View.VISIBLE
            subDivider3.visibility = View.VISIBLE
            subDivider4.visibility = View.VISIBLE
            groupDivider4.visibility = View.VISIBLE
        } else {
            deviceControl1.visibility = View.GONE
            deviceControl2.visibility = View.GONE
            deviceControl3.visibility = View.GONE

            mainDivider6.visibility = View.GONE
            mainDivider7.visibility = View.GONE
            subDivider3.visibility = View.GONE
            subDivider4.visibility = View.GONE
            groupDivider4.visibility = View.GONE
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

        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    private fun readPreferences() {
        getDeviceLedSettings()
        getServiceSetting()
    }

    private fun uiSetListener() {
        itemPrivacy.setOnClickListener {
            val intent = Intent(this@SettingActivity, PrivacySettingActivity::class.java)
            startActivity(intent)
        }

        itemDeviceNotify.setOnClickListener {
            val intent = Intent(this@SettingActivity, DeviceNotifySettingActivity::class.java)
            startActivity(intent)
        }

        itemCloudNotify.setOnClickListener {
            val intent = Intent(this@SettingActivity, CloudNotifySettingActivity::class.java)
            startActivity(intent)
        }

        itemCloudSync.setOnClickListener {
            val intent = Intent(this@SettingActivity, CloudSyncSettingActivity::class.java)
            startActivity(intent)
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

        dataExport.setOnClickListener {
            val cal = Calendar.getInstance()
            val dpd = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                cal.set(year, month, dayOfMonth)
                calObject.set(year, month, dayOfMonth)
                checkPermissions()
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            dpd.setMessage(getString(R.string.select_Date)) //請選擇日期
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

        swAllowServiceForeground.setOnCheckedChangeListener { _, isChecked ->
            myPref.setSharePreferenceServiceForeground(isChecked)
        }
    }

    private fun getDeviceLedSettings() {
        swLedPowerVal = myPref.getSharePreferenceLedOn()
        swLedOffLinePowerVal = myPref.getSharePreferenceDisconnectLedOn()

        ledPower.isChecked = swLedPowerVal
        ledDisconnectPower.isChecked = swLedOffLinePowerVal
    }

    private fun getServiceSetting() {
        isRunInForeground = myPref.getSharePreferenceServiceForeground()

        swAllowServiceForeground.isChecked = isRunInForeground
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
                showDownloadDialog()
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

    private fun showDownloadDialog() {
        val dlg = android.app.AlertDialog.Builder(this).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        dlg.setTitle(getString(R.string.new_FW_Arrival_Dialog_Title))
        dlg.setMessage(getString(R.string.new_FW_Arrival_Dialog))
        dlg.setCancelable(false)//讓返回鍵與空白無效
        //Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")

        dlg.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.new_FW_Arrival_Dialog_Button_Later))//否
        { dialog, _ ->
            dialog.dismiss()
        }
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.new_FW_Arrival_Dialog_Button_Update))//是
        { dialog, _ ->

            dialog.dismiss()

            //val fwVer = ""

            val aat = AirActionTask(this@SettingActivity)
            aat.execute("downloadFWFile")
        }
        dlg.show()
    }

    private fun showFwLatestDialog() {
        val dlg = android.app.AlertDialog.Builder(this).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        dlg.setTitle(getString(R.string.remind))
        //Dialog.setMessage("您的Mobile Nose已是最新版本。")
        dlg.setMessage(getString(R.string.new_FW_Arrival_Dialog_Last_Version))
        dlg.setCancelable(false)//讓返回鍵與空白無效
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Accept))//是
        { dialog, _ ->

            dialog.dismiss()
        }
        dlg.show()
    }

    private fun showNotChargingDialog() {
        val dlg = android.app.AlertDialog.Builder(this).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        dlg.setTitle(getString(R.string.remind))
        //Dialog.setMessage("請將您的Mobile Nose插上充電線。")
        dlg.setMessage(getString(R.string.new_FW_Arrival_Dialog_Charge))
        dlg.setCancelable(false)//讓返回鍵與空白無效
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Accept))//是
        { dialog, _ ->

            dialog.dismiss()
        }
        dlg.show()
    }

    private fun showDfuCompleteDialog() {
        val dlg = android.app.AlertDialog.Builder(this).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        dlg.setTitle(getString(R.string.remind))
        //Dialog.setMessage("Mobile Nose已更新完成，請將您的Mobile Nose重新開機。\n按下Yes將返回到主畫面。")
        dlg.setMessage(getString(R.string.new_FW_Arrival_Dialog_Update_Done))
        dlg.setCancelable(false)//讓返回鍵與空白無效
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Accept))//是
        { dialog, _ ->
            dialog.dismiss()
            finish()
        }
        dlg.show()
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
