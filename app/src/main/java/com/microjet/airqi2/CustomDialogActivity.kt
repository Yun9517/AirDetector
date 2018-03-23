package com.microjet.airqi2

import android.app.Activity
import android.os.Bundle
import android.view.Window
import kotlinx.android.synthetic.main.custom_dialog.*

/**
 * Created by ray650128 on 2017/11/24.
 */

class CustomDialogActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_dialog)

        uiSetAction()
        getText()
    }

    private fun uiSetAction() {
        btn_confirm.setOnClickListener {
            this@CustomDialogActivity.finish()
        }

        text_close!!.setOnClickListener {
            this@CustomDialogActivity.finish()
        }
    }

    private fun getText() {
        val bundle: Bundle? = intent.extras

        val title: String? = bundle!!.getString("dialogTitle")
        val content: String? = bundle.getString("dialogContent")

        text_title.text = title
        text_content.text = content
    }
}