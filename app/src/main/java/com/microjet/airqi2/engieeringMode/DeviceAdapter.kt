package com.microjet.airqi2.engieeringMode

import android.annotation.SuppressLint
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.ToggleButton
import com.microjet.airqi2.AsmDataModel
import com.microjet.airqi2.CustomAPI.SelectedItem
import com.microjet.airqi2.MyApplication
import com.microjet.airqi2.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by ray650128 on 2018/4/24.
 *
 */

class DeviceAdapter(val mContext: Context, val mDataset: ArrayList<DeviceInfo>) : RecyclerView.Adapter<DeviceAdapter.ViewHolder>(), View.OnClickListener {

    private var mOnItemClickListener: OnItemClickListener? = null

    // 入口
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): DeviceAdapter.ViewHolder {
        // 透過 Layout 指定 View
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.device_cardview, parent, false)
        // set the view's size, margins, paddings and layout parameters

        v.setOnClickListener(this)

        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.deviceName.text = mDataset[position].DeviceName
        holder.deviceAddr.text = mDataset[position].DeviceAddress
        holder.deviceSerial.text = mDataset[position].DeviceSerial
        holder.connTime.text = mDataset[position].ConnectTime
        holder.tvocVal.text = mDataset[position].TVOCValue
        holder.eco2Val.text = mDataset[position].ECO2Value
        holder.pm25Val.text = mDataset[position].PM25Value
        holder.humiVal.text = mDataset[position].HUMIValue
        holder.tempVal.text = mDataset[position].TEMPValue
        holder.rssiVal.text = mDataset[position].RSSIValue

        // 設置選中狀態
        if (position == SelectedItem.getSelectedItem()) {

        } else {

        }

        //holder.itemView.setOnClickListener { v -> mOnItemClickListener!!.onItemClick(holder.itemView, position) }
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        // each data item is just a string in this case
        //var textDate: TextView = v.findViewById(R.id.textDate)
        //var imgPointer: ImageView
        var deviceName: TextView = v.findViewById(R.id.deviceName)
        var deviceAddr: TextView = v.findViewById(R.id.deviceAddr)
        var deviceSerial: TextView = v.findViewById(R.id.deviceSerial)
        var connTime: TextView = v.findViewById(R.id.connTime)

        var tvocVal: TextView = v.findViewById(R.id.tvocVal)
        var eco2Val: TextView = v.findViewById(R.id.eco2Val)
        var pm25Val: TextView = v.findViewById(R.id.pm25Val)
        var humiVal: TextView = v.findViewById(R.id.humiVal)
        var tempVal: TextView = v.findViewById(R.id.tempVal)
        var rssiVal: TextView = v.findViewById(R.id.rssiVal)

        var pumpSW: ToggleButton = v.findViewById(R.id.pumpBtn)

        init {
            //imgPointer = v.findViewById(R.id.imgPointer)
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount(): Int {
        return mDataset.size
    }

    // 定義 OnItenClickListener 的介面
    interface OnItemClickListener {
        fun onItemClick(view: View, position: Int)
    }

    override fun onClick(v: View) {
        if (mOnItemClickListener != null) {
            //注意這裡使用getTag方法獲取position
            mOnItemClickListener!!.onItemClick(v, v.tag as Int)
        }
    }

    // 暴露給外部的方法
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.mOnItemClickListener = listener
    }
}