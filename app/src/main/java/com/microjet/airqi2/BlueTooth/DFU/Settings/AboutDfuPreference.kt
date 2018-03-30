package com.microjet.airqi2.BlueTooth.DFU.Settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.preference.Preference
import android.util.AttributeSet
import android.widget.Toast
import com.microjet.airqi2.R

/**
 * Created by B00055 on 2018/3/26.
 */
class AboutDfuPreference : Preference {

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    override fun onClick() {
        val context = context
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("http://infocenter.nordicsemi.com/index.jsp?topic=%2Fcom.nordic.infocenter.sdk52.v0.9.1%2Fexamples_ble_dfu.html&cp=4_0_0_4_2"))
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

        // is browser installed?
        if (intent.resolveActivity(context.packageManager) != null)
            context.startActivity(intent)
        else {
            Toast.makeText(getContext(), R.string.no_application, Toast.LENGTH_LONG).show()
        }
    }
}
