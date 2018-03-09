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
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.Definition.Colors
import com.microjet.airqi2.MyApplication
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.frg_main.*
import java.text.SimpleDateFormat
import java.util.*


class MainFragment : Fragment(), View.OnTouchListener {

    enum class DetectionData(val range1: Long, val range2: Long) {
        TVOC(220, 660),
        CO2(700, 1000),
        Temp(18, 25),
        Humi(45, 65),
        PM25(220, 660)
    }

    private var mContext: Context? = null

    //private var pressed="TVOC"//0=temperature 1=humidity 2=TVOC 3=CO2

    private var tvocDataFloat = 0f
    private var tempDataFloat = 0f
    private var humiDataFloat = 0f
    private var co2DataFloat = 0f
    private var pm25DataFloat = 0f
    private var preHeat = "0"
    //20180207
    private var isPumpOn = false
    //20180207
    //private  var fun:Int =0


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
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.frg_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        show_TVOC.setOnTouchListener(this)
        show_eCO2.setOnTouchListener(this)
        show_Temp.setOnTouchListener(this)
        show_RH.setOnTouchListener(this)
        show_PM.setOnTouchListener(this)

        /*show_TVOC?.setOnClickListener {
            it.parent.requestDisallowInterceptTouchEvent(true)
            val beforeState = dataForState
            dataForState = DetectionData.TVOC
            pumpOnStatus(beforeState, dataForState)
            checkUIState()
        }

        show_eCO2?.setOnClickListener {
            it.parent.requestDisallowInterceptTouchEvent(true)
            val beforeState = dataForState
            dataForState = DetectionData.CO2
            pumpOnStatus(beforeState, dataForState)
            checkUIState()
        }


        show_Temp?.setOnClickListener {
            it.parent.requestDisallowInterceptTouchEvent(true)
            val beforeState = dataForState
            dataForState = DetectionData.Temp
            pumpOnStatus(beforeState, dataForState)
            checkUIState()
        }

        show_RH?.setOnClickListener {
            it.parent.requestDisallowInterceptTouchEvent(true)
            val beforeState = dataForState
            dataForState = DetectionData.Humi
            pumpOnStatus(beforeState, dataForState)
            checkUIState()
        }*/
        imgLight.setOnTouchListener { view, motionEvent ->
            if (dataForState == DetectionData.TVOC || dataForState == DetectionData.CO2 || dataForState == DetectionData.PM25) {
                //Log.wtf("幹我怎麼了!!",motionEvent.action.toString()+ actionToSring(motionEvent.action))
                when(motionEvent.action) {
                    MotionEvent.ACTION_DOWN -> {//.ACTION_BUTTON_PRESS
                        view.parent.requestDisallowInterceptTouchEvent(true)
                        Log.i("幹我按下了!!", motionEvent.action.toString() + actionToSring(motionEvent.action))
                        //20180131
                        //************************************************************************************************************************************
                        sendPumpCommand(BroadcastActions.INTENT_KEY_PUMP_ON)
                        //20180202
                        view.isPressed = true
                        isPumpOn = true
                        //************************************************************************************************************************************
                    }
                    MotionEvent.ACTION_UP -> {//ACTION_BUTTON_RELEASE
                        Log.i("幹我不按了!!", motionEvent.action.toString() + actionToSring(motionEvent.action))
                        sendPumpCommand(BroadcastActions.INTENT_KEY_PUMP_OFF)
                        //20180202
                        //************************************************************************************************************************************
                        view.isPressed = false
                        isPumpOn = false
                        //************************************************************************************************************************************
                    }
                }
            }
            true
        }

