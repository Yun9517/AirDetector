package com.microjet.airqi2

import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.iid.FirebaseInstanceId
import io.realm.Realm
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject


/**
 * Created by B00190 on 2018/5/14.
 */
class FirebaseNotifTask : AsyncTask<String, Int, String>() {

     //主要背景執行
    override fun doInBackground(vararg params: String?): String? {
         val token = params[0]
        val phpToken = "Bearer " + token

        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse("https://mjairql.com/api/v1/notificationSetting")!!.newBuilder()

         val mediaType = MediaType.parse("application/x-www-form-urlencoded")
         val body = RequestBody.create(mediaType, "time"+ "" + "pm25=" + "" + "tvoc" + "")
         val url = urlBuilder.build().toString()
        val request = Request.Builder()
                .url(url)
                .post(body)
                .addHeader("authorization", phpToken)
                .build()
        Log.d("Download", "doInBackground")
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return "ResponseError"//null
            } else {
                val res = response.body()?.string()
                Log.e("res抓取",res)
                val jsonObj = JSONObject(res)
                val returnResult = jsonObj.get("userData")

            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "ReconnectNetwork"//null
        }
        return "ReconnectNetwork"//null
   }

}