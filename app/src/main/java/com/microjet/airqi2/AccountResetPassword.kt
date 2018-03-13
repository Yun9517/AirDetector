package com.microjet.airqi2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.EditText

class AccountResetPassword : AppCompatActivity() {
    private var mContext : Context? = null
    // 03/12
    private var enterPassword : EditText? = null
    private var checkPassword : EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        mContext = this@AccountResetPassword.applicationContext

        initActionBar()

        // 03/12
        //goResetAsyncTasks().execute("https://mjairql.com/api/v1/editUserData")
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
                intent.setClass(this@AccountResetPassword.mContext, AccountActive::class.java)
                startActivity(intent)
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //    private inner class goResetAsyncTasks : AsyncTask<String, Void, String>() {
    //
    //    }
}
