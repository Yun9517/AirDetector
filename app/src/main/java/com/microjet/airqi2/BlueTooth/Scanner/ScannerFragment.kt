package com.microjet.airqi2.BlueTooth.Scanner

import android.Manifest
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.ParcelUuid
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import com.microjet.airqi2.R
import java.util.*


import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings
/**
 * Created by B00055 on 2018/3/26.
 */
 class ScannerFragment : DialogFragment() {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mListener: OnDeviceSelectedListener? = null
    private var mAdapter: DeviceListAdapter? = null
    private val mHandler = Handler()
    private var mScanButton: Button? = null

    private var mPermissionRationale: View? = null

    private var mUuid: ParcelUuid? = null

    private var mIsScanning = false

    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            // do nothing
        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            mAdapter!!.update(results)
        }

        override fun onScanFailed(errorCode: Int) {
            // should never be called
        }
    }

    /**
     * Interface required to be implemented by activity.
     */
    interface OnDeviceSelectedListener {
        /**
         * Fired when user selected the device.
         *
         * @param device
         * the device to connect to
         * @param name
         * the device name. Unfortunately on some devices [BluetoothDevice.getName] always returns `null`, f.e. Sony Xperia Z1 (C6903) with Android 4.3. The name has to
         * be parsed manually form the Advertisement packet.
         */
        fun onDeviceSelected(device: BluetoothDevice, name: String)

        /**
         * Fired when scanner dialog has been cancelled without selecting a device.
         */
        fun onDialogCanceled()
    }

    /**
     * This will make sure that [OnDeviceSelectedListener] interface is implemented by activity.
     */
    override fun onAttach(context: Context?) {
        super.onAttach(context)
        try {
            this.mListener = context as OnDeviceSelectedListener?
        } catch (e: ClassCastException) {
            throw ClassCastException(context!!.toString() + " must implement OnDeviceSelectedListener")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = arguments
        if (args!!.containsKey(PARAM_UUID)) {
            mUuid = args!!.getParcelable(PARAM_UUID)
        }

        val manager = activity!!.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        mBluetoothAdapter = manager.adapter
    }

    override fun onDestroyView() {
        stopScan()
        super.onDestroyView()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity!!)
        val dialogView = LayoutInflater.from(activity).inflate(R.layout.fragment_device_selection, null)
        val listview = dialogView!!.findViewById<ListView>(android.R.id.list)

        listview.setEmptyView(dialogView.findViewById(android.R.id.empty))
         mAdapter= DeviceListAdapter(activity!!)
        listview.setAdapter(mAdapter)

        builder.setTitle(R.string.scanner_title)
        val dialog = builder.setView(dialogView).create()
        listview.setOnItemClickListener({ parent, view, position, id ->
            stopScan()
            dialog.dismiss()
            val d = mAdapter!!.getItem(position) as ExtendedBluetoothDevice
            mListener!!.onDeviceSelected(d.device, d.name!!)
        })

        mPermissionRationale = dialogView.findViewById(R.id.permission_rationale) // this is not null only on API23+

        mScanButton = dialogView.findViewById(R.id.action_cancel)
        mScanButton!!.setOnClickListener { v ->
            if (v.id == R.id.action_cancel) {
                if (mIsScanning) {
                    dialog.cancel()
                } else {
                    startScan()
                }
            }
        }

        addBondedDevices()
        if (savedInstanceState == null)
            startScan()
        return dialog
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)

        mListener!!.onDialogCanceled()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSION_REQ_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have been granted the Manifest.permission.ACCESS_COARSE_LOCATION permission. Now we may proceed with scanning.
                    startScan()
                } else {
                    mPermissionRationale!!.visibility = View.VISIBLE
                    Toast.makeText(activity, R.string.no_required_permission, Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

    /**
     * Scan for 5 seconds and then stop scanning when a BluetoothLE device is found then mLEScanCallback is activated This will perform regular scan for custom BLE Service UUID and then filter out.
     * using class ScannerServiceParser
     */
    private fun startScan() {
        // Since Android 6.0 we need to obtain either Manifest.permission.ACCESS_COARSE_LOCATION or Manifest.permission.ACCESS_FINE_LOCATION to be able to scan for
        // Bluetooth LE devices. This is related to beacons as proximity devices.
        // On API older than Marshmallow the following code does nothing.
        if (ContextCompat.checkSelfPermission(context!!, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // When user pressed Deny and still wants to use this functionality, show the rationale
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity!!, Manifest.permission.ACCESS_COARSE_LOCATION) && mPermissionRationale!!.visibility == View.GONE) {
                mPermissionRationale!!.visibility = View.VISIBLE
                return
            }

            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_PERMISSION_REQ_CODE)
            return
        }

        // Hide the rationale message, we don't need it anymore.
        if (mPermissionRationale != null)
            mPermissionRationale!!.visibility = View.GONE

        mAdapter!!.clearDevices()
        mScanButton!!.setText(R.string.scanner_action_cancel)

        val scanner = BluetoothLeScannerCompat.getScanner()
        val settings = ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).setUseHardwareBatchingIfSupported(false).build()
        val filters = ArrayList<ScanFilter>()
        filters.add(ScanFilter.Builder().setServiceUuid(mUuid).build())
        scanner.startScan(filters, settings, scanCallback)

        mIsScanning = true
        mHandler.postDelayed({
            if (mIsScanning) {
                stopScan()
            }
        }, SCAN_DURATION)
    }

    /**
     * Stop scan if user tap Cancel button
     */
    private fun stopScan() {
        if (mIsScanning) {
            mScanButton!!.setText(R.string.scanner_action_scan)

            val scanner = BluetoothLeScannerCompat.getScanner()
            scanner.stopScan(scanCallback)

            mIsScanning = false
        }
    }

    private fun addBondedDevices() {
        val devices = mBluetoothAdapter!!.bondedDevices
        mAdapter!!.addBondedDevices(devices)
    }

    companion object {
        private val TAG = "ScannerFragment"

        private val PARAM_UUID = "param_uuid"
        private val SCAN_DURATION: Long = 5000

        private val REQUEST_PERMISSION_REQ_CODE = 34 // any 8-bit number

        fun getInstance(uuid: UUID?): ScannerFragment {
            val fragment = ScannerFragment()

            val args = Bundle()
            if (uuid != null)
                args.putParcelable(PARAM_UUID, ParcelUuid(uuid))
            fragment.arguments = args
            return fragment
        }
    }
}
