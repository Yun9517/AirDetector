package com.microjet.airqi2

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import com.microjet.airqi2.CustomAPI.GetNetWork
import io.fabric.sdk.android.services.concurrency.AsyncTask
import kotlinx.android.synthetic.main.activity_namereplace.*
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import io.fabric.sdk.android.services.settings.IconRequest.build
import kotlinx.android.synthetic.main.activity_account_active.*
import okhttp3.MediaType
import okhttp3.Request
import okhttp3.RequestBody




@Suppress("IMPLICIT_CAST_TO_ANY")
class NameReplaceActivity : AppCompatActivity() {

    private var mContext: Context? = null
    //private var enter_ID : EditText? = null
    private var mMyThing: mything? = null
    var id_Confirm:Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_namereplace)

        initActionBar()

        mContext = this@NameReplaceActivity.applicationContext

        //val shareMSG = getSharedPreferences("authorization", Context.MODE_PRIVATE)
        //val reName = shareMSG.getString("Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6ImIyYjAxMjE3ODY4ZTE2ODJkMjM5ZjExZWFlNjQ5MDI5NWU0ZmZmOWUzMDM4YTYyNjc5ZWMwZDVlNjIwMTY3MTMwMDJiMmE4YTVhZTI4ZGE5In0.eyJhdWQiOiIxIiwianRpIjoiYjJiMDEyMTc4NjhlMTY4MmQyMzlmMTFlYWU2NDkwMjk1ZTRmZmY5ZTMwMzhhNjI2NzllYzBkNWU2MjAxNjcxMzAwMmIyYThhNWFlMjhkYTkiLCJpYXQiOjE1MjA0OTgzNzksIm5iZiI6MTUyMDQ5ODM3OSwiZXhwIjoxNTUyMDM0Mzc5LCJzdWIiOiI2NiIsInNjb3BlcyI6W119.pC6DcnZzzWncxDMckGkiK2wjAcmLYXDj9zMUd0ayqMP8Rf-nU8z_g0w334zpB0Zb4wcuQsk1cUN6ZBmew_Y0R6b_NnHsXG0RdPX0Q1KVnM1AQlYqqW9Y6YaVxBA0QUPZ93-QAJVIkoZ7Yzx3_0W1OThVnuHnQ0Rrot-klfYu9i5bk3vKZ9e5HkaVh6a0ojIFLlPHOiBOsEOu051yMo1Fx12LToSeUKEE3vuMl1dkYRIIXaY3_Eyuv4erw3Lsl8hgT2ubIkhi4AD9Rb7xWpHpki2TNIMqxkhC73j_KUY0ywGuMnhhOJS2zXen_GtErnTkf025-rzyadaB7Rb0dzI8vmE-ZlMtyAfr9z7fTW9x5PVzM-1D2xCjPY3nl70YtTuwsafeF0SSi3knxOToPBM3CCoVJtsvNKvz9T4rvn45Gls64aIoYG-orEpKwGe4aGBcoZaEkfpZ4ptv2svVjJVSfYV3DlyVFy3IWL7o9ca0Hn4Xt1Un3We58VvGSxmwkR_RdbhxASTz-alO4zc_IrbDX7TAMb7Wxv_fAs5N8Yi51y9eCdkxY63TN5_Q9z-zUJmCLa1wRpkL6M7HcaqCW46i_5cNFHOmYZnF7YMfTZGxv9iBPyLBgsSOQZHQ1mdQ6e_-6AJdjWg6UtvKHp9xYMBRrQq--ez0YOAyEcA350h0Vmo", "")

        // get reference to all views
        id_Confirm = findViewById(R.id.id_Confirm)


        mMyThing = mything(id_Confirm!!, false, "https://mjairql.com/api/v1/editUserData")

        // 03/12
        id_Confirm?.setOnClickListener {
            if (GetNetWork.isFastGetNet) {
                if (enter_ID?.text.toString() != "") {
                    if (com.microjet.airqi2.CustomAPI.Utils.isFastDoubleClick) {
                        showDialog("按慢一點太快了")
                    } else {
                        id_Confirm?.isEnabled = false
                        renameTask().execute(mMyThing)
                    }
                } else {
                    showDialog("請輸入欲更改的名字")
                }
            } else {
                showDialog("請連接網路")
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
                intent.setClass(this@NameReplaceActivity.mContext, AccountActive::class.java)
                startActivity(intent)
                finish()
                return true
            }
            else -> {
            }
        }

        return super.onOptionsItemSelected(item)
    }

    inner class renameTask : android.os.AsyncTask<mything, Void, String>() {
        override fun doInBackground(vararg params: mything): String? {

            val share = getSharedPreferences("TOKEN", MODE_PRIVATE)
            var token = share.getString("token", "")


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
                    params[0].myBlean = false
                } else {
                    runOnUiThread(java.lang.Runnable {
                        params[0].button!!.isEnabled = true
                        id_Confirm?.isEnabled = true
                    })
                    response.body()?.string()
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
                intent.setClass(this@NameReplaceActivity.mContext, AccountActive::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

    //20180312
    private fun showDialog(msg: String) {
        val Dialog = android.app.AlertDialog.Builder(this@NameReplaceActivity).create()
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


//20180314
class mything ( btn:Button?,blean:Boolean?,myString :String?){
    var button=btn
    var myBlean=blean
    var myAddress=myString
}