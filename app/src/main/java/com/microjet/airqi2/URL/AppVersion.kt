package com.microjet.airqi2.URL

import android.app.Application
import android.os.AsyncTask
import android.util.Log
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Created by B00055 on 2018/5/11.
 */
class AppVersion (release:Int,internal:Int,external:Int ):AsyncTask<String, Long, Boolean>()  {
    var release_version=release
    var internal_version=internal
    var external_version=external
    override fun onPreExecute() {

    }

    override fun doInBackground(vararg params: String?): Boolean {
      //  val result=getSWversion()
        return getSWversion()
    }

    override fun onPostExecute(result: Boolean) {
        if (result)//has new swVersion
        {
            Log.d(javaClass.simpleName,"has new sw")
        }
        else {
            Log.d(javaClass.simpleName,"no new sw")
        }//no new version so do nothing
    }
    fun getSWversion():Boolean{
        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse(MjAQIUrl.getSWversion)!!.newBuilder()
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
                .url(url)
                .get()
                .build()
        var hasNewSW =false
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                hasNewSW = false
            } else {
                val res = response.body()?.string()
                response.body()?.close()
                val jsonObj = JSONObject(res)
                val returnResult = jsonObj.getJSONObject("app_version")

                if( returnResult.getInt("release_version")>release_version)
                { hasNewSW=true }
                else{
                    if (returnResult.getInt("internal_version")>internal_version)
                    { hasNewSW=true }
                    else {
                        if (returnResult.getInt("external_version")>external_version)
                        { hasNewSW=true }
                        else
                        { }//do nothing because hasNewSW default is false
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(javaClass.simpleName,e.message)
            return false
        }
        return hasNewSW
    }
}