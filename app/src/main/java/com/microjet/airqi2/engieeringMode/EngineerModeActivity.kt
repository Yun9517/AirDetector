package com.microjet.airqi2.engieeringMode

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.MenuItem
import android.widget.LinearLayout
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.activity_engineer_mode.*
import android.widget.Toast
import com.microjet.airqi2.MainActivity



class EngineerModeActivity : AppCompatActivity() {

    private var device = ArrayList<DeviceInfo>()
    private lateinit var deviceAdapter: DeviceAdapter

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
    }

    private fun setAdapter() {
        val mLayoutManager = LinearLayoutManager(this)
        deviceAdapter = DeviceAdapter(this, device)
        deviceList.adapter = deviceAdapter
        mLayoutManager.orientation = LinearLayout.VERTICAL
        deviceList.layoutManager = mLayoutManager
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
}
