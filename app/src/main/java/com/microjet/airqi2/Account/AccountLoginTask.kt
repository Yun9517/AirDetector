package com.microjet.airqi2.Account

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.microjet.airqi2.BleEvent
import com.microjet.airqi2.TvocNoseData
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * Created by B00190 on 2018/6/22.
 */
class AccountLoginTask(content: Context?) : AsyncTask<String, Int, String>() {
    private val TAG: String = "AccountLoginTask"
    private var waitDialog: ProgressDialog? = null
    private val mcontent = content

    override fun onPreExecute() {
        super.onPreExecute()
        waitDialog = ProgressDialog(mcontent)
        waitDialog?.setMessage("先別提安麗，有聽過Addwii嗎?")
        waitDialog?.setCancelable(false)
        waitDialog?.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        waitDialog?.show()
    }


    //主要背景執行
    override fun doInBackground(vararg params: String?): String? {
        var loginResult: String = ""
        val client = OkHttpClient()
        val mediaType = MediaType.parse("application/x-www-form-urlencoded")
        val userInfo = "email=" + params[0] + "&password=" + params[1]
        val body = RequestBody.create(mediaType, userInfo)
        val request = Request.Builder()
                .url("https://mjairql.com/api/v1/login")
                .post(body)
                .addHeader("cache-control", "no-cache")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build()
        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
            loginResult = "successNetwork"
            val res: String = response.body()!!.string().toString()
            Log.e(TAG, res)
            val returnResult = JSONObject(res).getJSONObject("success")
            TvocNoseData.cloudToken = returnResult.getString("token").toString()
            TvocNoseData.cloudName = returnResult.getJSONObject("userData").getString("name").toString()
            TvocNoseData.cloudEmail = returnResult.getJSONObject("userData").getString("email").toString()
            TvocNoseData.cloudDeviceArr = returnResult.getJSONArray("deviceList").toString()
        } else {
            loginResult = "ResponseError"
        }
        return loginResult
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)

    }

    override fun onPostExecute(result: String?) {
        Log.e(TAG, result)
        if (result == "successNetwork") {
            val urlEvent = BleEvent("success Login")
            EventBus.getDefault().post(urlEvent)
            waitDialog?.dismiss()
        } else if (result == "ResponseError") {
            val urlEvent = BleEvent("wrong Login")
            EventBus.getDefault().post(urlEvent)
            waitDialog?.dismiss()
        }
    }


}