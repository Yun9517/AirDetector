package com.microjet.airqi2

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Typeface
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.ActivityCompat.checkSelfPermission
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.StyleSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.microjet.airqi2.BlueTooth.BLECallingTranslate
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.BroadcastActions
import com.mobile2box.MJGraphView.MJGraphData
import com.mobile2box.MJGraphView.MJGraphView
import io.realm.Realm
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_airmap.*
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


/**
 * Created by B00174 on 2017/11/27.
 *
 */

class AirMapActivity : AppCompatActivity(), OnMapReadyCallback, MJGraphView.MJGraphUpdateCallback {

    private val REQUEST_LOCATION = 2
    private val perms: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private var currentMarker: Marker? = null

    private var datepickerHandler = Handler()

    private lateinit var mDate: String

    private var errorTime = 0

    private var showTVOC = true

    private var runGetDataThread: Thread? = null
    private val runGetDataRunnable = Runnable {
        runOnUiThread({
            //getLocalData()
            drawMapPolyLine(showTVOC)
        })
    }

    companion object {
        private lateinit var mCal: Calendar
        private lateinit var mMap: GoogleMap
        private var dataArray = ArrayList<AirQiDataSet>()
        var aResult = java.util.ArrayList<MJGraphData>()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_airmap)

        initActionBar()
        initGoogleMapFragment()

        createLocationRequest()

        mCal = Calendar.getInstance()
        mDate = DateFormat.format("yyyy-MM-dd", mCal.time).toString()
        datePicker.text = setBtnText("DATE $mDate")

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
                        datePicker.text = setBtnText("DATE $mDate")

                        //pgLoading.visibility = View.VISIBLE
                        //pgLoading.bringToFront()

                        //startLoadDataThread()

