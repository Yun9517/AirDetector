package com.microjet.airqi2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import kotlinx.android.synthetic.main.activity_namereplace.*

class NameReplaceActivity : AppCompatActivity() {

    private var mContext: Context? = null
    private var enter_ID : EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_namereplace)

        initActionBar()

        mContext = this@NameReplaceActivity.applicationContext

        // get reference to all views
        enter_ID = findViewById(R.id.enter_Email)
        var id_Confirm = findViewById<Button>(R.id.id_Confirm)

        // 03/12
        id_Confirm.setOnClickListener {
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
}
