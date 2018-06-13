package com.microjet.airqi2

import android.content.ContentValues.TAG
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import okhttp3.*
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class SignOutTask(input: Context): AsyncTask<String, Int, String>() {

    private var mContext: Context? = input

    override fun doInBackground(vararg params: String): String? {
        try{
            val client = OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .build()
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            val body: RequestBody = RequestBody.create(mediaType, "registration_id=" + FirebaseInstanceId.getInstance().token)
            Log.e("par", params.first())
            val request = Request.Builder()
                    .url("https://mjairql.com/api/v1/logout")
                    .post(body)
                    .addHeader("authorization",params.first())//get token
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Cache-Control", "no-cache")
                    .build()
            val response = client.newCall(request).execute()
            Log.e("backLogOut", response.body()?.string().toString())

            if (!response.isSuccessful) { Log.d(TAG, "ERROR") }
            else {
                if (mContext != null) {
                    val shareToKen = mContext?.getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
                    shareToKen!!.edit().putString("token", "").apply()
                    shareToKen!!.edit().putString("LoginPassword", "").apply()
                }
            }
        } catch (e: Exception) {
            Log.e("backLogOutE", "restError")
            Log.e(TAG, e.toString())
        }
        return null
    }

}
