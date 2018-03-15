package com.microjet.airqi2

import android.util.Log
import io.realm.Realm
import io.realm.Sort
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by B00175 on 2018/1/29.
 */
object TvocNoseData {
    var spinnerPosition: Int = 0
    var calObject = Calendar.getInstance()

    var arrTvocDay: ArrayList<String> = arrayListOf()
    var arrEco2Day: ArrayList<String> = arrayListOf()
    var arrTempDay: ArrayList<String> = arrayListOf()
    var arrHumiDay: ArrayList<String> = arrayListOf()
    var arrTimeDay: ArrayList<String> = arrayListOf()

    var arrTvocWeek: ArrayList<String> = arrayListOf()
    var arrEco2Week: ArrayList<String> = arrayListOf()
    var arrTempWeek: ArrayList<String> = arrayListOf()
    var arrHumiWeek: ArrayList<String> = arrayListOf()
    var arrTimeWeek: ArrayList<String> = arrayListOf()

    var arrTvocMonth: ArrayList<String> = arrayListOf()
    var arrEco2Month: ArrayList<String> = arrayListOf()
    var arrTempMonth: ArrayList<String> = arrayListOf()
    var arrHumiMonth: ArrayList<String> = arrayListOf()
    var arrTimeMonth: ArrayList<String> = arrayListOf()


    //val arrTvoc3: ArrayList<String> = arrayListOf()
    //val arrTime3: ArrayList<String> = arrayListOf()
    val arrChartLabels: ArrayList<String> = arrayListOf()
    val arrTvLabeles = ArrayList<String>()

    fun getRealmDay() {
        arrTvocDay.clear()
        arrEco2Day.clear()
        arrTempDay.clear()
        arrHumiDay.clear()
        arrTimeDay.clear()

        //現在時間實體毫秒
        //var touchTime = Calendar.getInstance().timeInMillis
        val touchTime = calObject.timeInMillis + calObject.timeZone.rawOffset
        Log.d("TVOCbtncallRealm",calObject.get(Calendar.DAY_OF_MONTH).toString())
        //將日期設為今天日子加一天減1秒
        val endDay = touchTime / (3600000 * 24) * (3600000 * 24)// - calObject.timeZone.rawOffset
        val endDayLast = endDay + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        //設定時間區間
        val endTime = endDayLast
        val startTime = endDay
        //一天共有2880筆
        val dataCount = (endTime - startTime) / (60 * 1000)
        Log.d("TimePeriod", (dataCount.toString() + "thirtySecondsCount"))
        query.between("Created_time", startTime, endTime).sort("Created_time", Sort.ASCENDING)
        val result1 = query.findAll()
        Log.d("getRealmDay", result1.size.toString())
        var sumTvoc = 0
        //先生出2880筆值為0的陣列
        for (y in 0..dataCount) {
            arrTvocDay.add("0")
            arrEco2Day.add("0")
            arrTempDay.add("0")
            arrHumiDay.add("0")
            arrTimeDay.add(((startTime + y * 60 * 1000) - calObject.timeZone.rawOffset).toString())
        }
        var aveTvoc=0
        //關鍵!!利用取出的資料減掉抬頭時間除以30秒算出index換掉TVOC的值
        if (result1.size != 0) {
            result1.forEachIndexed { index, asmDataModel ->
                val count = ((asmDataModel.created_time - startTime) / (60 * 1000)).toInt()
                arrTvocDay[count] = asmDataModel.tvocValue.toString()
                arrEco2Day[count] = asmDataModel.ecO2Value.toString()
                arrTempDay[count] = (asmDataModel.tempValue.toFloat() + 10f).toString()
                arrHumiDay[count] = asmDataModel.humiValue.toString()
                //20180122
                sumTvoc += arrTvocDay[count].toInt()
                //Log.v("hilightCount:", count.toString())
            }
            Log.d("getRealmDay", result1.last().toString())
            //20180122
            aveTvoc = (sumTvoc / result1.size)
        }

        //前一天的０點起
        val sqlWeekBase = startTime - TimeUnit.DAYS.toMillis((1).toLong())
        // Show Date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")


        //show_Today!!.text = dateFormat.format(startTime)
        //show_Yesterday!!.text =  dateFormat.format(startTime - TimeUnit.DAYS.toMillis((1).toLong()))

        //第一筆為日 00:00
        val sqlStartDate = sqlWeekBase//+TimeUnit.DAYS.toMillis()
        //結束點為日 23:59
        val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
        //val realm= Realm.getDefaultInstance()
        val query1 = realm.where(AsmDataModel::class.java)
        //20180122
        var AVGTvoc3 :Float= 0F
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
            AVGTvoc3=0F
        }

