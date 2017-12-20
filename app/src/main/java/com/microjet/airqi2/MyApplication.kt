package com.microjet.airqi2

import android.app.Application
import android.content.IntentFilter
import io.realm.Realm
import io.realm.RealmConfiguration
import com.microjet.airqi2.BroadReceiver.MainReceiver


/**
 * Created by chang on 2017/12/9.
 */
class MyApplication : Application() {
    var mMainReceiver : MainReceiver? = null
    override fun onCreate() {
        super.onCreate()
        // The default Realm file is "default.realm" in Context.getFilesDir();
        // we'll change it to "myrealm.realm"
        Realm.init(this)
        val config = RealmConfiguration.Builder().name("myrealm.realm").build()
        Realm.setDefaultConfiguration(config)

        mMainReceiver = MainReceiver()
        val filter = IntentFilter("Main")
        this.registerReceiver(mMainReceiver, filter)
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterReceiver(mMainReceiver)
    }
}
