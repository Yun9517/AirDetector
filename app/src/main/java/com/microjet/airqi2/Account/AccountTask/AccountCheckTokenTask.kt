package com.microjet.airqi2.Account.AccountTask

import android.os.AsyncTask
import android.util.Log
import com.microjet.airqi2.BleEvent
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Created by B00190 on 2018/7/17.
 */
class AccountCheckTokenTask : AsyncTask<String, Int, String>() {
    private val TAG = "AccountCheckTokenTask"
    private var event: String? = ""
    /*
    onPreExecute -> AsyncTask 執行前的準備工作
    doInBackground -> 實際要執行的程式碼就是寫在這裡
    onProgressUpdate -> 用來顯示目前的進度
    onPostExecute -> 執行完的結果 - Result 會傳入這裡。
    */

    override fun onPreExecute() {
        super.onPreExecute()
        val urlEvent = BleEvent("wait Dialog")
        EventBus.getDefault().post(urlEvent)
    }

    override fun doInBackground(vararg params: String?): String? {
        val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
        val mediaType = MediaType.parse("application/x-www-form-urlencoded")
        val myToken: String? = "Bearer " + params[0]
        event = params[1]
        Log.e(TAG, myToken)
        val body = RequestBody.create(mediaType, myToken)
        val request = Request.Builder()
                .url("https://api.mjairql.com/api/v1/loginTokenCheck")
                .get()
                .addHeader("authorization", myToken)
                .addHeader("cache-control", "no-cache")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return "ResponseError"
            } else {
                val res: String = response.body()!!.string().toString()
                Log.e(TAG, res)
                val returnSuccess = JSONObject(res).optString("success")
                when (res != null) {
                    returnSuccess != null -> return "successToken"
                }
                Log.e(TAG, "onStart")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "ReconnectNetwork"
        }
        return null
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        Log.e(TAG, result)
        try {
            val urlEvent_close = BleEvent("close Wait Dialog")
            EventBus.getDefault().post(urlEvent_close)
            when (result) {
                "successToken" -> {
                    var urlEvent_success: BleEvent? = null
                    if (event == "checkTokenBybtEvent") {
                        urlEvent_success = BleEvent("successToken")
                    }
                    if (event == "checkTokenByOnstart") {
                       Log.d(TAG,"Token有效")
                    }
                    EventBus.getDefault().post(urlEvent_success)
                }
                "ResponseError" -> {
                    var urlEvent_success: BleEvent? = null
                    if (event == "checkTokenBybtEvent") {
                        urlEvent_success = BleEvent("ErrorTokenWithButton")
                    }
                    if (event == "checkTokenByOnstart") {
                        urlEvent_success = BleEvent("ErrorTokenWithOnstart")
                    }
                    EventBus.getDefault().post(urlEvent_success)
                }
                "ReconnectNetwork" -> {
                    val urlEvent_success = BleEvent("ReconnectNetwork")
                    EventBus.getDefault().post(urlEvent_success)
                }
            }
        } catch (e: Exception) {

        }

    }

}