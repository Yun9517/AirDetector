package com.microjet.airqi2

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.support.v4.app.ActivityCompat
import android.support.v4.app.ActivityCompat.checkSelfPermission
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v7.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.StyleSpan
import android.util.Log
import android.view.Menu
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
import com.microjet.airqi2.CustomAPI.OnMultipleClickListener
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.Colors
import com.mobile2box.MJGraphView.MJGraphData
import com.mobile2box.MJGraphView.MJGraphView
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_airmap.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * Created by B00174 on 2017/11/27.
 *
 */

class AirMapActivity : AppCompatActivity(), OnMapReadyCallback, MJGraphView.MJGraphUpdateCallback {

    private val REQUEST_LOCATION = 2
    private val perms: Array<String> = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)

    private var currentMarker: Marker? = null

    private var datePickerHandler = Handler()

    private lateinit var mDate: String

    private lateinit var realm: Realm
    private lateinit var result: RealmResults<AsmDataModel>

    private lateinit var listener: RealmChangeListener<RealmResults<AsmDataModel>>

    private lateinit var filter: List<AsmDataModel>

    private lateinit var mCal: Calendar
    private lateinit var mMap: GoogleMap

    //private var aResult = java.util.ArrayList<MJGraphData>()

    private lateinit var myPref: PrefObjects

    private var lati = 255f
    private var longi = 255f


    private var topMenu: Menu? = null
    private var bleIcon: MenuItem? = null       // 藍芽icon in actionbar
    private var battreyIcon: MenuItem? = null   //電量icon
    private var shareMap: MenuItem? = null      //分享icon


    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_airmap)

        myPref = PrefObjects(this)

        initActionBar()
        initGoogleMapFragment()
        initLineChart()
        createLocationRequest()

        mCal = Calendar.getInstance()
        mDate = DateFormat.format("yyyy-MM-dd", mCal.time).toString()
        datePicker.text = setBtnText("DATE $mDate")
        realm = Realm.getDefaultInstance()

        datePicker.setOnClickListener {
            if (Utils.isFastDoubleClick) {
                Utils.toastMakeTextAndShow(this@AirMapActivity, "連點，母湯喔！！",
                        Toast.LENGTH_SHORT)
            } else {
                datePickerHandler.post {
                    val dpd = DatePickerDialog(this@AirMapActivity, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        mCal.set(year, month, dayOfMonth)
                        Log.e("AirMap Button", mCal.get(Calendar.DAY_OF_MONTH).toString())
                        mDate = DateFormat.format("yyyy-MM-dd", mCal.time).toString()
                        datePicker.text = setBtnText("DATE $mDate")

                        pgLoading.visibility = View.VISIBLE
                        pgLoading.bringToFront()

                        runRealmQueryData()
                    }, mCal.get(Calendar.YEAR), mCal.get(Calendar.MONTH), mCal.get(Calendar.DAY_OF_MONTH))
                    dpd.setMessage(getString(R.string.select_Date))//請選擇日期
                    dpd.show()
                }
            }
        }

        imgExpand.setOnClickListener {
            if (valuePanel.visibility == View.VISIBLE) {
                imgExpand.setImageResource(R.drawable.airmap_infodrawer_open)

                collapseValuePanelAnim(250)

                valuePanel.visibility = View.GONE

                // 將面板顯示狀態放到偏好設定中
                myPref.setSharePreferenceMapPanelStat(false)
            } else {
                imgExpand.setImageResource(R.drawable.airmap_infodrawer_close)

                expandValuePanelAnim(250)

                valuePanel.visibility = View.VISIBLE

                // 將面板顯示狀態放到偏好設定中
                myPref.setSharePreferenceMapPanelStat(true)
            }
        }

        viewSelecter.setOnCheckedChangeListener { _, _ ->
            pgLoading.visibility = View.VISIBLE
            pgLoading.bringToFront()

            //runRealmQueryData()
            drawLineChart(filter)
            drawMapPolyLine(filter)
        }

        imgAirQuality.setOnClickListener(object : OnMultipleClickListener(10, 250) {
            override fun onMultipleClick(v: View) {
                loadFaceMarker()
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (myPref.getSharePreferenceMapPanelStat()) {
            valuePanel.visibility = View.VISIBLE
            imgExpand.setImageResource(R.drawable.airmap_infodrawer_close)
        } else {
            valuePanel.visibility = View.GONE
            imgExpand.setImageResource(R.drawable.airmap_infodrawer_open)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        result.removeAllChangeListeners()
        realm.close()
    }

    // 查詢資料庫
    private fun runRealmQueryData() {

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, mCal.get(Calendar.YEAR))
        cal.set(Calendar.MONTH, mCal.get(Calendar.MONTH))
        cal.set(Calendar.DAY_OF_MONTH, mCal.get(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 0) // ! clear would not reset the hour of day ! //這幾行是新寫法，好用
        cal.clear(Calendar.MINUTE) //這幾行是新寫法，好用
        cal.clear(Calendar.SECOND) //這幾行是新寫法，好用
        cal.clear(Calendar.MILLISECOND) //這幾行是新寫法，好用

        var startTime = cal.timeInMillis
        var endTime = startTime + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)

        val airMapInitData = getMapInitLati(startTime, endTime)
        if (airMapInitData.isNotEmpty()) {
            lati = airMapInitData.first()!!.latitude
            longi = airMapInitData.first()!!.longitude
            startTime = airMapInitData.first()!!.created_time
            endTime = airMapInitData.last()!!.created_time
        } else {
            val pastData = getMapInitThreeDaysAgoLati(startTime, endTime)
            if (pastData.isNotEmpty()) {
                lati = pastData.first()!!.latitude
                longi = pastData.first()!!.longitude
            } else {
                lati = TvocNoseData.lati
                longi = TvocNoseData.longi
            }
        }

        listener = RealmChangeListener {
            //filter = it.filter { it.latitude < 255f && it.latitude != null && it.macAddress == mDeviceAddress }
            filter = realm.copyFromRealm(it)
            if (filter.isNotEmpty()) {
                filter.forEach {
                    if (it.latitude == 255f) {// || Math.abs(it.latitude - lati) > 1f || Math.abs(it.longitude - longi) > 1f) {
                        it.latitude = lati
                        it.longitude = longi
                    }
                    lati = it.latitude
                    longi = it.longitude
                }
            }
            drawMapPolyLine(filter)
            drawLineChart(filter)
            Log.e("Realm Listener", "Update Map...")
        }

        result = realm.where(AsmDataModel::class.java)
                .between("Created_time", startTime, endTime)
                .sort("Created_time", Sort.ASCENDING).findAllAsync()

        result.addChangeListener(listener)
    }

    // 讀取線圖資料
    @SuppressLint("SimpleDateFormat")
    private fun drawLineChart(localDatas: List<AsmDataModel>) {
        val aResult = java.util.ArrayList<MJGraphData>()
        if (localDatas.isNotEmpty()) {
            for (i in 0 until localDatas.size) {
                // 判斷 RadioButton 選中的項目
                val itemValue = if (rbTVOC.isChecked) {
                    localDatas[i].tvocValue.toInt()
                } else {
                    localDatas[i].pM25Value.toInt()
                }

                val o: MJGraphData? = MJGraphData(localDatas[i].created_time, itemValue)
                if (o != null && i < localDatas.size) {
                    try {
                        aResult.add(o)
                        //lineChart.AddData(o)
                    } catch (_e: ClassCastException) {
                        _e.printStackTrace()
                    } catch (_e: IllegalArgumentException) {
                        _e.printStackTrace()
                    } catch (_e: UnsupportedOperationException) {
                        _e.printStackTrace()
                    }
                }

                val dateFormat = SimpleDateFormat("yyyy/MM/dd, HH:mm")
                Log.e("LoadChartData", "Time: ${dateFormat.format(localDatas[i].created_time)}, Value: $itemValue")
            }
            /*
            for (i in 1 .. 5) {
                val o: MJGraphData? = MJGraphData(localDatas.last().created_time + i * 60000, Random().nextInt(500))
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
            }*/
        } else {
            /*
            val o: MJGraphData? = MJGraphData(mCal.timeInMillis, 0)
            if (o != null) {
                try {
                    aResult.add(o)
                    //lineChart.AddData(o)
                } catch (_e: ClassCastException) {
                    _e.printStackTrace()
                } catch (_e: IllegalArgumentException) {
                    _e.printStackTrace()
                } catch (_e: UnsupportedOperationException) {
                    _e.printStackTrace()
                }
            }
            */

            val nullDataText = "-----"
            updateValuePanel(0, nullDataText, nullDataText, nullDataText, nullDataText, nullDataText, nullDataText, nullDataText)
        }

        lineChart.SetData(aResult)

        // 如果曲線圖目前的 Index 在很前面就不移動游標
        if (lineChart.CurrentIndex() > (aResult.size - 10) || lineChart.CurrentIndex() < 10) {
            lineChart.SetCurrentIndex(aResult.size - 1)
        }

        if (pgLoading.visibility == View.VISIBLE) {
            pgLoading.visibility = View.GONE
        }
    }

    // 畫軌跡
    private fun drawMapPolyLine(localDatas: List<AsmDataModel>) {
        mMap.clear()
        if (localDatas.isNotEmpty() && localDatas.size >= 2) {
            var rectOptions = PolylineOptions().width(25f)
            var _rangeID = judgePolyLineColorRange(localDatas[0]) //取第一筆資料的顏色Range
            localDatas.forEachIndexed { index, asmDataModel ->
                if (index < localDatas.size - 1) { //因為設定經緯度會用下一筆，所以大小要限制好
                    val newRangeID = judgePolyLineColorRange(asmDataModel) //每次都判斷新值的顏色range
                    if (_rangeID != newRangeID) { //如果不一樣
                        mMap.addPolyline(rectOptions.color(_rangeID)) //就要開始畫舊range的顏色
                        _rangeID = newRangeID //然後把舊range換掉
                        rectOptions = PolylineOptions().width(25f) //Polyline實體重設
                    } //繼續疊加新的上去
                    rectOptions.add(
                            LatLng(localDatas[index].latitude.toDouble(), localDatas[index].longitude.toDouble()),
                            LatLng(localDatas[index + 1].latitude.toDouble(), localDatas[index + 1].longitude.toDouble())
                    )
                    if (index == localDatas.size - 2) { //最後一筆畫最後的區塊
                        mMap.addPolyline(rectOptions.color(_rangeID))
                    }
                }
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

    // 展開動畫
    private fun expandValuePanelAnim(duration: Long) {
        val mShowAction = TranslateAnimation(Animation.RELATIVE_TO_PARENT, -1.0f,
                Animation.RELATIVE_TO_PARENT, 0.0f, Animation.RELATIVE_TO_PARENT,
                0f, Animation.RELATIVE_TO_PARENT, 0.0f)
        mShowAction.duration = duration

        panel.startAnimation(mShowAction)
    }

    // 關閉動畫
    private fun collapseValuePanelAnim(duration: Long) {
        val mHideAction = TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, -1.0f, Animation.RELATIVE_TO_PARENT,
                0.0f, Animation.RELATIVE_TO_PARENT, 0.0f)
        mHideAction.duration = duration

        panel.startAnimation(mHideAction)
    }

    // 更新那個笑到你心裡發寒的臉圖
    private fun updateFaceIcon(value: Int, isTVOC: Boolean) {
        if (isTVOC) {
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
        } else {
            when (value) {
                in 0..15 -> {
                    imgAirQuality.setImageResource(R.drawable.face_icon_01green_active)
                }
                in 16..34 -> {
                    imgAirQuality.setImageResource(R.drawable.face_icon_02yellow_active)
                }
                in 35..54 -> {
                    imgAirQuality.setImageResource(R.drawable.face_icon_03orange_active)
                }
                in 55..150 -> {
                    imgAirQuality.setImageResource(R.drawable.face_icon_04red_active)
                }
                in 151..250 -> {
                    imgAirQuality.setImageResource(R.drawable.face_icon_05purple_active)
                }
                else -> {
                    imgAirQuality.setImageResource(R.drawable.face_icon_06brown_active)
                }
            }
        }
    }

    // 更新左上角空污數值面板
    @SuppressLint("SetTextI18n", "SimpleDateFormat")
    private fun updateValuePanel(timeVal: Long, tvocVal: String, pm25Val: String, eco2Val: String,
                                 tempVal: String, humiVal: String, latiVal: String, longiVal: String) {
        val dateFormat = SimpleDateFormat("HH:mm")
        textTIMEvalue.text = if (timeVal != 0L) {
            dateFormat.format(timeVal)
        } else {
            "--:--"
        }

        textTVOCvalue.text = "$tvocVal ppb"

        textPM25value.text = if (pm25Val == "65535") {
            "沒有偵測"
        } else {
            "$pm25Val μg/m³"
        }
        val myPref = PrefObjects(this)
        val isFahrenheit = myPref.getSharePreferenceTempUnitFahrenheit()
        textECO2value.text = "$eco2Val ppm"
        textTEMPvalue.text = if (tempVal == "-----") {
            when (isFahrenheit) {
                true -> {
                    "$tempVal ℉"
                }
                false -> {
                    "$tempVal ℃"
                }
            }
        } else {
            Utils.convertTemperature(this@AirMapActivity, tempVal.toFloat())
        }
        textHUMIvalue.text = "$humiVal %"
        textLATIvalue.text = latiVal
        textLNGIvalue.text = longiVal
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

        // 移動畫面到目前的標記
        val zoomValue = mMap.cameraPosition.zoom
        Log.e("Zoom", "Value: $zoomValue")
        if (zoomValue < 5.0f) {     // 如果目前地圖縮放值為預設值2X，則放大到15X
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        } else {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomValue))
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
        //lineChart.SetInterval(10)
        //lineChart.SetInterval(30)

        // set gap between each item (min: 2px, max: 6px)
        // ----------------------------------------------
        //lineChart.SetItemGap(3)
        lineChart.SetItemGap(3)

        // set labels
        // ----------
        lineChart.SetLabelMonth(arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"))
        lineChart.SetLabelWeek(arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"))
        //lineChart.SetLabelYear(", %d")

        //lineChart.SetLabelMonth(arrayOf("一月", "二月", "三月", "四月", "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"))
        //lineChart.SetLabelWeek(arrayOf("週日", "週一", "週二", "週三", "週四", "週五", "週六"))
        //lineChart.SetLabelYear(" %d年")

        // set the graph line width (min: 2px, max: 8px)
        // ---------------------------------------------
        lineChart.SetLineWidth(2)
        //	lineChart.SetLineWidth(8)

        // set graph mode
        // --------------
        //	lineChart.SetMode(MJGraphView.MODE_MONTHLY)
        //	lineChart.SetMode(MJGraphView.MODE_WEEKLY)
        lineChart.SetMode(MJGraphView.MODE_DAILY)

        // set callback to handle updates on scroll or pinch
        // -------------------------------------------------
        lineChart.SetOnUpdateCallback(this)
    }

    // 初始化ActionBar
    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    // 初始化GoogleMap UI元件
    private fun initGoogleMapFragment() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    // 設定ActionBar返回鍵的動作
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

            try {
                // Customise the styling of the base map using a JSON object defined
                // in a raw resource file.
                mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json))
            } catch (e: Resources.NotFoundException) {
                e.printStackTrace()
            }

            initLocation()

            runRealmQueryData()
        }
    }

    // 圖表滑動時的callback
    @SuppressLint("SimpleDateFormat")
    override fun OnUpdate(_index: Int, _data: MJGraphData) {
        if (lineChart.Mode() != MJGraphView.MODE_DAILY) {
            lineChart.SetMode(MJGraphView.MODE_DAILY)
        }
        val data = if (rbTVOC.isChecked) {
            filter[_index].tvocValue!!.toInt()
        } else {
            filter[_index].pM25Value!!.toInt()
        }

        try {
            putMarker((filter[_index].latitude)!!.toDouble(),
                    (filter[_index].longitude)!!.toDouble())

            updateFaceIcon(data, rbTVOC.isChecked)

            updateValuePanel(filter[_index].created_time, filter[_index].tvocValue,
                    filter[_index].pM25Value, filter[_index].ecO2Value,
                    filter[_index].tempValue, filter[_index].humiValue,
                    filter[_index].latitude.toString(), filter[_index].longitude.toString())
        } catch (_e: IllegalArgumentException) {
            _e.printStackTrace()
        } catch (_e: NullPointerException) {
            _e.printStackTrace()
        }

        val dateFormat = SimpleDateFormat("yyyy/MM/dd, HH:mm")
        Log.e("Scroll", "Time: ${dateFormat.format(filter[_index].created_time)}, " +
                "Timestamp: ${filter[_index].created_time}, Value: $data, " +
                "Lat: ${filter[_index].latitude}, Lng: ${filter[_index].longitude}")
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
        locationRequest.interval = 50000         // original is 5000 milliseconds
        locationRequest.fastestInterval = 20000  // original is 2000 milliseconds
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


    // 彩蛋 軌跡圖變成一堆臉
    private fun loadFaceMarker() {
        mMap.clear()

        for (i in 0 until filter.size) {
            val latLng = LatLng(filter[i].latitude.toDouble(), filter[i].longitude.toDouble())
            val markerOptions = MarkerOptions()

            markerOptions.icon(when (filter[i].tvocValue.toInt()) {
                in 0..219 -> {
                    BitmapDescriptorFactory.fromResource(R.drawable.face_icon_01green_active)
                }
                in 220..659 -> {
                    BitmapDescriptorFactory.fromResource(R.drawable.face_icon_02yellow_active)
                }
                in 660..2199 -> {
                    BitmapDescriptorFactory.fromResource(R.drawable.face_icon_03orange_active)
                }
                in 2200..5499 -> {
                    BitmapDescriptorFactory.fromResource(R.drawable.face_icon_04red_active)
                }
                in 5500..19999 -> {
                    BitmapDescriptorFactory.fromResource(R.drawable.face_icon_05purple_active)
                }
                else -> {
                    BitmapDescriptorFactory.fromResource(R.drawable.face_icon_06brown_active)
                }
            })
            mMap.addMarker(markerOptions.position(latLng))
        }
    }

    private fun getMapInitThreeDaysAgoLati(start: Long, end: Long): RealmResults<AsmDataModel> {
        //取三天前的經緯度最新值位置
        val realm = Realm.getDefaultInstance()
        val startTime = start - TimeUnit.DAYS.toMillis(3)
        val endTime = end - TimeUnit.DAYS.toMillis(1)
        val pastAvailableGPSLocation = realm.where(AsmDataModel::class.java)
                .between("Created_time", startTime, endTime)
                .notEqualTo("Latitude", 255f)
                .sort("Created_time", Sort.DESCENDING).findAll()
        realm.close()
        return pastAvailableGPSLocation
    }

    private fun getMapInitLati(start: Long, end: Long): RealmResults<AsmDataModel> {
        val realm = Realm.getDefaultInstance()
        val availableGPSObj = realm.where(AsmDataModel::class.java)
                .between("Created_time", start, end)
                .notEqualTo("Latitude", 255f)
                .sort("Created_time", Sort.ASCENDING).findAll()
        realm.close()
        return availableGPSObj
    }

    private fun judgePolyLineColorRange(data: AsmDataModel): Int {
        if (rbTVOC.isChecked) {
            when (data.tvocValue.toInt()) {
                in 0..219 -> {
                    return (Colors.tvocCO2Colors[0])
                }
                in 220..659 -> {
                    return (Colors.tvocCO2Colors[1])
                }
                in 660..2199 -> {
                    return (Colors.tvocCO2Colors[2])
                }
                in 2200..5499 -> {
                    return (Colors.tvocCO2Colors[3])
                }
                in 5500..19999 -> {
                    return (Colors.tvocCO2Colors[4])
                }
                else -> {
                    return (Colors.tvocCO2Colors[5])
                }
            }
        } else {
            when (data.pM25Value.toInt()) {
                in 0..15 -> {
                    return (Colors.tvocCO2Colors[0])
                }
                in 16..34 -> {
                    return (Colors.tvocCO2Colors[1])
                }
                in 35..54 -> {
                    return (Colors.tvocCO2Colors[2])
                }
                in 55..150 -> {
                    return (Colors.tvocCO2Colors[3])
                }
                in 151..250 -> {
                    return (Colors.tvocCO2Colors[4])
                }
                else -> {
                    return (Colors.tvocCO2Colors[5])
                }
            }
        }
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
            ActivityCompat.checkSelfPermission(this@AirMapActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ->
                ActivityCompat.requestPermissions(this@AirMapActivity,
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

    private fun combineBitmapInCenter(background: Bitmap, midBitmap: Bitmap, foreground: Bitmap): Bitmap {
        var background = background
        if (!background.isMutable) {
            background = background.copy(Bitmap.Config.ARGB_8888, true)
        }
        val paint = Paint()
        val canvas = Canvas(background)
        val bw = background.width
        val bh = background.height

        val mw = midBitmap.width
        //val mh = midBitmap.height
        val mx = ((mw - bw) / 2).toFloat()
        val my = bh - (0.875f * bh)//((mh - bh) / 2).toFloat()
        canvas.drawBitmap(midBitmap, mx, my, paint)

        //val fw = foreground.width
        //val fh = foreground.height
        val fx = Utils.convertDpToPixel(8f, this@AirMapActivity)//((fw - bw) / 2).toFloat()
        val fy = (bh - (0.875f * bh)) + Utils.convertDpToPixel(8f, this@AirMapActivity)//((fh - bh) / 2).toFloat()
        canvas.drawBitmap(foreground, fx, fy, paint)

        canvas.save(Canvas.ALL_SAVE_FLAG)
        canvas.restore()
        return background
    }

    @SuppressLint("SimpleDateFormat")
    private fun picture() {
        val callback = GoogleMap.SnapshotReadyCallback{
            val midBitmap = it
            val bgBitmap = screenShot(window.decorView.rootView)
            val fgBitmap = screenShot(panel)

            val bitmap = combineBitmapInCenter(bgBitmap, midBitmap, fgBitmap)
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

            midBitmap.recycle()
            bgBitmap.recycle()
            fgBitmap.recycle()
            bitmap.recycle()
        }

        mMap.snapshot(callback)
    }
}