package com.microjet.airqi2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_account_active.*
import kotlinx.android.synthetic.main.drawer_header.*

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
        text_Account_status
        initActionBar()
        
        //20180310
        val shareMSG = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        val myName = shareMSG.getString("name", "")
        val myEmail= shareMSG.getString("email","")
        //val myPassword= shareMSG.getString("password","")
        Log.e("登入後我的資訊","登入中:"+myName + "信箱:" + myEmail)
        //cannot_Receive_mail.setText("登入中:"+myName + "信箱:" + myEmail)
        // get reference to all views
        var change_password = findViewById<TextView>(R.id.change_password)

        // 03/14 edit ID
        var edit_Name = findViewById<TextView>(R.id.rename)

        change_password.setOnClickListener {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            val intent = Intent()
            intent.setClass(this@AccountActive.mContext, AccountResetPassword::class.java)
            //startActivityForResult(intent,1)
            startActivity(intent)
            //finish()
        }
        // 03/14 edit ID
        edit_Name.setOnClickListener {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            val intent = Intent()
            intent.setClass(this@AccountActive.mContext, NameReplaceActivity::class.java)
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

    override fun onStart() {
        super.onStart()
        val share_token = getSharedPreferences("TOKEN", MODE_PRIVATE)
        val _token = share_token.getString("token","")
        Log.e("登入後onStart偷肯:",_token)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onRestart() {
        super.onRestart()
    }

    override fun onDestroy() {
        super.onDestroy()
        val share_token = getSharedPreferences("TOKEN", MODE_PRIVATE)
        val _token = share_token.getString("token","")
        Log.e("登出後onDestroy偷肯:",_token)
    }
}