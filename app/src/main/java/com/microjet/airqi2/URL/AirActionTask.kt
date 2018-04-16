package com.microjet.airqi2.URL

import android.os.AsyncTask
import android.util.Log
import android.app.ProgressDialog
import android.content.Context
import com.microjet.airqi2.URL.MjAQIUrl.postFWversion
import okhttp3.*

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import android.content.SharedPreferences
import android.content.Context.MODE_PRIVATE
import com.microjet.airqi2.BleEvent
import com.microjet.airqi2.Definition.SavePreferences.AirActionTask_KEY
import org.greenrobot.eventbus.EventBus
import java.io.*
import java.net.URL
import com.microjet.airqi2.URL.AirActionTask.PostDownload




/**
 * Created by B00055 on 2018/3/28.

關於 AsyncTask 的使用，有幾項原則必須遵守：
* AsyncTask 必須在 UI 主執行緒載入(JELLY_BEAN 版本開始會自動執行此事)。
* 必須在 UI 主執行緒建立 AsyncTask。
* 必須在 UI 主執行緒呼叫 AsyncTask.execute()。
* 不要自行呼叫 onPreExecute()，onPostExecute()，doInBackground()， onProgressUpdate()。
* AsyncTask 只能執行一次。
 *  new AsyncTask().execute(url1, url2, url3);//傳入參數的與啟動
 *  */
 class AirActionTask (): AsyncTask<String, Long, ArrayList<String>?>() {
    // AsyncTask的 3 個傳入的參數
    // String 就是 Params 參數的類別  doInBackground(vararg params: String?)
    // Long 就是 Progress 參數的類別  onProgressUpdate(vararg values: Long?)
    // ArrayList<String> 就是 Result 參數的類別  onPostExecute(result: ArrayList<String>?)

  //  AsyncTask 的運作有 4 個階段：
  //  onPreExecute -- AsyncTask 執行前的準備工作，例如畫面上顯示進度表，
  //  doInBackground -- 實際要執行的程式碼就是寫在這裡，
  //  onProgressUpdate -- 用來顯示目前的進度，
  //  onPostExecute -- 執行完的結果 - Result 會傳入這裡。
    /*
         Constructor
     */
    private var mProgressBar:ProgressDialog?=null //該方法已被棄用，之後必須找到其他的替代方式

    var mContext: Context? = null
    private var urlV:String?=null
    private var urlDT:String?=null
    private var callback: PostDownload? = null
    private var mPreference: SharedPreferences? = null

    init {//主建構元
      //  mContext=input
    }
    constructor(input: Context):this(){//第二建構元
        mContext=input
    //    callback=callbackInput
    }
    constructor(input: Context,urlVersion:String, urlDeviceType:String):this(){//第二建構元
        mContext=input
        urlV=urlVersion
        urlDT=urlDeviceType

    }
    override fun onPreExecute() {
        //在此要生成進度條
        if (mContext!=null) {
            mPreference=mContext?.getSharedPreferences(AirActionTask_KEY,MODE_PRIVATE)
            if (urlV==null) {
                mProgressBar = ProgressDialog(mContext)
                mProgressBar?.setMessage("Download File")
                mProgressBar?.isIndeterminate = false//功能不知道
                mProgressBar?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                mProgressBar?.setCancelable(true)//
                mProgressBar?.show()
            }
        }
        Log.d(javaClass.simpleName,"我在PreExecute")
        super.onPreExecute()
    }
    override fun doInBackground(vararg params: String?): ArrayList<String>? {
        // 呼叫 publishProgress() 以更新 UI 畫面,
        // 可藉由此方式更新畫面上的進度表

        val result: ArrayList<String> = ArrayList()
        var response:String?=null
        when (params[0]){
            "postFWVersion"->{
                if (mContext!=null) {
                    mPreference=mContext?.getSharedPreferences(AirActionTask_KEY,MODE_PRIVATE)
                }
                result.add("postFWVersion")
                response= doFWVersionCheck()
            }
            "downloadFWFile"->{
                val fileUrl= mPreference?.getString("FilePath", "")
                result.add("downloadFWFile")
                response=downloadFWFile(fileUrl!!)
            }
            else-> {

            }
        }
        result.add(response!!)
        Log.d(javaClass.simpleName,"我在doInBackground")
        return result
    }
        //run after call publishProgress()
     override fun onProgressUpdate(vararg values: Long?) {
        // 這裡接收傳入的 progress 值, 並更新進度表畫面
        // 參數是 Integer 型態的陣列
        // 但是因為在 doInBackground() 只傳一個參數
        // 所以將progress[0] 取得傳入參數即可
        if (mContext!=null) {
            mProgressBar?.max = values[1]!!.toInt()
            mProgressBar?.progress = values[0]!!.toInt()
        }
        Log.d(javaClass.simpleName,"我在onProgressUpdate")
    }

    override fun onPostExecute(result: ArrayList<String>?) {
        //  showDialog("Downloaded " + result + " bytes");
        when (result?.get(0)) {
            "postFWVersion"->{
                when(result?.get(1)){
                //無新版本
                    "version latest" ->{
                        Log.d(javaClass.simpleName,"version latest")
                    }
                //資訊有錯
                     "device type error"->{
                         Log.d(javaClass.simpleName,"device type error")
                     }
                     "internet error"->{
                        Log.d(javaClass.simpleName,"internet error")
                    }
                     "catch error"->{
                        Log.d(javaClass.simpleName,"catch error")
                    }
                    else->{
                        mPreference?.edit()?.putString("FilePath", result[1])?.apply()//將路徑存起來
                        EventBus.getDefault().post(BleEvent("New FW Arrival "))//使用event 通知有新的FW版本
                    }

                }

            }
            "downloadFWFile"->{
                mPreference?.edit()?.putString("FilePath", "")?.apply()//將路徑關閉
                EventBus.getDefault().post(BleEvent("Download Success"))
            }
            else ->{

            }
        }
        if (mProgressBar!=null) {
            mProgressBar?.dismiss()
        }
        else{

        }
        Log.d(javaClass.simpleName,"我在onPostExecute")
    }
    private fun doFWVersionCheck(): String {
        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse(postFWversion)!!.newBuilder()
                .addQueryParameter("deviceType", urlDT)
                .addQueryParameter("version", urlV)
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
                .url(url)
                .get()
                .build()
        var dataUrl ="internet error"
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                dataUrl = "internet error"
            } else {
                val res = response.body()?.string()
                response.body()?.close()
                val jsonObj = JSONObject(res)
                val returnResult = jsonObj.get("message")
                when (returnResult) {
                    //無新版本
                    "version latest" ->{ dataUrl=returnResult.toString()}
                    //資訊有錯
                    "device type error"->{dataUrl=returnResult.toString()}
                    else->{ //有新版本
                        dataUrl=jsonObj.getString("url")
                  }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(javaClass.simpleName,e.message)
            return "catch error"
        }
        return dataUrl
    }

    private fun downloadFWFileold(url:String):String{
        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse(url)!!.newBuilder()
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
                .url(url)
                .get()
                .build()
            val response = client.newCall(request).execute()
            if (response.code() == 200) {
                try {
                    var inputFile = response.body()?.byteStream()//開啟讀檔串流
                    var downloaded: Long = 0
                    val target = response.body()?.contentLength()//獲取檔案大小
                    publishProgress(0, target)
                     var buff=ByteArray(1024)//一次讀1024 byte

                    val file = File(mContext!!.cacheDir, "FWupdate.zip")
                    if (file.exists())
                        file.delete()
                    val outputStream=FileOutputStream(file)
                    var count:Int?=null
                    while ({ count = inputFile?.read(buff); count }() != -1)
                    {
                        outputStream.write(buff)
                        downloaded += count!!
                        publishProgress(downloaded, target)
                    }
                    outputStream.flush()
                    outputStream.close()
                    response.body()?.close()
                }
                catch(e: Exception) {
                    Log.v(javaClass.simpleName,e.toString())
                }
        }
        return "Download Success"
    }

    private fun downloadFWFile(url:String):String{
        val url = URL(url)
        val connection = url.openConnection()
        try{
            connection.connect()
            val lenghtOfFile = connection.contentLength.toLong()
            val input = BufferedInputStream(url.openStream())
            val file = File(mContext!!.cacheDir, "FWupdate.zip")
            val output = FileOutputStream(file) //context.openFileOutput("content.zip", Context.MODE_PRIVATE);
            val data = ByteArray(1024)
            var total: Long = 0
            var count: Int=0
            while ({count = input.read(data);count }() != -1)
            {
                total += count
                publishProgress(total,lenghtOfFile)
                output.write(data, 0, count)
            }
            output.flush()
            output.close()
            input.close()
        }
        catch(e: Exception) {
            Log.v(javaClass.simpleName,e.toString())
        }
        return "Download Success"
    }

    interface  PostDownload {
          fun downloadDone(fd: File)
    }
}