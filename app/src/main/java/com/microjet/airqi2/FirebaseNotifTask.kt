package com.microjet.airqi2


import android.os.AsyncTask
import android.util.Log
import okhttp3.*
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
                Log.e("Get_res",res)
                val jsonObj = JSONObject(res)
                val returnResult = jsonObj.getString("info")
                Log.e("returnResult",returnResult.toString())
                if(returnResult != null && returnResult !=""){
                    val jsonObj1 = JSONObject(returnResult)
                    TvocNoseData.firebaseNotiftime = jsonObj1.getInt("time")
                    TvocNoseData.firebaseNotifPM25 = jsonObj1.getInt("pm25")
                    TvocNoseData.firebaseNotifTVOC = jsonObj1.getInt("tvoc")
                    Log.e("抓取設定", "_"+TvocNoseData.firebaseNotiftime+"_"+ TvocNoseData.firebaseNotifPM25+"_" +TvocNoseData.firebaseNotifTVOC )


                }else{
                    Log.d("Setting_error","抓不到設定值")
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "ReconnectNetwork"//null
        }
        return "ReconnectNetwork"//null
    }

}