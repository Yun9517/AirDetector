package com.microjet.airqi2

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.webkit.WebView
import java.util.*

class KnowledgeActivity : AppCompatActivity() {

    private var webView: WebView? = null

    private var mContext: Context? = null

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knowledge)

        webView = findViewById(R.id.webView)

        mContext = this@KnowledgeActivity.applicationContext

        readHtmlFormAssets()
    }


    private fun readHtmlFormAssets() {
        val webSettings = webView!!.getSettings()

        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true

        val mLang = Locale.getDefault().language + "-" + Locale.getDefault().country

        Log.v("KnowledgeActivity", "Current language is: $mLang")

        webView!!.setBackgroundColor(Color.TRANSPARENT)  //  WebView 背景透明效果，不知道为什么在xml配置中无法设置？

        if(mLang == "zh-TW" || mLang == "zh-HK") {
            webView!!.loadUrl("file:///android_asset/knowledge/index.html")
        } else if (mLang == "zh-CN"){
            webView!!.loadUrl("file:///android_asset/knowledge/index-zh_CN.html")
        } else {
            webView!!.loadUrl("file:///android_asset/knowledge/index-en_US.html")
        }
    }
}
