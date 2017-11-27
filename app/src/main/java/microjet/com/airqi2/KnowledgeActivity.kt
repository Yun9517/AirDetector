package microjet.com.airqi2

import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView

class KnowledgeActivity : AppCompatActivity() {

    private var webView: WebView? = null

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_knowledge)

        webView = findViewById(R.id.webView)

        readHtmlFormAssets()
    }


    private fun readHtmlFormAssets() {
        val webSettings = webView!!.getSettings()

        webSettings.loadWithOverviewMode = true
        webSettings.useWideViewPort = true

        webView!!.setBackgroundColor(Color.TRANSPARENT)  //  WebView 背景透明效果，不知道为什么在xml配置中无法设置？
        webView!!.loadUrl("file:///android_asset/knowledge/index.html")
    }
}
