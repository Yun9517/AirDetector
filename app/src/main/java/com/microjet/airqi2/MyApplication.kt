package com.microjet.airqi2

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import com.microjet.airqi2.BroadReceiver.PrimaryReceiver
import com.microjet.airqi2.Definition.BroadcastIntents
import android.net.ConnectivityManager


import android.os.Build
import android.os.Handler
import com.microjet.airqi2.Definition.SavePreferences
import java.util.*
import android.app.Activity
import android.content.SharedPreferences
import android.content.res.Resources


/**
 * Created by chang on 2017/12/9.
 */
//新增研發線
class MyApplication : Application() {

    var mPrimaryReceiver: PrimaryReceiver? = null

    init {
        instance = this
    }

    companion object {
        private var instance: MyApplication? = null
        private var deviceVer: String = ""
        private var deviceSerial: String = ""
        private var deviceType: String = ""
        private var deviceChargeStatus: Boolean = false
        //var isPM25: String = "000000000000"

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }

        fun getConnectStatus(): String {
            val CM = instance!!.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            val netInfo = CM!!.activeNetworkInfo

            return if (netInfo != null && netInfo.isConnected) {
                netInfo.typeName
            } else {
                "DISCONNECT"
            }
        }

        fun applicationResText(id: Int): String {
            return instance!!.getString(id)
        }


        /**
         * Return pseudo unique ID
         * @return ID
         */
        fun getPsuedoUniqueID(): String {
            // If all else fails, if the user does have lower than API 9 (lower
            // than Gingerbread), has reset their phone or 'Secure.ANDROID_ID'
            // returns 'null', then simply the ID returned will be solely based
            // off their Android device information. This is where the collisions
            // can happen.
            // Thanks http://www.pocketmagic.net/?p=1662!
            // Try not to use DISPLAY, HOST or ID - these items could change.
            // If there are collisions, there will be overlapping data
            val m_szDevIDShort = ("35" +
                    Build.BOARD.length % 10
                    + Build.BRAND.length % 10
                    + Build.CPU_ABI.length % 10
                    + Build.DEVICE.length % 10
                    + Build.MANUFACTURER.length % 10
                    + Build.MODEL.length % 10
                    + Build.PRODUCT.length % 10)

            // Thanks to @Roman SL!
            // http://stackoverflow.com/a/4789483/950427
            // Only devices with API >= 9 have android.os.Build.SERIAL
            // http://developer.android.com/reference/android/os/Build.html#SERIAL
            // If a user upgrades software or roots their phone, there will be a duplicate entry
            var serial: String? = null
            try {
                serial = android.os.Build::class.java.getField("SERIAL").get(null).toString()

                // Go ahead and return the serial for api => 9
                return UUID(m_szDevIDShort.hashCode().toLong(), serial.hashCode().toLong()).toString()
            } catch (e: Exception) {
                // String needs to be initialized
                serial = "serial" // some value
            }

            // Thanks @Joe!
            // http://stackoverflow.com/a/2853253/950427
            // Finally, combine the values we have found by using the UUID class to create a unique identifier
            return UUID(m_szDevIDShort.hashCode().toLong(), serial!!.hashCode().toLong()).toString()
        }

        fun getSharePreferenceMAC(): String {
            val share = applicationContext().getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE)
            return share.getString("mac", "11:22:33:44:55:66")
        }

        fun putDeviceVersion(value: String) {
            deviceVer = value
        }

        fun getDeviceVersion(): String {
            return deviceVer
        }

        fun putDeviceSerial(value: String) {
            deviceSerial = value
        }

        fun getDeviceSerial(): String {
            return deviceSerial
        }

        fun putDeviceType(value: String) {
            deviceType = value
        }

        fun getDeviceType(): String {
            return deviceType
        }

        fun putDeviceChargeStatus(value: Boolean) {
            deviceChargeStatus = value
        }

        fun getDeviceChargeStatus(): Boolean {
            return deviceChargeStatus
        }

        fun getSharePreferenceManualDisconn(): Boolean {
            val share = applicationContext().getSharedPreferences(SavePreferences.SETTING_KEY, Context.MODE_PRIVATE)
            return share.getBoolean(SavePreferences.SETTING_MANUAL_DISCONNECT, false)
        }

        fun setSharePreferenceManualDisconn(value: Boolean) {
            val share = applicationContext().getSharedPreferences(SavePreferences.SETTING_KEY, Context.MODE_PRIVATE)
            share.edit().putBoolean(SavePreferences.SETTING_MANUAL_DISCONNECT, value).apply()
        }

        // ****** 2018/04/17 Identify the App is first time initial or not ************//
        fun saveIsFirstUsed() {
            val share = applicationContext().getSharedPreferences("GetSharedPreferences", Activity.MODE_PRIVATE)
            share.edit().putBoolean("isFirstUsed", false).apply()
        }

        fun getIsFirstUsed(): Boolean {
            val share = applicationContext().getSharedPreferences("GetSharedPreferences", Activity.MODE_PRIVATE)
            return share.getBoolean("isFirstUsed", true)
        }

        fun getSharePreferenceMapPanelStat(): Boolean {
            val share = applicationContext().getSharedPreferences(SavePreferences.SETTING_KEY, Context.MODE_PRIVATE)
            return share.getBoolean(SavePreferences.SETTING_MAP_PANEL_STATUS, false)
        }

        fun setSharePreferenceMapPanelStat(value: Boolean) {
            val share = applicationContext().getSharedPreferences(SavePreferences.SETTING_KEY, Context.MODE_PRIVATE)
            share.edit().putBoolean(SavePreferences.SETTING_MAP_PANEL_STATUS, value).apply()
        }
    }

    override fun onCreate() {
        super.onCreate()
        val context: Context = MyApplication.applicationContext()

        // The default Realm file is "default.realm" in Context.getFilesDir();
        // we'll change it to "myrealm.realm"
        Realm.init(this)
        //val config = RealmConfiguration.Builder().name("myrealm.realm").build()
        val config = RealmConfiguration.Builder().name("myrealm.realm").schemaVersion(2).migration(RealmMigrations()).build()
        Log.d("REALMAPP", config.schemaVersion.toString())
        Log.d("REALMAPP", RealmConfiguration.Builder().name("myrealm.realm").build().path.toString())
        Log.d("REALMAPP", RealmConfiguration.Builder().name("myrealm.realm").build().realmDirectory.toString())

        Realm.setDefaultConfiguration(config)

        /*
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java).sort("Created_time").findAll()
        Log.d("REALMAPP", query.toString())
        var createdTime = 0L
        val idArr = arrayListOf<Int>()
        query?.forEachIndexed { index, asmDataModel ->
            Log.d("REALMAPP", index.toString())
            if (asmDataModel.created_time == createdTime) {
                idArr.add(asmDataModel.dataId)
            }
            createdTime = asmDataModel!!.created_time
        }
        for (i in idArr) {
            val realm1 = Realm.getDefaultInstance()
            val query1 = realm1.where(AsmDataModel::class.java).equalTo("id", i).findAll()
            Log.d("REALMAPPDUP", query1.toString())
            realm1.executeTransaction {
                query1.deleteAllFromRealm()
            }
        }
        realm.close()
        */
        mPrimaryReceiver = PrimaryReceiver()
        val filter = IntentFilter(BroadcastIntents.PRIMARY)
        this.registerReceiver(mPrimaryReceiver, filter)
    }
}
