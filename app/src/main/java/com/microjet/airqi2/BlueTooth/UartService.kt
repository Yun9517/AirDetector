package com.microjet.airqi2.BlueTooth

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Looper
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.*
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.NotificationObj
import com.microjet.airqi2.TvocNoseData
import com.microjet.airqi2.warringClass.MainNotification
import java.util.*

/**
 * Created by B00175 on 2018/3/19.
 *
 */
class UartService : Service() {

    companion object {
        private val STATE_DISCONNECTED = 0
        private val STATE_CONNECTING = 1
        private val STATE_CONNECTED = 2
        private val STATE_DISCONNECTING = 3
        val CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
        val FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb")
        val DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb")
        val RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e")
        val RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e")
        val TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e")
    }

    private val TAG = UartService::class.java.simpleName
    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mConnectionState = STATE_DISCONNECTED

    // private val bus = EventBus.getDefault()
    //private val bleEventObj = BleEvent()

    // 20180328 Add Location Request to Service
    private var longi: Float = 255f
    private var lati: Float = 255f

    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedClient: FusedLocationProviderClient
    private var reConnectcount = 0

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            super.onLocationResult(locationResult)

            val location = locationResult!!.lastLocation

            if (location != null) {
                TvocNoseData.lati = location.latitude.toFloat()
                TvocNoseData.longi = location.longitude.toFloat()

                Log.e("LOCATION", "Get Location from LocationCallback: ${TvocNoseData.lati}, ${TvocNoseData.longi}")
            } else {
                //TvocNoseData.lati = lati
                //TvocNoseData.longi = longi

                Log.e("LOCATION", "Get Location from LocationCallback: null (set default 255, 255)")
            }
        }
    }

    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            when (status) {
                BluetoothGatt.GATT_SUCCESS -> {
                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        intentAction = BroadcastActions.ACTION_GATT_CONNECTED
                        mConnectionState = STATE_CONNECTED
                        broadcastUpdate(intentAction)
                        Log.i(TAG, "Connected to GATT server.")
                        // Attempts to discover services after successful connection.
                        Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt?.discoverServices())
                        //bleEventObj.message = intentAction
                        reConnectcount = 0
                        //bus.post(bleEventObj)
                    } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                        intentAction = BroadcastActions.ACTION_GATT_DISCONNECTED
                        mConnectionState = STATE_DISCONNECTED
                        Log.i(TAG, "Disconnected from GATT server.")
                        broadcastUpdate(intentAction)
                        close()
                    }
                }
                8 -> { //BLE Out of Range
                    intentAction = BroadcastActions.ACTION_GATT_DISCONNECTED
                    mConnectionState = STATE_DISCONNECTED
                    close()
                    broadcastUpdate(intentAction)
                    if (reConnectcount < 10) {
                        reConnectcount++
                        connect(mBluetoothDeviceAddress)
                    }

                }
                19 -> { //Device Disconnect
                    intentAction = BroadcastActions.ACTION_GATT_DISCONNECTED
                    mConnectionState = STATE_DISCONNECTED
                    close()
                    broadcastUpdate(intentAction)
                }
                257 -> { //Too Many Connect
                    intentAction = BroadcastActions.ACTION_GATT_DISCONNECTED
                    mConnectionState = STATE_DISCONNECTED
                    close()
                    broadcastUpdate(intentAction)
                }
                else -> {
                    intentAction = BroadcastActions.ACTION_GATT_DISCONNECTED
                    mConnectionState = STATE_DISCONNECTED
                    close() // 防止出现status 133
                    broadcastUpdate(intentAction)
                    if (reConnectcount < 10) {
                        reConnectcount++
                        connect(mBluetoothDeviceAddress)
                    }

                    /*防止133
                    http://www.loverobots.cn/android-ble-connection-solution-bluetoothgatt-status-133.html
                    Log.d(TAG, "onConnectionStateChange received: " + status);
                    intentAction = BluetoothConstants.GATT_STATUS_133;
                    mBLEConnectionState = BluetoothConstants.BLE_STATE_DISCONNECTED;
                    close(); // 防止出现status 133
                    broadcastUpdate(intentAction);
                    connect(reConnectAddress);*/
                }
            }
            Log.d(TAG, "onConnectionStateChange reConnectcount: $reConnectcount")
            Log.d(TAG, "onConnectionStateChange received: $status")
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(BroadcastActions.ACTION_GATT_SERVICES_DISCOVERED)
                enableTXNotification(gatt)
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicRead(gatt: BluetoothGatt,
                                          characteristic: BluetoothGattCharacteristic,
                                          status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(BroadcastActions.ACTION_DATA_AVAILABLE, characteristic)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt,
                                             characteristic: BluetoothGattCharacteristic) {
            broadcastUpdate(BroadcastActions.ACTION_DATA_AVAILABLE, characteristic)
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        //sendBroadcast(intent)
    }

    private fun broadcastUpdate(action: String,
                                characteristic: BluetoothGattCharacteristic) {
        val intent = Intent(action)
        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (TX_CHAR_UUID == characteristic.uuid) {
            // For all other profiles, writes the data formatted in HEX.
            val data = characteristic.value
            if (data != null && data.isNotEmpty()) {
                val stringBuilder = StringBuilder(data.size)
                for (byteChar in data)
                    stringBuilder.append(String.format("%02X ", byteChar))
                //intent.putExtra(BroadcastActions.ACTION_EXTRA_DATA, String(data) + "\n" + stringBuilder.toString())
                Log.d("UARTRAWDATA", stringBuilder.toString())
            }
            intent.putExtra(BroadcastActions.ACTION_EXTRA_DATA, data)
        } else {
            Log.d("UART", "broadcastUpdate Error")
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    inner class LocalBinder : Binder() {
        internal val service: UartService
            get() = this@UartService
    }

    override fun onBind(intent: Intent): IBinder? {
        //registerReceiver(mServiceReceiver, makeServiceIntentFilter())
        initFuseLocationProviderClient()
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        //unregisterReceiver(mServiceReceiver)
        close()

        //stopForeground(true)

        fusedClient.removeLocationUpdates(locationCallback)
        return super.onUnbind(intent)
    }

    private val mBinder = LocalBinder()

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    fun initialize(): Boolean {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.")
                return false
            }
        }

        mBluetoothAdapter = mBluetoothManager?.adapter
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }

        return true
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun connect(address: String?): Boolean {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.")
            return false
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address == mBluetoothDeviceAddress
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.")
            return if (mBluetoothGatt!!.connect()) {
                mConnectionState = STATE_CONNECTING
                true
            } else {
                false
            }
        }

        val device = mBluetoothAdapter?.getRemoteDevice(address)
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.")
            return false
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback)
        Log.d(TAG, "Trying to create a new connection.")
        mBluetoothDeviceAddress = address
        mConnectionState = STATE_CONNECTING
        return true
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * `BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)`
     * callback.
     */
    fun disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt?.disconnect()
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    fun close() {
        if (mBluetoothGatt == null) {
            return
        }
        mBluetoothGatt?.close()
        mBluetoothGatt = null
    }

    /**
     * Request a read on a given `BluetoothGattCharacteristic`. The read result is reported
     * asynchronously through the `BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)`
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    fun readCharacteristic(characteristic: BluetoothGattCharacteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt?.readCharacteristic(characteristic)
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    fun setCharacteristicNotification(characteristic: BluetoothGattCharacteristic,
                                      enabled: Boolean) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return
        }
        mBluetoothGatt?.setCharacteristicNotification(characteristic, enabled)


        // This is specific to Heart Rate Measurement.
        if ("6e400003-b5a3-f393-e0a9-e50e24dcca9e".equals(characteristic.uuid)) {
            val descriptor = characteristic.getDescriptor(
                    UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            mBluetoothGatt?.writeDescriptor(descriptor)
        }

    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after `BluetoothGatt#discoverServices()` completes successfully.
     *
     * @return A `List` of supported services.
     */
    fun getSupportedGattServices(): List<BluetoothGattService>? {
        return if (mBluetoothGatt == null) null else mBluetoothGatt?.services

    }

    fun enableTXNotification(gatt: BluetoothGatt) {
        val rxService = gatt.getService(RX_SERVICE_UUID)
        if (rxService == null) {
            Toast.makeText(this, "Rx service not found!", Toast.LENGTH_SHORT).show()
            return
        }
        val txChar = rxService.getCharacteristic(TX_CHAR_UUID)
        if (txChar == null) {
            Toast.makeText(this, "Tx characteristic not found!", Toast.LENGTH_SHORT).show()
            return
        }

        mBluetoothGatt?.setCharacteristicNotification(txChar, true)
        val descriptor = txChar.getDescriptor(CCCD)
        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
        mBluetoothGatt?.writeDescriptor(descriptor)
    }


    fun writeRXCharacteristic(value: ByteArray) {
        //確定mBluetoothGatt存在才去取uuid
        if (mBluetoothGatt != null) {
            val rxService = mBluetoothGatt?.getService(RX_SERVICE_UUID)
                    ?: //showMessage("mBluetoothGatt null " + mBluetoothGatt)
                    return
            val rxChar = rxService.getCharacteristic(RX_CHAR_UUID)
                    ?: //showMessage("Rx charateristic not found!")
                    return
            rxChar.value = value
            val status = mBluetoothGatt?.writeCharacteristic(rxChar)
            Log.d(TAG, "write TXchar - status=$status")
        }
    }

    @SuppressLint("MissingPermission")
    fun initFuseLocationProviderClient() {
        fusedClient = LocationServices.getFusedLocationProviderClient(this)

        createLocationRequest()

        fusedClient.lastLocation.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val location = task.result

                if (location != null) {
                    TvocNoseData.lati = location.latitude.toFloat()
                    TvocNoseData.longi = location.longitude.toFloat()

                    Log.e("LOCATION", "Get Location from OnCompleteListener: ${TvocNoseData.lati}, ${TvocNoseData.longi}")
                } else {
                    TvocNoseData.lati = lati
                    TvocNoseData.longi = longi

                    Log.e("LOCATION", "Get Location from OnCompleteListener: null (set default 255, 255)")
                }
            }
        }

        fusedClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private fun createLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.interval = 50000          // original is 5000 milliseconds
        locationRequest.fastestInterval = 30000   // original is 2000 milliseconds
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    /*private fun makeServiceIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastActions.INTENT_KEY_ONLINE_LED_OFF)
        intentFilter.addAction(BroadcastActions.INTENT_KEY_ONLINE_LED_ON)
        intentFilter.addAction(BroadcastActions.INTENT_KEY_OFFLINE_LED_OFF)
        intentFilter.addAction(BroadcastActions.INTENT_KEY_OFFLINE_LED_ON)
        intentFilter.addAction(BroadcastActions.INTENT_KEY_PUMP_ON)
        intentFilter.addAction(BroadcastActions.INTENT_KEY_PUMP_OFF)
        // 2018/05/08
        intentFilter.addAction(BroadcastActions.INTENT_KEY_PM25_FAN_ON)
        intentFilter.addAction(BroadcastActions.INTENT_KEY_PM25_FAN_OFF)
        return intentFilter
    }

    private val mServiceReceiver = object : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            when (p1!!.action) {
                BroadcastActions.INTENT_KEY_ONLINE_LED_OFF -> {
                    writeRXCharacteristic(BLECallingTranslate.SetLedOn(false, MyApplication.isOfflineLedOn))

                    MyApplication.isOnlineLedOn = false
                }
                BroadcastActions.INTENT_KEY_ONLINE_LED_ON -> {
                    writeRXCharacteristic(BLECallingTranslate.SetLedOn(true, MyApplication.isOfflineLedOn))

                    MyApplication.isOnlineLedOn = true
                }
                BroadcastActions.INTENT_KEY_OFFLINE_LED_OFF -> {
                    writeRXCharacteristic(BLECallingTranslate.SetLedOn(MyApplication.isOnlineLedOn, false))

                    MyApplication.isOfflineLedOn = true
                }
                BroadcastActions.INTENT_KEY_OFFLINE_LED_ON -> {
                    writeRXCharacteristic(BLECallingTranslate.SetLedOn(MyApplication.isOnlineLedOn, true))

                    MyApplication.isOfflineLedOn = true
                }
                BroadcastActions.INTENT_KEY_PUMP_ON -> writeRXCharacteristic(BLECallingTranslate.PumpOnCall(65002))
                BroadcastActions.INTENT_KEY_PUMP_OFF -> writeRXCharacteristic(BLECallingTranslate.PumpOnCall(1))
            // 2018/05/08
                BroadcastActions.INTENT_KEY_PM25_FAN_ON -> writeRXCharacteristic(BLECallingTranslate.PM25FanCall(10))
            //BroadcastActions.INTENT_KEY_PM25_FAN_OFF -> writeRXCharacteristic(BLECallingTranslate.PM25FanCall(0))
            }
        }
    }*/

    /*override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.e(TAG, "onStartCommand() called...")

        val action = intent?.action

        if (action != null) {
            when (action) {
                "START_FOREGROUND" -> startToForeground()
                "STOP_FOREGROUND" -> stopForeground(true)
                "MANUAL_DISCONNECT" -> disconnect()
            }
        }

        return START_STICKY
    }*/

    /*private fun startToForeground() {
        val mainNotification = MainNotification(this@UartService)

        val notification = mainNotification.makeNotification()
        //notification.

        startForeground(NotificationObj.MAIN_NOTIFICATION_ID, notification)
        Log.e(TAG, "Set service to foreground = on.")
    }*/

    fun setLedOnOff(onlineLED: Boolean, offlineLED: Boolean) {
        writeRXCharacteristic(BLECallingTranslate.SetLedOn(onlineLED, offlineLED))
    }

    fun setPumpOnOff(isOn: Boolean) {
        if(isOn) {
            writeRXCharacteristic(BLECallingTranslate.PumpOnCall(65002))
        } else {
            writeRXCharacteristic(BLECallingTranslate.PumpOnCall(1))
        }
    }

    fun setFanOn() {
        writeRXCharacteristic(BLECallingTranslate.PM25FanCall(10))
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("UART", "Destroy")
    }
}