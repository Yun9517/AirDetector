package com.microjet.airqi2

import android.content.Context
import android.content.SharedPreferences
import android.os.AsyncTask
import android.util.Log
import io.realm.Realm
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.TimeUnit

/**
 * Created by B00175 on 2018/3/30.
 */
class UploadTask: AsyncTask<String, Void, String>() {

    private val hasBeenUpLoaded = ArrayList<Int>()
    override fun doInBackground(vararg params: String): String? {
        val mDeviceAddress = params[0]
        val token = params[1]
        var return_body: RequestBody? = null
        var getResponeResult = java.lang.Boolean.parseBoolean(null)
        try {
            //取得getRequestBody
            return_body = getRequestBody(mDeviceAddress, token)
            //呼叫getResponse取得結果
            if (return_body!!.contentLength() > 0) {
                getResponeResult = getResponse(return_body, token)

                if (getResponeResult) {
                    //呼叫updateDB_UpLoaded方法更改此次傳輸的資料庫資料欄位UpLoaded
                    val DBSucess = updateDB_UpLoaded()
                    if (DBSucess) {
                        Log.e("幹改進去", DBSucess.toString())
                    }
                    hasBeenUpLoaded.clear()
                } else {
                    Log.e("幹改失敗拉!!", getResponeResult.toString())
                }
            } else {
                Log.e("幹太少筆啦!", return_body.contentLength().toString())
            }

        } catch (e: Exception) {
            Log.e("return_body_erro", e.toString())
        }

        return null
    }


