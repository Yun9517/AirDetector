package com.microjet.airqi2.Account

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject

/**
 * Created by B00190 on 2018/6/22.
 */
class AccountLoginTask(mContext: Context?) : AsyncTask<String, Int, String>() {
    private val TAG: String = "AccountLoginTask"
    private val mContext = mContext
    override fun onPreExecute() {
        super.onPreExecute()

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
            loginResult = "成功登入"
            val res: String = response.body()!!.string().toString()
            Log.e(TAG, res)
            val returnResult = JSONObject(res).getJSONObject("success")
            val token = returnResult.getString("token").toString()
            val name = returnResult.getJSONObject("userData").getString("name").toString()
            val email = returnResult.getJSONObject("userData").getString("email").toString()
            val deviceArr = returnResult.getJSONArray("deviceList")
            //Log.e(TAG, token) Log.e(TAG, name) Log.e(TAG, email) Log.e(TAG,deviceArr.toString())
            //val share = getSharedPreferences("TOKEN", MODE_PRIVATE)

        } else {
            loginResult = "登入失敗"
        }
        return loginResult
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)

    }


    override fun onPostExecute(result: String?) {
        Log.e(TAG, result)
        if (result == "成功登入") {
            val intent = Intent(mContext, AccountActiveActivity::class.java)
            mContext?.startActivity(intent)

        }else if(result == "登入失敗"){

        }
    }



}