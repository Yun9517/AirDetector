package com.microjet.airqi2.Account.AccountTask

import android.app.Activity
import android.app.DialogFragment
import android.os.AsyncTask
import android.util.Log
import com.microjet.airqi2.BleEvent
import com.microjet.airqi2.Fragment.CheckFragment
import com.microjet.airqi2.R
import okhttp3.OkHttpClient
import okhttp3.Request
import org.greenrobot.eventbus.EventBus
import java.util.concurrent.TimeUnit

/**
 * Created by B00190 on 2018/7/17.
 */
class AccountCheckTokenTask(gettedActivity: Activity, strEvent: String) : AsyncTask<String, Int, String>() {
    private val TAG = "AccountCheckTokenTask"
    private var event: String? = strEvent
    private val useGettedActivity: Activity = gettedActivity

    override fun onPreExecute() {
        super.onPreExecute()
        if (event == "checkTokenBybtEvent") {
            val newFrage = CheckFragment().newInstance(R.string.remind, R.string.connectServer, useGettedActivity, 0, "wait")
            newFrage.setCancelable(false)
            newFrage.show(useGettedActivity.fragmentManager, "dialog")
        }
    }

    override fun doInBackground(vararg params: String?): String? {
        val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
        val myToken: String? = "Bearer " + params[0]
        Log.e(TAG, myToken)
        val request = Request.Builder()
                .url("https://api.mjairql.com/api/v1/loginTokenCheck")
                .get()
                .addHeader("authorization", myToken)
                .addHeader("cache-control", "no-cache")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build()

        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val res: String = response.body()!!.string().toString()
                return "successToken"
                Log.e(TAG, res)
            } else {
                return "ResponseError"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "ReconnectNetwork"
        }

    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        Log.e(TAG, result)
            val previousDialog = useGettedActivity.fragmentManager.findFragmentByTag("dialog")
            if (previousDialog != null) {
                val dialog = previousDialog as DialogFragment
                dialog.dismissAllowingStateLoss()//處理縮小APP出現的沒回應事件
            }
            when (result) {
                "successToken" -> {
                    when (event) {
                        "checkTokenBybtEvent" -> EventBus.getDefault().post(BleEvent("successToken"))
                        "checkTokenByOnstart" -> Log.d(TAG, "Token有效")
                    }
                }
                "ResponseError" -> {
                    when (event) {
                        "checkTokenBybtEvent" -> EventBus.getDefault().post(BleEvent("ErrorTokenWithButton"))
                        "checkTokenByOnstart" -> EventBus.getDefault().post(BleEvent("ErrorTokenWithOnstart"))
                    }
                }
                "ReconnectNetwork" -> {
                    if (event == "checkTokenBybtEvent") {
                        val newFrage = CheckFragment().newInstance(R.string.remind, R.string.checkConnection, useGettedActivity, 1, "dismiss")
                        newFrage.show(useGettedActivity.fragmentManager, "dialog")
                    }
                }
            }
    }

}