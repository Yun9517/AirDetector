package com.microjet.airqi2


import android.os.AsyncTask
import android.os.Build
import android.util.Log
import android.widget.Toast
import okhttp3.*
import org.json.JSONObject


/**
 * Created by B00190 on 2018/5/14.
 */
class FirebaseNotifSettingTask : AsyncTask<String, Int, String>() {
        private var setting = 0
    //主要背景執行
    override fun doInBackground(vararg params: String?): String? {
        val token = params[0]
        val phpToken = "Bearer " + token
        val mediaTime: String?
        val mediaPM25: String?
        val mediaTVOC: String?
        setting = params.size

        if(params.size == 4){
            mediaTime = params[1]
            mediaPM25 = params[2]
            mediaTVOC = params[3]
        }else{
            mediaTime = ""
            mediaPM25 = ""
            mediaTVOC = ""
        }

        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse("https://mjairql.com/api/v1/notificationSetting")!!.newBuilder()

        val mediaType = MediaType.parse("application/x-www-form-urlencoded")
        val body = RequestBody.create(mediaType, "time="+ mediaTime + "&pm25=" + mediaPM25 + "&tvoc=" + mediaTVOC )
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
                    TvocNoseData.firebaseNotifTVOC = jsonObj1.getInt("tvoc")
                    TvocNoseData.firebaseNotifPM25 = jsonObj1.getInt("pm25")
                    Log.e("抓取設定", "_"+TvocNoseData.firebaseNotiftime+"_"+ TvocNoseData.firebaseNotifPM25+"_" +TvocNoseData.firebaseNotifTVOC )
                    return "FirebaseSetting_success"

                }else{
                    Log.d("Setting_error","抓不到設定值")
                    return "Error"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return "ReconnectNetwork"//null
        }
        return "ReconnectNetwork"//null
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        try {
            if (result != null && setting == 4 ) {
                when (result) {
                    "FirebaseSetting_success" -> {
                        if (Build.BRAND != "OPPO") {//雲端推播設定完成
                            Toast.makeText(MyApplication.applicationContext(), R.string.fireBase_Toast_Setup_Done, Toast.LENGTH_SHORT).show()
                        }
                    }
                    "Error" -> {
                        if (Build.BRAND != "OPPO") {//推播錯誤
                            Toast.makeText(MyApplication.applicationContext(), R.string.fireBase_Toast_Broadcast_Error, Toast.LENGTH_SHORT).show()
                        }
                    }
                    "ResponseError" -> {//請登入帳號
                        Log.e("ResponseError", "測試中")
                        Toast.makeText(MyApplication.applicationContext(), R.string.fireBase_Toast_SignIn, Toast.LENGTH_SHORT).show()
                    }
                    "ReconnectNetwork" -> {
                        if (Build.BRAND != "OPPO") {//請連結網路
                            Toast.makeText(MyApplication.applicationContext(), R.string.fireBase_Toast_Network_Connect, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }
        } catch (e: Exception) {

        }
    }

}