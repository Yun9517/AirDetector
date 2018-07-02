package com.microjet.airqi2

import android.Manifest
import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.ActivityCompat.checkSelfPermission
import android.support.v4.app.ActivityCompat.requestPermissions
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
        if(myPref.getSharePreferenceMapPanelStat()) {
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
        realm = Realm.getDefaultInstance()

        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, mCal.get(Calendar.YEAR))
        cal.set(Calendar.MONTH, mCal.get(Calendar.MONTH))
        cal.set(Calendar.DAY_OF_MONTH, mCal.get(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 0) // ! clear would not reset the hour of day ! //這幾行是新寫法，好用
        cal.clear(Calendar.MINUTE) //這幾行是新寫法，好用
        cal.clear(Calendar.SECOND) //這幾行是新寫法，好用
        cal.clear(Calendar.MILLISECOND) //這幾行是新寫法，好用

        val startTime = cal.timeInMillis
        val endTime = startTime + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)

        listener = RealmChangeListener {
            //filter = it.filter { it.latitude < 255f && it.latitude != null && it.macAddress == mDeviceAddress }
            filter = realm.copyFromRealm(it)
            filter.forEach {
                if (it.latitude == 255f) {
                    it.latitude = lati
                    it.longitude = longi
                }
                lati = it.latitude
                longi = it.longitude
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
            updateValuePanel(0 ,nullDataText, nullDataText, nullDataText, nullDataText, nullDataText, nullDataText, nullDataText)
        }

        lineChart.SetData(aResult)

        // 如果曲線圖目前的 Index 在很前面就不移動游標
        if(lineChart.CurrentIndex() > (aResult.size - 10) || lineChart.CurrentIndex() < 10) {
            lineChart.SetCurrentIndex(aResult.size - 1)
        }

        if (pgLoading.visibility == View.VISIBLE) {
            pgLoading.visibility = View.GONE
        }
    }

    // 畫軌跡
    private fun drawMapPolyLine(localDatas: List<AsmDataModel>) {

        val rectOptions1 = PolylineOptions().color(Colors.tvocCO2Colors[0])
        val rectOptions2 = PolylineOptions().color(Colors.tvocCO2Colors[1])
        val rectOptions3 = PolylineOptions().color(Colors.tvocCO2Colors[2])
        val rectOptions4 = PolylineOptions().color(Colors.tvocCO2Colors[3])
        val rectOptions5 = PolylineOptions().color(Colors.tvocCO2Colors[4])
        val rectOptions6 = PolylineOptions().color(Colors.tvocCO2Colors[5])
        
        //val datas = dataOrigin//.filter { it.latitude < 255f && it.latitude != null }
        localDatas?.forEachIndexed { index, asmDataModel ->
            if (index < localDatas.size - 1) {
                if (rbTVOC.isChecked) {
                    when (asmDataModel.tvocValue.toInt()) {
                        in 0..219 -> {
                            rectOptions1.add(LatLng(localDatas[index].latitude.toDouble(), localDatas[index].longitude.toDouble()))
                            rectOptions1.add(LatLng(localDatas[index + 1].latitude.toDouble(), localDatas[index + 1].longitude.toDouble()))
                        }
                        in 220..659 -> {
                            rectOptions2.add(LatLng(localDatas[index].latitude.toDouble(), localDatas[index].longitude.toDouble()))
                            rectOptions2.add(LatLng(localDatas[index + 1].latitude.toDouble(), localDatas[index + 1].longitude.toDouble()))
                        }
                        in 660..2199 -> {
                            rectOptions3.add(LatLng(localDatas[index].latitude.toDouble(), localDatas[index].longitude.toDouble()))
                            rectOptions3.add(LatLng(localDatas[index + 1].latitude.toDouble(), localDatas[index + 1].longitude.toDouble()))
                        }
                        in 2200..5499 -> {
                            rectOptions4.add(LatLng(localDatas[index].latitude.toDouble(), localDatas[index].longitude.toDouble()))
                            rectOptions4.add(LatLng(localDatas[index + 1].latitude.toDouble(), localDatas[index + 1].longitude.toDouble()))
                        }
                        in 5500..19999 -> {
                            rectOptions5.add(LatLng(localDatas[index].latitude.toDouble(), localDatas[index].longitude.toDouble()))
                            rectOptions5.add(LatLng(localDatas[index + 1].latitude.toDouble(), localDatas[index + 1].longitude.toDouble()))
                        }
                        else -> {
                            rectOptions6.add(LatLng(localDatas[index].latitude.toDouble(), localDatas[index].longitude.toDouble()))
                            rectOptions6.add(LatLng(localDatas[index + 1].latitude.toDouble(), localDatas[index + 1].longitude.toDouble()))
                        }
                    }

                } else {
                    when (asmDataModel.pM25Value.toInt()) {
                        in 0..15 -> {
                            rectOptions1.add(LatLng(localDatas[index].latitude.toDouble(), localDatas[index].longitude.toDouble()))
                            rectOptions1.add(LatLng(localDatas[index + 1].latitude.toDouble(), localDatas[index + 1].longitude.toDouble()))
                        }
                        in 16..34 -> {
                            rectOptions2.add(LatLng(localDatas[index].latitude.toDouble(), localDatas[index].longitude.toDouble()))
                            rectOptions2.add(LatLng(localDatas[index + 1].latitude.toDouble(), localDatas[index + 1].longitude.toDouble()))
                        }
                        in 35..54 -> {
                            rectOptions3.add(LatLng(localDatas[index].latitude.toDouble(), localDatas[index].longitude.toDouble()))
                            rectOptions3.add(LatLng(localDatas[index + 1].latitude.toDouble(), localDatas[index + 1].longitude.toDouble()))
                        }
                        in 55..150 -> {
                            rectOptions4.add(LatLng(localDatas[index].latitude.toDouble(), localDatas[index].longitude.toDouble()))
                            rectOptions4.add(LatLng(localDatas[index + 1].latitude.toDouble(), localDatas[index + 1].longitude.toDouble()))
                        }
                        in 151..250 -> {
                            rectOptions5.add(LatLng(localDatas[index].latitude.toDouble(), localDatas[index].longitude.toDouble()))
                            rectOptions5.add(LatLng(localDatas[index + 1].latitude.toDouble(), localDatas[index + 1].longitude.toDouble()))
                        }
                        else -> {
                            rectOptions6.add(LatLng(localDatas[index].latitude.toDouble(), localDatas[index].longitude.toDouble()))
                            rectOptions6.add(LatLng(localDatas[index + 1].latitude.toDouble(), localDatas[index + 1].longitude.toDouble()))
                        }
                    }
                }
            }

        }

        // 先清完再畫
        mMap.clear()

        mMap.addPolyline(rectOptions1)
        mMap.addPolyline(rectOptions2)
        mMap.addPolyline(rectOptions3)
        mMap.addPolyline(rectOptions4)
        mMap.addPolyline(rectOptions5)
        mMap.addPolyline(rectOptions6)
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
        if(isTVOC) {
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

        val finalTempVal = Utils.convertTemperature(this@AirMapActivity, tempVal.toFloat())

        val dateFormat = SimpleDateFormat("HH:mm")

        textTIMEvalue.text = if(timeVal != 0L) {
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

        textECO2value.text = "$eco2Val ppm"
        textTEMPvalue.text = finalTempVal
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
        if(zoomValue < 5.0f) {     // 如果目前地圖縮放值為預設值2X，則放大到15X
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
        if(lineChart.Mode() != MJGraphView.MODE_DAILY) {
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



    // 彩蛋 軌跡圖變成一堆臉
    private fun loadFaceMarker() {
        mMap.clear()

        for(i in 0 until result.size) {
            val latLng = LatLng(filter[i].latitude.toDouble(), filter[i].longitude.toDouble())
            val markerOptions = MarkerOptions()

            markerOptions.icon(when(filter[i].tvocValue.toInt()) {
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

}