        //}
        //result_Today!!.text = aveTvoc.toInt().toString() + " ppb"        //arrTvoc3[1].toString()+" ppb"
        //result_Yesterday!!.text = AVGTvoc3.toInt().toString()+ " ppb"

    }

    fun getRealmWeek() {
        arrTvocWeek.clear()
        arrEco2Week.clear()
        arrTempWeek.clear()
        arrHumiWeek.clear()
        arrTimeWeek.clear()

        //拿到現在是星期幾的Int
        val dayOfWeek = calObject.get(Calendar.DAY_OF_WEEK)
        val touchTime = calObject.timeInMillis + calObject.timeZone.rawOffset
        //今天的00:00
        val nowDateMills = touchTime / (3600000 * 24) * (3600000 * 24)// - calObject.timeZone.rawOffset
        //將星期幾退回到星期日為第一時間點
        val sqlWeekBase = nowDateMills - TimeUnit.DAYS.toMillis((dayOfWeek - 1).toLong())
        var aveLastWeekTvoc = 0
        Log.d("getRealmWeek", sqlWeekBase.toString())
        //跑七筆BarChart
        for (y in 0..6) {
            //第一筆為日 00:00
            val sqlStartDate = sqlWeekBase + TimeUnit.DAYS.toMillis(y.toLong())
            //結束點為日 23:59
            val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            Log.e("thisGetRealmWeekStart", sqlStartDate.toString())
            Log.e("thisGetRealmWeekEnd", sqlEndDate.toString())
            query.between("Created_time", sqlStartDate, sqlEndDate)
            val result1 = query.findAll()
            Log.d("getRealmWeek", result1.size.toString())
            if (result1.size != 0) {
                var sumTvoc = 0
                var sumEco2 = 0
                var sumTemp = 0f
                var sumHumi = 0
                //var thisWeekAVGTvoc: Int = 0
                //var thisWeekAVGEco2: Int = 0
                //var thisWeekAVGTemp: Float = 0f
                //var thisWeekAVGHumi: Int = 0
                for (i in result1) {
                    sumTvoc += i.tvocValue.toInt()
                    sumEco2 += i.ecO2Value.toInt()
                    sumTemp += i.tempValue.toFloat()
                    sumHumi += i.humiValue.toInt()
                }
                val thisWeekAVGTvoc = (sumTvoc / result1.size)
                val thisWeekAVGEco2 = (sumEco2 / result1.size)
                val thisWeekAVGTemp = (sumTemp / result1.size)
                val thisWeekAVGHumi = (sumHumi / result1.size)
                arrTvocWeek.add(thisWeekAVGTvoc.toString())
                arrEco2Week.add(thisWeekAVGEco2.toString())
                arrTempWeek.add((thisWeekAVGTemp + 10.0f).toString())
                arrHumiWeek.add(thisWeekAVGHumi.toString())
                //依序加入時間
                arrTimeWeek.add((sqlStartDate - calObject.timeZone.rawOffset).toString())
            } else {
                arrTvocWeek.add("0")
                arrEco2Week.add("0")
                arrTempWeek.add("0")
                arrHumiWeek.add("0")
                arrTimeWeek.add((sqlStartDate - calObject.timeZone.rawOffset).toString())
            }
        }
    }

    fun getRealmMonth() {
        arrTvocMonth.clear()
        arrEco2Month.clear()
        arrTempMonth.clear()
        arrHumiMonth.clear()
        arrTimeMonth.clear()
        //拿到現在是星期幾的Int
        val dayOfMonth = calObject.get(Calendar.DAY_OF_MONTH)
        val monthCount = calObject.getActualMaximum(Calendar.DAY_OF_MONTH)
        val touchTime = calObject.timeInMillis + calObject.timeZone.rawOffset
        val nowDateMills = touchTime / (3600000 * 24) * (3600000 * 24)// - calObject.timeZone.rawOffset
        //將星期幾退回到星期日為第一時間點
        val sqlMonthBase = nowDateMills - TimeUnit.DAYS.toMillis((dayOfMonth - 1).toLong())
        Log.d("getRealmMonth", sqlMonthBase.toString())
        //跑七筆BarChart
        for (y in 0..(monthCount-1)) {
            //第一筆為日 00:00
            val sqlStartDate = sqlMonthBase + TimeUnit.DAYS.toMillis(y.toLong())
            //結束點為日 23:59
            val sqlEndDate = sqlStartDate + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1)
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java)
            val dataCount = (sqlEndDate - sqlStartDate) / (60 * 1000)
            Log.d("TimePeriod", (dataCount.toString() + "thirtySecondsCount"))
            Log.d("getRealmMonth", sqlStartDate.toString())
            Log.d("getRealmMonth", sqlEndDate.toString())
            query.between("Created_time", sqlStartDate, sqlEndDate)
            val result1 = query.findAll()
            Log.d("getRealmMonth", result1.size.toString())
            if (result1.size != 0) {
                var sumTvoc = 0
                var sumEco2 = 0
                var sumTemp = 0f
                var sumHumi = 0
                for (i in result1) {
                    sumTvoc += i.tvocValue.toInt()
                    sumEco2 += i.ecO2Value.toInt()
                    sumTemp += i.tempValue.toFloat()
                    sumHumi += i.humiValue.toInt()
                }
                val thisMonthAVGTvoc = (sumTvoc / result1.size)
                val thisMonthAVGEco2 = (sumEco2 / result1.size)
                val thisMonthAVGTemp = (sumTemp / result1.size)
                val thisMonthAVGHumi = (sumHumi / result1.size)
                arrTvocMonth.add(thisMonthAVGTvoc.toString())
                arrEco2Month.add(thisMonthAVGEco2.toString())
                arrTempMonth.add((thisMonthAVGTemp + 10.0f).toString())
                arrHumiMonth.add(thisMonthAVGHumi.toString())
                //依序加入時間
                arrTimeMonth.add((sqlStartDate - calObject.timeZone.rawOffset).toString())
                Log.d("getRealmMonth", result1.last().toString())
            } else {
                arrTvocMonth.add("0")
                arrEco2Month.add("0")
                arrTempMonth.add("0")
                arrHumiMonth.add("0")
                arrTimeMonth.add((sqlStartDate - calObject.timeZone.rawOffset).toString())
            }
        }

    }

    //var Index: Int = 0
    //var TEMPValue: String = "20"
    //var HUMIValue: String = "20"
    //var TVOCValue: String = "20"
    //var ECO2Value: String = "20"
    //var PM25Value: String = "20"
    //var Created_time: Long = 1517195269840

    fun getMaxID(): Int {
        val realm = Realm.getDefaultInstance()
        val num = realm.where(AsmDataModel::class.java).max("id")
        val nextID: Int
        if (num == null) {
            nextID = 1
        } else {
            nextID = num!!.toInt() + 1
        }
        return nextID
    }
}