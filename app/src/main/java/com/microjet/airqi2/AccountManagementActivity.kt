package com.microjet.airqi2

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class AccountManagementActivity : AppCompatActivity() {

    private var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        mContext = this@AccountManagementActivity.applicationContext

        initActionBar()

        // get reference to all views
        var et_user_name = findViewById(R.id.email) as EditText
        var et_password = findViewById(R.id.password) as EditText
        var btn_submit = findViewById(R.id.login) as Button
        var forgot_password = findViewById(R.id.forgotPassword) as TextView
        var create_account = findViewById(R.id.newAccount) as TextView

        // set on-click listener
        btn_submit.setOnClickListener {
            val user_name = et_user_name.text
            val password = et_password.text
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
}
