package com.microjet.airqi2

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_publicmap.*
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient

/**
 * Created by B00175 on 2018/4/24.
 */
class PublicMapActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_publicmap)
        initActionBar()

        val target = intent.getStringExtra("URL")

        wvMap.loadUrl(target)

        //wvMap.loadUrl("http://mjairql.com/air_map/")
        wvMap.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                when (error!!.errorCode) {
                    -2 -> {
                        Toast.makeText(MyApplication.applicationContext(), "請連接網路", Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                    }
                }

            }
        }
        wvMap.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                callback.invoke(origin, true, false)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        var ws = wvMap.settings
        ws.javaScriptEnabled = true
        //置中webView內容
        //https://stackoverflow.com/questions/30493567/webview-setdefaultzoom-deprecated
        //ws.loadWithOverviewMode = true
        //ws.useWideViewPort = true
    }

    override fun onStop() {
        super.onStop()
        var ws = wvMap.settings
        ws.javaScriptEnabled = false
        //ws.loadWithOverviewMode = false
        //ws.useWideViewPort = false
    }

    override fun onDestroy() {
        super.onDestroy()
        wvMap.clearCache(true)
    }

    private fun initActionBar() {
        val actionBar = supportActionBar
        actionBar!!.setDisplayHomeAsUpEnabled(true)

        val title = intent.getStringExtra("TITLE")
        actionBar.title = title
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home
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