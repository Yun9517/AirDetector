package com.microjet.airqi2

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import com.microjet.airqi2.BroadReceiver.PrimaryReceiver
import com.microjet.airqi2.Definition.BroadcastIntents
import android.os.Build
import java.util.*


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

        fun applicationContext() : Context {
            return instance!!.applicationContext
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
    }

    override fun onCreate() {
        super.onCreate()
        val context: Context = MyApplication.applicationContext()

        // The default Realm file is "default.realm" in Context.getFilesDir();
        // we'll change it to "myrealm.realm"
        Realm.init(this)
        //val config = RealmConfiguration.Builder().name("myrealm.realm").build()
        val config = RealmConfiguration.Builder().name("myrealm.realm").schemaVersion(2).migration(RealmMigrations()).build()
        Log.d("REALMAPP",config.schemaVersion.toString())
        Log.d("REALMAPP",RealmConfiguration.Builder().name("myrealm.realm").build().path.toString())
        Log.d("REALMAPP",RealmConfiguration.Builder().name("myrealm.realm").build().realmDirectory.toString())

        Realm.setDefaultConfiguration(config)

        mPrimaryReceiver = PrimaryReceiver()
        val filter = IntentFilter(BroadcastIntents.PRIMARY)
        this.registerReceiver(mPrimaryReceiver, filter)
    }
}
