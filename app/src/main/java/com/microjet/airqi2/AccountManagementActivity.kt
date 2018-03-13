package com.microjet.airqi2

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.github.mikephil.charting.utils.Utils
import com.microjet.airqi2.CustomAPI.GetNetWork
import com.microjet.airqi2.R.id.text_Account_status
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.regex.Pattern


class AccountManagementActivity : AppCompatActivity() {

    private var mContext: Context? = null

    var et_user_name: EditText? = null
    var et_password: EditText? = null

    var userEmail = ""
    var userPassword = ""
    private var login_Result: String? = null
    // private var loginl_Result: String ? = null
    var mything:mything?=null
    var btn_submit:Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mContext = this@AccountManagementActivity.applicationContext

        initActionBar()

        // get reference to all views
        et_user_name = findViewById<EditText>(R.id.email)
        et_password = findViewById<EditText>(R.id.password)
        btn_submit = findViewById<Button>(R.id.login)
        var forgot_password = findViewById<TextView>(R.id.forgotPassword)
        var create_account = findViewById<TextView>(R.id.newAccount)
        var register_mail_Result: String?


        val bundle = intent.extras
        if (bundle != null) {
            userEmail = bundle.getString("email", "")
            userPassword = bundle.getString("pwd", "")
            et_user_name?.setText(userEmail)
            et_password?.setText(userPassword)

            Log.e("ㄍㄋㄋAndy", userEmail + userPassword)
        }


        create_account.setOnClickListener {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            val intent = Intent()
            intent.setClass(this@AccountManagementActivity.mContext, AccountRegisterActivity::class.java)
            //startActivityForResult(intent,1)
            startActivity(intent)
            //finish()
            //                    }else {

//                        register_mail_Result = "請輸入正確的E-mail地址"
//                        showDialog(register_mail_Result!!)
//                    }
            // }
        }

        forgot_password.setOnClickListener {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            val intent = Intent()
            intent.setClass(this@AccountManagementActivity.mContext, AccountForgetPassword::class.java)
            //startActivityForResult(intent,1)
            startActivity(intent)
            //finish()
        }

        mything = mything(btn_submit!!, false, "https://mjairql.com/api/v1/login")

