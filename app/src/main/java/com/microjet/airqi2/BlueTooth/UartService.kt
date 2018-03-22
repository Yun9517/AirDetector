package com.microjet.airqi2.BlueTooth

import android.app.Service
import android.bluetooth.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.content.LocalBroadcastManager
import android.util.Log
import android.widget.Toast
import com.microjet.airqi2.AsmDataModel
import com.microjet.airqi2.BleEvent
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.R
import io.realm.Realm
import org.greenrobot.eventbus.EventBus
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by B00175 on 2018/3/19.
 */
class UartService: Service() {

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

    private val TAG = UartService::class.java!!.simpleName
    private var mBluetoothManager: BluetoothManager? = null
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mBluetoothDeviceAddress: String? = null
    private var mBluetoothGatt: BluetoothGatt? = null
    private var mConnectionState = STATE_DISCONNECTED

    val ACTION_GATT_SERVICES_DISCOVERED = "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED"
    val ACTION_DATA_AVAILABLE = "com.example.bluetooth.le.ACTION_DATA_AVAILABLE"
    val EXTRA_DATA = "com.example.bluetooth.le.EXTRA_DATA"

    private val bus = EventBus.getDefault()
    private val bleEventObj = BleEvent()

    private val mGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            val intentAction: String
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = BroadcastActions.ACTION_GATT_CONNECTED
                mConnectionState = STATE_CONNECTED
                broadcastUpdate(intentAction)
                Log.i(TAG, "Connected to GATT server.")
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" + mBluetoothGatt?.discoverServices())
                //bleEventObj.message = intentAction
                //bus.post(bleEventObj)
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = BroadcastActions.ACTION_GATT_DISCONNECTED
                mConnectionState = STATE_DISCONNECTED
                Log.i(TAG, "Disconnected from GATT server.")
                broadcastUpdate(intentAction)
                close()
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(BroadcastActions.ACTION_GATT_SERVICES_DISCOVERED)
                enableTXNotification(gatt)
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status)
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
            if (data != null && data.size > 0) {
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
        return mBinder
    }

    override fun onUnbind(intent: Intent): Boolean {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close()
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

        mBluetoothAdapter = mBluetoothManager?.getAdapter()
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
            if (mBluetoothGatt!!.connect()) {
                mConnectionState = STATE_CONNECTING
                return true
            } else {
                return false
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
        return if (mBluetoothGatt == null) null else mBluetoothGatt?.getServices()

    }

    fun enableTXNotification(gatt: BluetoothGatt) {
        val rxService = gatt?.getService(RX_SERVICE_UUID)
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

    /*
    fun writeRXCharacteristic(value: ByteArray) {
        //確定mBluetoothGatt存在才去取uuid
        if (mBluetoothGatt != null) {
            val RxService = mBluetoothGatt?.getService(RX_SERVICE_UUID)
            if (RxService == null) {
                showMessage("mBluetoothGatt null " + mBluetoothGatt)
                return
            }
            val RxChar = RxService.getCharacteristic(RX_CHAR_UUID)
            if (RxChar == null) {
                showMessage("Rx charateristic not found!")
                return
            }
            RxChar.value = value
            val status = mBluetoothGatt?.writeCharacteristic(RxChar)
            Log.d(TAG, "write TXchar - status=" + status)
        }
    }


    fun saveToDB(tx: ByteArray) {
        var RString = CallingTranslate.ParserGetHistorySampleItems(tx)
        val maxItem = (Integer.parseInt(RString.get(0)))

        //setMaxItems(Integer.parseInt(RString.get(0)))//MAX Items
        //setCorrectTime(0)
        Log.d("0xB4", RString.toString())
        val b4correctTime = RString.get(3)
        //取得當前時間
        // String time=getDateTime();
        // setGetDataTime(time);
        Log.d("UART", "total item " + Integer.toString(maxItem))
        // Log.d("UART", "getItem 1");
        //如果目前Index大於0
        if (maxItem > 0) {
            //mainIntent.putExtra("status", "MAXPROGRESSITEM");
            //mainIntent.putExtra("MAXPROGRESSITEM", Integer.toString(getMaxItems()));
            //sendBroadcast(mainIntent);
            if (Build.BRAND != "OPPO") {
                Toast.makeText(applicationContext, getText(R.string.Loading_Data), Toast.LENGTH_SHORT).show()
            }
            //Utils.INSTANCE.toastMakeTextAndShow(getApplicationContext(), getString(R.string.Loading_Data), Toast.LENGTH_SHORT);
            Log.d("UART", "getItem 1")
            var nowItem = 1
            var counter = 0
            //將時間秒數寫入設定為 00  或  30
            timeSetNowToThirty(b4correctTime)
            //Realm 資料庫
            val realm = Realm.getDefaultInstance()
            //將資料庫最大時間與現在時間換算成Count
            var maxCreatedTime = realm.where(AsmDataModel::class.java).max("Created_time")
            if (maxCreatedTime == null) {
                maxCreatedTime = Calendar.getInstance().timeInMillis - TimeUnit.DAYS.toMillis(2)
            }
            if (maxCreatedTime != null) {
                //Long lastRowSaveTime = realm.where(AsmDataModel.class).equalTo("Created_time", maxCreatedTime.longValue())
                //.findAll().first().getCreated_time().longValue();
                val nowTime = getMyDate().getTime()
                Log.d("0xB4countLast", Date(maxCreatedTime.toLong()).toString())
                Log.d("0xB4countLast", Date(nowTime).toString())
                val countForItemTime = nowTime - maxCreatedTime.toLong()
                Log.d("0xB4countItemTime", countForItemTime!!.toString())
                countForItem = Math.min((countForItemTime!! / (60L * 1000L)).toInt(), getMaxItems())
                //當小於0的時候讓它等於0
                if (countForItem < 0) {
                    countForItem = 0
                }
                Log.d("0xB4countItem", java.lang.Long.toString(countForItem.toLong()))
                if (Build.BRAND != "OPPO") {
                    Toast.makeText(applicationContext, getText(R.string.Total_Data).toString() + java.lang.Long.toString(countForItem.toLong()) + getText(R.string.Total_Data_Finish), Toast.LENGTH_SHORT).show()
                }
            }
            if (countForItem >= 1) {
                NowItem = countForItem
                writeRXCharacteristic(CallingTranslate.GetHistorySample(NowItem))
                downloading = true
                //downloadComplete = false;
            } else {
                downloading = false
                //downloadComplete = true;
                if (Build.BRAND != "OPPO") {
                    Toast.makeText(applicationContext, getText(R.string.Loading_Completely), Toast.LENGTH_SHORT).show()
                }
                //Utils.INSTANCE.toastMakeTextAndShow(getApplicationContext(), getString(R.string.Loading_Completely), Toast.LENGTH_SHORT);
            }
            mainIntent.putExtra("status", BroadcastActions.INTENT_KEY_GET_HISTORY_COUNT)
            mainIntent.putExtra(BroadcastActions.INTENT_KEY_GET_HISTORY_COUNT, Integer.toString(countForItem))
            sendBroadcast(mainIntent)

        } else if (getMaxItems() <= 0) {
            //0xB6裡的Log會用到
            timeSetNowToThirty(b4correctTime)
            //downloadComplete = true;
            downloading = false
        }
    }

    private fun timeSetNowToThirty(afterB6Sec: String): Date {
        //取得當前時間
        //將時間秒數寫入設定為 00  或  30
        //Long dateSecMil = new Date().getTime();
        val dateSecMil = Calendar.getInstance().timeInMillis + Calendar.getInstance().timeZone.rawOffset - java.lang.Long.parseLong(afterB6Sec) * 1000
        val dateSecChange = dateSecMil / 1000 / 60 * (1000 * 60)
        //Log.d("0xB4",dateSecChange.toString());
        val date = Date(dateSecChange)
        Log.d("timeSetNowToThirty", date.toString())
        setMyDate(date)
    }
    */
}