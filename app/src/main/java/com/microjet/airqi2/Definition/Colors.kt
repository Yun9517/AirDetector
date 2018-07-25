package com.microjet.airqi2.Definition

import android.support.v4.content.ContextCompat
import com.microjet.airqi2.MyApplication
import com.microjet.airqi2.R

/**
 * Created by ray650128 on 2018/1/19.
 */
object Colors {
    // 定義顏色
    private val tvocColor1 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Good)
    private val tvocColor2 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Moderate)
    private val tvocColor3 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Orange)
    private val tvocColor4 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Bad)
    private val tvocColor5 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Purple)
    private val tvocColor6 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Unhealthy)

    private val tvocOldColor1 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Good)
    private val tvocOldColor2 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Moderate)
    private val tvocOldColor3 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Bad)

    private val tempColor1 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Blue)
    private val tempColor2 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Good)
    private val tempColor3 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Bad)

    private val humiColor1 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Blue)
    private val humiColor2 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Good)
    private val humiColor3 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Bad)

    // 2018/06/07 Add eCO2 color
    private val eco2Color1 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Good)
    private val eco2Color2 = ContextCompat.getColor(MyApplication.applicationContext(), R.color.Main_textResult_Bad)

    // 公用顏色陣列
    val tvocCO2Colors = intArrayOf(tvocColor1, tvocColor2, tvocColor3, tvocColor4, tvocColor5, tvocColor6)
    val tvocOldColors = intArrayOf(tvocOldColor1, tvocOldColor2, tvocOldColor3)
    val tempColors = intArrayOf(tempColor1, tempColor2, tempColor3)
    val humiColors = intArrayOf(humiColor1, humiColor2, humiColor3)
    // 2018/06/07 Add eCO2 color
    val eCO2Color = intArrayOf(eco2Color1, eco2Color2)

    val tvocCO2Angles = floatArrayOf(0.025f, 0.275f, 0.575f, 0.65f, 0.70f, 0.75f)
    val tvocOldAngles = floatArrayOf(0.15f, 0.375f, 0.7f)
    val tempAngles = floatArrayOf(0.15f, 0.375f, 0.7f)
    val humiAngles = floatArrayOf(0.2f, 0.375f, 0.7f)
    // 2018/06/07 Add eCO2 Color Angles
    val eco2Angles = floatArrayOf(0.375f, 0.7f)
    // 2018/07/23 Add PM2.5 & PM10 Color Angles
    //val PM25Angles = floatArrayOf(0.025f, 0.25f, 0.575f, 0.65f, 0.70f, 0.75f)
    //val PM10Angles = floatArrayOf(0.025f, 0.275f, 0.575f, 0.65f, 0.70f, 0.75f)
}