package microjet.com.airqi2.BlueTooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.List;
import java.util.UUID;
import microjet.com.airqi2.BroadReceiver.MainReceiver;

import microjet.com.airqi2.R;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
//藍牙連線Service
public class UartService extends Service {
    private final static String TAG = UartService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;


    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_DISCONNECTING = 3;

    public final static String ACTION_GATT_DISCONNECTED =
            "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_CONNECTING =
            "ACTION_GATT_CONNECTING";
    public final static String ACTION_GATT_CONNECTED =
            "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTING =
            "ACTION_GATT_DISCONNECTING";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.nordicsemi.nrfUART.EXTRA_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART =
            "com.nordicsemi.nrfUART.DEVICE_DOES_NOT_SUPPORT_UART";
    
    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    String intentAction;

    private MainReceiver mMainReceiver;
    private String shareStuff = "MACADDRESS";

    private Boolean mIsReceiverRegistered = false;
    private MyBroadcastReceiver mReceiver = null;

//    public UartService() { //建構式
//    }
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            switch (newState) {
                case BluetoothProfile.STATE_DISCONNECTED: {
                    intentAction = ACTION_GATT_DISCONNECTED;
                    //mConnectionState = STATE_DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
                    //broadcastUpdate(intentAction);
                    Intent mainIntent = new Intent("Main");
                    mainIntent.putExtra("status",intentAction);
                    sendBroadcast(mainIntent);
                    break;
                }
                case BluetoothProfile.STATE_CONNECTING: {
                    intentAction = ACTION_GATT_CONNECTING;
                    //mConnectionState = STATE_CONNECTING;
                    Log.i(TAG, "Disconnected from GATT server.");
                    //broadcastUpdate(intentAction);
                    break;
                }
                case BluetoothProfile.STATE_CONNECTED: {
                    intentAction = ACTION_GATT_CONNECTED;
                    //mConnectionState = STATE_CONNECTED;
                    //broadcastUpdate(intentAction);
                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt.discoverServices());

                    Intent mainIntent = new Intent("Main");
                    mainIntent.putExtra("status",intentAction);
                    sendBroadcast(mainIntent);
                    break;
                }
                case BluetoothProfile.STATE_DISCONNECTING: {
                    intentAction = ACTION_GATT_DISCONNECTING;
                    //mConnectionState = STATE_DISCONNECTING;
                    Log.i(TAG, "Disconnected from GATT server.");
                    //broadcastUpdate(intentAction);
                    break;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
            	Log.w(TAG, "mBluetoothGatt = " + mBluetoothGatt );
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt,
//                                         BluetoothGattCharacteristic characteristic,
//                                         int status) {
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//            }
//        }
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt,
//                                            BluetoothGattCharacteristic characteristic) {
//            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

//    private void broadcastUpdate(final String action,
//                                 final BluetoothGattCharacteristic characteristic) {
//        final Intent intent = new Intent(action);
//
//        // This is handling for the notification on TX Character of NUS service
//        if (TX_CHAR_UUID.equals(characteristic.getUuid())) {
//
//           // Log.d(TAG, String.format("Received TX: %d",characteristic.getValue() ));
//            intent.putExtra(EXTRA_DATA, characteristic.getValue());
//        } else {
//
//        }
//        //LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
//    }


    //BindService專用
    public class LocalBinder extends Binder {
       public UartService getServerInstance() {
            return UartService.this;
       }
    }

    IBinder mBinder = new LocalBinder();

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }


    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mMainReceiver = new MainReceiver();
        IntentFilter filter = new IntentFilter("Main");
        registerReceiver(mMainReceiver,filter);

        if (!mIsReceiverRegistered) {
            if (mReceiver == null)
                mReceiver = new MyBroadcastReceiver();
            registerReceiver(mReceiver, new IntentFilter("UartService"));
            mIsReceiverRegistered = true;
        }
    }


    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("UART ", "onStartCommand");
        //StartService後執行連線
        mBluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothDeviceAddress == null) {
            SharedPreferences share = getSharedPreferences(shareStuff, MODE_PRIVATE);
            mBluetoothDeviceAddress = share.getString("mac", "noValue");
        }

        if (mBluetoothDeviceAddress == "noValue") {
            mBluetoothDeviceAddress = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
        }else {
            Intent mainintent = new Intent("Main");
            mainintent.putExtra("status", "connect");
            mainintent.putExtra("mac", mBluetoothDeviceAddress);
            sendBroadcast(mainintent);
            //connect(mBluetoothDeviceAddress);
        }


        //Intent mainIntent = new Intent("Main");
        //sendBroadcast(mainIntent);

        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        Log.w(TAG, "mBluetoothGatt closed");
        mBluetoothDeviceAddress = null;
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *

    */
    
    /**
     * Enable Notification on TX characteristic
     *
     * @return 
     */
//    public void enableTXNotification()
//    {
//    	/*
//    	if (mBluetoothGatt == null) {
//    		showMessage("mBluetoothGatt null" + mBluetoothGatt);
//    		broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
//    		return;
//    	}
//    		*/
//    	BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
//    	if (RxService == null) {
//            showMessage("Rx service not found!");
//            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
//            return;
//        }
//    	BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
//        if (TxChar == null) {
//            showMessage("Tx charateristic not found!");
//            broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
//            return;
//        }
//        mBluetoothGatt.setCharacteristicNotification(TxChar,true);
//        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
//        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        mBluetoothGatt.writeDescriptor(descriptor);
//
//    }
    
    public void writeRXCharacteristic(byte[] value)
    {
    	BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
    	if (RxService == null) {
            showMessage("mBluetoothGatt null "+ mBluetoothGatt);
            showMessage("Rx service not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
    	BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(RX_CHAR_UUID);
        if (RxChar == null) {
            showMessage("Rx charateristic not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        RxChar.setValue(value);
    	boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
        Log.d(TAG, "write TXchar - status=" + status);
    }
    
    private void showMessage(String msg) {
        Log.e(TAG, msg);
    }
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }
    public int getmConnectionState() {
        return mConnectionState;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        intentAction = ACTION_GATT_DISCONNECTED;
        //mConnectionState = STATE_DISCONNECTED;
        Log.i(TAG, "Disconnected from GATT server.");
        //broadcastUpdate(intentAction);

        unregisterReceiver(mMainReceiver);
        unregisterReceiver(mReceiver);

        disconnect();
        close();
    }


    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra("status")) {
                case "disconnect":
                    disconnect();
                    break;
                case "connect":
                    connect(intent.getStringExtra("mac"));
                    break;
                case "message":
                    writeRXCharacteristic(CallingTranslate.INSTANCE.PumpOnCall(5000));
                    break;
            }
        }


    }

}
