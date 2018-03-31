package com.microjet.airqi2

import android.bluetooth.BluetoothGattCharacteristic

/**
 * Created by B00175 on 2018/3/20.
 */
class BleEvent (input :String){
    var char: BluetoothGattCharacteristic? = null
    var message: String? = null

    init{
        message=input
    }


}/* Additional fields if needed */