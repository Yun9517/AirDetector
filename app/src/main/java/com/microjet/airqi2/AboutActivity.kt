package com.microjet.airqi2

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.util.HashMap
import com.microjet.airqi2.CustomAPI.OnMultipleClickListener
import com.microjet.airqi2.CustomAPI.Utils
import kotlinx.android.synthetic.main.activity_about.*


@Suppress("DEPRECATION")
class AboutActivity : AppCompatActivity() {

    private var soundPool: SoundPool? = null
    private var soundsMap: HashMap<Int, Int>? = null

    private var SOUND1 = 1

    private var mContext: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        mContext = this@AboutActivity

        val string: String? = getString(R.string.show_app_version, BuildConfig.VERSION_NAME)
        aboutAppVersion!!.text = string

        // Create sound pool
        soundPool = SoundPool(4, AudioManager.STREAM_ALARM, 100)
        soundsMap = HashMap()
        soundsMap!!.put(SOUND1, soundPool!!.load(this, R.raw.pixiedust, 1))

        initActionBar()

        app_logo!!.setOnClickListener(object : OnMultipleClickListener(10, 500) {
            override fun onMultipleClick(v: View) {
                soundPool!!.play(soundsMap!![SOUND1]!!, 10f, 10f, 1, 0, 1f)
                Utils.toastMakeTextAndShow(mContext!!, "你為什麼要點我QAQ", Toast.LENGTH_SHORT)
            }
        })
    }

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> { //對用戶按home icon的處理，本例只需關閉activity，就可返回上一activity，即主activity。
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }
}
