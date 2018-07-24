package com.microjet.airqi2.Account.AccountTask

import android.app.Activity
import android.app.DialogFragment
import android.os.AsyncTask
import android.util.Log
import com.microjet.airqi2.BleEvent
import com.microjet.airqi2.Fragment.CheckFragment
import com.microjet.airqi2.R
import com.microjet.airqi2.TvocNoseData
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Created by B00190 on 2018/6/22.
 */
class AccountLoginTask(gettedActivity: Activity) : AsyncTask<String, Int, String>() {
    private val TAG: String = "AccountLoginTask"
    private val useManagementActivity: Activity = gettedActivity

    override fun onPreExecute() {
        super.onPreExecute()
        //等候dialog視窗開啟
        val newFrage = CheckFragment().newInstance(R.string.remind, R.string.wait_Login, useManagementActivity, 0, "wait")
        newFrage.setCancelable(false)
        newFrage.show(useManagementActivity.fragmentManager, "dialog")
    }


    //主要背景執行
    override fun doInBackground(vararg params: String?): String? {
        val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
        val mediaType = MediaType.parse("application/x-www-form-urlencoded")
        val userInfo = "email=" + params[0] + "&password=" + params[1]
        val body = RequestBody.create(mediaType, userInfo)
        val request = Request.Builder()
                .url("https://mjairql.com/api/v1/login")
                .post(body)
                .addHeader("cache-control", "no-cache")
                .addHeader("content-type", "application/x-www-form-urlencoded")
                .build()
        try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val res: String = response.body()!!.string().toString()
                Log.e(TAG, res)
                val returnResult = JSONObject(res).getJSONObject("success")
                TvocNoseData.cloudToken = returnResult.getString("token").toString()
                TvocNoseData.cloudName = returnResult.getJSONObject("userData").getString("name").toString()
                TvocNoseData.cloudEmail = returnResult.getJSONObject("userData").getString("email").toString()
                TvocNoseData.cloudDeviceArr = returnResult.getJSONArray("deviceList").toString()
                return "successNetwork"
            } else {
                return "ResponseError"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "ReconnectNetwork"
        }
    }

    override fun onPostExecute(result: String?) {
        Log.e(TAG, result)
        //獲得結果後，關閉所有dialog視窗
        val previousDialog = useManagementActivity.fragmentManager.findFragmentByTag("dialog")
        if (previousDialog != null) {
            val dialog = previousDialog as DialogFragment
            dialog.dismissAllowingStateLoss()//處理縮小APP出現的沒回應事件
        }
        //處理結果
        when(result){
            "successNetwork" ->   EventBus.getDefault().post(BleEvent("success Login"))
            "ResponseError" ->{ val newFrage = CheckFragment().newInstance(R.string.remind, R.string.errorPassword, useManagementActivity, 1, "dismiss").show(useManagementActivity.fragmentManager, "dialog")}
            "ReconnectNetwork" ->{ val newFrage = CheckFragment().newInstance(R.string.remind, R.string.checkConnection, useManagementActivity, 1, "dismiss").show(useManagementActivity.fragmentManager, "dialog")}
        }
    }


}