package com.microjet.airqi2

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View

//20180307
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_register.*
import okhttp3.*
import org.json.JSONException
import java.io.IOException
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.text.Editable
import android.widget.Button
import com.microjet.airqi2.CustomAPI.GetNetWork
import com.microjet.airqi2.CustomAPI.Utils
import org.json.JSONObject
import java.util.regex.Pattern


@Suppress("UNREACHABLE_CODE")
class AccountRegisterActivity : AppCompatActivity() {
    private var mContext: Context? = null

    private var user_register_mail : EditText ? = null
    private var register_mail_Result : String ? = null
    //private var register_mail_Faile : String ? = null

    var mything:mything?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        var mything: mything? = null

        mContext = this@AccountRegisterActivity.applicationContext
        initActionBar()
        //20180307
        // set on-click listener
        user_register_mail = this.findViewById(R.id.email)
        //var btn_next_step = this.findViewById<Button>(R.id.nextStep)
        //var registerMail = null


        mything = mything(nextStep, false, "https://mjairql.com/api/v1/register")

        nextStep.setOnClickListener {
            if (GetNetWork.isFastGetNet) {
                if (isEmail(user_register_mail?.text) && user_register_mail?.text.toString() != "") {
                    if (Utils.isFastDoubleClick) {
                        showDialog("按慢一點太快了")
                    } else {
                        goRegisterAsyncTasks().execute(mything)
                    }
                } else {
                    showDialog("請輸入正確的E-mail地址")
                }
            }else{
                showDialog("請連接網路")
            }
        }
    }

    var email=""
    var password=""
    var name=""



    @SuppressLint("StaticFieldLeak")
    private inner class goRegisterAsyncTasks : AsyncTask<mything, Void, String>() {
        override fun doInBackground(vararg params: mything): String? {
            try {
                var response: okhttp3.Response? = null
                val registerMail = user_register_mail?.text
                val client = OkHttpClient()
                val mediaType = MediaType.parse("application/x-www-form-urlencoded")
                val body = RequestBody.create(mediaType, "email=" + registerMail)
                val request = Request.Builder()
                        .url("https://mjairql.com/api/v1/register")
                        .post(body)
                        .addHeader("cache-control", "no-cache")
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .build()

                //上傳資料
                response = client.newCall(request).execute()
                response = client.newCall(request).execute()
                val any = if (response.isSuccessful) {
                    runOnUiThread(java.lang.Runnable {
                        params[0].button.isEnabled = true
                    })
                    params[0].myBlean = false
                    try {
                        val tempBody: String = response.body()!!.string().toString()
                        Log.e("註冊正確回來", tempBody)
                        val responseContent = JSONObject(tempBody)
                        email = responseContent.getJSONObject("success").getString("email").toString()
                        password = responseContent.getString("pwd").toString()
                        name = responseContent.getJSONObject("success").getString("name").toString()
                        Log.e("註冊正確回來11", password+"名字:"+name)

                        val share = getSharedPreferences("registerMSG", MODE_PRIVATE)
                        share.edit().putString("email", email).apply()
                        share.edit().putString("password", password).apply()
                        share.edit().putString("name", name).apply()
                        Log.e("我的名字:", name)

                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    register_mail_Result = "密碼已經寄送，請至登入頁面輸入密碼。"
                } else {
                    params[0].myBlean = false
                    Log.e("註冊錯誤回來", response.body()!!.string())
                    register_mail_Result = "此信箱已經被申請，請更改信箱再註冊謝謝。"

                }
                //Toast.makeText(mContext, response.toString(), Toast.LENGTH_LONG).show()
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return register_mail_Result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result == "密碼已經寄送，請至登入頁面輸入密碼。") {
                val Dialog = android.app.AlertDialog.Builder(this@AccountRegisterActivity).create()
                //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
                Dialog.setTitle("提示")
                Dialog.setMessage(result.toString())
                Dialog.setCancelable(false)//讓返回鍵與空白無效
                Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
                { dialog, _ ->
                    dialog.dismiss()
                    val intent = Intent()
                    val bundle = Bundle()
                    Log.e("ㄍㄋㄋAndy", email)
                    Log.e("ㄍㄋㄋAndy", password)
                    bundle.putString("email", email)
                    bundle.putString("pwd", password)
                    bundle.putString("name", name)

                    intent.putExtras(bundle)
                    intent.setClass(this@AccountRegisterActivity.mContext, AccountManagementActivity::class.java)

                    //setResult(1,intent)


                    startActivity(intent)
                    finish()
                }
                Dialog.show()
            } else {

                showDialog(result!!)
//                val Dialog = android.app.AlertDialog.Builder(this@AccountRegisterActivity).create()
//                //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
//                Dialog.setTitle("提示")
//                Dialog.setMessage(register_mail_Result.toString())
//                Dialog.setCancelable(false)//讓返回鍵與空白無效
//                Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
//                { dialog, _ ->
//                    dialog.dismiss()
//                    //finish()
//                }
//                Dialog.show()
            }
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


    //20180311
//    private fun setListener() {
//        user_register_mail?.setOnKeyListener(object : View.OnKeyListener {
//            override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
//                when (event.getAction()) {
//                //獲得操作動作
//                    KeyEvent.ACTION_DOWN//按下時
//                    -> {
//                        val inputString = user_register_mail.toString()//獲得user_register_mail輸入的內容
//                        if (inputString.matches("\\w+@\\w+\\.\\w+".toRegex())) {
//                            //                            驗證通過
//
//                        } else {
//
//                        }
//                    }
//                    KeyEvent.ACTION_UP//放開時
//                    -> {
//                    }
//                }
//                return false
//            }
//        })
//    }

    //20180311
    fun isEmail(strEmail: Editable?): Boolean {
        val strPattern = ("\\w+@\\w+\\.\\w+")
        val p = Pattern.compile(strPattern)
        val m = p.matcher(strEmail)
        return m.matches()
    }

    //20180311
    fun showDialog(msg:String){
        val Dialog = android.app.AlertDialog.Builder(this@AccountRegisterActivity).create()
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

//    //20180312
//    private fun getNetWork (): Boolean  {
//        var result = false
//        try {
//            val connManager: ConnectivityManager? = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
//            val networkInfo: NetworkInfo? = connManager!!.getActiveNetworkInfo() as NetworkInfo
//
//
//            //判斷是否有網路
//            //net = networkInfo.isConnected
//            if (networkInfo == null || !networkInfo.isConnected()) {
//                result = false
//            } else {
//                result = networkInfo.isAvailable()
//            }
//
//        }catch (E: Exception) {
//            Log.e("網路", E.toString())
//        }
//        return result
//    }

    //20180307
    // set on-click listener
//fun click(view: TextView){
//        when(view?.id){
//            R.id.nextStep ->{
//
//                val registerMail = user_register_mail
//                val client = OkHttpClient()
//                val mediaType = MediaType.parse("application/x-www-form-urlencoded")
//                val body = RequestBody.create(mediaType, "email=" + registerMail)
//                val request = Request.Builder()
//                        .url("https://mjairql.com/api/v1/register")
//                        .post(body)
//                        .addHeader("cache-control", "no-cache")
//                        .addHeader("postman-token", "7b3fe19e-ed36-3c04-7e41-1df0162b950d")
//                        .addHeader("content-type", "application/x-www-form-urlencoded")
//                        .build()
//
//                val response = client.newCall(request).execute()
//                Toast.makeText(mContext,response.toString(),Toast.LENGTH_LONG).show()
//
//            }
//        }
//    }

}



