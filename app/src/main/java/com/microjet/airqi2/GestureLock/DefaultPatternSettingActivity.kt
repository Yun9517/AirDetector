package com.microjet.airqi2.GestureLock

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import com.github.ihsg.patternlocker.OnPatternChangeListener
import com.github.ihsg.patternlocker.PatternIndicatorView
import com.github.ihsg.patternlocker.PatternLockerView
import com.microjet.airqi2.Definition.SavePreferences
import com.microjet.airqi2.R
import kotlinx.android.synthetic.main.activity_default_pattern_setting.*

class DefaultPatternSettingActivity : AppCompatActivity() {

    private var patternHelper: PatternHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_default_pattern_setting)

        patternLockerView.setOnPatternChangedListener(object : OnPatternChangeListener {
            override fun onStart(view: PatternLockerView) {}

            override fun onChange(view: PatternLockerView, hitList: List<Int>) {}

            override fun onComplete(view: PatternLockerView, hitList: List<Int>) {
                val isOk = isPatternOk(hitList)
                view.updateStatus(!isOk)
                //patternIndicatorView!!.updateState(hitList, !isOk)
                updateMsg()
            }

            override fun onClear(view: PatternLockerView) {
                finishIfNeeded()
            }
        })

        text_msg.text = resources.getText(R.string.text_set_pattern)
        this.patternHelper = PatternHelper()
    }

    private fun isPatternOk(hitList: List<Int>): Boolean {
        this.patternHelper!!.validateForSetting(hitList)
        return this.patternHelper!!.isOk
    }

    private fun updateMsg() {
        text_msg.text = this.patternHelper!!.message
        text_msg.setTextColor(if (this.patternHelper!!.isOk)
            ContextCompat.getColor(this, R.color.Main_textResult_Good)
        else
            ContextCompat.getColor(this, R.color.Main_textResult_Bad))
    }

    private fun finishIfNeeded() {
        if (this.patternHelper!!.isFinish) {
            finish()

            val share = getSharedPreferences(SavePreferences.SETTING_KEY, Context.MODE_PRIVATE)
            share.edit().putBoolean(SavePreferences.SETTING_MAP_PRIVACY, true).apply()
        }
    }

    companion object {

        fun startAction(context: Context) {
            val intent = Intent(context, DefaultPatternSettingActivity::class.java)
            context.startActivity(intent)
        }
    }
}