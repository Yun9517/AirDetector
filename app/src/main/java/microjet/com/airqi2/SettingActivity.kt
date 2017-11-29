package microjet.com.airqi2

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.SwitchCompat
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import microjet.com.airqi2.BuildConfig
import microjet.com.airqi2.R

/**
 * Created by B00174 on 2017/11/29.
 */
class SettingActivity : AppCompatActivity() {

    var spCycle: Spinner? = null
    var swMessage: SwitchCompat? = null
    var swViberate: SwitchCompat? = null
    var swSound: SwitchCompat? = null
    var swRunInBg: SwitchCompat? = null
    var swTotalNotify: SwitchCompat? = null
    var text_msg_stat: TextView? = null
    var text_vibe_stat: TextView? = null
    var text_sound_stat: TextView? = null
    var text_run_bg_stat: TextView? = null
    var text_total_notify_stat: TextView? = null
    var btn_clean: Button? = null
    var btn_export: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        uiFindViewById()

        swMessage!!.setOnClickListener {
            if (swMessage!!.isChecked) {
                text_msg_stat!!.text = getString(R.string.text_setting_on)
            } else {
                text_msg_stat!!.text = getString(R.string.text_setting_off)
            }
        }

        swViberate!!.setOnClickListener {
            if (swViberate!!.isChecked) {
                text_vibe_stat!!.text = getString(R.string.text_setting_on)
            } else {
                text_vibe_stat!!.text = getString(R.string.text_setting_off)
            }
        }

        swSound!!.setOnClickListener {
            if (swSound!!.isChecked) {
                text_sound_stat!!.text = getString(R.string.text_setting_on)
            } else {
                text_sound_stat!!.text = getString(R.string.text_setting_off)
            }
        }

        swRunInBg!!.setOnClickListener {
            if (swRunInBg!!.isChecked) {
                text_run_bg_stat!!.text = getString(R.string.text_setting_on)
            } else {
                text_run_bg_stat!!.text = getString(R.string.text_setting_off)
            }
        }

        swTotalNotify!!.setOnClickListener {
            if (swTotalNotify!!.isChecked) {
                text_total_notify_stat!!.text = getString(R.string.text_setting_on)
            } else {
                text_total_notify_stat!!.text = getString(R.string.text_setting_off)
            }
        }
    }

    private fun uiFindViewById() {
        spCycle = findViewById(R.id.spCycle)

        swMessage = findViewById(R.id.swMessage)
        swViberate = findViewById(R.id.swViberate)
        swSound = findViewById(R.id.swSound)
        swRunInBg = findViewById(R.id.swRunInBg)
        swTotalNotify = findViewById(R.id.swTotalNotify)

        text_msg_stat = findViewById(R.id.text_msg_stat)
        text_vibe_stat = findViewById(R.id.text_vibe_stat)
        text_sound_stat = findViewById(R.id.text_sound_stat)
        text_run_bg_stat = findViewById(R.id.text_run_bg_stat)
        text_total_notify_stat = findViewById(R.id.text_total_notify_stat)

        btn_clean = findViewById(R.id.btn_clean)
        btn_export = findViewById(R.id.btn_export)
    }
}
