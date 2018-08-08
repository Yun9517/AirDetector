package com.microjet.airqi2.FireBaseCloudMessage


import android.os.AsyncTask
import android.util.Log
import com.microjet.airqi2.BleEvent
import com.microjet.airqi2.TvocNoseData
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject
import java.util.concurrent.TimeUnit


/**
 * Created by B00190 on 2018/5/14.
 */
class FirebaseNotifSettingTask : AsyncTask<String, Int, String>() {
    private var setting = 0

    override fun onPreExecute() {
        super.onPreExecute()
        val urlEvent = BleEvent("waitDialog")
        EventBus.getDefault().post(urlEvent)
    }

    //主要背景執行
    override fun doInBackground(vararg params: String?): String? {
        val token = params[0]
        val phpToken = "Bearer " + token
        val mediaTime: String?
        val mediaPM25: String?
        val mediaTVOC: String?
        setting = params.size

        if (params.size == 4) {
            mediaTime = params[1]
            mediaPM25 = params[2]
            mediaTVOC = params[3]
        } else {
            mediaTime = ""
            mediaPM25 = ""
            mediaTVOC = ""
        }

        val client = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()

        val urlBuilder = HttpUrl.parse("https://mjairql.com/api/v1/notificationSetting")!!.newBuilder()

        val mediaType = MediaType.parse("application/x-www-form-urlencoded")
        val body = RequestBody.create(mediaType, "time=" + mediaTime + "&pm25=" + mediaPM25 + "&tvoc=" + mediaTVOC)
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
                Log.e("Get_res", res)
                val jsonObj = JSONObject(res)
                val returnResult = jsonObj.getString("info")
                Log.e("returnResult", returnResult.toString())
                if (returnResult != null && returnResult != "") {
                    val jsonObj1 = JSONObject(returnResult)
                    TvocNoseData.firebaseNotiftime = jsonObj1.getInt("time")
                    TvocNoseData.firebaseNotifTVOC = jsonObj1.getInt("tvoc")
                    TvocNoseData.firebaseNotifPM25 = jsonObj1.getInt("pm25")
                    Log.e("抓取設定", "_" + TvocNoseData.firebaseNotiftime + "_" + TvocNoseData.firebaseNotifPM25 + "_" + TvocNoseData.firebaseNotifTVOC)
                    return "FirebaseSetting_success"

                } else {
                    Log.d("Setting_error", "抓不到設定值")
                    return "Error"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d("Setting_error", "try裡面")
            return "ReconnectNetwork"
        }
        Log.d("Setting_error", "try外面")
        return null
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        try {
            if (result != null && setting == 4) {
                TvocNoseData.firebaseSettingResult = result
                EventBus.getDefault().post(BleEvent("firebaseNotifiSettingTask"))
            } else {
                Log.d("Setting_error", "沒有進入判斷")
            }
        } catch (e: Exception) {

        }
    }

}