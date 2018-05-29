package com.microjet.airqi2.URL

import android.os.AsyncTask
import android.util.Log
import com.microjet.airqi2.BleEvent
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import org.greenrobot.eventbus.EventBus
import org.json.JSONObject

/**
 * Created by B00055 on 2018/5/11.
 */
class AppMenuTask : AsyncTask<String, Long, ArrayList<String>>() {

    override fun onPreExecute() {

    }

    override fun doInBackground(vararg params: String?): ArrayList<String> {

        Log.e("JSON Parser", "Start Execute......")
        return getAppMenu()
    }

    override fun onPostExecute(result: ArrayList<String>) {
        if (result.isNotEmpty()) {
            val urlEvent = BleEvent("new URL get")
            // 2018/05/29 Add "introduction" & "ourStory", modify sequence. Thanks the original creator!
            urlEvent.userExp = result[0]
            urlEvent.introduction = result[1]
            urlEvent.buyProduct = result[2]
            urlEvent.ourStory = result[3]


            EventBus.getDefault().post(urlEvent)
            Log.d(javaClass.simpleName, "has new url")
        } else {
            Log.d(javaClass.simpleName, "no new url")
        }
    }

    fun getAppMenu(): ArrayList<String> {
        val client = OkHttpClient()
        val urlBuilder = HttpUrl.parse(MjAQIUrl.getAddMenu)!!.newBuilder()
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
                .url(url)
                .get()
                .build()

        val stringUrl = ArrayList<String>()
        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                stringUrl.clear()
                Log.e("JSON Parser", "Response: ${response.isSuccessful}")
            } else {
                val res = response.body()?.string()
                response.body()?.close()
                val jsonObj = JSONObject(res)
                val returnResult = jsonObj.getJSONArray("menu")
                Log.e("JSON Parser", "Return Result: $returnResult")

                // 2018/05/29 Add "introduction" & "ourStory", modify sequence. Thanks the original creator!
                val userExperience = JSONObject(returnResult[0].toString())["url"].toString()
                val introduction = JSONObject(returnResult[1].toString())["url"].toString()
                val productBuy = JSONObject(returnResult[2].toString())["url"].toString()
                val ourStory = JSONObject(returnResult[3].toString())["url"].toString()

                stringUrl.add(userExperience)
                stringUrl.add(introduction)
                stringUrl.add(productBuy)
                stringUrl.add(ourStory)

                Log.e("JSON Parser", "User Experience: $userExperience, Product Buy: $productBuy, Introduction: $introduction, Our Story: $ourStory")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.d(javaClass.simpleName, e.message)
            return stringUrl
        }
        return stringUrl
    }
}