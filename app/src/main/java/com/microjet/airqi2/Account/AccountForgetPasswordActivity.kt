package com.microjet.airqi2.Account

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import com.microjet.airqi2.CustomAPI.GetNetWork
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.R
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern

class AccountForgetPasswordActivity : AppCompatActivity() {
    private var mContext : Context? = null
    private var enterMail : EditText? = null
    private var btn_confirm : Button? = null
    var mything: mything?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        mContext = this@AccountForgetPasswordActivity.applicationContext
        //intent.setClass(this@AccountForgetPasswordActivity.mContext, AccountManagementActivity.kt::class.java)
        initActionBar()
        // get reference to all views
        enterMail = this.findViewById(R.id.enter_Email)
        btn_confirm =this.findViewById(R.id.enter_email_confirm)
        mything = mything(btn_confirm!!, false, "https://mjairql.com/api/v1/forgotPassword")

        // 03/12
        btn_confirm?.setOnClickListener{
            if (isEmail(enterMail?.text.toString().trim()))
            {
                if (GetNetWork.isFastGetNet)
                {
                    if (Utils.isFastDoubleClick)
                    {
                        //showDialog("按慢一點太快了")
                        showDialog(getString(R.string.tooFast))
                    }
                    else
                    {
                        btn_confirm?.isEnabled = false
                        forgetPassWordAsyncTasks().execute(mything)
                    }
                }
                else
                {
                    //showDialog("請連接網路")
                    showDialog(getString(R.string.checkConnection))
                }
            }
            else
            {
                //showDialog("信箱輸入不正確")
                showDialog(getString(R.string.dialog_Correct_Mail_Format))
            }
        }

    }//onCreat

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    // ***** 2018/04/17 Add function for intent activity *********************** //
    private fun accountManagementShow() {
        val i: Intent? = Intent(this, AccountManagementActivity::class.java)
        startActivity(i)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home //對用戶按home icon的處理，本例只需關閉activity，就可返回上一activity，即主activity。
            -> {
                Log.d(this.javaClass.simpleName,"home icon")
                accountManagementShow()
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    var mForgetPassword=""

    @SuppressLint("StaticFieldLeak")
    private inner class forgetPassWordAsyncTasks : AsyncTask<mything, Void, String>() {
        override fun doInBackground(vararg params: mything): String? {
            try {
                var response: okhttp3.Response? = null
                val forgetPassWordMail =enterMail?.text.toString()
                 Log.e("輸入的內容信箱", forgetPassWordMail)
                val client = OkHttpClient()
                val mediaType = MediaType.parse("application/x-www-form-urlencoded")
                val body = RequestBody.create(mediaType, "email=" + forgetPassWordMail)
                val request = Request.Builder()
                        .url("https://mjairql.com/api/v1/forgotPassword")
                        .post(body)
                       // .addHeader("authorization", "Bearer " )
                        .addHeader("cache-control", "no-cache")
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .build()

                //上傳資料
                response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    params[0].myBlean = false
                    val tempBody: String = response.body()!!.string().toString()
                    Log.e("忘記密碼正確回來", tempBody)
                    val responseContent = JSONObject(tempBody)
                    //mForgetPassword = responseContent.getJSONObject("success").getString("")
                    mForgetPassword = "密碼已經寄送，請至登入頁面輸入密碼。"
                } else {
                    params[0].myBlean = false
                    Log.e("更改密碼失敗", response.body()!!.string())
                    mForgetPassword = "忘記密碼失敗，請從新輸入正確的Mail。"
                    runOnUiThread(java.lang.Runnable {
                        params[0].button!!.isEnabled = true
                        btn_confirm?.isEnabled=true
                    })
                }
            }
            catch (e:Exception){
                when (e){
                    is IOException->{e.printStackTrace()}
                    is JSONException->{e.printStackTrace()}
                }
            }
            return mForgetPassword
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result == "密碼已經寄送，請至登入頁面輸入密碼。") {
                val Dialog = android.app.AlertDialog.Builder(this@AccountForgetPasswordActivity).create()
                //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
                //Dialog.setTitle("提示")
                //Dialog.setTitle(getString(R.string.remind))
                Dialog.setMessage(result.toString())
                Dialog.setCancelable(false)//讓返回鍵與空白無效
                //Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
                Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.confirm))
                { _, _ ->
                    val i: Intent? = Intent(applicationContext, AccountManagementActivity::class.java)
                    startActivity(i)
                    finish()
                }
                Dialog.show()
            } else {

                showDialog(result!!)
            }
        }


    }


    private fun cheackRestPassWord(resetword: String, checkword: String):Boolean{
        val RestPW: String = resetword
        val CheckPW: String = checkword
        return  RestPW.equals (CheckPW)
    }


    //20180311
    fun isEmail(strEmail: String): Boolean {
        //val strPattern = ("\\w+@\\w+\\.\\w+")
        val strPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
        val p = Pattern.compile(strPattern)
        val m = p.matcher(strEmail)
        return m.matches()
    }


    //20180311
    fun showDialog(msg:String){
        val Dialog = android.app.AlertDialog.Builder(this@AccountForgetPasswordActivity).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        //Dialog.setTitle(getString(R.string.remind))
        Dialog.setMessage(msg.toString())
        Dialog.setCancelable(false)//讓返回鍵與空白無效
        //Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
        Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.confirm))
        { dialog, _ ->
            dialog.dismiss()
            //finish()
        }
        Dialog.show()
    }

}
