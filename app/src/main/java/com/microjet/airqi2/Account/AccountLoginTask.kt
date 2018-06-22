package com.microjet.airqi2.Account

import android.os.AsyncTask

/**
 * Created by B00190 on 2018/6/22.
 */
class AccountLoginTask : AsyncTask<String, Int, String>(){
    private var setting = 0
    //主要背景執行
    override fun onPreExecute() {
        super.onPreExecute()

    }

    override fun doInBackground(vararg params: String?): String? {

        return "ReconnectNetwork"
    }

    override fun onProgressUpdate(vararg values: Int?) {
        super.onProgressUpdate(*values)

    }


    override fun onPostExecute(result: String?) {

    }

}