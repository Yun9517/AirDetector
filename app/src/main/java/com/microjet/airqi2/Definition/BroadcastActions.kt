package com.microjet.airqi2.Definition

/**
 * Created by B00055 on 2017/12/20.
 */
object BroadcastActions {
    // 藍芽連線狀態
    const val ACTION_GATT_CONNECTING = "com.microjet.airqi.ACTION_GATT_CONNECTING"
    const val ACTION_GATT_CONNECTED = "com.microjet.airqi.ACTION_GATT_CONNECTED"
    const val ACTION_GATT_DISCONNECTING = "com.microjet.airqi.ACTION_GATT_DISCONNECTING"
    const val ACTION_GATT_DISCONNECTED = "com.microjet.airqi.ACTION_GATT_DISCONNECTED"

    // GATT Service 動作
    const val DEVICE_DOES_NOT_SUPPORT_UART = "com.microjet.airqi.DEVICE_DOES_NOT_SUPPORT_UART"
    const val ACTION_GATT_SERVICES_DISCOVERED = "com.microjet.airqi.ACTION_GATT_SERVICES_DISCOVERED"
    const val ACTION_DATA_AVAILABLE = "com.microjet.airqi.ACTION_DATA_AVAILABLE"

    // Activity 對 Service 動作定義
    const val ACTION_CONNECT_DEVICE = "com.microjet.airqi.ACTION_CONNECT_DEVICE"
    const val ACTION_DISCONNECT_DEVICE = "com.microjet.airqi.ACTION_DISCONNECT_DEVICE"
    const val EXTRA_DATA = "com.microjet.airqi.EXTRA_DATA"

    const val ACTION_UPDATE_BLE_DEVICE = "com.microjet.airqi.ACTION_UPDATE_BLE_DEVICE"

    const val ACTION_UPDATE_BLE_CONN_STATUS = "com.microjet.airqi.ACTION_UPDATE_BLE_CONN_STATUS"
    const val ACTION_GET_NEW_DATA = "com.microjet.airqi.ACTION_GET_NEW_DATA"
    const val ACTION_GET_RESULT = "com.microjet.airqi.ACTION_GET_RESULT"

    const val ACTION_SET_APP_IN_SLEEP = "com.microjet.airqi.ACTION_SET_APP_IN_SLEEP"
    const val ACTION_SET_APP_RESUME = "com.microjet.airqi.ACTION_SET_APP_RESUME"

    const val ACTION_GET_HISTORY_COUNT = "com.microjet.airqi.ACTION_GET_HISTORY_COUNT"

    const val ACTION_LOADING_DATA = "com.microjet.airqi.ACTION_LOADING_DATA"

    const val ACTION_LOADING_DATA1 = "com.microjet.airqi.ACTION_LOADING_DATA1"

    const val ACTION_STATUS_HEATING = "com.microjet.airqi.ACTION_STATUS_HEATING"

    const val ACTION_ALARM_NOTIFICATION = "com.microjet.airqi.ACTION_ALARM_NOTIFICATION"

    const val ACTION_CREATE_ALARM_NOTIFICATION = "com.microjet.airqi.ACTION_CREATE_ALARM_NOTIFICATION"

    // 設定 Device 動作定義
    const val ACTION_CHECK_ITEMS = "com.microjet.airqi.ACTION_CHECK_ITEMS"
    const val ACTION_CALL_ITEMS = "com.microjet.airqi.ACTION_CALL_ITEMS"
    const val ACTION_SET_SAMPLE_RATE = "com.microjet.airqi.ACTION_SET_SAMPLE_RATE"
    const val ACTION_GET_SAMPLE_RATE = "com.microjet.airqi.ACTION_GET_SAMPLE_RATE"
    const val ACTION_CALL_DEVICE_START_SAMPLE = "com.microjet.airqi.ACTION_CALL_DEVICE_START_SAMPLE"

    // 設定頁動作
    const val ACTION_SET_NOTIFICATION = "com.microjet.airqi.ACTION_SET_NOTIFICATION"
    const val ACTION_SET_VIBRATION = "com.microjet.airqi.ACTION_SET_VIBRATION"
    const val ACTION_SET_SOUND = "com.microjet.airqi.ACTION_SET_SOUND"

    // Intent 傳遞參數定義
    const val INTENT_KEY_DEVICE_NAME = "airqi2.intent.BluetoothDevName"
    const val INTENT_KEY_DEVICE_ADDR = "airqi2.intent.BluetoothDevAddr"

    const val INTENT_KEY_SAMPLE_TIME = "airqi2.intent.SampleTime"
    const val INTENT_KEY_CALL_FROM_CONNECT = "airqi2.intent.callFromConnect"

    const val INTENT_KEY_RESULT = "airqi2.intent.result"
    const val INTENT_KEY_RESULT_SET = "airqi2.intent.resultSet"

    const val INTENT_KEY_TEMP_VALUE = "airqi2.intent.TEMPValue"
    const val INTENT_KEY_HUMI_VALUE = "airqi2.intent.HUMIValue"
    const val INTENT_KEY_TVOC_VALUE = "airqi2.intent.TVOCValue"
    const val INTENT_KEY_CO2_VALUE = "airqi2.intent.CO2Value"
    const val INTENT_KEY_PM25_VALUE = "airqi2.intent.PM25Value"
    const val INTENT_KEY_BATTERY_LIFE = "airqi2.intent.BatteryLife"
    const val INTENT_KEY_PREHEAT_COUNT = "airqi2.intent.PreheatCountDown"
    const val INTENT_KEY_CREATED_TIME = "airqi2.intent.CreatedTime"

    const val INTENT_KEY_GET_HISTORY_COUNT = "airqi2.intent.GetHistoryCount"
    const val INTENT_KEY_LOADING_DATA = "airqi2.intent.NOWPROGRESSITEM"
    const val ACTION_SAVE_INSTANT_DATA = "airqi2.intent.SAVE_INSTANT_DATA"
    //20180130
    const val INTENT_KEY_PUMP_ON="airqi2.intent.PUMP_ON"
    const val INTENT_KEY_PUMP_OFF="airqi2.intent.PUMP_OFF"
}
