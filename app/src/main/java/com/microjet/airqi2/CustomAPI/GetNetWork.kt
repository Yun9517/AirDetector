package com.microjet.airqi2.CustomAPI

import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.microjet.airqi2.MyApplication
import android.net.Proxy.getPort
import android.net.Proxy.getHost
import java.io.IOException
import java.net.Socket
import java.net.URL


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

            return this!!.isNetWorkAvailable()!!
        }

    // network available cannot ensure Internet is available
    fun isNetWorkAvailable(): Boolean? {
        val runtime = Runtime.getRuntime()
        var isOnLineCheack:Boolean?=null

        //再用非同步或開新執行續跑
//        val socket: Socket
//        try {
//            val url = URL("http://www.google.com")
//            socket = Socket(url.getHost(), url.getPort())
//        } catch (e: IOException) {
//            return false
//        }
//
//        try {
//            socket.close()
//        } catch (e: IOException) {
//            //will never happen, it's thread-safe
//        }
//
//        return true


        try {

            var mIpAddProcess: Process?
            mIpAddProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8")
            val mExitValue = mIpAddProcess!!.waitFor()
            System.out.println(" mExitValue " + mExitValue)



            if (mExitValue == 0) {
                isOnLineCheack=true
            } else {
                isOnLineCheack=false
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }
}
