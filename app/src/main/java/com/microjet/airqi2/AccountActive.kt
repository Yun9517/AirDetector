package com.microjet.airqi2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_account_active.*

/**
 * Created by B00170 on 2018/3/8.
 */
class AccountActive : AppCompatActivity() {
    private var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_active)
        mContext = this@AccountActive.applicationContext
        logout.setOnClickListener {
            val shareToKen = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
            shareToKen.edit().clear().apply()
            finish()
        }

        initActionBar()

        // get reference to all views
        var change_password = findViewById<TextView>(R.id.change_password)

        change_password.setOnClickListener {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            val intent = Intent()
            intent.setClass(this@AccountActive.mContext, AccountResetPassword::class.java)
            //startActivityForResult(intent,1)
            startActivity(intent)
            //finish()
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
}