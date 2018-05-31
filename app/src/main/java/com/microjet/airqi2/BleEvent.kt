package com.microjet.airqi2

import android.bluetooth.BluetoothGattCharacteristic

/**
 * Created by B00175 on 2018/3/20.
 */
class BleEvent(input: String) {
    var char: BluetoothGattCharacteristic? = null
    var message: String? = null
    // 2018/05/29 Add "introduction" & "ourStory", modify sequence. Thanks the original creator!
    var userExp: String? = null
    var introduction: String? = null
    var buyProduct: String? = null
    var ourStory: String? = null

    init {
        message = input
    }


}/* Additional fields if needed */