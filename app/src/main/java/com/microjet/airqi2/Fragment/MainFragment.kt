package com.microjet.airqi2.Fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Service
import android.content.Context
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Vibrator
import android.support.v4.app.Fragment
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.AbsoluteSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.frg_main.*
import com.microjet.airqi2.CustomAPI.ColorArcProgressBar
import com.microjet.airqi2.Definition.SavePreferences
import com.microjet.airqi2.R
import java.text.SimpleDateFormat
import java.util.*
import android.media.AudioManager
import android.media.SoundPool



/**
 * Created by ray650128 on 2017/11/23.
 */

class MainFragment : Fragment() {

    private var mContext : Context? = null
    private var bar1 : ColorArcProgressBar? = null
    private var ThreadHold1:TextView?=null
    private var ThreadHold2:TextView?=null
    private var tvocValue:TextView?=null
    private var pmValue:TextView?=null
    private var carbonValue:TextView?=null
    private var tempValue:TextView?=null
    private var wetValue:TextView?=null
    private var textView11Tvoc:TextView?=null
    private var textView3CO2:TextView?=null
    private var textView5Temperature:TextView?=null
    private var textView7Humidity:TextView?=null

    private var imgPanel: ImageView? = null
    private var LabelText:TextView?=null
    private var LastDetecterTime:TextView?=null
    private var pressed="TVOC"//0=temperature 1=humidity 2=TVOC 3=CO2
    private var DetectorValue=ArrayList<String>()
    //private var tvocValue2: TextView?=null
    @Suppress("OverridingDeprecatedMember")

    //20171219   Andy
    private var mp = MediaPlayer()
    private var mVibrator: Vibrator? = null

    //20171220   Andy
    private var alertId: Int = 0
    private var soundPool: SoundPool? = null

    private var sourceid: Int = 0
    private var spool: SoundPool? = null

