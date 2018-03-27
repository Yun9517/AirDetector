package com.microjet.airqi2.BlueTooth.Scanner

import android.bluetooth.BluetoothDevice

import no.nordicsemi.android.support.v18.scanner.ScanResult
/**
 * Created by B00055 on 2018/3/26.
 */
class ExtendedBluetoothDevice {
    val device: BluetoothDevice
    /** The name is not parsed by some Android devices, f.e. Sony Xperia Z1 with Android 4.3 (C6903). It needs to be parsed manually.  */
    var name: String? = null
    var rssi: Int = 0
    var isBonded: Boolean = false

    constructor(scanResult: ScanResult) {
        this.device = scanResult.device
        this.name = if (scanResult.scanRecord != null) scanResult.scanRecord!!.deviceName else null
        this.rssi = scanResult.rssi
        this.isBonded = false
    }

    constructor(device: BluetoothDevice) {
        this.device = device
        this.name = device.name
        this.rssi = NO_RSSI
        this.isBonded = true
    }

    fun matches(scanResult: ScanResult): Boolean {
        return device.address == scanResult.device.address
    }

    companion object {
        /* package */ internal val NO_RSSI = -1000
    }
}
