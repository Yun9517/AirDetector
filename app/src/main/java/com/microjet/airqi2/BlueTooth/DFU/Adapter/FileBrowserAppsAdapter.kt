package com.microjet.airqi2.BlueTooth.DFU.Adapter

/**
 * Created by B00055 on 2018/3/26.
 */

import android.content.Context
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

import com.microjet.airqi2.R
/**
 * This adapter displays some file browser applications that can be used to select HEX file. It is used when there is no such app already installed on the device. The hardcoded apps and Google Play
 * URLs are specified in res/values/strings_dfu.xml.
 */
class FileBrowserAppsAdapter(context: Context) : BaseAdapter() {
    private val mInflater: LayoutInflater
    private val mResources: Resources

    init {
        mInflater = LayoutInflater.from(context)
        mResources = context.resources
    }

    override fun getCount(): Int {
        return mResources.getStringArray(R.array.dfu_app_file_browser).size
    }

    override fun getItem(position: Int): Any {
        return mResources.getStringArray(R.array.dfu_app_file_browser_action)[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        var view: View? = convertView
        if (view == null) {
            view = mInflater.inflate(R.layout.app_file_browser_item, parent, false)
        }

        val item = view as TextView?
        item!!.text = mResources.getStringArray(R.array.dfu_app_file_browser)[position]
        item.compoundDrawablesRelative[0].level = position
        return view!!
    }
}
