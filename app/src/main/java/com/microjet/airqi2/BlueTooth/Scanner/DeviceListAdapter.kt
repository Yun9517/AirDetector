package com.microjet.airqi2.BlueTooth.Scanner

/**
 * Created by B00055 on 2018/3/26.
 */

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.microjet.airqi2.R

import java.util.ArrayList


import no.nordicsemi.android.support.v18.scanner.ScanResult

/**
 * DeviceListAdapter class is list adapter for showing scanned Devices name, address and RSSI image based on RSSI values.
 */
class DeviceListAdapter(private val mContext: Context) : BaseAdapter() {
    private val mListBondedValues =ArrayList<ExtendedBluetoothDevice>()
    private val mListValues = ArrayList<ExtendedBluetoothDevice>()

    /**
     * Sets a list of bonded devices.
     * @param devices list of bonded devices.
     */
    fun addBondedDevices(devices: kotlin.collections.Set<BluetoothDevice>) {
        val bondedDevices = mListBondedValues
        for (device in devices) {
            bondedDevices.add(ExtendedBluetoothDevice(device))
        }
        notifyDataSetChanged()
    }

    /**
     * Updates the list of not bonded devices.
     * @param results list of results from the scanner
     */
    fun update(results: kotlin.collections.List<ScanResult>) {
        for (result in results) {
            val device = findDevice(result)
            if (device == null) {
                mListValues.add(ExtendedBluetoothDevice(result))
            } else {
                device.name = if (result.scanRecord != null) result.scanRecord!!.deviceName else null
                device.rssi = result.rssi
            }
        }
        notifyDataSetChanged()
    }

    private fun findDevice(result: ScanResult): ExtendedBluetoothDevice? {
        for (device in mListBondedValues)
            if (device.matches(result))
                return device
        for (device in mListValues)
            if (device.matches(result))
                return device
        return null
    }

    fun clearDevices() {
        mListValues.clear()
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        val bondedCount = mListBondedValues.size + 1 // 1 for the title
        val availableCount = if (mListValues.isEmpty()) 2 else mListValues.size + 1 // 1 for title, 1 for empty text
        return if (bondedCount == 1) availableCount else bondedCount + availableCount
    }

    override fun getItem(position: Int): Any {
        val bondedCount = mListBondedValues.size + 1 // 1 for the title
        if (mListBondedValues.isEmpty()) {
            return if (position == 0)
                R.string.scanner_subtitle_not_bonded
            else
                mListValues.get(position - 1)
        } else {
            if (position == 0)
                return R.string.scanner_subtitle_bonded
            if (position < bondedCount)
                return mListBondedValues.get(position - 1)
            return if (position == bondedCount) R.string.scanner_subtitle_not_bonded else mListValues.get(position - bondedCount - 1)
        }
    }

    override fun getViewTypeCount(): Int {
        return 3
    }

    override fun areAllItemsEnabled(): Boolean {
        return false
    }

    override fun isEnabled(position: Int): Boolean {
        return getItemViewType(position) == TYPE_ITEM
    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0)
            return TYPE_TITLE

        if (!mListBondedValues.isEmpty() && position == mListBondedValues.size + 1)
            return TYPE_TITLE

        return if (position == count - 1 && mListValues.isEmpty()) TYPE_EMPTY else TYPE_ITEM

    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, oldView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(mContext)
        val type = getItemViewType(position)

        var view: View? = oldView
        when (type) {
            TYPE_EMPTY -> if (view == null) {
                view = inflater.inflate(R.layout.device_list_empty, parent, false)
            }
            TYPE_TITLE -> {
                if (view == null) {
                    view = inflater.inflate(R.layout.device_list_title, parent, false)
                }
                val title = view as TextView?
                title!!.setText(getItem(position) as Int)
            }
            else -> {
                if (view == null) {
                    view = inflater.inflate(R.layout.device_list_row, parent, false)
                    val holder = ViewHolder()
                    holder.name = view!!.findViewById(R.id.name)
                    holder.address = view.findViewById(R.id.address)
                    holder.rssi = view.findViewById(R.id.rssi)
                    view.tag = holder
                }

                val device = getItem(position) as ExtendedBluetoothDevice
                val holder = view.tag as ViewHolder
                val name = device.name
                holder.name!!.text = name ?: mContext.getString(R.string.not_available)
                holder.address!!.setText(device.device.getAddress())
                if (!device.isBonded || device.rssi !== ExtendedBluetoothDevice.NO_RSSI) {
                    val rssiPercent = (100.0f * (127.0f + device.rssi) / (127.0f + 20.0f)).toInt()
                    holder.rssi!!.setImageLevel(rssiPercent)
                    holder.rssi!!.visibility = View.VISIBLE
                } else {
                    holder.rssi!!.visibility = View.GONE
                }
            }
        }

        return view!!
    }

    private inner class ViewHolder {
         var name: TextView? = null
         var address: TextView? = null
         var rssi: ImageView? = null
    }

    companion object {
        private val TYPE_TITLE = 0
        private val TYPE_ITEM = 1
        private val TYPE_EMPTY = 2
    }
}