    private var countsound660:Int?=0
    private var countsound220:Int?=0


    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        mContext = this.context.applicationContext
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View?{
        return inflater!!.inflate(R.layout.frg_main, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?){
        super.onActivityCreated(savedInstanceState)
        bar1 = this.view!!.findViewById(R.id.tvocBar)
        LabelText=this.view?.findViewById(R.id.textView2)

        ThreadHold1=this.view?.findViewById(R.id.textView13)
        ThreadHold2=this.view?.findViewById(R.id.textView14)
        tvocValue=this.view?.findViewById(R.id.tvocValue)
        pmValue=this.view?.findViewById(R.id.pmValue)
        carbonValue=this.view?.findViewById(R.id.carbonValue)
        tempValue=this.view?.findViewById(R.id.tempValue)
        wetValue=this.view?.findViewById(R.id.wetValue)

        textView11Tvoc=this.view?.findViewById(R.id.textView11)
        textView11Tvoc?.setOnClickListener { pressed="TVOC"
            SetThresholdValue()
            SetbarMaxValue()
           bar1?.setCurrentValues(DetectorValue[2].toFloat())
            textView2.text=getString(R.string.text_label_auto_detect)
            val temp=DetectorValue[2]+" ppb "
            val textSpan= SpannableStringBuilder(temp)
            textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            tvocValue2?.text = textSpan

        }
        textView3CO2=this.view?.findViewById(R.id.textView3)
        textView3CO2?.setOnClickListener { pressed="CO2"
            SetThresholdValue()
            SetbarMaxValue()
            bar1?.setCurrentValues(DetectorValue[3].toFloat())
            textView2.text=getString(R.string.text_label_co2)
            val temp=DetectorValue[3]+" ppm "
            val textSpan= SpannableStringBuilder(temp)
            textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

            tvocValue2?.text = textSpan
        }
        textView5Temperature=this.view?.findViewById(R.id.textView5)
    /*    textView5Temperature?.setOnClickListener { pressed="temperature"
            SetThresholdValue()
            SetbarMaxValue()
            bar1?.setCurrentValues(DetectorValue[0].toFloat())
            textView2.text=getString(R.string.text_label_temperature)
            val temp=DetectorValue[0]+" °C "
            val textSpan= SpannableStringBuilder(temp)
            textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            tvocValue2?.text = textSpan
        }
        textView7Humidity=this.view?.findViewById(R.id.textView7)
        textView7Humidity?.setOnClickListener { pressed="humidity"
            SetThresholdValue()
            SetbarMaxValue()
            bar1?.setCurrentValues(DetectorValue[1].toFloat())
            textView2.text=getString(R.string.text_label_humidity)
            val temp=DetectorValue[1]+" ％ "
            val textSpan= SpannableStringBuilder(temp)
            textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
            tvocValue2?.text = textSpan
        }
        */
        LastDetecterTime=this.view?.findViewById(R.id.lastDetectTime)
    //    imgPanel = this.view!!.findViewById(R.id.imgPanel)

     //   imgPanel!!.bringToFront()
     //   tvocValue2=this.view?.findViewById(R.id.tvocValue2)
        //20171219   Andy
        mp = MediaPlayer.create (mContext, R.raw.pixiedust)
        // 初始化震动通知
        if (isInitVibratorNotify()) {
            mVibrator = mContext!!.getSystemService(Service.VIBRATOR_SERVICE) as Vibrator?
        }


        //20171220   Andy
        soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 1)
        //alertId = soundPool!!.load(mContext, R.raw.tvoc_over660, 1)
        //sourceid = spool!!.load(mContext, R.raw.tvoc_over220, 1)

    }
    private fun SetThresholdValue(){
        when (pressed){/*
            "temperature"->{
                ThreadHold1?.text="20"
                ThreadHold2?.text="50"
            }
            "humidity"->{
                ThreadHold1?.text="20"
                ThreadHold2?.text="50"
            }*/
            "TVOC"->{
                ThreadHold1?.text="220"
                ThreadHold2?.text="660"
            }
            "CO2"->{
                ThreadHold1?.text="800"
                ThreadHold2?.text="1500"
            }
            else ->{pressed="TVOC"
                ThreadHold1?.text="220"
                ThreadHold2?.text="660"
            }
        }
    }
    private fun SetbarMaxValue(){
        val range1:Float=ThreadHold1?.text.toString().toFloat()
        val range2:Float=ThreadHold2?.text.toString().toFloat()
        bar1?.setThreadholdValue(floatArrayOf(range1,range2))
    //    pressed="temperature"
        when (pressed){
        /*    "temperature"->{
                bar1?.setMaxValues(100f)

            }
            "humidity"->{
                bar1?.setMaxValues(100f)
            }
            */
            "TVOC"->{
                bar1?.setMaxValues(1000f)
            }
            "CO2"->{
                bar1?.setMaxValues(2000f)
            }
            else ->{pressed="TVOC"
                bar1?.setMaxValues(1000f)
            }
        }
    }
    override fun onResume() {
        super.onResume()
        SetThresholdValue()
        val range1:Float=ThreadHold1?.text.toString().toFloat()
        val range2:Float=ThreadHold2?.text.toString().toFloat()
        bar1?.setThreadholdValue(floatArrayOf(range1,range2))
    //    pressed="temperature"
        when (pressed){/*
            "temperature"->{
                bar1?.setMaxValues(100f)
            }
            "humidity"->{
                bar1?.setMaxValues(100f)
            }
            */
            "TVOC"->{
                bar1?.setMaxValues(1000f)
            }
            "CO2"->{
                bar1?.setMaxValues(2000f)
            }
            else ->{pressed="TVOC"
                bar1?.setMaxValues(1000f)
            }
        }

       // bar1!!.setCurrentValues(10f)
    }

