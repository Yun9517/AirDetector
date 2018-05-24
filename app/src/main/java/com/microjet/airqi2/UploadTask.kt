package com.microjet.airqi2

import android.os.AsyncTask
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import io.realm.Realm
import io.realm.Sort
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient


/**
 * Created by B00175 on 2018/3/30.
 */
// 上傳非同步執行類別
class UploadTask: AsyncTask<String, Void, String>() {

    val TAG = this.javaClass.simpleName

    override fun doInBackground(vararg params: String): String? {

        val realm = Realm.getDefaultInstance()
        val result: List<AsmDataModel> = realm.where(AsmDataModel::class.java).equalTo("UpLoaded", "0").equalTo("MACAddress", params[0]).findAll()
                .sort("Created_time", Sort.ASCENDING)
                .take(5000)

        val itemCount = result.size
        Log.d(TAG, "共有$itemCount 筆未上傳")

        if (itemCount > 0) {
            try {
                val client = OkHttpClient.Builder()
                        .connectTimeout(30, TimeUnit.SECONDS)
                        .writeTimeout(30, TimeUnit.SECONDS)
                        .readTimeout(30, TimeUnit.SECONDS)
                        .build()

                val mediaType = MediaType.parse("application/x-www-form-urlencoded")
                val UUID = MyApplication.getPsuedoUniqueID()
                val mDeviceAddress = params[0]
                val uploadToken = "Bearer " + params[1]
                val weather = resultToJsonArray(result)
                //如果欄位名稱與參數相同可以直接用這招
                //val weather = ArrayList<String>()
                //weather.add(Gson().toJson(realm.copyFromRealm(result)))
                val jsonObj = JSONObject()
                        .put("uuid", UUID)
                        .put("mac_address", mDeviceAddress)
                        .put("registration_id", FirebaseInstanceId.getInstance().token)
                        .put("weather", weather)

                val body: RequestBody = RequestBody.create(mediaType, "data=" + jsonObj.toString())
                val request = Request.Builder()
                        .url("https://mjairql.com/api/v1/upUserData")
                        .post(body)
                        .addHeader("authorization", uploadToken)
                        .addHeader("Cache-Control", "no-cache")
                        .addHeader("Content-Type", "application/x-www-form-urlencoded")
                        .build()
                val response = client.newCall(request).execute()


                if (!response.isSuccessful) { Log.d(TAG, "ERROR") }
                else {
                    realm.executeTransaction {
                        result.forEach {
                            it.upLoaded = "1"
                            Log.d(TAG, "SUCCESS" + it.toString())
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, e.toString())
            }
        }
        realm.close()
        return null
    }

    private fun resultToJsonArray(result: List<AsmDataModel>): JSONArray {
        val arr = JSONArray()
        result.forEach {
            val jsonObj = JSONObject()
            jsonObj.put("tvoc", it.tvocValue)
            jsonObj.put("eco2", it.ecO2Value)
            jsonObj.put("temperature", it.tempValue)
            jsonObj.put("humidity", it.humiValue)
            jsonObj.put("pm25", it.pM25Value)
            jsonObj.put("longitude", it.longitude.toString())
            jsonObj.put("latitude", it.latitude.toString())
            jsonObj.put("timestamp", it.created_time)
            arr.put(jsonObj)
        }
        Log.d(TAG, arr.toString())
        return arr
    }
}