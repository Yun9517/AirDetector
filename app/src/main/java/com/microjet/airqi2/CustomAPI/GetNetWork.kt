package com.microjet.airqi2.CustomAPI

import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.microjet.airqi2.MyApplication


/**
 * Created by B00170 on 2018/3/12.
 */
//20180312
object  GetNetWork {

    val isFastGetNet: Boolean
        get() {
            val connManager: ConnectivityManager? = MyApplication.applicationContext().getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo: NetworkInfo? = connManager?.getActiveNetworkInfo()


            //判斷是否有網路
            //net = networkInfo.isConnected
            if (networkInfo == null || !networkInfo.isConnected()) {
                return false
            } else {
                return networkInfo.isAvailable()
            }

        }
}