    override fun onStop() {
        super.onStop()

    }
    var m_flagTemp=0
    fun setGetTimeFlag(flag:Int) {
        if (flag == m_flagTemp) {
            //no change
        } else {//change
            if (flag>m_flagTemp)//jude 0 to 1 or 1 to 0
            {
                LastDetecterTime?.text = getDateTime()

            }
            m_flagTemp = flag
        }
    }
    fun getDateTime(): String {
        val sdFormat = SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.TAIWAN)
        val date = Date().time
        return sdFormat.format(date)
    }
    fun setCurrentValue(currentValue:ArrayList<String>){
        DetectorValue=currentValue
        tvocValue?.text=currentValue[2] + " ppb"
        pmValue?.text="Coming soon"
        carbonValue?.text=currentValue[3] + " ppm"
        tempValue?.text="Coming soon"/*currentValue[0] + " ℃"*/
        wetValue?.text="Coming soon"/*currentValue[1] + " %"*/

    }

     @SuppressLint("SetTextI18n")
     fun setBar1CurrentValue(tempVal: String, humiVal: String, tvocVal: String, co2Val: String, pm25Val: String) {

         val stringArray=ArrayList<String>()
         stringArray.add(tempVal)
         stringArray.add(humiVal)
         stringArray.add(tvocVal)
         stringArray.add(co2Val)
         setCurrentValue(stringArray)
/*

         if (currentValue.toFloat() < 221){
             textView?.text = getString(R.string.text_message_air_good)
             tvocStatus?.text = getString(R.string.text_label_ststus_good)

             tvocValue2.setTextColor(resources.getColor(R.color.Main_textResult_Good))
             tvocStatus.setTextColor(resources.getColor(R.color.Main_textResult_Good))
         }
         else if (currentValue.toFloat() > 661) {
             textView?.text = getString(R.string.text_message_air_bad)
             tvocStatus?.text = getString(R.string.text_label_ststus_bad)
             tvocValue2.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
             tvocStatus.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
         }
         else{
             textView?.text = getString(R.string.text_message_air_mid)
             tvocStatus?.text = getString(R.string.text_label_ststus_mid)

             tvocValue2.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))
             tvocStatus.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))
         }
     // ********* 2017/12/05 主頁面大小字 ************************************************* //

         */
         SetThresholdValue()
         SetbarMaxValue()
         var temp = ""
         when (pressed){
             "temperature"->{
                 temp = DetectorValue[0] + " °C "
                 bar1?.setCurrentValues(DetectorValue[0].toFloat())
             }
             "humidity"->{
                 temp = DetectorValue[1] + " ％ "
                 bar1?.setCurrentValues(DetectorValue[1].toFloat())
             }
             "TVOC"->{
                 temp = DetectorValue[2] +" ppb "
                 bar1?.setCurrentValues(DetectorValue[2].toFloat())
                 TVOCStatusTextShow(DetectorValue[2])
             }
             "CO2"->{
                 temp = DetectorValue[3] +" ppm "
                 bar1?.setCurrentValues(DetectorValue[3].toFloat())
             }
             else ->{
                 temp = DetectorValue[3] +" ppb "
                 bar1?.setCurrentValues(DetectorValue[2].toFloat())
             }
         }

         val textSpan = SpannableStringBuilder(temp)
         textSpan.setSpan( 30,0,temp.indexOf(" ") +1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
         textSpan.setSpan(AbsoluteSizeSpan(50), temp.indexOf(" ") + 1, temp.length - 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
         textSpan.setSpan(30,temp.indexOf(" ") - 1,temp.length, Spannable.SPAN_INCLUSIVE_INCLUSIVE)

         tvocValue2?.text = textSpan
         //tvocValue?.text = textSpan
     // *********************************************************************************** //
    }
    fun TVOCStatusTextShow(currentValue:String){
        if (currentValue.toFloat() < 221){
            textView?.text = getString(R.string.text_message_air_good)
            tvocStatus?.text = getString(R.string.text_label_ststus_good)

            tvocValue2.setTextColor(resources.getColor(R.color.Main_textResult_Good))
            tvocStatus.setTextColor(resources.getColor(R.color.Main_textResult_Good))
        }
        else if (currentValue.toFloat() > 661) {
            textView?.text = getString(R.string.text_message_air_bad)
            tvocStatus?.text = getString(R.string.text_label_ststus_bad)
            tvocValue2.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
            tvocStatus.setTextColor(resources.getColor(R.color.Main_textResult_Bad))
            var mPreference: SharedPreferences = this.activity.getSharedPreferences(SavePreferences.SETTING_KEY, 0)
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false))//&& (countsound660==5||countsound660==0)) {
            {

                if ((countsound660 == 5 || countsound660 == 0)) {

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
                if ((countsound660 == 5|| countsound660 == 0)) {
                    if (mVibrator == null) {
                    } else {
                        // 震动 1s
                        mVibrator!!.vibrate(2000)
                    }
                }
            }

            if (countsound660 == 5) {
                countsound660 = 0
            }
            countsound660 = countsound660!! + 1
        }
        else{
            textView?.text = getString(R.string.text_message_air_mid)
            tvocStatus?.text = getString(R.string.text_label_ststus_mid)

            tvocValue2.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))
            tvocStatus.setTextColor(resources.getColor(R.color.Main_textResult_Moderate))

            var mPreference: SharedPreferences= this.activity.getSharedPreferences(SavePreferences.SETTING_KEY,0)
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND,false))//&&(countsound220==5||countsound220==0))
            {

                if ((countsound220 == 5 || countsound220 == 0)) {
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
                if ((countsound220 == 5|| countsound220 == 0)) {
                    if (mVibrator == null) {
                    } else {
                        // 震动 1s
                        mVibrator!!.vibrate(1000)
                    }
                }

            }

            if (countsound220 == 5) {
                countsound220 = 0
            }
            countsound220 = countsound220!! + 1

        }
    }

    protected fun isInitVibratorNotify(): Boolean {
        return true
    }
}
