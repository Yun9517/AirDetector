package com.microjet.airqi2


import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Spannable
import android.text.SpannableString
import android.text.format.DateFormat
import android.text.style.StyleSpan
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import com.amap.api.maps2d.AMap
import com.amap.api.maps2d.CameraUpdateFactory
import com.amap.api.maps2d.CoordinateConverter
import com.amap.api.maps2d.model.*
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.Colors
import com.mobile2box.MJGraphView.MJGraphData
import com.mobile2box.MJGraphView.MJGraphView
import io.realm.Realm
import io.realm.RealmChangeListener
import io.realm.RealmResults
import io.realm.Sort
import kotlinx.android.synthetic.main.activicy_goldenmap.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


/**
 * AMapV1地图中介绍如何显示世界图
 */
class GoldenMapActivity : AppCompatActivity(), OnClickListener, MJGraphView.MJGraphUpdateCallback {

    private var aMap: AMap? = null

    private var currentMarker: Marker? = null

    private var datePickerHandler = Handler()

    private lateinit var mDate: String

    private lateinit var realm: Realm
    private lateinit var result: RealmResults<AsmDataModel>

    private lateinit var listener: RealmChangeListener<RealmResults<AsmDataModel>>

    private lateinit var filter: List<AsmDataModel>

    private lateinit var mCal: Calendar
    var aResult = java.util.ArrayList<MJGraphData>()

    private lateinit var myLocationStyle: MyLocationStyle
    
