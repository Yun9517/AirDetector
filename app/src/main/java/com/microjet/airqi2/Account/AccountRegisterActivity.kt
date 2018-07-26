package com.microjet.airqi2.Account

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import com.microjet.airqi2.BleEvent
import com.microjet.airqi2.Fragment.CheckFragment
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.activity_register.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern


class AccountRegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initActionBar()

        nextStep.setOnClickListener {
            checkNetwork()
        }
    }

    private fun checkNetwork() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            registerAccount()
        } else {
            val newFrage = CheckFragment().newInstance(R.string.remind, R.string.checkConnection, this, 1, "dismiss")
            newFrage.show(fragmentManager, "dialog")
        }
    }

    private fun isEmail(strEmail: String?): Boolean {
        val strPattern = ("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        val p = Pattern.compile(strPattern)
        val m = p.matcher(strEmail)
        return m.matches()
    }

    private fun registerAccount() {
        when (isEmail(email?.text.toString()) && email?.text.toString() != "") {
            true -> registerTasks().execute(email?.text.toString())
            false -> {
                val newFrage = CheckFragment().newInstance(R.string.remind, R.string.errorMail_address, this, 1, "dismiss")
                newFrage.show(fragmentManager, "dialog")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        if(AccountObject.accountRegisterStrResult != null &&  AccountObject.accountRegisterStrResult !=""){
            processResult()
        }
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe
    fun onEvent(bleEvent: BleEvent) {
        /* 處理事件 */
        Log.d("AirAction", bleEvent.message)
        when (bleEvent.message) {
            "RegisterTaskResult"-> processResult()
        }
    }

    private fun processResult(){
        AccountObject.closeWaitDialog(this)
        //處理結果
        if(AccountObject.accountRegisterStrResult != null &&  AccountObject.accountRegisterStrResult !=""){
            //處理結果
            when(AccountObject.accountRegisterStrResult){
                "successNetwork" ->{ val newFrage = CheckFragment().newInstance(R.string.remind,R.string.dialog_Mail_Has_Sent, this, 1, "dismiss").show(fragmentManager, "dialog")}
                "ResponseError" ->{ val newFrage = CheckFragment().newInstance(R.string.remind,R.string.dialog_Mail_Registered, this, 1, "dismiss").show(fragmentManager, "dialog")}
                "ReconnectNetwork" ->{ val newFrage = CheckFragment().newInstance(R.string.remind, R.string.checkConnection, this, 1, "dismiss").show(fragmentManager, "dialog")}
            }
            AccountObject.accountRegisterStrResult =""
        }
    }

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home //對用戶按home icon的處理，本例只需關閉activity，就可返回上一activity，即主activity。
            -> {
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class registerTasks : AsyncTask<String, Void, String>() {
        val TAG: String? = "registerTasks"
        override fun onPreExecute() {
            super.onPreExecute()
            AccountObject.openWatiDialog(this@AccountRegisterActivity)
        }

        override fun doInBackground(vararg params: String?): String {
            val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            val email = "email=" + params[0]
            val body = RequestBody.create(mediaType, email)
            val request = Request.Builder()
                    .url("https://api.mjairql.com/api/v1/register")
                    .post(body)
                    .addHeader("Cache-Control", "no-cache")
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build()
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val res: String = response.body()!!.string().toString()
                    Log.e(TAG, res)
                    return "successNetwork"
                } else {
                    val res: String = response.body()!!.string().toString()
                    Log.e(TAG, res)
                    return "ResponseError"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return "ReconnectNetwork"
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            Log.e(TAG, result)
            if(result != null){
                AccountObject.accountRegisterStrResult =result
                EventBus.getDefault().post(BleEvent("RegisterTaskResult"))
                Log.e(TAG, result)
            }
        }

    }


}