        // 初始化inCircleTitle文字大小
        fixInCircleTextSize()
    }


    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        when(event!!.action) {
            MotionEvent.ACTION_DOWN -> {
                v!!.parent.requestDisallowInterceptTouchEvent(true)

                val beforeState = dataForState

                when (v.id) {
                    R.id.show_TVOC -> {
                        dataForState = DetectionData.TVOC
                    }
                    R.id.show_eCO2 -> {
                        dataForState = DetectionData.CO2
                    }
                    R.id.show_Temp -> {
                        dataForState = DetectionData.Temp
                    }
                    R.id.show_RH -> {
                        dataForState = DetectionData.Humi
                    }
                    R.id.show_PM -> {
                        dataForState = DetectionData.PM25
                    }
                }
                pumpOnStatus(beforeState, dataForState)
                checkUIState()
            }
        }
        return true
    }


    private fun sendPumpCommand(command: String) {
        val intent: Intent? = Intent(BroadcastIntents.PRIMARY)
        intent!!.putExtra("status", command)
        mContext!!.sendBroadcast(intent)
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
        Log.e("MainFrg", "onDestroy() called...")
    }


    private fun fixInCircleTextSize() {
        val dm = resources.displayMetrics
        val dpi = dm.densityDpi
        Log.i("DPI", "目前解析度為: $dpi")
        when (dpi) {
        /*DisplayMetrics.DENSITY_HIGH -> {   // HDPI
            //inCircleTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
            inCircleState.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
        }*/
            in DisplayMetrics.DENSITY_420..DisplayMetrics.DENSITY_XXHIGH -> {   // XXHDPI
                //inCircleTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                //inCircleState.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                tvNotify.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                tvLastDetectTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            }
            in DisplayMetrics.DENSITY_560..DisplayMetrics.DENSITY_XXXHIGH -> {
                //inCircleTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)
                //inCircleState.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20f)
                tvNotify.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                tvLastDetectTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            }
        }
    }

    private fun textSpannable(temp: String) {
        val dm = resources.displayMetrics
        val textSpan = SpannableStringBuilder(temp)
        val text1Size = when (dm.densityDpi) {
            DisplayMetrics.DENSITY_DEFAULT -> convertSpToPx(50f)   // MDPI
            DisplayMetrics.DENSITY_HIGH -> convertSpToPx(30f)   // HDPI
            in DisplayMetrics.DENSITY_420..DisplayMetrics.DENSITY_XXHIGH -> convertSpToPx(34f)   // XXHDPI
            in DisplayMetrics.DENSITY_560..DisplayMetrics.DENSITY_XXXHIGH -> convertSpToPx(36f)   // XXXHDPI
            else -> convertSpToPx(30f)
        }
        val text2Size = when (dm.densityDpi) {
            DisplayMetrics.DENSITY_DEFAULT -> convertSpToPx(30f)   // MDPI
            DisplayMetrics.DENSITY_HIGH -> convertSpToPx(18f)   // HDPI
            in DisplayMetrics.DENSITY_420..DisplayMetrics.DENSITY_XXHIGH -> convertSpToPx(20f)   // XXHDPI
            in DisplayMetrics.DENSITY_560..DisplayMetrics.DENSITY_XXXHIGH -> convertSpToPx(22f)   // XXXHDPI
            else -> convertSpToPx(18f)
        }

        textSpan.setSpan(AbsoluteSizeSpan(text1Size),
                0, temp.indexOf(" ") + 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        textSpan.setSpan(AbsoluteSizeSpan(text2Size),
                temp.indexOf(" ") + 1, temp.length,
                Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        //textSpan.setSpan(AbsoluteSizeSpan(text1Size),temp.indexOf(" ") - 1, temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        inCircleValue.text = textSpan
    }

    private fun setThresholdValue(dataForState: DetectionData) {
        //tvRange1.text = dataForState.range1.toString()
        //tvRange2.text = dataForState.range2.toString()
        tvRange1.visibility = View.INVISIBLE
        tvRange2.visibility = View.INVISIBLE
    }

    private fun setBarMaxValue(state: DetectionData) {
        inCircleBar.setThreadholdValue(floatArrayOf(state.range1.toFloat(), state.range2.toFloat()))
        when (state) {

            DetectionData.TVOC -> {
                inCircleBar.setMaxValues(1000f)
            }
            DetectionData.CO2 -> {
                inCircleBar.setMaxValues(5000f)
            }
            DetectionData.Temp -> {
                inCircleBar.setMaxValues(50f)
            }
            DetectionData.Humi -> {
                inCircleBar.setMaxValues(100f)
            }
            DetectionData.PM25 -> {
                inCircleBar.setMaxValues(1000f)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setBtmCurrentValue() {
        //DetectorValue=currentValue
        tvBtmTVOCValue.text = tvocDataFloat.toInt().toString() + " ppb"
        if (MyApplication.isPM25 == "000000000000") {
        //if (pm25DataFloat == 65535f) {
            tvBtmPM25Value.text = "Not Support"
            show_PM.isEnabled = false
        } else {
            tvBtmPM25Value.text = pm25DataFloat.toInt().toString() + " μg/m³"
            show_PM.isEnabled = true
        }
        tvBtmCO2Value.text = co2DataFloat.toInt().toString() + " ppm" //co2DataFloat.toInt().toString()+ " ppm"
        tvBtmTEMPValue.text = tempDataFloat.toString() + " ℃"/*currentValue[0] + " ℃"*/
        tvBtmHUMIValue.text = humiDataFloat.toInt().toString() + " %"/*currentValue[1] + " %"*/
    }

    @SuppressLint("SetTextI18n")
    private fun tvocStatusTextShow(currentValue: Float) {
        when (currentValue) {
            in 0..219 -> {
                tvNotify?.text = getString(R.string.text_message_air_good)
                inCircleState.text = getString(R.string.text_label_status_good)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_01)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_green)
            }
            in 220..659 -> {
                tvNotify?.text = getString(R.string.text_message_air_mid)
                inCircleState.text = getString(R.string.text_label_status_mid)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_02)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_orange)
            }
            in 660..2199 -> {
                tvNotify?.text = getString(R.string.text_message_air_Medium_Orange)
                inCircleState.text = getString(R.string.text_label_status_medium_Orange)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_03)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_orange)
            }
            in 2200..5499 -> {
                tvNotify?.text = getString(R.string.text_message_air_bad)
                inCircleState.text = getString(R.string.text_label_status_bad)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_04)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_red)
            }
            in 5500..19999 -> {
                tvNotify?.text = getString(R.string.text_message_air_Serious_Purple)
                inCircleState.text = getString(R.string.text_label_status_Serious_Purple)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_05)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_red)
            }
            else -> {
                tvNotify?.text = getString(R.string.text_message_air_Extreme_Dark_Purple)
                inCircleState.text = getString(R.string.text_label_status_Extreme_Dark_Purple)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_06)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_red)
            }
        }
    }

    private fun eco2StatusTextShow(currentValue: Float) {
        when (currentValue) {
            in 0..699 -> {
                tvNotify?.text = getString(R.string.message_eCO2_Green)
                inCircleState.text = getString(R.string.label_eCO2_Green)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_01)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_green)
            }
            in 700..999 -> {
                tvNotify?.text = getString(R.string.message_eCO2_Yellow)
                inCircleState.text = getString(R.string.label_eCO2_Yellow)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_02)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_orange)
            }
            in 1000..1499 -> {
                tvNotify?.text = getString(R.string.message_eCO2_Orange)
                inCircleState.text = getString(R.string.label_eCO2_Orange)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_03)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_orange)
            }
            in 1500..2499 -> {
                tvNotify?.text = getString(R.string.message_eCO2_Red)
                inCircleState.text = getString(R.string.label_eCO2_Red)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_04)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_red)
            }
            in 2500..4999 -> {
                tvNotify?.text = getString(R.string.message_eCO2_Purple)
                inCircleState.text = getString(R.string.label_eCO2_Purple)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_05)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_red)
            }
            else -> {
                tvNotify?.text = getString(R.string.message_eCO2_Brown)
                inCircleState.text = getString(R.string.label_eCO2_Brown)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_06)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_red)
            }
        }
    }

    private fun tempStatusTextShow(currentValue: Float) {
        when (currentValue) {
            in 18..24 -> {
                tvNotify?.text = getString(R.string.text_message_temperature)
                inCircleState.text = " "
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                //20180202
                imgLight.setImageResource(R.drawable.face_icon_temp_02)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.bg_temp_green)
            }
            in 25..200 -> {
                tvNotify?.text = getString(R.string.text_message_temperature)
                inCircleState.text = " "
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                //20180202
                imgLight.setImageResource(R.drawable.face_icon_temp_03)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.bg_temp_red)
            }
            else -> {
                tvNotify?.text = getString(R.string.text_message_temperature)
                inCircleState.text = " "
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMiddleBlue))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMiddleBlue))
                //20180202
                imgLight.setImageResource(R.drawable.face_icon_temp_01)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.bg_temp_blue)
            }
        }
    }

    private fun humiStatusTextShow(currentValue: Float) {
        when (currentValue) {
            in 0..44 -> {
                tvNotify?.text = getString(R.string.text_message_humidity)
                inCircleState.text = " "
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMiddleBlue))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.progressBarMiddleBlue))
                //20180202
                imgLight.setImageResource(R.drawable.face_icon_hmi_01)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.bg_rh_blue)
            }
            in 45..65 -> {
                tvNotify?.text = getString(R.string.text_message_humidity)
                inCircleState.text = " "
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                //20180202
                imgLight.setImageResource(R.drawable.face_icon_hmi_02)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.bg_rh_green)
            }
            else -> {

                tvNotify?.text = getString(R.string.text_message_humidity)
                inCircleState.text = " "
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                //20180202
                imgLight.setImageResource(R.drawable.face_icon_hmi_03)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.bg_rh_red)
            }
        }
    }

    private fun pm25StatusTextShow(currentValue: Float) {
        when (currentValue) {
            in 0..15 -> {
                tvNotify?.text = getString(R.string.message_pm25_Green)
                inCircleState.text = getString(R.string.label_pm25_Green)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Good))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_01)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_green)
            }

            in 16..34 -> {
                tvNotify?.text = getString(R.string.message_pm25_Yellow)
                inCircleState.text = getString(R.string.label_pm25_Yellow)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Moderate))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_02)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_orange)
            }
            in 35..54 -> {
                tvNotify?.text = getString(R.string.message_pm25_Orange)
                inCircleState.text = getString(R.string.label_pm25_Orange)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Orange))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_03)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_orange)
            }
            in 55..150 -> {
                tvNotify?.text = getString(R.string.message_pm25_Red)
                inCircleState.text = getString(R.string.label_pm25_Red)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Bad))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_04)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_red)
            }
            in 151..250 -> {
                tvNotify?.text = getString(R.string.message_pm25_Purple)
                inCircleState.text = getString(R.string.label_pm25_Purple)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Main_textResult_Purple))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_05)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_red)
            }
            else -> {
                tvNotify?.text = getString(R.string.message_pm25_Brown)
                inCircleState.text = getString(R.string.label_pm25_Brown)
                inCircleValue.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
                inCircleState.setTextColor(
                        ContextCompat.getColor(mContext, R.color.Test_Unhealthy))
                //20180131
                imgLight.setImageResource(R.drawable.face_icon_06)
                //20180301
                PrimaryBackground.setBackgroundResource(R.drawable.app_bg_cloud_red)
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
                    when (tvocDataFloat) {
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
                    when (co2DataFloat) {
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
                    inCircleTitle.text = getString(R.string.text_label_temperature_full)
                    setThresholdValue(dataForState)
                    setBarMaxValue(dataForState)
                    inCircleBar.setColor(Colors.tempColors, Colors.tempAngles)
                    //Modify Progress Bar
                    when (tempDataFloat) {
                        in -10..18 -> inCircleBar.setCurrentValues(tempDataFloat)
                        else -> inCircleBar.setCurrentValues((tempDataFloat / 60) + 700)
                    }
                    inCircleBar.setCurrentValues(tempDataFloat)
                    //inCircleBar.setCurrentValues(18f)
                    tempStatusTextShow(tempDataFloat)
                    val temp = tempDataFloat.toString() + " °C"
                    textSpannable(temp)
                }

                DetectionData.Humi -> {
                    inCircleTitle.text = getString(R.string.text_label_humidity_full)
                    setThresholdValue(dataForState)
                    setBarMaxValue(dataForState)
                    inCircleBar.setColor(Colors.humiColors, Colors.humiAngles)
                    //Modify Progress Bar
                    when (humiDataFloat) {
                        in 0..45 -> inCircleBar.setCurrentValues(humiDataFloat)
                        else -> inCircleBar.setCurrentValues((humiDataFloat / 60) + 700)
                    }
                    inCircleBar.setCurrentValues(humiDataFloat)
                    //inCircleBar.setCurrentValues(40f)
                    humiStatusTextShow(humiDataFloat)
                    val temp = humiDataFloat.toInt().toString() + " %"
                    textSpannable(temp)
                }
                DetectionData.PM25 -> {
                    inCircleTitle.text = getString(R.string.text_label_pm25)
                    setThresholdValue(dataForState)
                    setBarMaxValue(dataForState)
                    //inCircleBar.setColor(Colors.tvocOldColors, Colors.tvocOldAngles)
                    //inCircleBar.setCurrentValues(tvocDataFloat)
                    inCircleBar.setColor(Colors.tvocCO2Colors, Colors.tvocCO2Angles)
                    //數值不等比顯示
                    when (pm25DataFloat) {
                        in 0..15 -> inCircleBar.setCurrentValues(pm25DataFloat)
                        in 16..35 -> inCircleBar.setCurrentValues((pm25DataFloat / 60) + 700)
                        in 36..65 -> inCircleBar.setCurrentValues((pm25DataFloat / 60) + 770)
                        in 66..150 -> inCircleBar.setCurrentValues((pm25DataFloat / 180) + 830)
                        else -> inCircleBar.setCurrentValues((pm25DataFloat / 360) + 890)
                    }
                    //inCircleBar.setCurrentValues(1000f)
                    pm25StatusTextShow(pm25DataFloat)
                    val temp = pm25DataFloat.toInt().toString() + " μg/m³"
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
            tvBtmTVOCValue.text = "---"
            tvBtmPM25Value.text = "---"
            tvBtmCO2Value?.text = "---"
            tvBtmTEMPValue.text = "---"/*currentValue[0] + " ℃"*/
            tvBtmHUMIValue.text = "---"/*currentValue[1] + " %"*/
            tvNotify?.text = " "
            tvLastDetectTime.text = " "
            inCircleBar.setCurrentValues(0f)
        }
    }

    private fun convertSpToPx(input: Float): Int {
        return (input * resources.displayMetrics.scaledDensity).toInt()
    }

    private val myBroadcastReceiver = object : BroadcastReceiver() {
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
                    pm25DataFloat = bundle.getString(BroadcastActions.INTENT_KEY_PM25_VALUE).toFloat()
                    preHeat = bundle.getString(BroadcastActions.INTENT_KEY_PREHEAT_COUNT)
                }
            }
            checkUIState()
        }
    }
    //20180207
    private fun pumpOnStatus(beforeState: DetectionData, dataState: DetectionData) {
        if (beforeState != dataState) {
            if (isPumpOn) {
                //************************************************************************************************************************************
                //Log.i("幹我不按了!!", motionEvent.action.toString() + actionToSring(motionEvent.action))
                //Toast.makeText(mContext,actionToSring(motionEvent.action).toString(),Toast.LENGTH_SHORT).show()
                val intent: Intent? = Intent(BroadcastIntents.PRIMARY)
                intent!!.putExtra("status", BroadcastActions.INTENT_KEY_PUMP_OFF)
                mContext!!.sendBroadcast(intent)
                //20180202
                imgLight.isPressed = false
                //20180207
                isPumpOn = false
                //************************************************************************************************************************************
            }
        }
    }
}