        btn_submit?.setOnClickListener {
            if (GetNetWork.isFastGetNet) {
                if (isEmail(et_user_name?.text.toString()) && et_user_name?.text.toString() != "") {
                    if (com.microjet.airqi2.CustomAPI.Utils.isFastDoubleClick) {
                        showDialog("按慢一點太快了")
                    } else {
                        btn_submit?.isEnabled=false
                        goLoginAsyncTasks().execute(mything)
                    }
                } else {
                    showDialog("請輸入正確的E-mail地址與密碼")
                }
            }else{
                showDialog("請連接網路")
            }
        }
    }


    override fun onStart() {
        super.onStart()
        }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
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

    private inner class goLoginAsyncTasks : AsyncTask<mything, Void, String>() {
        override fun doInBackground(vararg params: mything): String? {
            try {
                var response: okhttp3.Response? = null
                //val registerMail = user_register_mail?.text
                val client = OkHttpClient()
                val mediaType = MediaType.parse("application/x-www-form-urlencoded")

                userEmail = et_user_name!!.text.toString()
                userPassword = et_password!!.text.toString()
                val ccc = "email=" + userEmail + "&password=" + userPassword

                Log.e("內容", ccc)

                val body = RequestBody.create(mediaType, ccc)// )
                Log.e("登入內容", "")
                val request = Request.Builder()
                        .url(params[0].myAddress)
                        .post(body)
                        .addHeader("cache-control", "no-cache")
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .build()

                //上傳資料
                response = client.newCall(request).execute()
                val any = if (response.isSuccessful) {
                    runOnUiThread(java.lang.Runnable {
                        params[0].button!!.isEnabled = true
                    })
                    params[0].myBlean = false
                    try {
                        val tempBody: String = response.body()!!.string().toString()
                        Log.e("登入正確回來", tempBody)
                        val responseContent = JSONObject(tempBody)
                        val token = responseContent.getJSONObject("success").getString("token").toString()
                        Log.e("登入正確回來拿token", token)
                        login_Result = "成功登入"
                        val share = getSharedPreferences("TOKEN", MODE_PRIVATE)
                        share.edit().putString("token", token).apply()

                        //val share_token = getSharedPreferences("TOKEN", MODE_PRIVATE)
                        val _token = share.getString("token", "")
                        Log.e("偷肯:", _token)

//                        text_Account_status.toString(R.string.active)
//                        login_Result = "登入正確"


//                        val intent = Intent()
//                        intent.setClass(this@AccountManagementActivity.mContext, AccountActive::class.java)
//                        startActivity(intent)
//                        finish()

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                } else {
                    runOnUiThread(java.lang.Runnable {
                        params[0].button!!.isEnabled = true
                        btn_submit?.isEnabled=true
                    })

                    params[0].myBlean = false
                    Log.e("登入錯誤回來", response.body()!!.string())
                    login_Result = "登入失敗"

//                    val Dialog = android.app.AlertDialog.Builder(this@AccountManagementActivity).create()
//                    //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
//                    Dialog.setTitle("提示")
//                    Dialog.setMessage(login_Result.toString())
//                    Dialog.setCancelable(false)//讓返回鍵與空白無效
//                    Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
//                    { dialog, _ ->
//                        dialog.dismiss()
//                    }
//                    Dialog.show()
                }
                //Toast.makeText(mContext, response.toString(), Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return login_Result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result== "成功登入") {
                /*
                val Dialog = android.app.AlertDialog.Builder(this@AccountManagementActivity).create()
                //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
                Dialog.setTitle("提示")
                Dialog.setMessage(result.toString())
                Dialog.setCancelable(false)//讓返回鍵與空白無效
                Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
                */
                //{ dialog, _ ->
                    //dialog.dismiss()



                    val intent = Intent()
                    intent.setClass(this@AccountManagementActivity.mContext, AccountActive::class.java)
                    startActivity(intent)
                //}
                /*    Dialog.setOnDismissListener(DialogInterface.OnDismissListener {
                val intent = Intent()
                intent.setClass(this@AccountManagementActivity.mContext, AccountActive::class.java)
                startActivity(intent)
            })
            */
                finish()
                //Dialog.show()

            }else{
                val Dialog = android.app.AlertDialog.Builder(this@AccountManagementActivity).create()
                //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
                Dialog.setTitle("提示")
                Dialog.setMessage(result.toString())
                Dialog.setCancelable(false)//讓返回鍵與空白無效
                Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
                { dialog, _ ->
                    dialog.dismiss()
                }
                Dialog.show()
                //finish()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("完成活動回來",requestCode.toString()+"幹"+resultCode.toString()+"幹"+data.toString())


    }

    //20180311
    fun isEmail(strEmail: String?): Boolean {
        val strPattern = ("\\w+@\\w+\\.\\w+")
        val p = Pattern.compile(strPattern)
        val m = p.matcher(strEmail)
        return m.matches()
}


    //20180312
    fun showDialog(msg:String){
        val Dialog = android.app.AlertDialog.Builder(this@AccountManagementActivity).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        Dialog.setTitle("提示")
        Dialog.setMessage(msg.toString())
        Dialog.setCancelable(false)//讓返回鍵與空白無效
        Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
        { dialog, _ ->
            dialog.dismiss()
            //finish()
        }
        Dialog.show()
    }
}

//private fun getNetWork (): Boolean  {
//    var result = false
//    try {
//        val connManager: ConnectivityManager? = MyApplication.applicationContext().getSystemService(AppCompatActivity.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkInfo: NetworkInfo? = connManager!!.getActiveNetworkInfo() as NetworkInfo
//
//
//        //判斷是否有網路
//        //net = networkInfo.isConnected
//        if (networkInfo == null || !networkInfo.isConnected()) {
//            result = false
//        } else {
//            result = networkInfo.isAvailable()
//        }
//
//    }catch (E: Exception) {
//        Log.e("網路", E.toString())
//    }
//    return result
//}
 class mything ( btn:Button?,blean:Boolean?,myString :String?){
    var button=btn
    var myBlean=blean
    var myAddress=myString
}
