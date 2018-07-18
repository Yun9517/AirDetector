package com.microjet.airqi2.Account

import android.app.DialogFragment
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.microjet.airqi2.Account.AccountTask.AccountCheckTokenTask
import com.microjet.airqi2.BleEvent
import com.microjet.airqi2.Fragment.CheckFragment
import com.microjet.airqi2.MyApplication
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.activity_account_retry.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Created by B00190 on 2018/7/17.
 */
class AccountRetryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_retry)

        btRetry?.setOnClickListener {
            checkNetwork()
        }

        initActionBar()

    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    private fun checkNetwork() {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            val shareToken = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
            val myToken = shareToken.getString("token", "")
            AccountCheckTokenTask().execute(myToken,"checkTokenBybtEvent")
        } else {
            val newFrage = CheckFragment().newInstance(R.string.checkConnection, this, 1)
            newFrage.show(fragmentManager, "dialog")
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
        }
        return super.onOptionsItemSelected(item)
    }

    @Subscribe
    fun onEvent(bleEvent: BleEvent) {
        /* 處理事件 */
        Log.d("AirAction", bleEvent.message)
        when (bleEvent.message) {
            "wait Dialog" -> {
                val newFrage = CheckFragment().newInstance(R.string.wait_Login, this, 0)
                newFrage.setCancelable(false)
                newFrage.show(fragmentManager, "dialog")
            }
            "close Wait Dialog" -> {
                val previousDialog = fragmentManager.findFragmentByTag("dialog")
                if (previousDialog != null) {
                    val dialog = previousDialog as DialogFragment
                    dialog.dismiss()
                }
            }
            "successToken" -> {
                val i: Intent? = Intent(this, AccountActiveActivity::class.java)
                startActivity(i)
                finish()
            }
            "ErrorTokenWithButton" -> {
                cleanUserData()
                val i: Intent? = Intent(this, AccountManagementActivity::class.java)
                startActivity(i)
                finish()
            }
            "ReconnectNetwork" -> {
                val newFrage = CheckFragment().newInstance(R.string.checkConnection, this, 1)
                newFrage.show(fragmentManager, "dialog")
            }
        }
    }

    private fun cleanUserData() {
        //失效的Token，清除Apk內所有使用者資料
        val share = getSharedPreferences("TOKEN", MODE_PRIVATE)
        share.edit().putString("token", "").apply()
        share.edit().putString("name", "").apply()
        share.edit().putString("email", "").apply()
        share.edit().putString("deviceLi", "").apply()
        if (Build.BRAND != "OPPO") {
            Toast.makeText(MyApplication.applicationContext(), R.string.errorToken, Toast.LENGTH_SHORT).show()
        }
    }

}