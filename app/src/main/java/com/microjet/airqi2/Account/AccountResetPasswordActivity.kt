package com.microjet.airqi2.Account

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
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

class AccountResetPasswordActivity : AppCompatActivity() {
    private var mContext : Context? = null
    // 03/13
    private var enterPassword : EditText? = null
    private var checkPassword : EditText? = null
    private var btn_confirm_Modify : Button? = null

    private var restpassword_Result : String? = null

    var mything: mything? = null
    //20180313
    private var MyToKen: String? = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        mContext = this@AccountResetPasswordActivity.applicationContext
        initActionBar()

        enterPassword = this.findViewById(R.id.change_Password_first)
        checkPassword = this.findViewById(R.id.change_Password_second)
        btn_confirm_Modify = this.findViewById(R.id.confirm_Modify)
        // 03/12
        //goResetAsyncTasks().execute("https://mjairql.com/api/v1/editUserData")

        mything = mything(btn_confirm_Modify!!, false, "https://mjairql.com/api/v1/register")


        btn_confirm_Modify?.setOnClickListener {
            val shareToKen = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
            MyToKen = shareToKen.getString("token", "")

            if (checkRestPassWord(enterPassword?.text.toString().trim(), checkPassword?.text.toString().trim()) && enterPassword?.text.toString() != "") {
                if (GetNetWork.isFastGetNet) {
                    if (Utils.isFastDoubleClick) {
                        //showDialog("按慢一點太快了")
                        showDialog(getString(R.string.tooFast))
                    } else {
                        btn_confirm_Modify?.isEnabled = false
                        if (isConnected()) {
                            resetPassWordAsyncTasks().execute(mything)
                        } else {
                            showDialog(getString(R.string.checkConnection))
                        }
                    }
                    //showDialog("請連接網路")
                } else {
                    //showDialog("請連接網路")
                    showDialog(getString(R.string.checkConnection))
                }
            } else {
                //showDialog("密碼輸入不正確")
                if(enterPassword?.text.toString().trim() != checkPassword?.text.toString().trim()) {
                    if (enterPassword?.text.toString() != " " || checkPassword?.text.toString() != " "){
                        showDialog(getString(R.string.dialog_Password_noMatch))
                    }
                } else {
                    showDialog(getString(R.string.dialog_Password_Empty))
                }
            }
        }
    }

    // 2018/03/30

    private fun isConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    // ***** 2018/04/17 Add function for intent activity *********************** //
    private fun accountActiveShow() {
        val i: Intent? = Intent(this, AccountActiveActivity::class.java)
        startActivity(i)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home //對用戶按home icon的處理，本例只需關閉activity，就可返回上一activity，即主activity。
            -> {
                //accountActiveShow()
                finish()
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }


    var resetpassword_Result : String?=""
    var password : String? =""

        @SuppressLint("StaticFieldLeak")
    private inner class resetPassWordAsyncTasks : AsyncTask<mything, Void, String>() {
        override fun doInBackground(vararg params: mything): String? {
            try {
                var response: okhttp3.Response? = null
                val restpassword = enterPassword?.text
                val client = OkHttpClient()
                val mediaType = MediaType.parse("application/x-www-form-urlencoded")
                val body = RequestBody.create(mediaType, "password=" + restpassword)
                val request = Request.Builder()
                        .url("https://mjairql.com/api/v1/editUserData")
                        .post(body)
                        .addHeader("authorization", "Bearer " + MyToKen)
                        .addHeader("cache-control", "no-cache")
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .build()

                //上傳資料
                response = client.newCall(request).execute()
                val any = if (response.isSuccessful) {

                    params[0].myBlean = false
                    try {
                        val tempBody: String = response.body()!!.string().toString()
                        Log.e("修改密碼正確回來", tempBody)
                        val responseContent = JSONObject(tempBody)
                        resetpassword_Result = responseContent.getString("success")
                    } catch (e: JSONException) {
                        e.printStackTrace()
                        Log.e("修改密碼正確回來的錯誤", e.toString())
                    }
                    restpassword_Result = getString(R.string.dialog_Password_Change_Successfully) //密碼已經修改，請至登入頁面輸入帳密。
                    val shareToKen = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
                    shareToKen.edit().putString("token","").apply()
                    //showDialog(restpassword_Result!!)
                } else {
                    params[0].myBlean = false
                    Log.e("改密碼錯誤回來", response.body()!!.string())
                    restpassword_Result = getString(R.string.dialog_Password_noMatch) //修改密碼失敗
                    runOnUiThread(java.lang.Runnable {
                        params[0].button!!.isEnabled = true
                        btn_confirm_Modify?.isEnabled = true
                    })

                }
                //Toast.makeText(mContext, response.toString(), Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            return restpassword_Result
        }

            // 2018/03/30

            private fun isConnected(): Boolean {
                val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val networkInfo = cm.activeNetworkInfo
                return networkInfo != null && networkInfo.isConnected
            }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result == getString(R.string.dialog_Password_Change_Successfully)) {
                val Dialog = android.app.AlertDialog.Builder(this@AccountResetPasswordActivity).create()
                //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
                //Dialog.setTitle("提示")
                Dialog.setTitle(getString(R.string.remind))
                Dialog.setMessage(result.toString())
                Dialog.setCancelable(false)//讓返回鍵與空白無效
                //Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
                Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.confirm))
                { dialog, _ ->

                    AccountObject.activityAccountActive?.finish()
                    val intent = Intent()
                    intent.setClass(this@AccountResetPasswordActivity.mContext, AccountManagementActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                Dialog.show()
            } else {

                showDialog(result!!)
            }
        }


    }


    private fun checkRestPassWord(resetPassword: String, checkPassword: String): Boolean{
        val ResetPW: String = resetPassword
        val CheckPW: String = checkPassword
        return  ResetPW.equals (CheckPW)
    }


        //20180311
        fun showDialog (msg:String) {
            val Dialog = android.app.AlertDialog.Builder(this@AccountResetPasswordActivity).create()
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
