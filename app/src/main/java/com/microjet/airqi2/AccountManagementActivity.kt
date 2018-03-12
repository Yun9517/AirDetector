package com.microjet.airqi2

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.microjet.airqi2.R.id.text_Account_status
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException


class AccountManagementActivity : AppCompatActivity() {

    private var mContext: Context? = null

    var et_user_name: EditText? = null
    var et_password: EditText? = null
    var userEmail = ""
    var userPassword = ""
    private var login_Result: String? = null
    // private var loginl_Result: String ? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mContext = this@AccountManagementActivity.applicationContext

        initActionBar()

        // get reference to all views
        et_user_name = findViewById(R.id.email) as EditText
        et_password = findViewById(R.id.password) as EditText
        var btn_submit = findViewById(R.id.login) as Button
        var forgot_password = findViewById(R.id.forgotPassword) as TextView
        var create_account = findViewById(R.id.newAccount) as TextView

        val bundle = intent.extras
        if (bundle != null) {
            userEmail = bundle.getString("email", "")
            userPassword = bundle.getString("pwd", "")
            et_user_name?.setText(userEmail)
            et_password?.setText(userPassword)
            Log.e("ㄍㄋㄋAndy", userEmail + userPassword)
        }

        create_account.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                val intent = Intent()
                intent.setClass(this@AccountManagementActivity.mContext, AccountRegisterActivity::class.java)
                //startActivityForResult(intent,1)

                startActivity(intent)


                //finish()
            }
        })


        btn_submit.setOnClickListener {

            userEmail = et_user_name!!.text.toString()
            userPassword = et_password!!.text.toString()

            goLoginAsyncTasks().execute("https://mjairql.com/api/v1/login")

//            val Dialog = android.app.AlertDialog.Builder(this@AccountManagementActivity).create()
            //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
//            Dialog.setTitle("提示")
//            Dialog.setMessage("登入中請稍後")
//            Dialog.setCancelable(false)//讓返回鍵與空白無效
//            Dialog.setButton(DialogInterface.BUTTON_NEGATIVE,"确定")
//            {
//                dialog, _->dialog.dismiss()
//            }
//            Dialog.show()

        }

        // your code to validate the user_name and password combination
        // and verify the same

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

    private inner class goLoginAsyncTasks : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg params: String): String? {
            try {
                var response: okhttp3.Response? = null
                //val registerMail = user_register_mail?.text
                val client = OkHttpClient()
                val mediaType = MediaType.parse("application/x-www-form-urlencoded")


                val ccc = "email=" + userEmail + "&password=" + userPassword

                Log.e("內容", ccc)

                val body = RequestBody.create(mediaType, ccc)// )
                Log.e("登入內容", "")
                val request = Request.Builder()
                        .url("https://mjairql.com/api/v1/login")
                        .post(body)
                        .addHeader("cache-control", "no-cache")
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .build()

                //上傳資料
                response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    try {
                        val tempBody: String = response.body()!!.string().toString()
                        Log.e("登入正確回來", tempBody)
                        val responseContent = JSONObject(tempBody)
                        val token = responseContent.getJSONObject("success").getString("token").toString()
                        Log.e("登入正確回來拿token", token)
                        login_Result = "成功登入"

                        val share = getSharedPreferences("TOKEN", MODE_PRIVATE)
                        share.edit().clear().putString("token", token).apply()
                        //val share_token = share.getString("token","")
                        // Log.e("偷肯:",share_token)

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
                    Log.e("登入錯誤回來", response.body()!!.string())


                    login_Result = "登入失敗"
                    val Dialog = android.app.AlertDialog.Builder(this@AccountManagementActivity).create()
                    //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
                    Dialog.setTitle("提示")
                    Dialog.setMessage(login_Result.toString())
                    Dialog.setCancelable(false)//讓返回鍵與空白無效
                    Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
                    { dialog, _ ->
                        dialog.dismiss()
                    }
                    Dialog.show()
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
                finish()
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.e("完成活動回來",requestCode.toString()+"幹"+resultCode.toString()+"幹"+data.toString())


    }
}
