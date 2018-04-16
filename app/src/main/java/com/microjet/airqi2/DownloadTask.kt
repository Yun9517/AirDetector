package com.microjet.airqi2

import android.app.Application
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.microjet.airqi2.Definition.SavePreferences
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
class DownloadTask(input: Context) : AsyncTask<String, Int, String>() {


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
    private var mContext: Context = input
    private var mProgressBar: ProgressDialog?=null


    override fun onPreExecute() {
        super.onPreExecute()
        //白告: 總數最大值，無法更為0，預設值似乎不能低於100
        if (mContext!=null) {
            mProgressBar = ProgressDialog(mContext)
            mProgressBar?.setMessage("下載資料中")
            mProgressBar?.isIndeterminate = false//功能不知道
            mProgressBar?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)//STYLE_SPINNER
            mProgressBar?.setCancelable(false)
            mProgressBar?.show()
        }
    }
    //主要背景執行
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
                .addHeader("authorization", phpToken)
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
                    //攝取userData的內容
                    val jsonArr: JSONArray = jsonObj.getJSONArray("userData")
                    //讀取並計算字元個數
                    val jsonArrSize = jsonArr.length()
                    Log.d("DownloadSize", jsonArrSize.toString())
                    val timeStampArr = arrayListOf<Long>()
                    for (i in 0 until jsonArr.length()) {
                        val timeStamp = jsonArr.getJSONObject(i).getString("timestamp").toLong()
                        timeStampArr.add(timeStamp)
                    }
                    Log.d("Download", timeStampArr.toString())
                    val realm = Realm.getDefaultInstance()
                    Log.d("timeStampArr.size", timeStampArr.size.toString())
                    for (i in 0 until timeStampArr.size) {
                        val time = timeStampArr[i]
                        val query = realm.where(AsmDataModel::class.java).equalTo("Created_time", time).findAll()
                        if (query.isEmpty() && time > 1514736000000) {
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
                        //val ii = ((i / timeStampArr.size.toFloat()) * 100).toInt()  取百分比的進度條
                        publishProgress(i,timeStampArr.size)        //取總比數的進度條
                    }
                    realm.close()
                    //Log.d("Download",timeStamp)
                    Log.d("Download", res.toString())
                    return "DownloadCloudDone"
                } else {
                    Log.d("Download", res.toString())
                    return "Error"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    //更新視窗的改變
    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
        mProgressBar?.progress = values[0]!!
        mProgressBar?.max =values[1]!!
    }

    override fun onPostExecute(result: String?) {
        super.onPostExecute(result)
        if (result != null) {
            when (result) {
                "DownloadCloudDone" -> {
                    if (Build.BRAND != "OPPO") {
                        Toast.makeText(MyApplication.applicationContext(), "雲端下載完成", Toast.LENGTH_SHORT).show()
                    }
                }
                "Error" -> {
                    if (Build.BRAND != "OPPO") {
                        Toast.makeText(MyApplication.applicationContext(), "下載失敗", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            mProgressBar?.dismiss()
        }
    }
}