package com.microjet.airqi2

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.webkit.WebView
import java.util.*

class QandAActivity : AppCompatActivity() {

    private var webView: WebView? = null

    private var mContext: Context? = null

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qanda)

        initActionBar()

        webView = findViewById(R.id.webView)

        mContext = this@QandAActivity.applicationContext

        readHtmlFormAssets()
    }


    private fun readHtmlFormAssets() {
        val webSettings = webView!!.settings

        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true

        val mLang = Locale.getDefault().language + "-" + Locale.getDefault().country

        Log.v("KnowledgeActivity", "Current language is: $mLang")

        webView!!.setBackgroundColor(Color.TRANSPARENT)  //  WebView 背景透明效果，不知道为什么在xml配置中无法设置？

        if(mLang == "zh-TW" || mLang == "zh-HK") {
            webView!!.loadUrl("file:///android_asset/knowledge/QandA.html")
        } else if (mLang == "zh-CN"){
            webView!!.loadUrl("file:///android_asset/knowledge/QandA-zh_CN.html")
        } else {
            webView!!.loadUrl("file:///android_asset/knowledge/QandA-en_US.html")
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