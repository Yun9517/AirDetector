package com.microjet.airqi2.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.*
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.frg_main.*
import com.microjet.airqi2.CustomAPI.ColorArcProgressBar
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.SavePreferences
import com.microjet.airqi2.R
import java.text.SimpleDateFormat
import java.util.*
import android.media.AudioManager
import android.media.SoundPool
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.util.Log




/**
 * Created by ray650128 on 2017/11/23.
 */


class MainFragment : Fragment() {

    enum class DetectionData(val range1: Long,val range2: Long) {
        TVOC(220,660),
        CO2(800,1500),
        Temp(18,25),
        Humi(45,65)
    }

    private var mContext : Context? = null
    private var bar1 : ColorArcProgressBar? = null
    private var tvThreadHold1:TextView?=null
    private var tvThreadHold2:TextView?=null
    private var tvInCycleTitle:TextView?=null
    private var tvInCycleValue:TextView?=null
    private var tvInCycleState:TextView?=null
    private var tvNotify:TextView?=null

    private var tvBtmTVOCValue:TextView?=null
    private var tvBtmPmValue:TextView?=null
    private var tvBtmCarbonValue:TextView?=null
    private var tvBtmTempValue:TextView?=null
    private var tvBtmHUMIValue:TextView?=null

    private var tvLastDetecteTime:TextView?=null
    //private var pressed="TVOC"//0=temperature 1=humidity 2=TVOC 3=CO2

    private var tvocDataFloat = 0f
    private var tempDataFloat = 0f
    private var humiDataFloat = 0f
    private var co2DataFloat = 0f
    private var preHeat = "0"

    private var dataForState = DetectionData.TVOC
    private var mConnState = false


    @Suppress("OverridingDeprecatedMember")

    //20171219   Andy
//    private var mp = MediaPlayer()
    private var mVibrator: Vibrator? = null

    //20171220   Andy
    private var alertId: Int = 0
    private var soundPool: SoundPool? = null

//    private var sourceid: Int = 0
//    private var spool: SoundPool? = null

