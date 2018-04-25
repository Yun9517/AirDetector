package com.microjet.airqi2.engieeringMode

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.widget.LinearLayout
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.MapStyleOptions
import com.microjet.airqi2.AirMapActivity
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.activity_engineer_mode.*


class EngineerModeActivity : AppCompatActivity() {

    var device = ArrayList<DeviceInfo>()
    lateinit var deviceAdapter: DeviceAdapter

    private val REQUEST_CAMERA = 2
    private val perms: Array<String> = arrayOf(Manifest.permission.CAMERA)

    private val PICK_QRCODE_REQUEST = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_engineer_mode)

        initActionBar()

        // 裝置假資料
        val device1 = DeviceInfo()
        device1.DeviceName = "NAME: ㄍㄋㄋAndy"
        device1.DeviceAddress = "MAC: 00:88:55:77:00:00"
        device1.DeviceSerial = "SN: 0938-5938-78-3064"
        device1.ConnectTime = "1970/12/01 23:59:59"
        device1.TVOCValue = "60000"
        device1.ECO2Value = "60000"
        device1.PM25Value = "60000"
        device1.HUMIValue = "60000"
        device1.TEMPValue = "60000"
        device1.RSSIValue = "127"

        setAdapter()

        fab.setOnClickListener {
            device.add(device1)
            deviceAdapter.notifyDataSetChanged()
        }

        fab2.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, perms, REQUEST_CAMERA)
            } else {
                val intent = Intent(this@EngineerModeActivity, ScanActivity::class.java)
                startActivityForResult(intent, PICK_QRCODE_REQUEST)
            }
        }
    }

    private fun setAdapter() {
        val mLayoutManager = LinearLayoutManager(this)
        deviceAdapter = DeviceAdapter(this, device)
        deviceList.adapter = deviceAdapter
        mLayoutManager.orientation = LinearLayout.VERTICAL
        deviceList.layoutManager = mLayoutManager
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        // Check which request we're responding to
        if (requestCode == PICK_QRCODE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                val device1 = DeviceInfo()
                device1.DeviceName = "NAME: ${data.getStringExtra("deviceName")}"
                device1.DeviceAddress = "MAC: ${data.getStringExtra("deviceAddr")}"
                device1.DeviceSerial = "SN: 0938-5938-78-3064"
                device1.ConnectTime = "1970/12/01 23:59:59"
                device1.TVOCValue = "10000"
                device1.ECO2Value = "30000"
                device1.PM25Value = "10000"
                device1.HUMIValue = "100"
                device1.TEMPValue = "0"
                device1.RSSIValue = "-128"

                device.add(device1)
                deviceAdapter.notifyDataSetChanged()
            }
        }
    }

    // 初始化ActionBar
    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    // 設定ActionBar返回鍵的動作
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

    // 權限要求結果，由於已經先在onMapReady()中要求權限了，因此在處理的程式碼中無需再次要求權限
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {
            REQUEST_CAMERA -> {
                val intent = Intent(this@EngineerModeActivity, ScanActivity::class.java)
                startActivity(intent)
            }
        }
    }
}
