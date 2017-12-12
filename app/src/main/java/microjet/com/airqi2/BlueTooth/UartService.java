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
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

import microjet.com.airqi2.BroadReceiver.MainReceiver;
import microjet.com.airqi2.MainActivity;
import microjet.com.airqi2.myData;
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
    public static int mConnectionState;


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
            "EXTRA_DATA";
    public final static String DEVICE_DOES_NOT_SUPPORT_UART =
            "DEVICE_DOES_NOT_SUPPORT_UART";

    public static final UUID TX_POWER_UUID = UUID.fromString("00001804-0000-1000-8000-00805f9b34fb");
    public static final UUID TX_POWER_LEVEL_UUID = UUID.fromString("00002a07-0000-1000-8000-00805f9b34fb");
    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static final UUID FIRMWARE_REVISON_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static final UUID DIS_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID RX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
    public static final UUID TX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

    String intentAction;

    //private MainReceiver mMainReceiver;
    //private String shareStuff = "MACADDRESS";

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
                    mConnectionState = STATE_DISCONNECTED;
                    Log.i(TAG, "Disconnected from GATT server.");
                    //broadcastUpdate(intentAction);
                    Intent mainIntent = new Intent("Main");
                    mainIntent.putExtra("status", intentAction);
                    sendBroadcast(mainIntent);
                    break;
                }
                case BluetoothProfile.STATE_CONNECTING: {
                    intentAction = ACTION_GATT_CONNECTING;
                    mConnectionState = STATE_CONNECTING;
                    Log.i(TAG, "Disconnected from GATT server.");
                    //broadcastUpdate(intentAction);
                    Intent mainIntent = new Intent("Main");
                    mainIntent.putExtra("status", intentAction);
                    sendBroadcast(mainIntent);
                    break;
                }
                case BluetoothProfile.STATE_CONNECTED: {
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    //broadcastUpdate(intentAction);
                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery:" +
                            mBluetoothGatt.discoverServices());

                    Intent mainIntent = new Intent("Main");
                    mainIntent.putExtra("status", intentAction);
                    // ***** 2017/12/11 Drawer連線 會秀出 Mac Address ************************ //
                    mainIntent.putExtra("macAddress", mBluetoothDeviceAddress);
                    sendBroadcast(mainIntent);
                    break;
                }
                case BluetoothProfile.STATE_DISCONNECTING: {
                    intentAction = ACTION_GATT_DISCONNECTING;
                    mConnectionState = STATE_DISCONNECTING;
                    Log.i(TAG, "Disconnected from GATT server.");
                    //broadcastUpdate(intentAction);
                    break;
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "mBluetoothGatt = " + mBluetoothGatt);
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                sendToMainBroadcast(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                //Intent mainIntent = new Intent("Main");
                //mainIntent.putExtra("status","ACTION_DATA_AVAILABLE");
                //mainIntent.putExtra("txValue",characteristic.getValue());
                //sendBroadcast(mainIntent);
                sendCharToMainBroadcast(characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            //broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            sendCharToMainBroadcast(characteristic);
        }
    };

    //private void broadcastUpdate(final String action) {
    //    final Intent intent = new Intent(action);
    //    LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    //}

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
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
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
        /* 寫在mainfest的不用註冊
        mMainReceiver = new MainReceiver();
        IntentFilter filter = new IntentFilter("Main");
        registerReceiver(mMainReceiver,filter);
        */

        if (!mIsReceiverRegistered) {
            if (mReceiver == null) {
                mReceiver = new MyBroadcastReceiver();
                registerReceiver(mReceiver, new IntentFilter("UartService"));
                mIsReceiverRegistered = true;
                //LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver.get(), makeGattUpdateIntentFilter());
            }
        }
    }


    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        mBluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        // 會慢一點點連上去，但是可以保持一條主從的關係，如果拿掉會一直新增新的CLient Server到後期就會錯誤
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
        //自動連線當裝置在範圍內時
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        //enableTXNotification();
        return true;
    }

    //CallBack只能有一個實體的方法,NORDIC的作法，但如果用變數用 FINAL也是同樣意思
    protected BluetoothGattCallback getGattCallback() {
        return mGattCallback;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d("UART ", "onStartCommand");
        //StartService後執行連線
        mBluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
//        SharedPreferences share = getSharedPreferences("MACADDRESS", MODE_PRIVATE);
//        mBluetoothDeviceAddress = share.getString("mac", "noValue");

//        if (mBluetoothDeviceAddress != "noValue") {
//            //mBluetoothDeviceAddress = intent.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
//            //} else {
//            Intent mainintent = new Intent("Main");
//            mainintent.putExtra("status", "connect");
//            mainintent.putExtra("mac", mBluetoothDeviceAddress);
//            sendBroadcast(mainintent);
//            //connect(mBluetoothDeviceAddress);
//        }


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
        disableTXNotification();
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
    public void enableTXNotification() {
        /*
    	if (mBluetoothGatt == null) {
    		showMessage("mBluetoothGatt null" + mBluetoothGatt);
    		broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
    		return;
    	}
    		*/
        BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
        if (RxService == null) {
            showMessage("Rx service not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            sendToMainBroadcast(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            showMessage("Tx charateristic not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            sendToMainBroadcast(DEVICE_DOES_NOT_SUPPORT_UART);
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);

    }

    public void disableTXNotification() {
        if (mBluetoothGatt != null) {
            BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
            if (RxService == null) {
                showMessage("Rx service not found!");
                //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
                sendToMainBroadcast(DEVICE_DOES_NOT_SUPPORT_UART);
                return;
            }
            BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
            if (TxChar == null) {
                showMessage("Tx charateristic not found!");
                //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
                sendToMainBroadcast(DEVICE_DOES_NOT_SUPPORT_UART);
                return;
            }
            mBluetoothGatt.setCharacteristicNotification(TxChar, false);
        }
    }

    public void writeRXCharacteristic(byte[] value) {
        //確定mBluetoothGatt存在才去取uuid
        if (mBluetoothGatt != null) {
            BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
            if (RxService == null) {
                showMessage("mBluetoothGatt null " + mBluetoothGatt);
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
        //intentAction = ACTION_GATT_DISCONNECTED;
        mConnectionState = STATE_DISCONNECTED;
        Log.i(TAG, "Disconnected from GATT server.");
        //broadcastUpdate(intentAction);
        //sendToMainBroadcast(intentAction);
        //unregisterReceiver(mMainReceiver);
        disconnect();
        unregisterReceiver(mReceiver);
        close();
        super.onDestroy();
    }

    boolean CallFromConnect =false;
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
                case "close":
                    disconnect();
                    close();
                    break;
                case "message":
                    break;
                case "checkItems":
                    Log.d(TAG, "checkItems");
                    writeRXCharacteristic(CallingTranslate.INSTANCE.GetHistorySampleItems());
                    break;
                case "callItems":
                    Log.d(TAG, "callItems");
                    writeRXCharacteristic(CallingTranslate.INSTANCE.GetHistorySample(++NowItem));
                    break;
                case "setSampleRate":
                    int SampleTime = intent.getIntExtra("SampleTime", 1);
                    int[] param = {SampleTime, SampleTime*30, /*SampleTime*30-*/15, /*SampleTime*30-*/2, 1, 0, 0};
                    Log.d(TAG, "setSampleRate");
                    writeRXCharacteristic(CallingTranslate.INSTANCE.SetSampleRate(param));
                    break;
                case "getSampleRate":
                    Log.d(TAG, "getSampleRate");
                    if (intent.getStringExtra("callFromConnect").equals("yes"))
                        CallFromConnect=true;
                    else
                        CallFromConnect=false;
                    writeRXCharacteristic(CallingTranslate.INSTANCE.GetSampleRate());
                    break;
                case "callDeviceStartSample":
                    Date date = new Date();
                    SimpleDateFormat sdFormatY = new SimpleDateFormat("yy", Locale.TAIWAN);
                    SimpleDateFormat sdFormatM = new SimpleDateFormat("MM", Locale.TAIWAN);
                    SimpleDateFormat sdFormatD = new SimpleDateFormat("dd", Locale.TAIWAN);
                    SimpleDateFormat sdFormatH = new SimpleDateFormat("hh", Locale.TAIWAN);
                    SimpleDateFormat sdFormatm = new SimpleDateFormat("mm", Locale.TAIWAN);
                    SimpleDateFormat sdFormatS = new SimpleDateFormat("ss", Locale.TAIWAN);
                    String[] strY = {sdFormatY.format(date), sdFormatM.format(date), sdFormatD.format(date), sdFormatH.format(date), sdFormatm.format(date), sdFormatS.format(date)};
                    int[] param2 = {Integer.parseInt(strY[0]), Integer.parseInt(strY[1]), Integer.parseInt(strY[2]), Integer.parseInt(strY[3]), Integer.parseInt(strY[4]), Integer.parseInt(strY[5])};
                    Log.d(TAG, "callDeviceStartSample");
                    writeRXCharacteristic(CallingTranslate.INSTANCE.CallDeviceStartRecord(param2));
                    break;
                case ACTION_GATT_SERVICES_DISCOVERED:
                    enableTXNotification();
                    break;
                case ACTION_DATA_AVAILABLE:
                    dataAvaliable(intent);
                    break;
                case DEVICE_DOES_NOT_SUPPORT_UART:
                    showMessage("Device Does Not support UART. Disconnecting");
                    break;

            }
        }

    }

//    private static IntentFilter makeGattUpdateIntentFilter() {
//        final IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
//        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
//        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
//        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
//        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
//        return intentFilter;
//    }

//    private final ThreadLocal<BroadcastReceiver> UARTStatusChangeReceiver = new ThreadLocal<BroadcastReceiver>() {
//        @Override
//        protected BroadcastReceiver initialValue() {
//            return new BroadcastReceiver() {
//                public void onReceive(Context context, Intent intent) {
//                    String action = intent.getAction();
                    //if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                        //showMessage("UART:Connecting");
                        //showMessage(get(R.string.UART_Connecting));
                    //}
                    //if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                    //    //showMessage("UART:Disconnecting");
                    //    Bundle bundle = new Bundle();
                    //    bundle.putInt("Connect", 0);

                     //   bundle.putInt("mStatus", UART_PROFILE_DISCONNECTED);
                    //}
                    //if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                    //   enableTXNotification();
                    //}
                     //*********************/
//
//    if(action.equals(UartService.ACTION_DATA_AVAILABLE))
//            {//資料進來
//        final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
//        // if (txValue.length == 4 && txValue[0] == (byte) 0xEA) {
//        //     while (ReturnQ.size()!=0) {
//        //        ReturnQ.poll();
//        //     }
//        switch (txValue[2]) {
//            case (byte) 0xE0:
//                Log.d("UART feeback", "ok");
//                return;
//            case (byte) 0xE1:
//                Log.d("UART feedback", "Couldn't write in device");
//                return;
//            case (byte) 0xE2:
//                Log.d("UART feedback", "Temperature sensor fail");
//                return;
//            case (byte) 0xE3:
//                Log.d("UART feedback", "TVOC sensor fail");
//                return;
//            case (byte) 0xE4:
//                Log.d("UART feedback", "Pump power fail");
//                return;
//            case (byte) 0xE5:
//                if (NowItem > 0)
//                    //************** 2017/12/04 "尊重原創 留原始文字 方便搜尋" 更改成從String撈文字資料 *****************************//
//                    //Toast.makeText(getApplicationContext(),"讀取第"+Integer.toString(NowItem)+"筆失敗",Toast.LENGTH_LONG).show();
//                    Toast.makeText(getApplicationContext(), getText(R.string.Number_of_data) + Integer.toString(NowItem) + getText(R.string.Loading_fail), Toast.LENGTH_LONG).show();
//                //***************************************************************************************************************//
//                Log.d("UART feedback", "Invalid value");
//                return;
//            case (byte) 0xE6:
//                Log.d("UART feedback", "Unknown command");
//                return;
//            case (byte) 0xE7:
//                Log.d("UART feedback", "Waiting timeout");
//                return;
//            case (byte) 0xE8:
//                Log.d("UART feedback", "Checksum error");
//                return;
//            case (byte) 0xB1:
//                Log.d(TAG, "cmd:0xB1 feedback");
//                break;
//            case (byte) 0xB2:
//                Log.d(TAG, "cmd:0xB2 feedback");
//                break;
//            case (byte) 0xB4:
//                Log.d(TAG, "cmd:0xB4 feedback");
//                break;
//            case (byte) 0xB5:
//                Log.d(TAG, "cmd:0xB5 feedback");
//                break;
//        }
//
//        if (getErrorTime() >= 3) {
//            ResetErrorTime();
//        }
//        if (!checkCheckSum(txValue)) {
//            setErrorTime();
//        } else {
//
//            //   final CallingTranslate CT = new CallingTranslate();
//            ArrayList<String> RString;
//            String val;
//            switch (txValue[2]) {
//                case (byte) 0xB1:
//                    RString = CallingTranslate.INSTANCE.ParserGetInfo(txValue);
//                    break;
//                case (byte) 0xB2:
//                    RString = CallingTranslate.INSTANCE.ParserGetSampleRate(txValue);
//                    setSampleRateTime(Integer.parseInt(RString.get(0)));
//                    writeRXCharacteristic(CallingTranslate.INSTANCE.GetHistorySampleItems());
//                    break;
//                case (byte) 0xB4:
//                    RString = CallingTranslate.INSTANCE.ParserGetHistorySampleItems(txValue);
//                    myDeviceData.clear();
//                    setMaxItems(Integer.parseInt(RString.get(0)));//MAX Items
//                    //************** 2017/12/03 "尊重原創 留原始文字 方便搜尋" 更改成從String撈文字資料(中文) *************************//
//                    //Toast.makeText(getApplicationContext(),"共有資料"+Integer.toString(getMaxItems())+"筆",Toast.LENGTH_LONG).show();
//                    //Toast.makeText(getApplicationContext(),"讀取資料中請稍候",Toast.LENGTH_LONG).show();
//                    Toast.makeText(getApplicationContext(), getText(R.string.Total_Data) + Integer.toString(getMaxItems()) + getText(R.string.Total_Data_Finish), Toast.LENGTH_LONG).show();
//                    Toast.makeText(getApplicationContext(), getText(R.string.Loading_Data), Toast.LENGTH_LONG).show();
//                    //setCorrectTime(Integer.parseInt(RString.get(8)));
//                    setCorrectTime(0);
//                    //取得當前時間
//                    Date date = new Date();
//                    setMyDate(date);
//                    // String time=getDateTime();
//                    // setGetDataTime(time);
//                    Log.d("UART", "getItem 1");
//                    if (getMaxItems() != 0) {
//                        NowItem = 0;
//                        counter = 0;
//                        writeRXCharacteristic(CallingTranslate.INSTANCE.GetHistorySample(++NowItem));
//                    }
//                    //   setCorrectTime(Integer.parseInt(RString.get(j)));
//                    break;
//                case (byte) 0xB5:
//                    RString = CallingTranslate.INSTANCE.ParserGetHistorySampleItem(txValue);
//                    //getDateTime(getMyDate().getTime()-getCorrectTime()*60*1000);
//                    if (Integer.parseInt(RString.get(0)) == NowItem) {//將資料存入MyData
//                        //   long tt= getMyDate().getTime();//-getSampleRateTime()*counter*60*1000-getCorrectTime()*60*1000;
//                        //   long yy= getSampleRateTime()*counter*60*1000;
//                        //   long zz=getCorrectTime()*60*1000;
//                        Log.d("UART:ITEM ", Integer.toString(NowItem));
//                        myDeviceData.add(new myData(RString.get(1), RString.get(2), RString.get(3), RString.get(4), getDateTime(getMyDate().getTime() - getSampleRateTime() * counter * 60 * 1000 - getCorrectTime() * 60 * 1000)));
//                        if (NowItem >= getMaxItems()) {
//                            NowItem = 0;
//                            //************** 2017/12/03 "尊重原創 留原始文字 方便搜尋" 更改成從String撈中英文字資料 ***************************//
//                            //Toast.makeText(getApplicationContext(),"讀取完成",Toast.LENGTH_LONG).show();
//                            //*****************************************************************************************************************//
//                            Toast.makeText(getApplicationContext(), getText(R.string.Loading_Completely), Toast.LENGTH_LONG).show();
//                            Intent mainIntent = new Intent("Main");
//                            mainIntent.putExtra("status", "B5");
//                            Bundle data = new Bundle();
//                            data.putParcelableArrayList("resultSet", myDeviceData);
//                            mainIntent.putExtra("result", data);
//                            sendBroadcast(mainIntent);
//                        } else {
//                            NowItem++;
//                            counter++;
//                            Handler mHandler = new Handler();
//                            mHandler.post(runnable);
//                        }
//                    } else {//重送
//                        Handler mHandler = new Handler();
//                        mHandler.post(runnable);
//                    }
//                    break;
//                case (byte) 0xB6:
//                    RString = CallingTranslate.INSTANCE.ParserGetAutoSendData(txValue);
//                    Intent mainIntent = new Intent("Main");
//                    mainIntent.putExtra("status", "B6");
//                    mainIntent.putExtra("TVOCValue", RString.get(2));
//                    mainIntent.putExtra("BatteryLife", RString.get(4));
//                    sendBroadcast(mainIntent);
//                    break;
//            }
//        }
//        if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
//            showMessage("Device support UART. Disconnecting");
//        }
//    }

    public boolean CompareDufaultValue(ArrayList<String> RString){
        int sampleRate,sensorOntime,timeToGetSample,pumpOnTime,pumpingTime;
        sampleRate=Integer.parseInt(RString.get(0));
        sensorOntime=Integer.parseInt(RString.get(1));
        timeToGetSample=Integer.parseInt(RString.get(2));
        pumpOnTime=Integer.parseInt(RString.get(3));
        pumpingTime=Integer.parseInt(RString.get(4));
        if ( (sampleRate!=1&& sampleRate!=30&&sampleRate!=40&&sampleRate!=60)|| sensorOntime!=30||timeToGetSample!=29|| pumpOnTime!=28||pumpingTime!=1) {//設定新參數設為預設值
            return true;//參數不正確須重置
        }
        else {
            return false;
        }
    }
    public void dataAvaliable(Intent intent) {
        final byte[] txValue = intent.getByteArrayExtra(EXTRA_DATA);
        switch (txValue[2]) {
            case (byte) 0xE0:
                Log.d("UART feeback", "ok");
                return;
            case (byte) 0xE1:
                Log.d("UART feedback", "Couldn't write in device");
                return;
            case (byte) 0xE2:
                Log.d("UART feedback", "Temperature sensor fail");
                return;
            case (byte) 0xE3:
                Log.d("UART feedback", "TVOC sensor fail");
                return;
            case (byte) 0xE4:
                Log.d("UART feedback", "Pump power fail");
                return;
            case (byte) 0xE5:
                if (NowItem > 0)
                    //************** 2017/12/04 "尊重原創 留原始文字 方便搜尋" 更改成從String撈文字資料 *****************************//
                    //Toast.makeText(getApplicationContext(),"讀取第"+Integer.toString(NowItem)+"筆失敗",Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), getText(R.string.Number_of_data) + Integer.toString(NowItem) + getText(R.string.Loading_fail), Toast.LENGTH_LONG).show();
                //***************************************************************************************************************//
                Log.d("UART feedback", "Invalid value");
                return;
            case (byte) 0xE6:
                Log.d("UART feedback", "Unknown command");
                return;
            case (byte) 0xE7:
                Log.d("UART feedback", "Waiting timeout");
                return;
            case (byte) 0xE8:
                Log.d("UART feedback", "Checksum error");
                return;
            case (byte) 0xB1:
                Log.d(TAG, "cmd:0xB1 feedback");
                break;
            case (byte) 0xB2:
                Log.d(TAG, "cmd:0xB2 feedback");
                break;
            case (byte) 0xB4:
                Log.d(TAG, "cmd:0xB4 feedback");
                break;
            case (byte) 0xB5:
                Log.d(TAG, "cmd:0xB5 feedback");
                break;
        }

        if (getErrorTime() >= 3) {
            ResetErrorTime();
        }

        if (!checkCheckSum(txValue)) {
            setErrorTime();
        } else {

            //   final CallingTranslate CT = new CallingTranslate();
            ArrayList<String> RString;
            String val;
            Intent mainIntent = new Intent("Main");
            switch (txValue[2]) {
                case (byte) 0xB0:
                    RString = CallingTranslate.INSTANCE.GetAllSensor(txValue);
                    mainIntent.putExtra("status", "B0");
                    mainIntent.putExtra("TEMPValue", RString.get(0));
                    mainIntent.putExtra("HUMIValue", RString.get(1));
                    mainIntent.putExtra("TVOCValue", RString.get(2));
                    mainIntent.putExtra("eCO2Value", RString.get(3));
                    //mainIntent.putExtra("PM25", RString.get(4));
                    mainIntent.putExtra("BatteryLife", RString.get(5));
                    mainIntent.putExtra("PreheatCountDown", RString.get(6));
                    sendBroadcast(mainIntent);
                    break;
                case (byte) 0xB1:
                    RString = CallingTranslate.INSTANCE.ParserGetInfo(txValue);
                    break;
                case (byte) 0xB2:
                    RString = CallingTranslate.INSTANCE.ParserGetSampleRate(txValue);
                    setSampleRateTime(Integer.parseInt(RString.get(0)));
                  //  int number=Integer.parseInt(RString.get(0));

                    writeRXCharacteristic(CallingTranslate.INSTANCE.GetHistorySampleItems());

                    break;
                case (byte) 0xB4:
                    RString = CallingTranslate.INSTANCE.ParserGetHistorySampleItems(txValue);
                    myDeviceData.clear();
                    setMaxItems(Integer.parseInt(RString.get(0)));//MAX Items
                    //************** 2017/12/03 "尊重原創 留原始文字 方便搜尋" 更改成從String撈文字資料(中文) *************************//
                    //Toast.makeText(getApplicationContext(),"共有資料"+Integer.toString(getMaxItems())+"筆",Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(),"讀取資料中請稍候",Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), getText(R.string.Total_Data) + Integer.toString(getMaxItems()) + getText(R.string.Total_Data_Finish), Toast.LENGTH_LONG).show();
                    //setCorrectTime(Integer.parseInt(RString.get(8)));
                    setCorrectTime(0);
                    //取得當前時間
                    Date date = new Date();
                    setMyDate(date);
                    // String time=getDateTime();
                    // setGetDataTime(time);
                    Log.d("UART", "total item "+Integer.toString(getMaxItems()));
                   // Log.d("UART", "getItem 1");
                    if (getMaxItems() != 0) {
                        Toast.makeText(getApplicationContext(), getText(R.string.Loading_Data), Toast.LENGTH_LONG).show();
                        Log.d("UART", "getItem 1");
                        NowItem = 0;
                        counter = 0;
                        writeRXCharacteristic(CallingTranslate.INSTANCE.GetHistorySample(++NowItem));
                    }
                    //   setCorrectTime(Integer.parseInt(RString.get(j)));
                    break;
                case (byte) 0xB5:
                    RString = CallingTranslate.INSTANCE.ParserGetHistorySampleItem(txValue);
                    //getDateTime(getMyDate().getTime()-getCorrectTime()*60*1000);
                    if (Integer.parseInt(RString.get(0)) == NowItem) {//將資料存入MyData
                        //   long tt= getMyDate().getTime();//-getSampleRateTime()*counter*60*1000-getCorrectTime()*60*1000;
                        //   long yy= getSampleRateTime()*counter*60*1000;
                        //   long zz=getCorrectTime()*60*1000;
                        Log.d("UART:ITEM ", Integer.toString(NowItem));
                        myDeviceData.add(new myData(RString.get(1), RString.get(2), RString.get(3), RString.get(4), getDateTime(getMyDate().getTime() - getSampleRateUnit() * counter * 30 * 1000 - getCorrectTime() * 30 * 1000)));
                        if (NowItem >= getMaxItems()) {
                            NowItem = 0;
                            //************** 2017/12/03 "尊重原創 留原始文字 方便搜尋" 更改成從String撈中英文字資料 ***************************//
                            //Toast.makeText(getApplicationContext(),"讀取完成",Toast.LENGTH_LONG).show();
                            //*****************************************************************************************************************//
                            Toast.makeText(getApplicationContext(), getText(R.string.Loading_Completely), Toast.LENGTH_LONG).show();
                            mainIntent.putExtra("status", "B5");
                            Bundle data = new Bundle();
                            data.putParcelableArrayList("resultSet", myDeviceData);
                            mainIntent.putExtra("result", data);
                            sendBroadcast(mainIntent);
                        } else {
                            NowItem++;
                            counter++;
                            Handler mHandler = new Handler();
                            mHandler.post(runnable);
                        }
                    } else {//重送
                        Handler mHandler = new Handler();
                        mHandler.post(runnable);
                    }
                    break;
                case (byte) 0xB6:
                    RString = CallingTranslate.INSTANCE.ParserGetAutoSendData(txValue);
                    mainIntent.putExtra("status", "B6");
                    mainIntent.putExtra("TEMPValue", RString.get(0));
                    mainIntent.putExtra("HUMIValue", RString.get(1));
                    mainIntent.putExtra("TVOCValue", RString.get(2));
                    mainIntent.putExtra("eCO2Value", RString.get(3));
                    //mainIntent.putExtra("PM25", RString.get(4));
                    mainIntent.putExtra("BatteryLife", RString.get(5));
                    mainIntent.putExtra("flag",RString.get(6));
                    sendBroadcast(mainIntent);
                    break;
            }
        }
    }

    private void sendToMainBroadcast(String value) {
        Intent mainIntent = new Intent("Main");
        mainIntent.putExtra("status", value);
        sendBroadcast(mainIntent);
    }

    private void sendCharToMainBroadcast(BluetoothGattCharacteristic characteristic) {
        if (TX_CHAR_UUID.equals(characteristic.getUuid())) {
            Intent mainIntent = new Intent("Main");
            mainIntent.putExtra("status", ACTION_DATA_AVAILABLE);
            mainIntent.putExtra("txValue", characteristic.getValue());
            sendBroadcast(mainIntent);
        }
    }

    private boolean checkCheckSum(byte[] InputValue) {
        //   InputValue.length;
        int j = InputValue.length;
        byte CheckSum = (byte) 0x00;
        byte max = (byte) 0xFF;
        for (int i = 0; i < j; i++)
            CheckSum += InputValue[i];
        return CheckSum == max;
    }

    ArrayList<myData> myDeviceData = new ArrayList<myData>();

    final Runnable runnable = new Runnable() {
        public void run() {
            writeRXCharacteristic(CallingTranslate.INSTANCE.GetHistorySample(NowItem));
            // TODO Auto-generated method stub
            // 需要背景作的事
        }
    };

    private int NowItem = 0;

    private int MaxItems = 0;

    void setMaxItems(int input) {
        MaxItems = input;
    }

    int getMaxItems() {
        return MaxItems;
    }

    private int CorrectTime = 0;

    void setCorrectTime(int input) {
        CorrectTime = input;
    }

    int getCorrectTime() {
        return CorrectTime;
    }

    private int SampleRateTime = 0;

    void setSampleRateTime(int input) {
        SampleRateTime = input;
    }

    int getSampleRateUnit() {
        return SampleRateTime;
    }

    private int counter = 0;
    private int ErrorTime;

    private int getErrorTime() {
        return ErrorTime;
    }

    private void setErrorTime() {
        ErrorTime += 1;
    }

    private void ResetErrorTime() {
        ErrorTime = 0;
    }

    public Date getMyDate() {
        return myDate;
    }

    public void setMyDate(Date myDate) {
        this.myDate = myDate;
    }

    private Date myDate;

    private String getDateTime(long longtime) {
        SimpleDateFormat sdFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss", Locale.TAIWAN);
        Date date = new Date();
        date.setTime(longtime);
        return sdFormat.format(date);
    }

    private String DataTime;

    public String getGetDataTime() {
        return DataTime;
    }


    class Data {
        Data(String Temp, String Humidity, String Tvoc, String CO2, String Time) {
            Temperatur_Data = Temp;
            Humidy_Data = Humidity;
            TVOC_Data = Tvoc;
            CO2_Data = CO2;
            time = Time;
        }
        String Temperatur_Data;
        String Humidy_Data;
        String TVOC_Data;
        String CO2_Data;
        String time;
    }

    private class GetData extends AsyncTask<String, Integer, Data> {

        @Override
        protected void onPreExecute() {
            //執行前 設定可以在這邊設定
            super.onPreExecute();
        }

        @Override
        protected Data doInBackground(String... params) {
            //執行中 在背景做事情

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            //執行中 可以在這邊告知使用者進度
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Data bitmap) {
            //執行後 完成背景任務
            super.onPostExecute(bitmap);

        }
    }
}
