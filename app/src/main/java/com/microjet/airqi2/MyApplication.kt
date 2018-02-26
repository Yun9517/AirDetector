package com.microjet.airqi2

import android.app.Application
import android.content.Context
import android.content.IntentFilter
import android.util.Log
import io.realm.Realm
import io.realm.RealmConfiguration
import com.microjet.airqi2.BroadReceiver.PrimaryReceiver
import com.microjet.airqi2.Definition.BroadcastIntents


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
    }

    override fun onCreate() {
        super.onCreate()
        val context: Context = MyApplication.applicationContext()

        // The default Realm file is "default.realm" in Context.getFilesDir();
        // we'll change it to "myrealm.realm"
        Realm.init(this)
        //val config = RealmConfiguration.Builder().name("myrealm.realm").build()
        val config = RealmConfiguration.Builder().name("myrealm.realm").schemaVersion(1).migration(RealmMigrations()).build()
        Log.d("REALM",config.schemaVersion.toString())
        Log.d("REALM",RealmConfiguration.Builder().name("myrealm.realm").build().path.toString())
        Log.d("REALM",RealmConfiguration.Builder().name("myrealm.realm").build().realmDirectory.toString())

        Realm.setDefaultConfiguration(config)

        mPrimaryReceiver = PrimaryReceiver()
        val filter = IntentFilter(BroadcastIntents.PRIMARY)
        this.registerReceiver(mPrimaryReceiver, filter)
    }

}
