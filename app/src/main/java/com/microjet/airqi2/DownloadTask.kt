package com.microjet.airqi2

import android.app.Application
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import io.fabric.sdk.android.services.settings.IconRequest.build
import okhttp3.Request
import io.fabric.sdk.android.services.settings.IconRequest.build
import io.realm.Realm
import okhttp3.HttpUrl
import org.json.JSONArray
import org.json.JSONObject


/**
 * Created by B00175 on 2018/3/13.
 */
class DownloadTask : AsyncTask<String, Void, String>() {


    //取MAC
    //private val share = getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE)
    //private val mDeviceAddress = share.getString("mac", "noValue")
    //private val share_token = getSharedPreferences("TOKEN", AppCompatActivity.MODE_PRIVATE)
    //private val token = share_token.getString("token","")

    //jsonBack KEY
    private val TEMPValue = "temperature"
    private val HUMIValue = "humidity"
    private val TVOCValue = "tvoc"
    private val ECO2Value = "eco2"
    private val PM25Value = "pm25"
    private val Created_time = "timestamp"
    private val Longitude = "longitude"
    private val Latitude = "latitude"
    //private val UpLoaded = "UpLoaded"
    //private val MACAddress = "MACAddress"

    override fun onPreExecute() {
        super.onPreExecute()

        // ...
    }

    override fun doInBackground(vararg params: String?): String? {
        val mDeviceAddress = params[0]
        val token = params[1]
        val phpToken = "Bearer " + token

        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse("http://api.mjairql.com/api/v1/getUserData?mac_address")!!.newBuilder()
                .addQueryParameter("mac_address", mDeviceAddress)
        //.addQueryParameter("start_time", "0")
        //.addQueryParameter("end_time", "1520941868267")
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
                .url(url)
                .get()
                .addHeader("authorization",phpToken)
                .build()
        Log.d("Download", "doInBackground")
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return null
            } else {
                val res = response.body()?.string()
                val jsonObj = JSONObject(res)
                val returnResult = jsonObj.get("userData")
                if (returnResult != "connect info error") {
                    val jsonArr: JSONArray = jsonObj.getJSONArray("userData")
                    val jsonArrSize = jsonArr.length()
                    Log.d("DownloadSize", jsonArrSize.toString())
                    val timeStampArr = arrayListOf<Long>()
                    for (i in 0 until jsonArr.length()) {
                        val timeStamp = jsonArr.getJSONObject(i).getString("timestamp").toLong()
                        timeStampArr.add(timeStamp)
                    }
                    Log.d("Download", timeStampArr.toString())

                    val realm = Realm.getDefaultInstance()
                    for (i in 0 until timeStampArr.size) {
                        val query = realm.where(AsmDataModel::class.java).equalTo("Created_time", timeStampArr[i]).findAll()
                        if (query.isEmpty()) {
                            realm.executeTransaction {
                                val asmData = realm.createObject(AsmDataModel::class.java, TvocNoseData.getMaxID())
                                asmData.tvocValue = jsonArr.getJSONObject(i).getString(TVOCValue)
                                asmData.ecO2Value = jsonArr.getJSONObject(i).getString(ECO2Value)
                                asmData.tempValue = jsonArr.getJSONObject(i).getString(TEMPValue)
                                asmData.humiValue = jsonArr.getJSONObject(i).getString(HUMIValue)
                                asmData.pM25Value = jsonArr.getJSONObject(i).getString(PM25Value)
                                asmData.created_time = jsonArr.getJSONObject(i).getString(Created_time).toLong()
                                asmData.latitude = jsonArr.getJSONObject(i).getString(Latitude).toFloat()
                                asmData.longitude = jsonArr.getJSONObject(i).getString(Longitude).toFloat()
                                asmData.upLoaded = "1"
                                asmData.macAddress = mDeviceAddress
                                Log.d("Download", asmData.toString())
                            }
                        }
                    }
                    realm.close()
                    //Log.d("Download",timeStamp)
                }
                Log.d("Download",res.toString())
                val downLoadDone = "DownloadCloudDone"
                return downLoadDone
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun onProgressUpdate(vararg values: Void?) {
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        if (result != null) {
            if (result == "DownloadCloudDone") {
                if (Build.BRAND != "OPPO") {
                    Toast.makeText(MyApplication.applicationContext(), "雲端下載完成", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}