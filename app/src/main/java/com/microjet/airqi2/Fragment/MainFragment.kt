package com.microjet.airqi2.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.Definition.Colors
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.frg_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainFragment : Fragment() {

    enum class DetectionData(val range1: Long,val range2: Long) {
        TVOC(220,660),
        CO2(700,1000),
        Temp(18,25),
        Humi(45,65)
    }

    private var mContext: Context? = null

    //private var pressed="TVOC"//0=temperature 1=humidity 2=TVOC 3=CO2

    private var tvocDataFloat = 0f
    private var tempDataFloat = 0f
    private var humiDataFloat = 0f
    private var co2DataFloat = 0f
    private var preHeat = "0"


    private var dataForState = DetectionData.TVOC
    private var mConnState = false


    @Suppress("OverridingDeprecatedMember", "DEPRECATION")
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        mContext = this.context.applicationContext
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(myBroadcastReceiver,
                makeMainFragmentUpdateIntentFilter())
    }

    override fun onCreateView(inflater: LayoutInflater?,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View?{
        return inflater!!.inflate(R.layout.frg_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?){
        super.onActivityCreated(savedInstanceState)


        show_TVOC?.setOnClickListener {
            dataForState = DetectionData.TVOC
            checkUIState()

        }

        show_eCO2?.setOnClickListener {
            dataForState = DetectionData.CO2
            checkUIState()
        }

        show_Temp?.setOnClickListener{
            dataForState = DetectionData.Temp
            checkUIState()
        }

        show_RH?.setOnClickListener {
            dataForState = DetectionData.Humi
            checkUIState()
        }

        imgLight.setOnTouchListener { _, motionEvent ->
            //Log.wtf("幹我怎麼了!!",motionEvent.action.toString()+ actionToSring(motionEvent.action))
            when {
                motionEvent.action == MotionEvent.ACTION_DOWN -> {//.ACTION_BUTTON_PRESS
                    Log.i("幹我按下了!!",motionEvent.action.toString()+ actionToSring(motionEvent.action))
                    //Toast.makeText(mContext,actionToSring(motionEvent.action).toString(),Toast.LENGTH_SHORT).show()
                    //20180131
                    //************************************************************************************************************************************
                    val intent: Intent? = Intent(BroadcastIntents.PRIMARY)
                    intent!!.putExtra("status", BroadcastActions.INTENT_KEY_PUMP_ON)
                    mContext!!.sendBroadcast(intent) ///sendBroadcast(intent)
                    //************************************************************************************************************************************
                }
                motionEvent.action == MotionEvent.ACTION_UP -> {//ACTION_BUTTON_RELEASE
                    Log.i("幹我不按了!!",motionEvent.action.toString()+ actionToSring(motionEvent.action))
                    //Toast.makeText(mContext,actionToSring(motionEvent.action).toString(),Toast.LENGTH_SHORT).show()
                    //************************************************************************************************************************************
                    val intent: Intent? = Intent(BroadcastIntents.PRIMARY)
                    intent!!.putExtra("status", BroadcastActions.INTENT_KEY_PUMP_OFF)
                    mContext!!.sendBroadcast(intent)
                    //************************************************************************************************************************************
                }
                motionEvent.action == MotionEvent.ACTION_MOVE -> //ACTION_BUTTON_RELEASE
                    Log.i("幹我按中了!!",motionEvent.action.toString()+ actionToSring(motionEvent.action))
                else -> {
                    Log.i("幹我取消了了!!",motionEvent.action.toString()+ actionToSring(motionEvent.action))
                    //************************************************************************************************************************************
                    val intent: Intent? = Intent(BroadcastIntents.PRIMARY)
                    intent!!.putExtra("status", BroadcastActions.INTENT_KEY_PUMP_OFF)
                    mContext!!.sendBroadcast(intent)
                    //************************************************************************************************************************************
                }
            }
            true
        }


        // 初始化inCircleTitle文字大小
        fixInCircleTextSize()
    }

    private fun actionToSring(action: Int): String {
        when (action) {
            MotionEvent.ACTION_DOWN -> return "Down"
            MotionEvent.ACTION_MOVE -> return "Move"
            MotionEvent.ACTION_POINTER_DOWN -> return "Pointer down"
            MotionEvent.ACTION_UP -> return "UP"
            MotionEvent.ACTION_POINTER_UP -> return "Pointer up"
            MotionEvent.ACTION_OUTSIDE -> return "Outside"
            MotionEvent.ACTION_CANCEL -> return "Cancel"
        }
        return ""
    }


    override fun onResume() {
        super.onResume()
        checkUIState()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(mContext!!).unregisterReceiver(myBroadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun fixInCircleTextSize() {
        val dm = resources.displayMetrics
        val dpi = dm.densityDpi
        Log.i("DPI", "目前解析度為: $dpi")
        when(dpi) {
            240 -> {   // HDPI
                //inCircleTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                inCircleState.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            }
            480 -> {   // XXHDPI
                //inCircleTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                inCircleState.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                tvNotify.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                tvLastDetectTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            }
            560 -> {   // Samsung S8+
                //inCircleTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                inCircleState.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                tvNotify.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                tvLastDetectTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            }
            640 -> {   // XXXHDPI
                //inCircleTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                inCircleState.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                tvNotify.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                tvLastDetectTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            }
        }
    }

    private fun setThresholdValue(dataForState: DetectionData){
        //tvRange1.text = dataForState.range1.toString()
        //tvRange2.text = dataForState.range2.toString()
        tvRange1.visibility = View.INVISIBLE
        tvRange2.visibility = View.INVISIBLE
    }

    private fun setBarMaxValue(state: DetectionData){
        inCircleBar.setThreadholdValue(floatArrayOf(state.range1.toFloat(), state.range2.toFloat()))
        when (state){

            DetectionData.TVOC ->{
                inCircleBar.setMaxValues(1000f)
            }
            DetectionData.CO2 ->{
                inCircleBar.setMaxValues(5000f)
            }
            DetectionData.Temp ->{
                inCircleBar.setMaxValues(50f)
            }
            DetectionData.Humi ->{
                inCircleBar.setMaxValues(100f)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setBtmCurrentValue() {
        //DetectorValue=currentValue
        tvBtmTVOCValue.text = tvocDataFloat.toInt().toString() + " ppb"
        tvBtmPM25Value.text = "Not Support"
        tvBtmCO2Value.text = co2DataFloat.toInt().toString() + " ppm" //co2DataFloat.toInt().toString()+ " ppm"
        tvBtmTEMPValue.text = tempDataFloat.toString() + " ℃"/*currentValue[0] + " ℃"*/
        tvBtmHUMIValue.text = humiDataFloat.toInt().toString() + " %"/*currentValue[1] + " %"*/
    }

    @SuppressLint("SetTextI18n")
    private fun tvocStatusTextShow(currentValue: Float) {
        when(currentValue) {
            in 0..219 -> {
                tvNotify?.text = getString(R.string.text_message_air_good)
                inCircleState.text = getString(R.string.text_label_status_good)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_01green_press)
            }
            in 220..659 -> {
                tvNotify?.text = getString(R.string.text_message_air_mid)
                inCircleState.text = getString(R.string.text_label_status_mid)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_02yellow_press)
            }
            in 660..2199 -> {
                tvNotify?.text = getString(R.string.text_message_air_Medium_Orange)
                inCircleState.text = getString(R.string.text_label_status_medium_Orange)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_03orange_press)
            }
            in 2200..5499 -> {
                tvNotify?.text = getString(R.string.text_message_air_bad)
                inCircleState.text = getString(R.string.text_label_status_bad)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_04red_press)
            }
            in 5500..19999 -> {
                tvNotify?.text = getString(R.string.text_message_air_Serious_Purple)
                inCircleState.text = getString(R.string.text_label_status_Serious_Purple)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_05purple_press)
            }
            else -> {
                tvNotify?.text = getString(R.string.text_message_air_Extreme_Dark_Purple)
                inCircleState.text = getString(R.string.text_label_status_Extreme_Dark_Purple)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_06brown_press)
            }
        }
    }

    private fun eco2StatusTextShow(currentValue:Float){
        when(currentValue) {
            in 0..699 -> {
                tvNotify?.text = getString(R.string.message_eCO2_Green)
                inCircleState.text = getString(R.string.label_eCO2_Green)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_01green_press)
            }
            in 700..999 -> {
                tvNotify?.text = getString(R.string.message_eCO2_Yellow)
                inCircleState.text = getString(R.string.label_eCO2_Yellow)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_02yellow_press)
            }
            in 1000..1499 -> {
                tvNotify?.text = getString(R.string.message_eCO2_Orange)
                inCircleState.text = getString(R.string.label_eCO2_Orange)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_03orange_press)
            }
            in 1500..2499 -> {
                tvNotify?.text = getString(R.string.message_eCO2_Red)
                inCircleState.text = getString(R.string.label_eCO2_Red)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_04red_press)
            }
            in 2500..4999 -> {
                tvNotify?.text = getString(R.string.message_eCO2_Purple)
                inCircleState.text = getString(R.string.label_eCO2_Purple)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_05purple_press)
            }
            else -> {
                tvNotify?.text = getString(R.string.message_eCO2_Brown)
                inCircleState.text = getString(R.string.label_eCO2_Brown)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_06brown_press)
            }
        }
    }

    private fun tempStatusTextShow(currentValue:Float){
        when(currentValue) {
            in 18..25 -> {
                tvNotify?.text = getString(R.string.text_message_temperature)
                inCircleState.text = " "
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                //20180131
                imgLight.setImageResource(R.drawable.app_android_icon_light)
            }
            in 26..200 -> {
                tvNotify?.text = getString(R.string.text_message_temperature)
                inCircleState.text = " "
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                //20180131
                imgLight.setImageResource(R.drawable.app_android_icon_light)
            }
            else -> {
                tvNotify?.text = getString(R.string.text_message_temperature)
                inCircleState.text = " "
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMiddleBlue))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMiddleBlue))
                //20180131
                imgLight.setImageResource(R.drawable.app_android_icon_light)
            }
        }
    }

    private fun humiStatusTextShow(currentValue:Float){
        when(currentValue) {
            in 0..44 -> {
                tvNotify?.text = getString(R.string.text_message_humidity)
                inCircleState.text = " "
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMiddleBlue))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMiddleBlue))
                //20180131
                imgLight.setImageResource(R.drawable.app_android_icon_light)
            }
            in 45..65 -> {
                tvNotify?.text = getString(R.string.text_message_humidity)
                inCircleState.text = " "
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                //20180131
                imgLight.setImageResource(R.drawable.app_android_icon_light)
            }
            else -> {

                tvNotify?.text = getString(R.string.text_message_humidity)
                inCircleState.text = " "
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                //20180131
                imgLight.setImageResource(R.drawable.app_android_icon_light)
            }
        }
    }

    private fun makeMainFragmentUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastActions.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GET_NEW_DATA)
        return intentFilter
    }

    @SuppressLint("SimpleDateFormat")
    @Synchronized private fun checkUIState() {
        if (mConnState && preHeat == "255") {
            //setThresholdValue(dataForState)
            //setBarMaxValue(dataForState)
            when (dataForState) {
                DetectionData.TVOC -> {
                    inCircleTitle.text = getString(R.string.text_label_tvoc_detect)
                    setThresholdValue(dataForState)
                    setBarMaxValue(dataForState)
                    //inCircleBar.setColor(Colors.tvocOldColors, Colors.tvocOldAngles)
                    //inCircleBar.setCurrentValues(tvocDataFloat)
                    inCircleBar.setColor(Colors.tvocCO2Colors, Colors.tvocCO2Angles)
                    //數值不等比顯示
                    when(tvocDataFloat) {
                        in 0..660 -> inCircleBar.setCurrentValues(tvocDataFloat)
                        in 661..2200 -> inCircleBar.setCurrentValues((tvocDataFloat / 60) + 700)
                        in 2201..5500 -> inCircleBar.setCurrentValues((tvocDataFloat / 60) + 770)
                        in 5501..20000 -> inCircleBar.setCurrentValues((tvocDataFloat / 180) + 830)
                        else -> inCircleBar.setCurrentValues((tvocDataFloat / 360) + 890)
                    }
                    //inCircleBar.setCurrentValues(1000f)
                    tvocStatusTextShow(tvocDataFloat)
                    val temp = tvocDataFloat.toInt().toString() + " ppb"
                    textSpannable(temp)
                }
                DetectionData.CO2 -> {
                    inCircleTitle.text = getString(R.string.text_label_co2)
                    setThresholdValue(dataForState)
                    setBarMaxValue(dataForState)
                    inCircleBar.setColor(Colors.tvocCO2Colors, Colors.tvocCO2Angles)
                    //數值不等比顯示
                    when(co2DataFloat) {
                        in 0..700 -> inCircleBar.setCurrentValues(co2DataFloat)
                        in 701..1000 -> inCircleBar.setCurrentValues((co2DataFloat / 60) + 700)
                        in 1001..1500 -> inCircleBar.setCurrentValues((co2DataFloat / 60) + 650)
                        in 1501..2500 -> inCircleBar.setCurrentValues((co2DataFloat / 180) + 590)
                        else -> inCircleBar.setCurrentValues((co2DataFloat / 360) + 890)
                    }
                    inCircleBar.setCurrentValues(co2DataFloat)
                    //inCircleBar.setCurrentValues(60000f)
                    eco2StatusTextShow(co2DataFloat)
                    val temp = co2DataFloat.toInt().toString() + " ppm"
                    textSpannable(temp)
                }
                DetectionData.Temp -> {
                    inCircleTitle.text = getString(R.string.text_label_temperature)
                    setThresholdValue(dataForState)
                    setBarMaxValue(dataForState)
                    inCircleBar.setColor(Colors.tempColors, Colors.tempAngles)
                    //Modify Progress Bar
                    when(tempDataFloat) {
                        in -10..18 -> inCircleBar.setCurrentValues(tempDataFloat)
                        else -> inCircleBar.setCurrentValues((tempDataFloat / 60) + 700)
                    }
                    inCircleBar.setCurrentValues(tempDataFloat)
                    //inCircleBar.setCurrentValues(18f)
                    tempStatusTextShow(tempDataFloat)
                    val temp = tempDataFloat.toString() + " ℃"
                    textSpannable(temp)
                }

                DetectionData.Humi -> {
                    inCircleTitle.text = getString(R.string.text_label_humidity)
                    setThresholdValue(dataForState)
                    setBarMaxValue(dataForState)
                    inCircleBar.setColor(Colors.humiColors, Colors.humiAngles)
                    //Modify Progress Bar
                    when(humiDataFloat) {
                        in 0..45 -> inCircleBar.setCurrentValues(humiDataFloat)
                        else -> inCircleBar.setCurrentValues((humiDataFloat / 60) + 700)
                    }
                    inCircleBar.setCurrentValues(humiDataFloat)
                    //inCircleBar.setCurrentValues(40f)
                    humiStatusTextShow(humiDataFloat)
                    val temp = humiDataFloat.toInt().toString() + " %"
                    textSpannable(temp)
                }
            }

            setBtmCurrentValue()
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val date = Date()
            tvLastDetectTime.text = dateFormat.format(date).toString()
            //20171228 ANDY增加
        } else {
            inCircleValue.text = " "
            inCircleState.text = " "
            tvBtmTVOCValue.text= "---"
            tvBtmPM25Value.text= "---"
            tvBtmCO2Value?.text= "---"
            tvBtmTEMPValue.text= "---"/*currentValue[0] + " ℃"*/
            tvBtmHUMIValue.text= "---"/*currentValue[1] + " %"*/
            tvNotify?.text = " "
            tvLastDetectTime.text = " "
            inCircleBar.setCurrentValues(0f)
        }
    }

    private fun textSpannable(temp : String) {
        val dm = resources.displayMetrics
        val textSpan = SpannableStringBuilder(temp)
        val text1Size  = when(dm.densityDpi) {
            160 -> convertSpToPx(36f)   // MDPI
            240 -> convertSpToPx(28f)   // HDPI
            480 -> convertSpToPx(30f)   // XXHDPI
            in 560 .. 640 -> convertSpToPx(36f)   // XXXHDPI
            else -> 50
        }
        val text2Size  = when(dm.densityDpi) {
            160 -> convertSpToPx(20f)   // MDPI
            240 -> convertSpToPx(14f)   // HDPI
            480 -> convertSpToPx(18f)   // XXHDPI
            in 560 .. 640 -> convertSpToPx(22f)   // XXXHDPI
            else -> 30
        }

        textSpan.setSpan(AbsoluteSizeSpan(text1Size),
                0,temp.indexOf(" ") + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        textSpan.setSpan(AbsoluteSizeSpan(text2Size),
                temp.indexOf(" ") + 1, temp.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        //textSpan.setSpan(AbsoluteSizeSpan(text1Size),temp.indexOf(" ") - 1, temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        inCircleValue.text = textSpan
    }

    private fun convertSpToPx(input: Float): Int {
        return (input * resources.displayMetrics.scaledDensity).toInt()
    }

    private val myBroadcastReceiver = object: BroadcastReceiver() {
        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                BroadcastActions.ACTION_GATT_DISCONNECTED -> {
                    mConnState = false
                    //setinCircleBarCurrentValue("0","0","0","0","0")
                }

                BroadcastActions.ACTION_GATT_CONNECTED -> {
                    mConnState = true
                }

                BroadcastActions.ACTION_GET_NEW_DATA -> {
                    val bundle = intent.extras
                    tempDataFloat = bundle.getString(BroadcastActions.INTENT_KEY_TEMP_VALUE).toFloat()
                    humiDataFloat = bundle.getString(BroadcastActions.INTENT_KEY_HUMI_VALUE).toFloat()
                    tvocDataFloat = bundle.getString(BroadcastActions.INTENT_KEY_TVOC_VALUE).toFloat()
                    co2DataFloat = bundle.getString(BroadcastActions.INTENT_KEY_CO2_VALUE).toFloat()
                    preHeat = bundle.getString(BroadcastActions.INTENT_KEY_PREHEAT_COUNT)
                }
            }
            checkUIState()
        }
    }
}

