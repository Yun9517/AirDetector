package com.microjet.airqi2

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem


import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.activity_fetch_data_main.*
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
private var arrTime3 = ArrayList<String>()
private var arrTvoc3 = ArrayList<String>()
private var AllTime = ArrayList<String>()
private var AllTvoc = ArrayList<String>()
private var AllECo2 = ArrayList<String>()
private var AllHumidy = ArrayList<String>()
private var AllTemp = ArrayList<String>()
private var AllPM25 = ArrayList<String>()
private val calObject = Calendar.getInstance()

private var adapter: Fetch_Adapter? = null
private val TAG = FetchDataMain::class.java.simpleName

@TargetApi(Build.VERSION_CODES.O)
class FetchDataMain : AppCompatActivity() {
   @RequiresApi(Build.VERSION_CODES.O)
   private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_tvoc -> {
                    message.setText(R.string.title_tvoc)
                    //getRealmDay()
//                    Log.d("getRealmDay--Tvoc Value", "ALL Tvoc" + AllTvoc)
                    adapter = Fetch_Adapter(AllTime,AllTvoc,this)
                    lv_data_info.adapter = adapter
                    Log.d(TAG + "Time size", "ALL Time " + AllTime.size)
                    Log.d(TAG + "Tvoc size", "ALL Tvoc " + AllTvoc.size)

                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_pm25 -> {
                    message.setText(R.string.title_pm25)
                    //getRealmDay()
//                    Log.d("getRealmDay--AllPM25 Value", "ALL AllPM25" + AllPM25)
                    adapter = Fetch_Adapter(AllTime,AllPM25,this)
                    lv_data_info.adapter = adapter
                    Log.d(TAG + "Time size", "ALL Time " + AllTime.size)
                    Log.d(TAG + "AllPM25 size", "ALL AllPM25 " + AllPM25.size)

                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_co2 -> {
                    message.setText(R.string.title_eco2)
                    //getRealmDay()
                    adapter = Fetch_Adapter(AllTime, AllECo2,this)
                    lv_data_info.adapter = adapter

//                    Log.d("getRealmDay-ECo2 Value", "ALL ECo2" + AllECo2)
                    Log.d(TAG + "Time size", "ALL Time " + AllTime.size)
                    Log.d(TAG + "AllECo2 size", "ALL ECo2 " + AllECo2.size)

                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_humidy -> {
                    message.setText(R.string.title_humidy)
                    //getRealmDay()
                    adapter = Fetch_Adapter(AllTime, AllHumidy,this)
                    lv_data_info.adapter = adapter
                    Log.d(TAG + "Time size", "ALL Time " + AllTime.size)
                    Log.d(TAG + "AllHumidy size", "ALL Humidy " + AllHumidy.size)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_temperatur -> {
                    message.setText(R.string.title_temperatur)
                    //getRealmDay()
                    adapter = Fetch_Adapter(AllTime, AllTemp,this)
                    lv_data_info.adapter = adapter
                    Log.d(TAG + "Time size", "ALL Time " + AllTime.size)
                    Log.d(TAG + "AllTemp size", "ALL Temp " + AllTemp.size)
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_fetch_data_main)
            //試Realm拉資料
            getRealmDay()
//            itemList.getData()
            adapter = Fetch_Adapter(AllTime,AllTvoc,this)

            lv_data_info.adapter = adapter

//                    Log.d("getRealmDay--Tvoc Value", "ALL Tvoc" + AllTvoc)
            Log.d(TAG + "Time size", "ALL Time " + AllTime.size)
            Log.d(TAG + "Tvoc size", "ALL Tvoc " + AllTvoc.size)

            initActionBar()
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
        }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("LongLogTag")
    private fun getRealmDay() {
        arrTime3.clear()
        arrTvoc3.clear()
        AllTime.clear()
        AllTvoc.clear()
        AllECo2.clear()
        AllTemp.clear()
        AllHumidy.clear()
        AllPM25.clear()
        //現在時間實體毫秒
        //var touchTime = Calendar.getInstance().timeInMillis
        val touchTime = if (calObject.get(Calendar.HOUR_OF_DAY) >= 8) calObject.timeInMillis else calObject.timeInMillis + calObject.timeZone.rawOffset
        //val touchTime = calObject.timeInMillis + calObject.timeZone.rawOffset
        Log.d(TAG + "TVOCbtncallRealm",calObject.get(Calendar.DAY_OF_MONTH).toString())
        //將日期設為今天日子加一天減1秒
        val endDay = touchTime / (3600000 * 24) * (3600000 * 24) - calObject.timeZone.rawOffset
        val endDayLast = endDay + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        //設定時間區間

        val endTime = endDayLast
        val startTime = endDay
        Log.d("getRealmDay--endTime", endTime.toString())
        Log.d("getRealmDay--startTime", startTime.toString())
        //一天共有2880筆
        val dataCount = (endTime - startTime) / (60 * 1000)
//        Log.d("TimePeriod", (dataCount.toString() + " thirtySecondsCount"))
        query.between("Created_time", startTime, endTime).sort("Created_time", Sort.ASCENDING)
        val result1 = query.findAll()

//        Log.d(TAG + "getRealmDay--alll", result1.size.toString())
//        Log.d(TAG + "getRealmDay--alll-value", result1.toString())

        var sumTvoc = 0
        //先生出2880筆值為0的陣列
        for (y in 0..dataCount) {
            arrTvoc3.add("0")
            arrTime3.add((startTime + y * 60 * 1000).toString())
        }
        var aveTvoc = 0
        //關鍵!!利用取出的資料減掉抬頭時間除以30秒算出index換掉TVOC的值
        if (result1.size != 0) {
            result1.forEachIndexed { index, asmDataModel ->
                val count = ((asmDataModel.created_time - startTime) / (60 * 1000)).toInt()
                arrTvoc3[count] = asmDataModel.tvocValue.toString()
                //20180122
                sumTvoc += arrTvoc3[count].toInt()
                @SuppressLint("SimpleDateFormat")
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val date = dateFormat.format(asmDataModel.created_time)//-28800000

//                AllTime.add(asmDataModel.created_time.toString())
                AllTime.add(date)
                AllTvoc.add(asmDataModel.tvocValue)
                AllECo2.add(asmDataModel.ecO2Value)
                AllHumidy.add(asmDataModel.humiValue)
                AllTemp.add(asmDataModel.tempValue)
                AllPM25.add(asmDataModel.pM25Value)
//                Log.v("sumTvoc:--", sumTvoc.toString())
//                Log.v("hilightCount:--", count.toString())
                Log.v(TAG +"all:--", "ID "+asmDataModel.dataId+" "+result1.get(index).toString()+"date "+date)

            }
            Log.d( TAG + "getRealmDay", result1.last().toString())
            //20180122
            aveTvoc = (sumTvoc / result1.size)
        }
//        Log.d(TAG + "getRealmDay--Tvoc Value", "ALL Tvoc" + AllTvoc.get(1))

        //arrTvoc3.add(aveTvoc.toString())

        //前一天的０點起
        val sqlWeekBase: Long = startTime - TimeUnit.DAYS.toMillis((1).toLong())
        // Show Date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")


//        show_Today!!.text = dateFormat.format(startTime)
//        show_Yesterday!!.text =  dateFormat.format(startTime - TimeUnit.DAYS.toMillis((1).toLong()))

        //
        //Log.d("getRealmWeek", sqlWeekBase.toString())
        //跑七筆BarChart
        // for (y in 0..1) {
        //第一筆為日 00:00
        val sqlStartDate = sqlWeekBase//+TimeUnit.DAYS.toMillis()
        //結束點為日 23:59
        val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        //val realm= Realm.getDefaultInstance()
        val query1 = realm.where(AsmDataModel::class.java)
        //20180122
        var AVGTvoc3: Float= 0F
        Log.d("getRealmWeek", sqlStartDate.toString())
        Log.d("getRealmWeek", sqlEndDate.toString())
        query1.between("Created_time", sqlStartDate, sqlEndDate)
        val result2 = query1.findAll()
        Log.d("getRealmWeek", result2.size.toString())
        if (result2.size != 0) {
            var sumTvocYesterday = 0F
            for (i in result2) {
                sumTvocYesterday += i.tvocValue.toInt()
            }
            AVGTvoc3 = (sumTvocYesterday / result2.size)
        } else {
            AVGTvoc3 = 0F
        }

        //}
//        result_Today!!.text = aveTvoc.toInt().toString() + " ppb"        //arrTvoc3[1].toString()+" ppb"
//        result_Yesterday!!.text = AVGTvoc3.toInt().toString()+ " ppb"

    }

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
    }

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
