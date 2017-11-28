/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package microjet.com.airqi2.BlueTooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import android.view.Window
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast

import java.util.ArrayList
import java.util.HashMap

import microjet.com.airqi2.R

//選單按下去的後跳出的視窗及連線資料
class DeviceListActivity : Activity() {
    private var mBluetoothAdapter: BluetoothAdapter? = null
    // private BluetoothAdapter mBtAdapter;
    private var mEmptyList: TextView? = null

    private var deviceList: MutableList<BluetoothDevice>? = null
    private var deviceAdapter: DeviceAdapter? = null
    private val onService: ServiceConnection? = null
    private var devRssiValues: MutableMap<String, Int>? = null
    private var mHandler: Handler? = null
    private var mScanning: Boolean = false
    private var mBluetoothLeService : UartService? = null
    private var mDeviceAddress: String? = null
    private val TAG = "DeviceListActivity"
    private val REQUEST_SELECT_DEVICE = -1


    private val mLeScanCallback = BluetoothAdapter.LeScanCallback { device, rssi, scanRecord ->
        runOnUiThread {
            val kk = device.name
            if (kk != null) {
                if (kk.contains("Microjet") || kk.contains("ADDWI"))
                    addDevice(device, rssi)
            }
        }
    }

    private val mDeviceClickListener = OnItemClickListener { parent, view, position, id ->
        val device = deviceList!![position]
        mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
        val b = Bundle()
        b.putString(BluetoothDevice.EXTRA_DEVICE, deviceList!![position].address)

        val result = Intent()
        result.putExtras(b)
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        //getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.device_list)
        //android.view.WindowManager.LayoutParams layoutParams = this.getWindow().getAttributes();
        //layoutParams.gravity= Gravity.TOP;
        //layoutParams.y = 200;
        mHandler = Handler()
        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = bluetoothManager.adapter
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        populateList()
        mEmptyList = findViewById<View>(R.id.empty) as TextView
        val cancelButton = findViewById<View>(R.id.btn_cancel) as Button
        cancelButton.setOnClickListener {
            if (mScanning == false)
                scanLeDevice(true)
            else
                finish()
        }
    }

    private fun populateList() {
        /* Initialize device list container */
        Log.d(TAG, "populateList")
        deviceList = ArrayList()
        deviceAdapter = DeviceAdapter(this, deviceList!!)
        devRssiValues = HashMap()

        val newDevicesListView = findViewById<View>(R.id.new_devices) as ListView
        newDevicesListView.adapter = deviceAdapter
        newDevicesListView.onItemClickListener = mDeviceClickListener

        scanLeDevice(true)

    }

    private fun scanLeDevice(enable: Boolean) {
        val cancelButton = findViewById<View>(R.id.btn_cancel) as Button
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler!!.postDelayed({
                mScanning = false
                mBluetoothAdapter!!.stopLeScan(mLeScanCallback)

                cancelButton.setText(R.string.scan)
            }, SCAN_PERIOD)

            mScanning = true
            mBluetoothAdapter!!.startLeScan(mLeScanCallback)
            cancelButton.setText(R.string.cancel)
        } else {
            mScanning = false
            mBluetoothAdapter!!.stopLeScan(mLeScanCallback)
            cancelButton.setText(R.string.scan)
        }
    }

    private fun addDevice(device: BluetoothDevice, rssi: Int) {
        var deviceFound = false
        for (listDev in deviceList!!) {
            if (listDev.address == device.address) {
                deviceFound = true
                break
            }
        }
        devRssiValues!!.put(device.address, rssi)
        if (!deviceFound) {
            deviceList!!.add(device)
            mEmptyList!!.visibility = View.GONE
            deviceAdapter!!.notifyDataSetChanged()
        }
    }

    public override fun onStart() {
        super.onStart()

        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
    }

    public override fun onStop() {
        super.onStop()
        mBluetoothAdapter!!.stopLeScan(mLeScanCallback)

    }

    override fun onDestroy() {
        super.onDestroy()
        mBluetoothAdapter!!.stopLeScan(mLeScanCallback)

    }


    internal inner class DeviceAdapter(var context: Context, var devices: List<BluetoothDevice>) : BaseAdapter() {
        var inflater: LayoutInflater

        init {
            inflater = LayoutInflater.from(context)
        }

        override fun getCount(): Int {
            return devices.size
        }

        override fun getItem(position: Int): Any {
            return devices[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val vg: ViewGroup

            if (convertView != null) {
                vg = convertView as ViewGroup
            } else {
                vg = inflater.inflate(R.layout.device_element, null) as ViewGroup
            }

            val device = devices[position]
            val tvadd = vg.findViewById<View>(R.id.address) as TextView
            val tvname = vg.findViewById<View>(R.id.name) as TextView
            val tvpaired = vg.findViewById<View>(R.id.paired) as TextView
            val tvrssi = vg.findViewById<View>(R.id.rssi) as TextView

            tvrssi.visibility = View.VISIBLE
            val rssival = devRssiValues!![device.address]!!.toInt().toByte()
            if (rssival.toInt() != 0) {
                tvrssi.text = "Rssi = " + rssival.toString()
            }

            tvname.text = device.name
            tvadd.text = device.address
            if (device.bondState == BluetoothDevice.BOND_BONDED) {
                //                Log.i(TAG, "device::"+device.getName());
                //                tvname.setTextColor(Color.WHITE);
                //                tvadd.setTextColor(Color.WHITE);
                //                tvpaired.setTextColor(Color.GRAY);
                //                tvpaired.setVisibility(View.VISIBLE);
                //                //tvpaired.setText(R.string.paired);
                //                tvrssi.setVisibility(View.VISIBLE);
                //                tvrssi.setTextColor(Color.WHITE);

            } else {
                //                tvname.setTextColor(Color.WHITE);
                //                tvadd.setTextColor(Color.WHITE);
                //                tvpaired.setVisibility(View.GONE);
                //                tvrssi.setVisibility(View.VISIBLE);
                //                tvrssi.setTextColor(Color.WHITE);
            }
            return vg
        }
    }


    fun clickFunction(view: View) {
        val id = view.id

        when (id) {

        }//case R.id.set_location:
        // 啟動地圖元件用的Intent物件
        //Intent intentMap = new Intent(this,  DeviceListActivity.class );
        // 啟動地圖元件
        // startActivityForResult(intentMap, START_LOCATION);
        //break;

    }

    companion object {

        private val SCAN_PERIOD: Long = 10000 //scanning for 10 seconds
    }

}
