package com.microjet.airqi2.Account

import android.annotation.SuppressLint
import android.app.Activity
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
import kotlinx.android.synthetic.main.activity_forget_password.*
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class AccountForgetPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        initActionBar()

        enter_email_confirm.setOnClickListener {
            checkNetwork()
        }
    }


    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        if(AccountObject.accountForgetStrResult != null &&  AccountObject.accountForgetStrResult !=""){
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
            "ForgetTaskResult"-> processResult()
        }
    }
    private fun checkNetwork() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            resetPassword()
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

    private fun resetPassword() {
        when (isEmail(enter_Email?.text.toString()) && enter_Email?.text.toString() != "") {
            true -> forgetPassWordTasks(this).execute(enter_Email?.text.toString())
            false -> {
                val newFrage = CheckFragment().newInstance(R.string.remind, R.string.errorMail_address, this, 1, "dismiss")
                newFrage.show(fragmentManager, "dialog")
            }
        }
    }

    private fun processResult(){
        AccountObject.closeWaitDialog(this)
        //處理結果
        if(AccountObject.accountForgetStrResult != null &&  AccountObject.accountForgetStrResult !=""){
            //處理結果
            when(AccountObject.accountForgetStrResult){
                "successNetwork" ->{ val newFrage = CheckFragment().newInstance(R.string.remind, R.string.dialog_Password_Change_Successfully, this, 1, "dismiss").show(fragmentManager, "dialog")}
                "ResponseError" ->{ val newFrage = CheckFragment().newInstance(R.string.remind, R.string.dialog_Mail_Verification_Failed, this, 1, "dismiss").show(fragmentManager, "dialog")}
                "ReconnectNetwork" ->{ val newFrage = CheckFragment().newInstance(R.string.remind, R.string.checkConnection, this, 1, "dismiss").show(fragmentManager, "dialog")}
            }
            AccountObject.accountForgetStrResult =""
        }
    }

    @SuppressLint("StaticFieldLeak")
    private inner class forgetPassWordTasks(gettedActivity: Activity) : AsyncTask<String, Void, String>() {
        private val TAG: String = "forgetPassWordTasks"
        private val useActivity: Activity = gettedActivity

        override fun onPreExecute() {
            super.onPreExecute()
            AccountObject.openWatiDialog(useActivity)
        }

        override fun doInBackground(vararg params: String?): String {
            val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            val email = "email="+params[0]
            val body = RequestBody.create(mediaType, email)
            val request = Request.Builder()
                    .url("http://api.mjairql.com/api/v1/forgotPassword")
                    .post(body)
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .addHeader("Cache-Control", "no-cache")
                    .build()
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val res: String = response.body()!!.string().toString()
                    Log.e(TAG, res)
                    return "successNetwork"
                }else{
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
                AccountObject.accountForgetStrResult =result
                EventBus.getDefault().post(BleEvent("ForgetTaskResult"))
                Log.e(TAG, result)
            }
            /*

            */
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
}
