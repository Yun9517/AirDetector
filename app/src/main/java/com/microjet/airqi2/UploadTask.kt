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

    //取得資料庫資料並封裝上傳資料
    /*
    private fun prepareData(DeviceAddress: String): RequestBody? {
        //首先將要丟進陣列內的JSON物件存好內容後丟進陣列
        val unUpLoadedRealm = Realm.getDefaultInstance()
        val unUpLoadedQuery = unUpLoadedRealm.where(AsmDataModel::class.java)
        val unUpLoadedResult = unUpLoadedQuery.equalTo("UpLoaded", "0").findAll()
        Log.e("未上傳ID", unUpLoadedResult.toString())
        Log.e("未上傳資料筆數", unUpLoadedResult.size.toString())
        Log.e("未上傳資料", unUpLoadedResult.toString().toString())
        val UUID = MyApplication.getPsuedoUniqueID()
        //製造RequestBody的地方
        var postBody: RequestBody? = null
        //20170227
        val packJsonFinalObj = JSONObject()            //用來當內層被丟進陣列內的JSON物件
        val packJsonBigArry = JSONArray()                //JSON陣列
        try {
            if (unUpLoadedResult.size > 0) {
                for (i in unUpLoadedResult.indices) {
                    if (i == 6000) {
                        break
                    }
                    recordChangDataId.add(unUpLoadedResult[i]!!.dataId)
                    Log.i("text", "i=" + i + "\n")
                    val packJsonObj = JSONObject()            //單筆weather資料
                    packJsonObj.put("temperature", unUpLoadedResult[i]!!.tempValue)
                    packJsonObj.put("humidity", unUpLoadedResult[i]!!.humiValue)
                    packJsonObj.put("tvoc", unUpLoadedResult[i]!!.tvocValue)
                    packJsonObj.put("eco2", unUpLoadedResult[i]!!.ecO2Value)
                    packJsonObj.put("pm25", unUpLoadedResult[i]!!.pM25Value)
                    packJsonObj.put("longitude", unUpLoadedResult[i]!!.longitude!!.toString())
                    packJsonObj.put("latitude", unUpLoadedResult[i]!!.latitude!!.toString())
                    packJsonObj.put("timestamp", unUpLoadedResult[i]!!.created_time)
                    Log.e("timestamp", "i=" + i + "timestamp=" + unUpLoadedResult[i]!!.created_time!!.toString())
                    packJsonBigArry.put(packJsonObj)
                }
            } else {
                Log.e("未上傳資料筆數", unUpLoadedResult.size.toString())
            }
            packJsonFinalObj.put("uuid", UUID)
            packJsonFinalObj.put("mac_address", DeviceAddress)
            packJsonFinalObj.put("registration_id", FirebaseInstanceId.getInstance().getToken())
            //再來將JSON陣列設定key丟進JSON物件
            packJsonFinalObj.put("weather", packJsonBigArry)
            Log.e("全部資料", packJsonFinalObj.toString())
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            postBody = RequestBody.create(mediaType, "data=" + packJsonFinalObj.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        unUpLoadedRealm.close()
        return postBody
    }

    //上傳資料庫資料
    private fun sendDataToClound(body: RequestBody, MyToKen: String): Boolean {
        var upLoadedResponse: Response? = null
        var upLoadedReselt = java.lang.Boolean.parseBoolean(null)
        try {
            if (body.contentLength() > 0) {
                //丟資料
                val request = Request.Builder()
                        .url("https://mjairql.com/api/v1/upUserData")
                        .post(body)
                        .addHeader("authorization", "Bearer " + MyToKen)
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .build()
                try {
                    val client = OkHttpClient.Builder()
                            .connectTimeout(0, TimeUnit.SECONDS)
                            .writeTimeout(0, TimeUnit.SECONDS)
                            .readTimeout(0, TimeUnit.SECONDS)
                            .build()
                    //上傳資料
                    upLoadedResponse = client.newCall(request).execute()
                    if (upLoadedResponse!!.isSuccessful) {//正確回來
                        upLoadedReselt = true
                        Log.e("正確回來!!", upLoadedResponse!!.body()!!.string())
                    } else {//錯誤回來
                        Log.e("錯誤回來!!", upLoadedResponse!!.body()!!.string())
                        upLoadedReselt = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("回來處理有錯!", e.toString())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return upLoadedReselt
    }

    //更改資料庫欄位UpLoaded
    private fun changeDBstatus(): Boolean {
        var dbChangStatus = java.lang.Boolean.parseBoolean(null)
        val changRealmStatus = Realm.getDefaultInstance()
        try {
            changRealmStatus.executeTransaction { _ ->
                Log.e("正確回來TRY", recordChangDataId.size.toString())
                for (i in 0 until recordChangDataId.size) {
                    //realm.beginTransaction();
                    val changDB = changRealmStatus.where(AsmDataModel::class.java)
                            .equalTo("id", recordChangDataId.get(i))
                            .findFirst()
                    changDB!!.setUpLoaded("1")
                    Log.e("回來更新", changDB!!.getDataId()!!.toString() + "更新?" + changDB!!.getUpLoaded())
                }
            }
            dbChangStatus = true
        } catch (e: Exception) {
            Log.e("dbChangStatus", e.toString())
            dbChangStatus = false
        }
        changRealmStatus.close()
        return dbChangStatus
    }
    */
}