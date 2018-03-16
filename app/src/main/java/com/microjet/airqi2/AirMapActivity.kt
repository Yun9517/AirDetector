package com.microjet.airqi2

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat.checkSelfPermission
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.format.DateFormat
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.microjet.airqi2.CustomAPI.AirMapAdapter
import com.microjet.airqi2.CustomAPI.SelectedItem
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.BroadcastActions
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_airmap.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


/**
 * Created by B00174 on 2017/11/27.
 *
 */

class AirMapActivity: AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    private val REQUEST_LOCATION = 2
    private val perms: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private var dataArray = ArrayList<AsmDataModel>()

    private var currentMarker: Marker? = null

    private var datepickerHandler = Handler()

    private lateinit var mCal: Calendar
    private lateinit var mDate: String

    private lateinit var mAdapter: AirMapAdapter

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_airmap)

        initActionBar()
        initGoogleMapFragment()

        createLocationRequest()

        initRecyclerView()

        mCal = Calendar.getInstance()
        mDate = DateFormat.format("yyyy-MM-dd", mCal.time).toString()
        datePicker.text = "DATE $mDate"

        datePicker.setOnClickListener {
            if (Utils.isFastDoubleClick) {
                Utils.toastMakeTextAndShow(this@AirMapActivity, "連點，母湯喔！！",
                        Toast.LENGTH_SHORT)
            } else {
                datepickerHandler.post {
                    val dpd = DatePickerDialog(this@AirMapActivity, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        mCal.set(year, month, dayOfMonth)
                        Log.e("AirMap Button", mCal.get(Calendar.DAY_OF_MONTH).toString())
                        mDate = DateFormat.format("yyyy-MM-dd", mCal.time).toString()
                        datePicker.text = "DATE $mDate"
                        getLocalData()
                        //timePickerShow()
                    }, mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH), mCal.get(Calendar.DAY_OF_MONTH))
                    dpd.setMessage("請選擇日期")
                    dpd.show()
                }
            }
        }

        LocalBroadcastManager.getInstance(this@AirMapActivity).registerReceiver(mGattUpdateReceiver,
                makeMainFragmentUpdateIntentFilter())
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(this@AirMapActivity).unregisterReceiver(mGattUpdateReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 資料庫查詢
    @SuppressLint("SimpleDateFormat")
    private fun getLocalData() {
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)

        mMap.clear()

        //val calendar = Calendar.getInstance()

        //現在時間實體毫秒
        val touchTime = mCal.timeInMillis + mCal.timeZone.rawOffset
        //將日期設為今天日子加一天減1秒
        val startTime = touchTime / (3600000 * 24) * (3600000 * 24)
        val endTime = startTime + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        query.between("Created_time", startTime, endTime).sort("Created_time", Sort.ASCENDING)
        val result = query.findAll()
        Log.d("DATE", "Today total count: ${result.size}")

        if(result.size > 0) {
            val rectOptions = PolylineOptions().color(Color.RED).width(20F)
            dataArray.clear()

            for (i in 0 until result.size) {
                dataArray.add(result[i]!!)

                val latitude: Double = result[i]!!.latitude.toDouble()
                val longitude: Double = result[i]!!.longitude.toDouble()

                // 針對經緯度相反做處理
                val latLng = if(latitude < 90) {
                    LatLng(latitude, longitude)
                } else {
                    LatLng(longitude, latitude)
                }

                rectOptions.add(latLng)

                Log.e("LOCATION", "Now get [$i], LatLng is: ${result[i]!!.latitude}, ${result[i]!!.longitude}")
            }

            mMap.addPolyline(rectOptions)
        } else {
            dataArray.clear()
        }

        realm.close()       // 撈完資料千萬要記得關掉！！！

        mAdapter = AirMapAdapter(dataArray)
        recyclerView.adapter = mAdapter

        if(dataArray.size > 0) {
            val nowPosition = dataArray.size - 1
            SelectedItem.setSelectedItem(nowPosition)    //自定義的方法，告訴adpter被點擊item
            recyclerView.scrollToPosition(nowPosition)
            updateFaceIcon(dataArray[nowPosition].tvocValue.toInt())

            updateValuePanel(dataArray[nowPosition].tvocValue, dataArray[nowPosition].pM25Value,
                    dataArray[nowPosition].ecO2Value, dataArray[nowPosition].tempValue,
                    dataArray[nowPosition].humiValue)

            putMarker((dataArray[nowPosition].latitude).toDouble(),
                    (dataArray[nowPosition].longitude).toDouble())
        } else {
            updateValuePanel("----", "----", "----", "----",
                    "----")
        }

        mAdapter.notifyDataSetChanged()

        mAdapter.setOnItemClickListener { _, position ->
            putMarker((result[position]!!.latitude).toDouble(),
                    (result[position]!!.longitude).toDouble())

            SelectedItem.setSelectedItem(position)    //自定義的方法，告訴adpter被點擊item
            mAdapter.notifyDataSetChanged()

            updateFaceIcon(result[position]!!.tvocValue.toInt())

            updateValuePanel(result[position]!!.tvocValue, result[position]!!.pM25Value,
                    result[position]!!.ecO2Value, result[position]!!.tempValue,
                    result[position]!!.humiValue)
        }
    }

    // 更新那個笑到你心裡發寒的臉圖
    private fun updateFaceIcon(value: Int) {
        when(value) {
            in 0..219 -> {
                imgAirQuality.setImageResource(R.drawable.face_icon_01green_active)
            }
            in 220..659 -> {
                imgAirQuality.setImageResource(R.drawable.face_icon_02yellow_active)
            }
            in 660..2199 -> {
                imgAirQuality.setImageResource(R.drawable.face_icon_03orange_active)
            }
            in 2200..5499 -> {
                imgAirQuality.setImageResource(R.drawable.face_icon_04red_active)
            }
            in 5500..19999 -> {
                imgAirQuality.setImageResource(R.drawable.face_icon_05purple_active)
            }
            else -> {
                imgAirQuality.setImageResource(R.drawable.face_icon_06brown_active)
            }
        }
    }

    // 更新左上角空汙數值面板
    @SuppressLint("SetTextI18n")
    private fun updateValuePanel(tvocVal: String, pm25Val: String, eco2Val: String,
                                 tempVal: String, humiVal: String) {
        textTVOCvalue.text = "$tvocVal ppb"

        textPM25value.text = if(pm25Val == "65535") {
            "沒有偵測"
        } else {
            "$pm25Val μg/m³"
        }

        textECO2value.text = "$eco2Val ppm"
        textTEMPvalue.text = "$tempVal °C"
        textHUMIvalue.text = "$humiVal %"
    }

    // 放入地圖圖釘
    private fun putMarker(latitude: Double, longitude: Double) {

        val latLng = LatLng(latitude, longitude)

        if (currentMarker != null) {
            currentMarker!!.remove()
            currentMarker = null
        }

        if (currentMarker == null) {
            currentMarker = mMap.addMarker(MarkerOptions().position(latLng))
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

    // 初始化 RecyclerView
    private fun initRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        recyclerView.layoutManager = linearLayoutManager
    }

    // 初始化GoogleMap UI元件
    private fun initGoogleMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // 初始化位置，由於已經先在onMapReady()中要求權限了，因此無需再次要求權限
    @SuppressLint("MissingPermission")
    private fun initLocation() {
        val client = LocationServices.getFusedLocationProviderClient(this)

        client.lastLocation.addOnCompleteListener(this, {
            if(it != null && it.isSuccessful) {
                val location = it.result
                if (location == null) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(24.959817, 121.4215), 15f))
                } else {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15f))
                }
            }

            Log.i("LOCATION", "Location Task is Successful: ${it.isSuccessful}")
        })
    }

    // 設定位置要求的參數
    private fun createLocationRequest() {
        val locationRequest = LocationRequest()
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 2000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // 權限要求結果，由於已經先在onMapReady()中要求權限了，因此在處理的程式碼中無需再次要求權限
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        when(requestCode) {
            REQUEST_LOCATION -> {
                mMap.isMyLocationEnabled =
                        grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED

                initLocation()
            }
        }
    }

    // 當 Map 可用時做相關處理
    override fun onMapReady(p0: GoogleMap?) {
        mMap = p0!!

        // 彩蛋，好棒棒座標（拜託不要刪XD）
        val howBonBon = LatLng(25.029639, 121.544416)
        mMap.addMarker(MarkerOptions()
                .position(howBonBon)
                .title("好棒棒！"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(howBonBon))

        if(checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(this, perms, REQUEST_LOCATION)
        } else {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true

            initLocation()

            getLocalData()
        }
    }

    private fun makeMainFragmentUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastActions.ACTION_SAVE_INSTANT_DATA)
        return intentFilter
    }

    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                BroadcastActions.ACTION_SAVE_INSTANT_DATA -> {
                    getLocalData()
                }
            }
        }
    }

}