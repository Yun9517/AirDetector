package com.microjet.airqi2

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.Definition.SavePreferences
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_setting.*

/**
 * Created by B00174 on 2017/11/29.
 *
 */
class SettingActivity : AppCompatActivity() {

    private var mPreference: SharedPreferences? = null

    private var spCycleVal: Int = 0
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        uiSetListener()

        mPreference = getSharedPreferences(SavePreferences.SETTING_KEY, 0)
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

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        readPreferences()   // 當Activity onResume時載入設定值

        text_local_uuid.text = MyApplication.getPsuedoUniqueID()
        text_device_ver.text = resources.getString(R.string.text_label_device_version) + MyApplication.getDeviceVersion()
    }

    private fun readPreferences() {
        swAllowNotifyVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)
        swMessageVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_MESSAGE, false)
        swViberateVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false)
        swSoundVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false)
        swRunInBgVal = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_RUN_IN_BG, false)
        swTotalNotifyVal = mPreference!!.getBoolean(SavePreferences.SETTING_TOTAL_POLLUTION_NOTIFY, false)
        //20180130
        //swPumpVal = mPreference!!.getBoolean(SavePreferences.SETTING_PUMP_MUNUAL, false)
        //20180206
        batSoundVal = mPreference!!.getBoolean(SavePreferences.SETTING_BATTERY_SOUND, false)

        swLedPowerVal = mPreference!!.getBoolean(SavePreferences.SETTING_LED_SWITCH, true)

        //20180227
        swCloudVal = mPreference!!.getBoolean(SavePreferences.SETTING_CLOUD_FUN, true)


        swAllowNotify.isChecked = swAllowNotifyVal

        if (swAllowNotify.isChecked) {
            cgMessage.visibility = View.VISIBLE
            cgVibration.visibility = View.VISIBLE
            cgSound.visibility = View.VISIBLE
            cgLowBatt.visibility = View.VISIBLE
        } else {
            cgMessage.visibility = View.GONE
            cgVibration.visibility = View.GONE
            cgSound.visibility = View.GONE
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

        swCloudFun.isChecked = swCloudVal

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

        swAllowNotify.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                cgMessage.visibility = View.VISIBLE
                cgVibration.visibility = View.VISIBLE
                cgSound.visibility = View.VISIBLE
                cgLowBatt.visibility = View.VISIBLE
            } else {
                cgMessage.visibility = View.GONE
                cgVibration.visibility = View.GONE
                cgSound.visibility = View.GONE
                cgLowBatt.visibility = View.GONE

                mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_MESSAGE,
                        isChecked).apply()

                mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_VIBERATION,
                        isChecked).apply()

                mPreference!!.edit().putBoolean(SavePreferences.SETTING_ALLOW_SOUND,
                        isChecked).apply()

                mPreference!!.edit().putBoolean(SavePreferences.SETTING_BATTERY_SOUND,
                        isChecked).apply()
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
                        BroadcastActions.INTENT_KEY_LED_ON
                    } else {
                        BroadcastActions.INTENT_KEY_LED_OFF
                    }
            )

            sendBroadcast(intent)

            mPreference!!.edit().putBoolean(SavePreferences.SETTING_LED_SWITCH,
                    isChecked).apply()
        }

        //20180227  CloudFun
        swCloudFun.setOnCheckedChangeListener { _, isChecked ->

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

    private fun updateData() {
        //拉取資料加上傳搞定
        val realm = Realm.getDefaultInstance()
        val result = realm.where(AsmDataModel::class.java).equalTo("UpLoaded", "0").findFirst()
        Log.d("SETTCLOUD", result.toString())

        //對資料庫做操作的方法
        /*
        realm.executeTransactionAsync {
            val realm1 = Realm.getDefaultInstance()
            for (i in 601..1000) {
                val dataId = i
                val user = realm1.where(AsmDataModel::class.java).equalTo("id", dataId).findFirst()
                user!!.upLoaded = "1"
            }
        }
        realm.executeTransactionAsync {
            val realm1 = Realm.getDefaultInstance()
            val dataId = 1453
            val user = realm1.where(AsmDataModel::class.java).equalTo("id", dataId).findFirst()
            user?.deleteFromRealm()
        }
        realm.executeTransactionAsync {
            val realm1 = Realm.getDefaultInstance()
            val num = realm1.where(AsmDataModel::class.java).max("id")
            val nextID: Int
            if (num == null) {
                nextID = 1
            } else {
                nextID = num.toInt() + 1
            }
            Log.d("REALMAPPID",nextID.toString())
            val user = realm1.createObject(AsmDataModel::class.java,nextID)
            user.tempValue = "400"
            user.humiValue = "400"
            user.tvocValue = "800"
            user.ecO2Value = "400"
            user.pM25Value = "400"
            user.created_time = 1520429700000
        }
        */


        val result1 = realm.where(AsmDataModel::class.java).equalTo("UpLoaded", "0").findFirst()
        Log.d("SETTCLOUD", result1.toString())

        //val result2 = realm.where(AsmDataModel::class.java).equalTo("Created_time",1520332440000).findAll()
        val result2 = realm.where(AsmDataModel::class.java).between("Created_time", 1520424060000, 1520424060000).findAll()

        Log.d("SETTCLOUD", result2.toString())

    }

    private fun getLocation() {
        /*
        // checkGPSPermisstion()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        //val locationListener = MyLocationListener()
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, 5000, 10f, locationListener)
        }
        */
    }

    private fun checkGPSPermisstion() {
        val permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        Log.d("UARTPER", permission.toString())
        val permission1 = PackageManager.PERMISSION_GRANTED
        Log.d("UARTPER", permission1.toString())
    }

}
