package com.microjet.airqi2

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText

class AccountForgetPassword : AppCompatActivity() {
    private var mContext : Context? = null
    private var et_Mail : EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forget_password)

        mContext = this@AccountForgetPassword.applicationContext
        //intent.setClass(this@AccountForgetPassword.mContext, AccountManagementActivity::class.java)
        initActionBar()

        // get reference to all views
        et_Mail = findViewById(R.id.enter_Email)
        var btn_confirm = findViewById<Button>(R.id.enter_email_confirm)

        // 03/12
        btn_confirm.setOnClickListener {
            //goForgotAsyncTasks().execute("https://mjairql.com/api/v1/forgotPassword")
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
                Log.d(this.javaClass.simpleName,"home icon")
                val intent = Intent()
                intent.setClass(this@AccountForgetPassword.mContext, AccountManagementActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

//    private inner class goForgotAsyncTasks : AsyncTask<String, Void, String>() {
//
//    }
}