                        LoadData(WeakReference(lineChart), rbTVOC.isChecked).execute()
                    }, mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH), mCal.get(Calendar.DAY_OF_MONTH))
                    dpd.setMessage("請選擇日期")
                    dpd.show()
                }
            }
        }

        imgExpand.setOnClickListener {
            if (valuePanel.visibility == View.VISIBLE) {
                imgExpand.setImageResource(R.drawable.airmap_infodrawer_open)

                collapseValuePanelAnim(250)

                valuePanel.visibility = View.GONE
            } else {
                imgExpand.setImageResource(R.drawable.airmap_infodrawer_close)

                expandValuePanelAnim(250)

                valuePanel.visibility = View.VISIBLE
            }
        }

        viewSelecter.setOnCheckedChangeListener { _, _ ->
            //startLoadDataThread()
            showTVOC = rbTVOC.isChecked
            LoadData(WeakReference(lineChart), rbTVOC.isChecked).execute()
        }

        LocalBroadcastManager.getInstance(this@AirMapActivity).registerReceiver(mGattUpdateReceiver,
                makeBroadcastReceiverFilter())
    }

    override fun onResume() {
        super.onResume()
        LoadData(WeakReference(lineChart), rbTVOC.isChecked).execute()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(this@AirMapActivity).unregisterReceiver(mGattUpdateReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // GetData執行緒啟動與關閉🙄
    fun startLoadDataThread() {
        runGetDataThread = Thread(runGetDataRunnable)
        runGetDataThread!!.start()
    }

    private fun stopLoadDataThread() {
        if (runGetDataThread != null) {
            runGetDataThread!!.interrupt()
            runGetDataThread = null
        }

        //if (pgLoading.visibility == View.VISIBLE) {
        //    pgLoading.visibility = View.GONE
        //}
    }

    // 取得軌跡顏色
    private fun setPolylineColor(value: Int, isTVOC: Boolean): Int {
        if (isTVOC) {
            return when (value) {
                in 0..219 -> ContextCompat.getColor(MyApplication.applicationContext(), R.color.air_map_line_value1)
                in 220..659 -> ContextCompat.getColor(MyApplication.applicationContext(), R.color.air_map_line_value2)
                in 660..2199 -> ContextCompat.getColor(MyApplication.applicationContext(), R.color.air_map_line_value3)
                in 2200..5499 -> ContextCompat.getColor(MyApplication.applicationContext(), R.color.air_map_line_value4)
                in 5500..19999 -> ContextCompat.getColor(MyApplication.applicationContext(), R.color.air_map_line_value5)
                else -> ContextCompat.getColor(MyApplication.applicationContext(), R.color.air_map_line_value6)
            }
        } else {
            return when (value) {
                in 0..15 -> ContextCompat.getColor(MyApplication.applicationContext(), R.color.air_map_line_value1)
                in 16..34 -> ContextCompat.getColor(MyApplication.applicationContext(), R.color.air_map_line_value2)
                in 35..54 -> ContextCompat.getColor(MyApplication.applicationContext(), R.color.air_map_line_value3)
                in 55..150 -> ContextCompat.getColor(MyApplication.applicationContext(), R.color.air_map_line_value4)
                in 151..250 -> ContextCompat.getColor(MyApplication.applicationContext(), R.color.air_map_line_value5)
                else -> ContextCompat.getColor(MyApplication.applicationContext(), R.color.air_map_line_value6)
            }
        }
    }

    // 文字分割
    private fun setBtnText(value: String): SpannableString {
        val textSpan = SpannableString(value)
        textSpan.setSpan(StyleSpan(Typeface.BOLD),
                0, 4, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        textSpan.setSpan(StyleSpan(Typeface.NORMAL),
                5, value.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

        return textSpan
    }

    // 動畫
    private fun expandValuePanelAnim(duration: Long) {
        val mShowAction = TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0f, Animation.RELATIVE_TO_PARENT, 0.0f)
        mShowAction.duration = duration

        panel.startAnimation(mShowAction)
    }

    private fun collapseValuePanelAnim(duration: Long) {
        val mHideAction = TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT,
                0.0f, Animation.RELATIVE_TO_PARENT, 0.0f)
        mHideAction.duration = duration

        panel.startAnimation(mHideAction)
    }

    // 畫軌跡
    private fun drawMapPolyLine(boolean: Boolean) {
        if (dataArray.size > 0) {
            for (i in 0 until dataArray.size) {

                // 過濾掉初始值
                if (dataArray[i].getLatitude() != 24.959817f && dataArray[i].getLongitude() != 121.4215f) {

                    val data = if (boolean) {
                        dataArray[i].getTVOCValue()!!.toInt()
                    } else {
                        dataArray[i].getPM25Value()!!.toInt()
                    }

                    val rectOptions = PolylineOptions()
                            .width(20F)
                            .color(setPolylineColor(data, boolean))

                    if (i < dataArray.size - 1) {
                        rectOptions.add(LatLng(dataArray[i].getLatitude()!!.toDouble(), dataArray[i].getLongitude()!!.toDouble()))
                        rectOptions.add(LatLng(dataArray[i + 1].getLatitude()!!.toDouble(), dataArray[i + 1].getLongitude()!!.toDouble()))
                        mMap.addPolyline(rectOptions)
                    }
                }
            }
        } else {
            dataArray.clear()
            aResult.clear()
        }

        stopLoadDataThread()
    }

    // 更新那個笑到你心裡發寒的臉圖
    private fun updateFaceIcon(value: Int) {
        when (value) {
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

        textPM25value.text = if (pm25Val == "65535") {
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

        // 移動畫面到目前的標記
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))

        if (currentMarker != null) {
            currentMarker!!.remove()
            currentMarker = null
        }

        if (currentMarker == null) {
            currentMarker = mMap.addMarker(MarkerOptions().position(latLng))
        }
    }

    // 初始化 lineChart
    private fun initLineChart() {
        // --------------------------
        // MJGraphView Configurations
        // --------------------------

        // create cursor (min: 8px, max: 24px)
        // -----------------------------------
        lineChart.CreateCursor(0xffff0000.toInt(), 0x80000000.toInt(), 16)

        // initialize the data interval in minutes (min: 1min, max: 60min)
        // ---------------------------------------------------------------
        lineChart.SetInterval(1)
//			viewAppMainGraph.SetInterval(10)
//			viewAppMainGraph.SetInterval(30)

        // set gap between each item (min: 2px, max: 6px)
        // ----------------------------------------------
//			viewAppMainGraph.SetItemGap(3)
        lineChart.SetItemGap(6)

        // set labels
        // ----------
        lineChart.SetLabelMonth(arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"))
        lineChart.SetLabelWeek(arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"))
//			viewAppMainGraph.SetLabelYear(", %d")

//			viewAppMainGraph.SetLabelMonth(arrayOf("一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"))
//			viewAppMainGraph.SetLabelWeek(arrayOf("週日", "週一", "週二", "週三", "週四", "週五", "週六"))
//			viewAppMainGraph.SetLabelYear(" %d年")

        // set the graph line width (min: 2px, max: 8px)
        // ---------------------------------------------
        lineChart.SetLineWidth(3)
//			viewAppMainGraph.SetLineWidth(8)

        // set graph mode
        // --------------
//			viewAppMainGraph.SetMode(MJGraphView.MODE_MONTHLY)
//			viewAppMainGraph.SetMode(MJGraphView.MODE_WEEKLY)
        lineChart.SetMode(MJGraphView.MODE_DAILY)

        // set callback to handle updates on scroll or pinch
        // -------------------------------------------------
        lineChart.SetOnUpdateCallback(this)

        LoadData(WeakReference(lineChart), rbTVOC.isChecked).execute()
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
            if (it.isSuccessful) {
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
    @SuppressLint("RestrictedApi")
    private fun createLocationRequest() {
        val locationRequest = LocationRequest()
        locationRequest.interval = 5000         // original is 5000 milliseconds
        locationRequest.fastestInterval = 2000  // original is 2000 milliseconds
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    // 權限要求結果，由於已經先在onMapReady()中要求權限了，因此在處理的程式碼中無需再次要求權限
    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<out String>,
                                            grantResults: IntArray) {
        when (requestCode) {
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
        //val howBonBon = LatLng(25.029639, 121.544416)
        //mMap.addMarker(MarkerOptions()
        //        .position(howBonBon)
        //        .title("好棒棒！"))
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(howBonBon))

        if (checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(this, perms, REQUEST_LOCATION)
        } else {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isZoomControlsEnabled = true

            initLocation()

            //pgLoading.visibility = View.VISIBLE
            //pgLoading.bringToFront()

            //startLoadDataThread()
            initLineChart()
        }

        /*try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json))

            if (!success) {

            }
        } catch (e: Resources.NotFoundException) {
        }*/
    }

    @SuppressLint("SimpleDateFormat")
    override fun OnUpdate(_data: MJGraphData) {
        val position = aResult.indexOf(_data)
        //Log.e("LineChart", "Value: ${_data.Value()}, index: $position")
        try {
            putMarker((dataArray[position].getLatitude())!!.toDouble(),
                    (dataArray[position].getLongitude())!!.toDouble())

            val data = if (rbTVOC.isChecked) {
                dataArray[position].getTVOCValue()!!.toInt()
            } else {
                dataArray[position].getPM25Value()!!.toInt()
            }

            updateFaceIcon(data)

            updateValuePanel(dataArray[position].getTVOCValue()!!, dataArray[position].getPM25Value()!!,
                    dataArray[position].getECO2Value()!!, dataArray[position].getTEMPValue()!!,
                    dataArray[position].getHUMIValue()!!)
        } catch (_e: IllegalArgumentException) {
            _e.printStackTrace()
        } catch (_e: NullPointerException) {
            _e.printStackTrace()
        }


        val dateFormat = SimpleDateFormat("yyyy/MM/dd, HH:mm")
        Log.e("on ScrollView", "Time: ${dateFormat.format(dataArray[position].getCreatedTime()!!)}, Timestamp: ${dataArray[position].getCreatedTime()!!}")
    }

    private fun makeBroadcastReceiverFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastActions.ACTION_SAVE_INSTANT_DATA)
        intentFilter.addAction(BroadcastActions.ACTION_DATA_AVAILABLE)
        return intentFilter
    }

    private val mGattUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                BroadcastActions.ACTION_SAVE_INSTANT_DATA -> {
                    //startLoadDataThread()

                    LoadData(WeakReference(lineChart), rbTVOC.isChecked).execute()
                }
                BroadcastActions.ACTION_DATA_AVAILABLE -> {
                    dataAvaliable(intent)
                }
            }
        }
    }




    private fun runRealmQueryData(): RealmResults<AsmDataModel> {
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)

        //現在時間實體毫秒
        val touchTime = if (mCal.get(Calendar.HOUR_OF_DAY) >= 8) mCal.timeInMillis else mCal.timeInMillis + mCal.timeZone.rawOffset
        //將日期設為今天日子加一天減1秒
        val startTime = touchTime / (3600000 * 24) * (3600000 * 24) - mCal.timeZone.rawOffset
        val endTime = startTime + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        query.between("Created_time", startTime, endTime).sort("Created_time", Sort.ASCENDING)
        return query.findAll()
    }

    @SuppressLint("SimpleDateFormat")
    private fun dataAvaliable(intent: Intent) {
        val txValue = intent.getByteArrayExtra(BroadcastActions.ACTION_EXTRA_DATA)
        when (txValue[0]) {
            0xE0.toByte() -> {
            }
            0xE1.toByte() -> {
            }
            0xEA.toByte() -> {
            }
            else -> {
            }
        }
        when (txValue[2]) {
            0xB1.toByte() -> Log.d("AirMapAC", "cmd:0xB1 feedback")
            0xB2.toByte() -> Log.d("AirMapAC", "cmd:0xB2 feedback")
            0xB4.toByte() -> Log.d("AirMapAC", "cmd:0xB4 feedback")
            0xB5.toByte() -> Log.d("AirMapAC", "cmd:0xB5 feedback")
            0xB9.toByte() -> Log.d("AirMapAC", "cmd:0xB9 feedback")
            0xBA.toByte() -> Log.d("AirMapAC", "cmd:0xBA feedback")
        }
        when (txValue[3]) {
            0xE0.toByte() -> {
                Log.d("AirMapAC feeback", "ok"); }
            0xE1.toByte() -> {
                Log.d("AirMapAC feedback", "Couldn't write in device"); return
            }
            0xE2.toByte() -> {
                Log.d("AirMapAC feedback", "Temperature sensor fail"); return
            }
            0xE3.toByte() -> {
                Log.d("AirMapAC feedback", "B0TVOC sensor fail"); return
            }
            0xE4.toByte() -> {
                Log.d("AirMapAC feedback", "Pump power fail"); return
            }
            0xE5.toByte() -> {
                Log.d("AirMapAC feedback", "Invalid value"); return
            }
            0xE6.toByte() -> {
                Log.d("AirMapAC feedback", "Unknown command"); return
            }
            0xE7.toByte() -> {
                Log.d("AirMapAC feedback", "Waiting timeout"); return
            }
            0xE8.toByte() -> {
                Log.d("AirMapAC feedback", "Checksum error"); return
            }
        }

        if (errorTime >= 3) {
            errorTime = 0
        }
        if (!Utils.checkCheckSum(txValue)) {
            errorTime += 1
        } else {
            when (txValue[2]) {
                0xB0.toByte() -> {
                }
                0xB1.toByte() -> {
                }
                0xB2.toByte() -> {
                }
                0xB4.toByte() -> {
                }
                0xB5.toByte() -> {
                }
                0xB9.toByte() -> {
                }
                0xBA.toByte() -> {
                    //MyApplication.setSharePreferenceManualDisconn(true)
                    //Log.e("AirMapAC", "Manual Disconnect from Device.........")
                }
                0xE0.toByte() -> {
                }
                0xBB.toByte() -> {
                }
                0xC0.toByte() -> {
                }
                0xC5.toByte() -> {
                }
                0xC6.toByte() -> {
                    //LoadData(WeakReference(lineChart), rbTVOC.isChecked).execute()
                    val hashMap = BLECallingTranslate.ParserGetAutoSendDataKeyValueC6(txValue)

                    val data = if(rbTVOC.isChecked) {
                        hashMap[TvocNoseData.C6TVOC]!!.toInt()
                    } else {
                        hashMap[TvocNoseData.C6PM25]!!.toInt()
                    }

                    val rtcTime = hashMap[TvocNoseData.C6TIME]!!.toLong() * 1000
                    lineChart.AddData(MJGraphData(rtcTime, data))
                    aResult.add(MJGraphData(rtcTime, data))

                    val temp = AirQiDataSet()
                    temp.setTVOCValue(hashMap[TvocNoseData.C6TVOC]!!)
                    temp.setPM25Value(hashMap[TvocNoseData.C6PM25]!!)
                    temp.setHUMIValue(hashMap[TvocNoseData.C6HUMI]!!)
                    temp.setTEMPValue(hashMap[TvocNoseData.C6TEMP]!!)
                    temp.setECO2Value(hashMap[TvocNoseData.C6ECO2]!!)
                    temp.setCreatedTime(rtcTime/* + mCal.timeZone.rawOffset*/)
                    temp.setLatitude(TvocNoseData.lati)
                    temp.setLongitude(TvocNoseData.longi)
                    dataArray.add(temp)

                    val dateFormat = SimpleDateFormat("yyyy/MM/dd, HH:mm")
                    Log.e("on DataAvailable", "Time: ${dateFormat.format(temp.getCreatedTime()!!)}, Value: $data")
                }
            }
        }
    }


    private class LoadData(private val _viewGraph: WeakReference<MJGraphView>?,
                           private val _isTVOC: Boolean) :
            AsyncTask<Void, Void, MutableList<MJGraphData>>() {     //AsyncTask<Void, Void, MutableList<MJGraphData>>()

        @SuppressLint("SimpleDateFormat")
        override fun doInBackground(vararg _params: Void): MutableList<MJGraphData>? {
            Looper.prepare()
            val result = AirMapActivity().runRealmQueryData()

            Log.d("DATE", "Today total count: ${result.size}")

            if (result.size > 0) {
                dataArray.clear()
                aResult.clear()

                for (i in 0 until result.size) {

                    // 過濾掉初始值
                    if (result[i]!!.latitude != 24.959817f && result[i]!!.longitude != 121.4215f) {
                        val temp = AirQiDataSet()
                        temp.setTVOCValue(result[i]!!.tvocValue)
                        temp.setPM25Value(result[i]!!.pM25Value)
                        temp.setHUMIValue(result[i]!!.humiValue)
                        temp.setTEMPValue(result[i]!!.tempValue)
                        temp.setECO2Value(result[i]!!.ecO2Value)
                        temp.setLatitude(result[i]!!.latitude)
                        temp.setLongitude(result[i]!!.longitude)
                        temp.setCreatedTime(result[i]!!.created_time)
                        dataArray.add(temp)

                        // 判斷 RadioButton 選中的項目
                        val data = if (_isTVOC) {
                            result[i]!!.tvocValue.toInt()
                        } else {
                            result[i]!!.pM25Value.toInt()
                        }

                        val o: MJGraphData? = MJGraphData(temp.getCreatedTime()!!, data)
                        if (o != null) {
                            try {
                                aResult.add(o)
                            } catch (_e: ClassCastException) {
                                _e.printStackTrace()
                            } catch (_e: IllegalArgumentException) {
                                _e.printStackTrace()
                            } catch (_e: UnsupportedOperationException) {
                                _e.printStackTrace()
                            }
                        }

                        val dateFormat = SimpleDateFormat("yyyy/MM/dd, HH:mm")
                        Log.e("onFirstLoad", "Time: ${dateFormat.format(temp.getCreatedTime()!!)}, Value: $data")
                    }
                }
            } else {
                dataArray.clear()
                aResult.clear()
            }

            return aResult
        }

        override fun onPostExecute(_data: MutableList<MJGraphData>?) {
            if (_viewGraph != null) {
                // set source data
                // ---------------
                _viewGraph.get()!!.SetData(_data)
                _viewGraph.clear()

                AirMapActivity().startLoadDataThread()
            }

            //if(_viewGraph != null) {
            //    _viewGraph.get()!!.AddData(aResult[dataArray.lastIndex])
            //}
        }
    }
}