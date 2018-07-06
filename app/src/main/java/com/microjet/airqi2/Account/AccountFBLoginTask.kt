package com.microjet.airqi2.Account

import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.microjet.airqi2.BleEvent
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * Created by B00190 on 2018/7/5.
 */
class AccountFBLoginTask(input: Context?) : AsyncTask<String, Int, String>() {
    val TAG = "AccountFBLoginTask"
    var cloudToken: String = ""
    var name: String = ""
    var email: String = ""
    private var mContext: Context? = input

    //主要背景執行
    override fun doInBackground(vararg params: String?): String? {
        val token = params[0]
        val fbToken = "token=" + token
        val social_type: String? = "&social_type=" + params[1]
        Log.e(TAG, fbToken)
        Log.e(TAG, social_type)

        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse("https://api.mjairql.com/api/v1/socialLogin")!!.newBuilder()

        val mediaType = MediaType.parse("application/x-www-form-urlencoded")
        val body = RequestBody.create(mediaType, fbToken + social_type)
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
                .url(url)
                .post(body)
                .build()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return "ResponseError"//null
            } else {
                val res = response.body()?.string()
                Log.e(TAG, res)
                val jsonObj = JSONObject(res)
                //取出success內容
                val responseContent = jsonObj.getJSONObject("success")
                cloudToken = responseContent.getString("token")
                name = responseContent.getString("name")
                email = responseContent.getString("email")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "successNetwork"//null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(result: String?) {
        try {
            if (result != null) {
                when (result) {
                    "ResponseError" -> {
                        Log.e(TAG, " \"ResponseError!!\"")
                    }
                    "successNetwork" -> {
                        val urlEvent = BleEvent("fb_Login")
                        EventBus.getDefault().post(urlEvent)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}