    private fun getRequestBody(DeviceAddress: String, token: String): RequestBody? {
        //很重要同區域才可以叫到同一個東西
        //        String serial = "";
        //        //確認唯一識別碼(https://blog.mosil.biz/2014/05/android-device-id-uuid/)
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
        //            serial = Build.SERIAL;
        //        }
        //首先將要丟進陣列內的JSON物件存好內容後丟進陣列
        val realm2 = Realm.getDefaultInstance()
        val query2 = realm2.where(AsmDataModel::class.java)
        val result2 = query2.equalTo("UpLoaded", "1").findAll()
        //        realm.executeTransaction((Realm realm1) -> {
        //
        //            for (int i = 0 ; i < result5.size() ; i++) {
        //
        //                result5.get(i).setUpLoaded("0");
        //
        //                Log.e("這個時間", String.valueOf(result5.toString()));
        //            }
        //
        //        });
        val realm1 = Realm.getDefaultInstance()
        val query1 = realm1.where(AsmDataModel::class.java)
        val result1 = query1.equalTo("UpLoaded", "0").findAll()

        /*
        RealmQuery<AsmDataModel> query9 = realm.where(AsmDataModel.class);
        RealmResults<AsmDataModel> result7 =query9.distinct("Created_time");
        Log.e("幹",String.valueOf(result7.size()));
        Log.e("幹蝦小",String.valueOf(result1.size()));
*/

        Log.e("未上傳ID", result1.toString())
        Log.e("已上ID", result2.toString())
        Log.e("未上傳資料筆數", result1.size.toString())
        Log.e("未上傳資料", result1.toString().toString())
        Log.e("已上傳資料筆數", result2.size.toString())


        //MyApplication getUUID=new MyApplication();
        val UUID = MyApplication.getPsuedoUniqueID()
        //製造RequestBody的地方
        var body: RequestBody? = null

        //20170227
        val json_obj = JSONObject()            //用來當內層被丟進陣列內的JSON物件
        val json_arr = JSONArray()                //JSON陣列
        // int toltoSize = 0;
        // int i = 0;

        val timestampTEMP: Long? = null

        try {
            if (result1.size > 0) {
                for (i in result1.indices) {
                    //toltoSize++;
                    if (i == 6000) {
                        break
                    }
                    //                if (result1.get(i).getCreated_time().equals(result1.get(i + 1).getCreated_time())) {
                    //                    realm.beginTransaction();
                    //                    result1.get(i).deleteFromRealm();
                    //                    realm.commitTransaction();
                    //                    Log.e("資料相同時", result1.get(i).getCreated_time().toString() + "下筆資料" + result1.get(i).getCreated_time().toString());
                    //                }
                    hasBeenUpLoaded.add(result1[i]!!.dataId)
                    Log.i("text", "i=" + i + "\n")
                    val json_obj_weather = JSONObject()            //單筆weather資料
                    json_obj_weather.put("temperature", result1[i]!!.tempValue)
                    json_obj_weather.put("humidity", result1[i]!!.humiValue)
                    json_obj_weather.put("tvoc", result1[i]!!.tvocValue)
                    json_obj_weather.put("eco2", result1[i]!!.ecO2Value)
                    json_obj_weather.put("pm25", result1[i]!!.pM25Value)
                    json_obj_weather.put("longitude", result1[i]!!.longitude!!.toString())
                    json_obj_weather.put("latitude", result1[i]!!.latitude!!.toString())
                    json_obj_weather.put("timestamp", result1[i]!!.created_time)
                    Log.e("timestamp", "i=" + i + "timestamp=" + result1[i]!!.created_time!!.toString())
                    json_arr.put(json_obj_weather)
                    //Log.e("下一筆資料","這筆資料:"+result1.get(i).getCreated_time().toString()+"下一筆資料:"+result1.get(i+1).getCreated_time().toString());
                }
            } else {
                Log.e("未上傳資料筆數", result1.size.toString())
            }

            json_obj.put("uuid", UUID)
            json_obj.put("mac_address", DeviceAddress)
            json_obj.put("registration_id", "qooo123457")
            //再來將JSON陣列設定key丟進JSON物件
            json_obj.put("weather", json_arr)
            Log.e("全部資料", json_obj.toString())
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            body = RequestBody.create(mediaType, "data=" + json_obj.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        realm1.close()
        realm2.close()
        return body
    }

    //傳資料
    private fun getResponse(body: RequestBody, MyToKen: String): Boolean {
        var response: Response? = null
        var resonseReselt = java.lang.Boolean.parseBoolean(null)
        try {
            if (body.contentLength() > 0) {
                //丟資料
                val request = Request.Builder()
                        .url("https://mjairql.com/api/v1/upUserData")
                        .post(body)
                        .addHeader("authorization", "Bearer " + MyToKen)
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .addHeader("cache-control", "no-cache")
                        .addHeader("postman-token", "a2fa2822-765d-209a-ec8c-82170c5171c0")
                        .build()
                try {
                    val client = OkHttpClient.Builder()
                            .connectTimeout(0, TimeUnit.SECONDS)
                            .writeTimeout(0, TimeUnit.SECONDS)
                            .readTimeout(0, TimeUnit.SECONDS)
                            .build()
                    //上傳資料
                    response = client.newCall(request).execute()
                    if (response!!.isSuccessful) {//正確回來
                        resonseReselt = true
                        Log.e("正確回來!!", response!!.body()!!.string())
                    } else {//錯誤回來
                        Log.e("錯誤回來!!", response!!.body()!!.string())
                        resonseReselt = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("回來處理有錯!", e.toString())

                }

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return resonseReselt
    }

    private fun updateDB_UpLoaded(): Boolean {
        var dbSucessOrNot = java.lang.Boolean.parseBoolean(null)
        val realm3 = Realm.getDefaultInstance()
        try {
            realm3.executeTransaction { realm1: Realm ->
                Log.e("正確回來TRY", hasBeenUpLoaded.size.toString())
                for (i in 0 until hasBeenUpLoaded.size) {
                    //realm.beginTransaction();
                    val aaa = realm3.where(AsmDataModel::class.java)
                            .equalTo("id", hasBeenUpLoaded.get(i))
                            .findFirst()
                    aaa!!.setUpLoaded("1")
                    Log.e("回來更新", aaa!!.getDataId()!!.toString() + "更新?" + aaa!!.getUpLoaded())
                }
                val query3 = realm3.where(AsmDataModel::class.java)
                val result3 = query3.equalTo("UpLoaded", "1").findAll()
                Log.e("正確更改", result3.size.toString())
                Log.e("正確更改內容", result3.toString())
            }
            dbSucessOrNot = true
        } catch (e: Exception) {
            Log.e("dbSucessOrNot", e.toString())
            dbSucessOrNot = false
        }

        realm3.close()

        return dbSucessOrNot
    }

}