    private var countsound660:Int?=0
    private var countsound220:Int?=0
    private var countsound800:Int?=0
    private var countsound1500:Int?=0

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        mContext = this.context.applicationContext
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(mContext!!).registerReceiver(MyBroadcastReceiver, makeMainFragmentUpdateIntentFilter())
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater!!.inflate(R.layout.frg_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?){
        super.onActivityCreated(savedInstanceState)
        bar1 = this.view!!.findViewById(R.id.tvocBar)
        tvThreadHold1=this.view!!.findViewById(R.id.tvRange1)
        tvThreadHold2=this.view!!.findViewById(R.id.tvRange2)
        tvInCycleTitle=this.view!!.findViewById(R.id.inCycleTitle)
        tvInCycleValue= this.view!!.findViewById(R.id.inCycleValue)
        tvInCycleState= this.view!!.findViewById(R.id.inCycleState)
        tvNotify = this.view!!.findViewById(R.id.tvNotify)
        tvLastDetecteTime=this.view!!.findViewById(R.id.tvLastDetectTime)

        tvBtmTVOCValue =this.view!!.findViewById(R.id.tvBtmTVOCValue)
        tvBtmPmValue=this.view!!.findViewById(R.id.tvBtmPMValue)
        tvBtmCarbonValue =this.view!!.findViewById(R.id.tvBtmCarbonValue)
        tvBtmTempValue =this.view!!.findViewById(R.id.tvBtmTEMPValue)
        tvBtmHUMIValue=this.view!!.findViewById(R.id.tvBtmHUMIValue)

        show_TVOC?.setOnClickListener {
            dataForState = DetectionData.TVOC
            checkUIState()

        }
//        show_eCO2?.setOnClickListener {
//            dataForState = DetectionData.CO2
//            checkUIState()
//        }
        show_Temp?.setOnClickListener{
            dataForState = DetectionData.Temp
            checkUIState()
        }
        show_RH?.setOnClickListener {
            dataForState = DetectionData.Humi
            checkUIState()
        }


        //20171219   Andy
        //mp = MediaPlayer.create (mContext, R.raw.pixiedust)
        // 初始化震动通知
        if (isInitVibratorNotify()) {
            mVibrator = mContext!!.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator?
        }


        //20171220   Andy
        soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 1)
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        checkUIState()
    }
    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            LocalBroadcastManager.getInstance(mContext!!).unregisterReceiver(MyBroadcastReceiver)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDetach() {
        super.onDetach()
    }

    private fun SetThresholdValue(dataForState: DetectionData){
        tvThreadHold1!!.text= dataForState.range1.toString()
        tvThreadHold2!!.text= dataForState.range2.toString()
    }

    private fun SetbarMaxValue(state: DetectionData){
        bar1?.setThreadholdValue(floatArrayOf(state.range1.toFloat(), state.range2.toFloat()))
        when (state){

            DetectionData.TVOC ->{
                bar1?.setMaxValues(1000f)
            }
            DetectionData.CO2 ->{
                bar1?.setMaxValues(2000f)
            }
            DetectionData.Temp ->{
                bar1?.setMaxValues(3000f)
            }
            DetectionData.Humi ->{
                bar1?.setMaxValues(4000f)
            }
        }
    }


    fun setBtmCurrentValue(){
        //DetectorValue=currentValue
        tvBtmTVOCValue?.text=tvocDataFloat.toInt().toString() + " ppb"
        tvBtmPMValue?.text="Coming soon"
        tvBtmCarbonValue?.text= "Coming soon"//co2DataFloat.toInt().toString()+ " ppm"
        tvBtmTEMPValue?.text=tempDataFloat.toInt().toString() + " ℃"/*currentValue[0] + " ℃"*/
        tvBtmHUMIValue?.text=humiDataFloat.toInt().toString() + " %"/*currentValue[1] + " %"*/
    }

     @SuppressLint("SetTextI18n")

     private fun TVOCStatusTextShow(currentValue:Float){
        if (currentValue < 221){
            tvNotify?.text = getString(R.string.text_message_air_good)
            tvInCycleState?.text = getString(R.string.text_label_ststus_good)
            tvInCycleValue?.setTextColor(resources.getColor(R.color.Main_textResult_Good))
            tvInCycleState?.setTextColor(resources.getColor(R.color.Main_textResult_Good))
            //20171226  Andy
            if(countsound220!=0||countsound660!=0) {
                countsound220=0
                countsound660=0
                Log.e("歸零TVOC220計數變數:", countsound220.toString())
                Log.e("歸零TVOC660計數變數:", countsound660.toString())
            }
        }
        else if (currentValue  > 661) {
            countsound220=0
            Log.e("更新TVOC220計數變數:",countsound220.toString())
            tvNotify?.text = getString(R.string.text_message_air_bad)
            tvInCycleState?.text = getString(R.string.text_label_ststus_bad)
            tvInCycleValue?.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
            tvInCycleState?.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
        }
        else{
            //20171226  Andy
            countsound660=0
            Log.e("更新TVOC660計數變數:",countsound660.toString())
            tvNotify?.text = getString(R.string.text_message_air_mid)
            tvInCycleState?.text = getString(R.string.text_label_ststus_mid)

            tvInCycleValue?.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))
            tvInCycleState?.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))
        }
    }

    private fun tempStatusTextShow(currentValue:Float){
        if (currentValue < 18){
            tvNotify?.text = getString(R.string.text_message_temperature)
            tvInCycleState?.text = " "
            tvInCycleValue?.setTextColor(resources.getColor(R.color.progressBarLittleBlue))
            tvInCycleState?.setTextColor(resources.getColor(R.color.progressBarLittleBlue))
        }
        else if (currentValue > 25){
            tvNotify?.text = getString(R.string.text_message_temperature)
            tvInCycleState?.text = " "
            tvInCycleValue?.setTextColor(resources.getColor(R.color.progressBarDarkBlue))
            tvInCycleState?.setTextColor(resources.getColor(R.color.progressBarDarkBlue))
        }
        else{
            tvNotify?.text = getString(R.string.text_message_temperature)
            tvInCycleState?.text = " "
            tvInCycleValue?.setTextColor(resources.getColor(R.color.progressBarLittleBlue))
            tvInCycleState?.setTextColor(resources.getColor(R.color.progressBarLittleBlue))
        }
    }

    private fun humiStatusTextShow(currentValue:Float){
        if (currentValue < 45){
            tvNotify?.text = getString(R.string.text_message_humidity)
            tvInCycleState?.text = " "
            tvInCycleValue?.setTextColor(resources.getColor(R.color.progressBarLittleBlue))
            tvInCycleState?.setTextColor(resources.getColor(R.color.progressBarLittleBlue))
        }
        else if (currentValue >65){
            tvNotify?.text = getString(R.string.text_message_humidity)
            tvInCycleState?.text = " "
            tvInCycleValue?.setTextColor(resources.getColor(R.color.progressBarDarkBlue))
            tvInCycleState?.setTextColor(resources.getColor(R.color.progressBarDarkBlue))
        }
        else{
            tvNotify?.text = getString(R.string.text_message_humidity)
            tvInCycleState?.text = " "
            tvInCycleValue?.setTextColor(resources.getColor(R.color.progressBarLittleBlue))
            tvInCycleState?.setTextColor(resources.getColor(R.color.progressBarLittleBlue))
        }
    }

    private fun CO2tatusTextShow(currentValue:Float){
        if (currentValue < 800){
            //tvNotify?.text = getString(R.string.text_message_air_good)
            //tvInCycleState?.text = getString(R.string.text_label_ststus_good)
            tvInCycleValue?.setTextColor(resources.getColor(R.color.Main_textResult_Good))
            tvInCycleState?.setTextColor(resources.getColor(R.color.Main_textResult_Good))
        }
        else if (currentValue > 1500) {
            //tvNotify?.text = getString(R.string.text_message_air_bad)
            //tvInCycleState?.text = getString(R.string.text_label_ststus_bad)
            tvInCycleValue?.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
            tvInCycleState?.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
            var mPreference: SharedPreferences = this.activity.getSharedPreferences(SavePreferences.SETTING_KEY, 0)
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false))//&& (countsound660==5||countsound660==0)) {
            {

                if ((countsound1500 == 5 || countsound1500 == 0)) {
                    //20171220   Andy
                    try {
                        alertId = soundPool!!.load(mContext, R.raw.tvoc_over660, 1)
                        Thread.sleep(500)
                        soundPool!!.play(alertId, 1F, 1F, 0, 0, 1F)
                        //20171219   Andy
                        //mp.start()
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
            }

            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false))//&& (countsound660==5||countsound660==0)) {
            {
                if ((countsound1500 == 5|| countsound1500 == 0)) {
                    if (mVibrator == null) {
                    } else {
                        // 震动 1s
                        mVibrator!!.vibrate(2000)
                    }
                }
            }

            if (countsound1500 == 5) {
                countsound1500 = 0
            }
            countsound1500 = countsound1500!! + 1
        }
        else{
            tvNotify?.text = getString(R.string.text_message_air_mid)
            tvInCycleState?.text = getString(R.string.text_label_ststus_mid)

            tvInCycleValue?.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))
            tvInCycleState?.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))

            var mPreference: SharedPreferences= this.activity.getSharedPreferences(SavePreferences.SETTING_KEY,0)
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND,false))//&&(countsound220==5||countsound220==0))
            {
                if ((countsound800 == 5 || countsound800 == 0)) {
                    //20171219   Andy
                    //mp.start()
                    //20171220   Andy
                    try {
                        alertId = soundPool!!.load(mContext, R.raw.tvoc_over220, 1)
                        Thread.sleep(500)
                        soundPool!!.play(alertId, 1F, 1F, 0, 0, 1F)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                }
            }
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false))//&& (countsound660==5||countsound660==0)) {
            {
                if ((countsound800 == 5|| countsound800 == 0)) {
                    if (mVibrator == null) {
                    } else {
                        // 震动 1s
                        mVibrator!!.vibrate(1000)
                    }
                }

            }

            if (countsound800 == 5) {
                countsound800 = 0
            }
            countsound800 = countsound220!! + 1

        }
    }
    protected fun isInitVibratorNotify(): Boolean {
        return true
    }

    private fun makeMainFragmentUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastActions.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GET_NEW_DATA)
        return intentFilter
    }

    @Synchronized private fun checkUIState() {
        if (mConnState && preHeat == "255") {
            //SetThresholdValue(dataForState)
            //SetbarMaxValue(dataForState)
            when (dataForState) {
                DetectionData.TVOC -> {
                    tvInCycleTitle!!.text = getString(R.string.text_label_tvoc_detect)
                    SetThresholdValue(dataForState)
                    SetbarMaxValue(dataForState)
                    bar1?.setCurrentValues(tvocDataFloat)
                    TVOCStatusTextShow(tvocDataFloat)
                    val temp = tvocDataFloat.toInt().toString() + " ppb "
                    textSpannble(temp)
                }
                DetectionData.CO2 -> {
                    tvInCycleTitle!!.text = getString(R.string.text_label_co2)
                    SetThresholdValue(dataForState)
                    SetbarMaxValue(dataForState)
                    bar1?.setCurrentValues(co2DataFloat)
                    CO2tatusTextShow(co2DataFloat)
                    val temp = co2DataFloat.toInt().toString() + " ppm "
                    textSpannble(temp)
                }
                DetectionData.Temp -> {
                    bar1?.setTemperaterColor()
                    tvInCycleTitle!!.text = getString(R.string.text_label_temperature)
                    SetThresholdValue(dataForState)
                    SetbarMaxValue(dataForState)
                    bar1?.setCurrentValues(tempDataFloat)
                    tempStatusTextShow(tempDataFloat)
                    val temp = tempDataFloat.toInt().toString() + " ℃"
                    textSpannble(temp)
                }

                DetectionData.Humi -> {
                    tvInCycleTitle!!.text = getString(R.string.text_label_humidity)
                    SetThresholdValue(dataForState)
                    SetbarMaxValue(dataForState)
                    bar1?.setCurrentValues(humiDataFloat)
                    humiStatusTextShow(humiDataFloat)
                    val temp = humiDataFloat.toInt().toString() + " % "
                    textSpannble(temp)
                }
            }

            setBtmCurrentValue()
            val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
            val date = Date()
            tvLastDetecteTime?.text = dateFormat.format(date).toString()
            //20171228 ANDY增加
        } else {
            tvInCycleValue?.text = "---"
            tvInCycleState?.text = "---"
            tvBtmTVOCValue?.text= "---"
            tvBtmPmValue?.text= "---"
            tvBtmCarbonValue?.text= "---"
            tvBtmTempValue?.text= "---"/*currentValue[0] + " ℃"*/
            tvBtmHUMIValue?.text= "---"/*currentValue[1] + " %"*/
            tvNotify?.text = "---"
            tvLastDetecteTime?.text = "---"
            bar1?.setCurrentValues(0f)
        }
    }

    private fun textSpannble (temp : String) {
        val textSpan= SpannableStringBuilder(temp)
        textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        tvInCycleValue!!.text = textSpan
    }




    private val MyBroadcastReceiver = object: BroadcastReceiver() {
        @SuppressLint("SimpleDateFormat", "SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            when (action) {
                BroadcastActions.ACTION_GATT_DISCONNECTED -> {
                    mConnState = false
                    //setBar1CurrentValue("0","0","0","0","0")
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

