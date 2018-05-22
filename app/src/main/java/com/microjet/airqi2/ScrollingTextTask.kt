package com.microjet.airqi2

import android.os.AsyncTask
import android.util.Log
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

/**
 * Created by B00190 on 2018/5/22. by 白~~~~~~~~~~~~~~~~~~~~~~~~~~告
 */
class ScrollingTextTask : AsyncTask<String, Int, String>() {
    //主要背景執行
    override fun doInBackground(vararg params: String?): String? {
        val scroTitle: Array<String>
        val scroUrl: Array<String>
        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse("https://www.addwii.com/api/get_recent_posts/?count=3")!!.newBuilder()
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
                .url(url)
                .build()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return "ResponseError"//null
            } else {
                val res = response.body()?.string()
                Log.e("get_scrolling",res)
                val jsonObj = JSONObject(res)
                //取出posts內容
                val resultArray = jsonObj.getJSONArray("posts")
                for(i in 0 until resultArray.length()){
                    val jsonObjScrolling = resultArray.getJSONObject(i)
                    TvocNoseData.scrollingTitle.add(jsonObjScrolling.getString("title"))
                    TvocNoseData.scrollingUrl.add(jsonObjScrolling.getString("url"))
                }
                //查看內容
                for (e in TvocNoseData.scrollingTitle) {
                    Log.e("scrollingTitle的內容",e + "\t")
                }

           }
        } catch (e: Exception) {
            e.printStackTrace()
            return "ReconnectNetwork"//null
        }
        return null
    }

}