package com.microjet.airqi2

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_publicmap.*
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat

/**
 * Created by B00175 on 2018/4/24.
 *
 */
class PublicMapActivity : AppCompatActivity() {

    private var topMenu: Menu? = null
    private var bleIcon: MenuItem? = null       // 藍芽icon in actionbar
    private var battreyIcon: MenuItem? = null   //電量icon
    private var shareMap: MenuItem? = null      //分享icon

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

    @SuppressLint("SetJavaScriptEnabled")
    override fun onStart() {
        super.onStart()

        val ws = wvMap.settings
        ws.javaScriptEnabled = true
        //置中webView內容
        //https://stackoverflow.com/questions/30493567/webview-setdefaultzoom-deprecated
        //ws.loadWithOverviewMode = true
        //ws.useWideViewPort = true
    }

    override fun onStop() {
        super.onStop()
        val ws = wvMap.settings
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
            R.id.shareMap -> {
                checkPermissions()
            }
            android.R.id.home -> {
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        topMenu = menu
        //menuItem= menu!!.findItem(R.id.batStatus)
        bleIcon = menu!!.findItem(R.id.bleStatus)
        battreyIcon = menu.findItem(R.id.batStatus)
        shareMap = menu.findItem(R.id.shareMap)
        bleIcon!!.isVisible = false
        battreyIcon!!.isVisible = false
        shareMap!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        return super.onCreateOptionsMenu(menu)
    }

    private fun checkPermissions() {
        when {
            ActivityCompat.checkSelfPermission(this@PublicMapActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ->
                ActivityCompat.requestPermissions(this@PublicMapActivity,
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 333)
            else -> {
                picture()
                Log.e("CheckPerm", "Permission Granted...")
            }
        }
    }

    private fun screenShot(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        Log.d("YYY", "done")
        return bitmap
    }

    @SuppressLint("SimpleDateFormat")
    private fun picture() {
        val bitmap = screenShot(window.decorView.rootView)
        val now = System.currentTimeMillis()
        val folderName = "ADDWII Mobile Nose"
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_hh-mm-ss")

        val folderPath = File("${Environment.getExternalStorageDirectory()}/$folderName")
        folderPath.mkdir()

        val mPath = "${folderPath.absolutePath}/${simpleDateFormat.format(now)}.jpg"

        val imageFile = File(mPath)

        val bundle = Bundle()
        bundle.putString(ShareDialog.EXTRA_FILE_PATH, imageFile.absolutePath)

        val dialog = ShareDialog()
        dialog.arguments = bundle
        dialog.show(fragmentManager, ShareDialog.TAG)

        //shareContent(imageFile)
        val outputStream = FileOutputStream(imageFile)
        val quality = 100
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        outputStream.flush()
        outputStream.close()
    }
}