    private lateinit var myPref: PrefObjects

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activicy_goldenmap)
        goldenMap.onCreate(savedInstanceState)

        myPref = PrefObjects(this)
        
        mCal = Calendar.getInstance()

        initActionBar()
        initGoldenMap()

        initLineChart()

        mDate = DateFormat.format("yyyy-MM-dd", mCal.time).toString()
        datePicker.text = setBtnText("DATE $mDate")

        datePicker.setOnClickListener {
            if (Utils.isFastDoubleClick) {
                Utils.toastMakeTextAndShow(this@GoldenMapActivity, "連點，母湯喔！！",
                        Toast.LENGTH_SHORT)
            } else {
                datePickerHandler.post {
                    val dpd = DatePickerDialog(this@GoldenMapActivity, DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
                        mCal.set(year, month, dayOfMonth)
                        Log.e("AirMap Button", mCal.get(Calendar.DAY_OF_MONTH).toString())
                        mDate = DateFormat.format("yyyy-MM-dd", mCal.time).toString()
                        datePicker.text = setBtnText("DATE $mDate")

                        pgLoading.visibility = View.VISIBLE
                        pgLoading.bringToFront()

                        runRealmQueryData()
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
            drawLineChart(result)
            drawMapPolyLine(result)
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

        runRealmQueryData()
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
        Log.e("Scroll", "Index:$_index Time: ${dateFormat.format(filter[_index].created_time)}, " +
                "Timestamp: ${filter[_index].created_time}, Value: $data, " +
                "Lat: ${filter[_index].latitude}, Lng: ${filter[_index].longitude}")
    }

    // 查詢資料庫
    private fun runRealmQueryData() {
        realm = Realm.getDefaultInstance()

        //現在時間實體毫秒
        val touchTime = if (mCal.get(Calendar.HOUR_OF_DAY) >= 8) mCal.timeInMillis else mCal.timeInMillis + mCal.timeZone.rawOffset
        //將日期設為今天日子加一天減1秒
        val startTime = touchTime / (3600000 * 24) * (3600000 * 24) - mCal.timeZone.rawOffset
        val endTime = startTime + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)

        val mDeviceAddress = myPref.getSharePreferenceMAC()

        listener = RealmChangeListener {
            filter = it.filter { it.latitude < 255f && it.latitude != null && it.macAddress == mDeviceAddress }
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
    private fun drawLineChart(datas: List<AsmDataModel>) {

        aResult.clear()

        if (datas.isNotEmpty()) {
            for (i in 0 until datas.size) {
                // 判斷 RadioButton 選中的項目
                val data = if (rbTVOC.isChecked) {
                    datas[i].tvocValue.toInt()
                } else {
                    datas[i].pM25Value.toInt()
                }

                val o: MJGraphData? = MJGraphData(datas[i].created_time, data)
                if (o != null && i < result.size - 1) {
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
                Log.e("LoadChartData", "Time: ${dateFormat.format(datas[i].created_time)}, Value: $data")
            }
        } else {
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

        lineChart.invalidate()
    }

    // 畫軌跡
    private fun drawMapPolyLine(datas: List<AsmDataModel>) {
        val rectOptions1 = PolylineOptions()
        rectOptions1.color(Colors.tvocCO2Colors[0])

        val rectOptions2 = PolylineOptions()
        rectOptions2.color(Colors.tvocCO2Colors[1])

        val rectOptions3 = PolylineOptions()
        rectOptions3.color(Colors.tvocCO2Colors[2])

        val rectOptions4 = PolylineOptions()
        rectOptions4.color(Colors.tvocCO2Colors[3])

        val rectOptions5 = PolylineOptions()
        rectOptions5.color(Colors.tvocCO2Colors[4])

        val rectOptions6 = PolylineOptions()
        rectOptions6.color(Colors.tvocCO2Colors[5])

        //val dataFilter = datas.filter { it.latitude < 255f && it.latitude != null }

        datas.forEachIndexed { index, asmDataModel ->
            if (index < datas.size - 1) {
                if (rbTVOC.isChecked) {
                    when (asmDataModel.tvocValue.toInt()) {
                        in 0..219 -> {
                            rectOptions1.add(goldenLocationConvert(LatLng(datas[index].latitude.toDouble(), datas[index].longitude.toDouble())))
                            rectOptions1.add(goldenLocationConvert(LatLng(datas[index + 1].latitude.toDouble(), datas[index + 1].longitude.toDouble())))
                        }
                        in 220..659 -> {
                            rectOptions2.add(goldenLocationConvert(LatLng(datas[index].latitude.toDouble(), datas[index].longitude.toDouble())))
                            rectOptions2.add(goldenLocationConvert(LatLng(datas[index + 1].latitude.toDouble(), datas[index + 1].longitude.toDouble())))
                        }
                        in 660..2199 -> {
                            rectOptions3.add(goldenLocationConvert(LatLng(datas[index].latitude.toDouble(), datas[index].longitude.toDouble())))
                            rectOptions3.add(goldenLocationConvert(LatLng(datas[index + 1].latitude.toDouble(), datas[index + 1].longitude.toDouble())))
                        }
                        in 2200..5499 -> {
                            rectOptions4.add(goldenLocationConvert(LatLng(datas[index].latitude.toDouble(), datas[index].longitude.toDouble())))
                            rectOptions4.add(goldenLocationConvert(LatLng(datas[index + 1].latitude.toDouble(), datas[index + 1].longitude.toDouble())))
                        }
                        in 5500..19999 -> {
                            rectOptions5.add(goldenLocationConvert(LatLng(datas[index].latitude.toDouble(), datas[index].longitude.toDouble())))
                            rectOptions5.add(goldenLocationConvert(LatLng(datas[index + 1].latitude.toDouble(), datas[index + 1].longitude.toDouble())))
                        }
                        else -> {
                            rectOptions1.add(goldenLocationConvert(LatLng(datas[index].latitude.toDouble(), datas[index].longitude.toDouble())))
                            rectOptions1.add(goldenLocationConvert(LatLng(datas[index + 1].latitude.toDouble(), datas[index + 1].longitude.toDouble())))
                        }
                    }
                } else {
                    when (asmDataModel.pM25Value.toInt()) {
                        in 0..15 -> {
                            rectOptions1.add(goldenLocationConvert(LatLng(datas[index].latitude.toDouble(), datas[index].longitude.toDouble())))
                            rectOptions1.add(goldenLocationConvert(LatLng(datas[index + 1].latitude.toDouble(), datas[index + 1].longitude.toDouble())))
                        }
                        in 16..34 -> {
                            rectOptions2.add(goldenLocationConvert(LatLng(datas[index].latitude.toDouble(), datas[index].longitude.toDouble())))
                            rectOptions2.add(goldenLocationConvert(LatLng(datas[index + 1].latitude.toDouble(), datas[index + 1].longitude.toDouble())))
                        }
                        in 35..54 -> {
                            rectOptions3.add(goldenLocationConvert(LatLng(datas[index].latitude.toDouble(), datas[index].longitude.toDouble())))
                            rectOptions3.add(goldenLocationConvert(LatLng(datas[index + 1].latitude.toDouble(), datas[index + 1].longitude.toDouble())))
                        }
                        in 55..150 -> {
                            rectOptions4.add(goldenLocationConvert(LatLng(datas[index].latitude.toDouble(), datas[index].longitude.toDouble())))
                            rectOptions4.add(goldenLocationConvert(LatLng(datas[index + 1].latitude.toDouble(), datas[index + 1].longitude.toDouble())))
                        }
                        in 151..250 -> {
                            rectOptions5.add(goldenLocationConvert(LatLng(datas[index].latitude.toDouble(), datas[index].longitude.toDouble())))
                            rectOptions5.add(goldenLocationConvert(LatLng(datas[index + 1].latitude.toDouble(), datas[index + 1].longitude.toDouble())))
                        }
                        else -> {
                            rectOptions6.add(goldenLocationConvert(LatLng(datas[index].latitude.toDouble(), datas[index].longitude.toDouble())))
                            rectOptions6.add(goldenLocationConvert(LatLng(datas[index + 1].latitude.toDouble(), datas[index + 1].longitude.toDouble())))
                        }
                    }
                }
            }
        }

        // 先清完再畫
        aMap!!.clear()

        aMap!!.addPolyline(rectOptions1)
        aMap!!.addPolyline(rectOptions2)
        aMap!!.addPolyline(rectOptions3)
        aMap!!.addPolyline(rectOptions4)
        aMap!!.addPolyline(rectOptions5)
        aMap!!.addPolyline(rectOptions6)
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
        textTEMPvalue.text = "$tempVal °C"
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
            currentMarker = aMap!!.addMarker(MarkerOptions().position(latLng))
        }

        // 移動畫面到目前的標記
        val zoomValue = aMap!!.cameraPosition.zoom
        Log.e("GoldenMap", "Zoom Level: $zoomValue")
        //if(aMap!!.cameraPosition.zoom < 5.0f) {     // 如果目前地圖縮放值為預設值2X，則放大到15X
        //    aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15F))
        //} else {
            aMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomValue))
        //}
    }

    /**
     * 初始化AMap对象
     */
    private fun initGoldenMap() {
        if (aMap == null) {
            aMap = goldenMap.map
            val uiSettings = aMap?.uiSettings
            uiSettings?.isScaleControlsEnabled = true
            uiSettings?.isMyLocationButtonEnabled = true

            myLocationStyle = MyLocationStyle()
            myLocationStyle.interval(2000)
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_FOLLOW)
            myLocationStyle.strokeColor(
                    ContextCompat.getColor(MyApplication.applicationContext(),
                            R.color.myLocationRange))//设置定位蓝点精度圆圈的边框颜色的方法。
            myLocationStyle.radiusFillColor(
                    ContextCompat.getColor(MyApplication.applicationContext(),
                            R.color.myLocationRange))//设置定位蓝点精度圆圈的填充颜色的方法。
            aMap!!.setMyLocationStyle(myLocationStyle)
            aMap!!.isMyLocationEnabled = true

            aMap!!.moveCamera(CameraUpdateFactory.zoomTo(15f))
        }
    }

    private fun goldenLocationConvert(srcLatLng: LatLng): LatLng {
        val converter = CoordinateConverter()
        converter.from(CoordinateConverter.CoordType.GPS)
        converter.coord(srcLatLng)
        return converter.convert()
    }

    /**
     * 方法必须重写
     */
    override fun onResume() {
        super.onResume()
        goldenMap.onResume()
    }

    /**
     * 方法必须重写
     */
    override fun onPause() {
        super.onPause()
        goldenMap.onPause()
    }

    /**
     * 方法必须重写
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        goldenMap.onSaveInstanceState(outState)
    }

    /**
     * 方法必须重写
     */
    override fun onDestroy() {
        super.onDestroy()
        goldenMap.onDestroy()
    }

    override fun onClick(v: View?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}
