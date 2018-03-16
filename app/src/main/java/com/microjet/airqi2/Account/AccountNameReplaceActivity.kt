package com.microjet.airqi2.Account

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import com.microjet.airqi2.CustomAPI.GetNetWork
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.activity_namereplace.*
import okhttp3.OkHttpClient
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject


@Suppress("IMPLICIT_CAST_TO_ANY")
class AccountNameReplaceActivity : AppCompatActivity() {

    private var mContext: Context? = null
    //private var enter_ID : EditText? = null
    private var mMyThing: mything? = null
    var id_Confirm:Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_namereplace)

        initActionBar()

        mContext = this@AccountNameReplaceActivity.applicationContext

        //val shareMSG = getSharedPreferences("authorization", Context.MODE_PRIVATE)
        //val reName = shareMSG.getString("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImIyYjAxMjE3ODY4ZTE2ODJkMjM5ZjExZWFlNjQ5MDI5NWU0ZmZmOWUzMDM4YTYyNjc5ZWMwZDVlNjIwMTY3MTMwMDJiMmE4YTVhZTI4ZGE5In0.eyJhdWQiOiIxIiwianRpIjoiYjJiMDEyMTc4NjhlMTY4MmQyMzlmMTFlYWU2NDkwMjk1ZTRmZmY5ZTMwMzhhNjI2NzllYzBkNWU2MjAxNjcxMzAwMmIyYThhNWFlMjhkYTkiLCJpYXQiOjE1MjA0OTgzNzksIm5iZiI6MTUyMDQ5ODM3OSwiZXhwIjoxNTUyMDM0Mzc5LCJzdWIiOiI2NiIsInNjb3BlcyI6W119.pC6DcnZzzWncxDMckGkiK2wjAcmLYXDj9zMUd0ayqMP8Rf-nU8z_g0w334zpB0Zb4wcuQsk1cUN6ZBmew_Y0R6b_NnHsXG0RdPX0Q1KVnM1AQlYqqW9Y6YaVxBA0QUPZ93-QAJVIkoZ7Yzx3_0W1OThVnuHnQ0Rrot-klfYu9i5bk3vKZ9e5HkaVh6a0ojIFLlPHOiBOsEOu051yMo1Fx12LToSeUKEE3vuMl1dkYRIIXaY3_Eyuv4erw3Lsl8hgT2ubIkhi4AD9Rb7xWpHpki2TNIMqxkhC73j_KUY0ywGuMnhhOJS2zXen_GtErnTkf025-rzyadaB7Rb0dzI8vmE-ZlMtyAfr9z7fTW9x5PVzM-1D2xCjPY3nl70YtTuwsafeF0SSi3knxOToPBM3CCoVJtsvNKvz9T4rvn45Gls64aIoYG-orEpKwGe4aGBcoZaEkfpZ4ptv2svVjJVSfYV3DlyVFy3IWL7o9ca0Hn4Xt1Un3We58VvGSxmwkR_RdbhxASTz-alO4zc_IrbDX7TAMb7Wxv_fAs5N8Yi51y9eCdkxY63TN5_Q9z-zUJmCLa1wRpkL6M7HcaqCW46i_5cNFHOmYZnF7YMfTZGxv9iBPyLBgsSOQZHQ1mdQ6e_-6AJdjWg6UtvKHp9xYMBRrQq--ez0YOAyEcA350h0Vmo", "")

        // get reference to all views
        id_Confirm = findViewById(R.id.id_Confirm)


        mMyThing = mything(id_Confirm!!, false, "https://mjairql.com/api/v1/editUserData")

        // 03/16 InputFilter max 20
        enter_ID.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(20))
        
        // 03/12
        id_Confirm?.setOnClickListener {
            if (GetNetWork.isFastGetNet) {
                if (enter_ID?.text.toString() != "") {
                    if (com.microjet.airqi2.CustomAPI.Utils.isFastDoubleClick) {
                        //showDialog("按慢一點太快了")
                        showDialog(getString(R.string.tooFast))
                    } else {
                        id_Confirm?.isEnabled = false
                        renameTask().execute(mMyThing)
                    }
                } else {
                    //showDialog("請輸入欲更改的名字")
                    showDialog(getString(R.string.exceptName))
                }
            } else {
                //showDialog("請連接網路")
                showDialog(getString(R.string.checkConnection))
            }
        }

        //goRenameAsyncTasks().execute("https://mjairql.com/api/v1/editUserData")


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
                val intent = Intent()
                intent.setClass(this@AccountNameReplaceActivity.mContext, AccountActiveActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            else -> {
            }
        }

        return super.onOptionsItemSelected(item)
    }




    var email=""
    var password=""
    var name=""



    inner class renameTask : android.os.AsyncTask<mything, Void, String>() {
        override fun doInBackground(vararg params: mything): String? {

            val share = getSharedPreferences("TOKEN", MODE_PRIVATE)
            var token = share.getString("token", "")
//            share.edit().putString("email", email).apply()
//            share.edit().putString("name", name).apply()
//            Log.e("我的名字:", name+"and"+password)


            val client = OkHttpClient()
            var mediaType = MediaType.parse("application/x-www-form-urlencoded")
            val nametxt = enter_ID?.text.toString()
            val ccc = "name=$nametxt&password=yun_hsieh"
            var body = RequestBody.create(mediaType, ccc)
            var request = Request.Builder()
                    .url(params[0].myAddress)
                    .post(body)
                    .addHeader("authorization", "Bearer " + token)
                    .addHeader("content-type", "application/x-www-form-urlencoded")
                    .addHeader("cache-control", "no-cache")
                    .addHeader("postman-token", "faf24922-3fc9-f28f-1c89-3ace2f560cdb")
                    .build()
            try {
                var response = client.newCall(request).execute()
                val any = if (response.isSuccessful) {
                    runOnUiThread(java.lang.Runnable {
                        params[0].button!!.isEnabled = true
                    })

                    nametxt+"修改成功"
                    //params[0].myBlean = false
//                    val tempBody: String = response.body()!!.string().toString()
//                    Log.e("名字已經修改成功正確回來", tempBody)
//                    val responseContent = JSONObject(tempBody)
//                    val resetName_Result = responseContent.getString("success")
//                    //resetName_Result = "名字已經修改成功。"
//                    val shareToKen = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
                } else {
                    runOnUiThread(java.lang.Runnable {
                        params[0].button!!.isEnabled = true
                        id_Confirm?.isEnabled = true
                    })
                    response.body()?.string()
                    nametxt+"修改失敗"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return nametxt
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result != null) {
                val share = getSharedPreferences("TOKEN", MODE_PRIVATE)
                share.edit().putString("name", result).apply()
                //    share.edit().putString("name", name).apply()
                Log.d("Download", result.toString())
                val intent = Intent()
                intent.setClass(this@AccountNameReplaceActivity.mContext, AccountActiveActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    //20180312
    private fun showDialog(msg: String) {
        val Dialog = android.app.AlertDialog.Builder(this@AccountNameReplaceActivity).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        Dialog.setTitle(getString(R.string.remind))
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


//20180314
class mything ( btn:Button?,blean:Boolean?,myString :String?){
    var button = btn
    var myBlean = blean
    var myAddress = myString
}