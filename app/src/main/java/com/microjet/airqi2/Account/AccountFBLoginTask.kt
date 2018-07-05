package com.microjet.airqi2.Account

import android.os.AsyncTask
import android.util.Log

/**
 * Created by B00190 on 2018/7/5.
 */
class AccountFBLoginTask: AsyncTask<String, Int, String>() {

    //主要背景執行
    override fun doInBackground(vararg params: String?): String? {
        val token = params[0]
        val FBToken = "Bearer " + token
        val social_type: String? = params[1]
        Log.e("FBToken",FBToken)
        Log.e("FBToken",social_type)

        return "ReconnectNetwork"//null
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)
    }

    override fun onPostExecute(result: String?) {

    }

}