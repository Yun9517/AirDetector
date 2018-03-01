package com.microjet.airqi2.BlueTooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

import com.microjet.airqi2.AsmDataModel;
import com.microjet.airqi2.Definition.BroadcastActions;
import com.microjet.airqi2.Definition.BroadcastIntents;
import com.microjet.airqi2.Definition.SavePreferences;
import com.microjet.airqi2.MainActivity;
import com.microjet.airqi2.myData;
import com.microjet.airqi2.R;
import com.microjet.airqi2.NotificationHelper;

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
    private static BroadcastReceiver mMainReceiver;


    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private static final int STATE_DISCONNECTING = 3;

    //public final static String ACTION_GATT_SERVICES_DISCOVERED =
            //"ACTION_GATT_SERVICES_DISCOVERED";
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

    //private PrimaryReceiver mPrimaryReceiver;
    //private String shareStuff = "MACADDRESS";

    private Boolean mIsReceiverRegistered = false;
    private MyBroadcastReceiver mReceiver = null;
    private Realm realm;
    private Integer countForItem = 0;


    //20180102   Andy
    private int countsound220 = 0;
    private int countsound660 = 0;
    //20180122
    private int countsound2200 = 0;
    private int countsound5500 = 0;
    private int countsound20000 = 0;


    private int countsound800 = 0;
    private int countsound1500 = 0;
    //20180122
    private SoundPool soundPool= null;
    private Vibrator mVibrator = null;
    private int alertId = 0;
    private int alertId2 = 0;
    //private MediaPlayer mp = null;
    private HashMap<Integer, Integer> soundsMap;
    int SOUND1 = 1;
    int SOUND2 = 2;
    //20180122

    int SOUND5 = 5;
    int SOUND4 = 4;
    int SOUND3 = 3;
    private Boolean showWithVibrate = false;
    private SharedPreferences mPreference = null;
    //20180103   Andy
    private final int NOTIFICATION_ID = 0xa01;
    private final int REQUEST_CODE = 0xb01;


    public static Activity nowActivity = null;

    private boolean downloading = false;
    private boolean downloadComplete = false;
    private int dataNotSaved = 0;
    private ArrayList<HashMap> arrB6 = new ArrayList();

    private NotificationManager notificationManager = null;
    private int counterB5 = 1;
    private int callbackErrorTimes = 0;
    private Handler errHandler = new Handler();


    // 20180226 add
    private Boolean isFirstB0 = true;

    //    public UartService() { //建構式
//    }
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                switch (newState) {
                    case BluetoothProfile.STATE_DISCONNECTED: {
                        intentAction = BroadcastActions.ACTION_GATT_DISCONNECTED;
                        Log.i(TAG, "Disconnected from GATT server.");
                        //broadcastUpdate(intentAction);
                        Intent mainIntent = new Intent(BroadcastIntents.PRIMARY);
                        mainIntent.putExtra("status", intentAction);
                        sendBroadcast(mainIntent);
                        mBluetoothAdapter = mBluetoothManager.getAdapter();
                        mConnectionState = STATE_DISCONNECTED;
                        dataNotSaved = 0;
                        arrB6.clear();

                        isFirstB0 = true;

                        if (!mBluetoothAdapter.isEnabled()) {
                            close();
                        } else {
                            disconnect();
                            close();
                        }
                        break;
                    }
                    case BluetoothProfile.STATE_CONNECTING: {
                        intentAction = BroadcastActions.ACTION_GATT_CONNECTING;
                        mConnectionState = STATE_CONNECTING;
                        Log.i(TAG, "Disconnected from GATT server.");
                        //broadcastUpdate(intentAction);
                        Intent mainIntent = new Intent(BroadcastIntents.PRIMARY);
                        mainIntent.putExtra("status", intentAction);
                        sendBroadcast(mainIntent);
                        break;
                    }
                    case BluetoothProfile.STATE_CONNECTED: {
                        intentAction = BroadcastActions.ACTION_GATT_CONNECTED;
                        mConnectionState = STATE_CONNECTED;
                        //broadcastUpdate(intentAction);
                        Log.i(TAG, "Connected to GATT server.");
                        Log.i(TAG, "Attempting to start service discovery:" +
                                mBluetoothGatt.discoverServices());

                        Intent mainIntent = new Intent(BroadcastIntents.PRIMARY);
                        mainIntent.putExtra("status", intentAction);
                        // ***** 2017/12/11 Drawer連線 會秀出 Mac Address ************************ //
                        mainIntent.putExtra("macAddress", mBluetoothDeviceAddress);
                        sendBroadcast(mainIntent);
                        callbackErrorTimes = 0;
                        break;
                    }
                    case BluetoothProfile.STATE_DISCONNECTING: {
                        intentAction = BroadcastActions.ACTION_GATT_DISCONNECTING;
                        mConnectionState = STATE_DISCONNECTING;

                        Log.i(TAG, "Disconnected from GATT server.");
                        //broadcastUpdate(intentAction);
                        break;
                    }
                }
            } else if (status == BluetoothGatt.GATT_CONNECTION_CONGESTED) {
                intentAction = BroadcastActions.ACTION_GATT_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                //broadcastUpdate(intentAction);
                Intent mainIntent = new Intent(BroadcastIntents.PRIMARY);
                mainIntent.putExtra("status", intentAction);
                sendBroadcast(mainIntent);
                mBluetoothAdapter = mBluetoothManager.getAdapter();
                mConnectionState = STATE_DISCONNECTED;
                dataNotSaved = 0;
                arrB6.clear();
                disconnect();
                close();
            } else if (status == 19){
                intentAction = BroadcastActions.ACTION_GATT_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                //broadcastUpdate(intentAction);
                Intent mainIntent = new Intent(BroadcastIntents.PRIMARY);
                mainIntent.putExtra("status", intentAction);
                sendBroadcast(mainIntent);
                mBluetoothAdapter = mBluetoothManager.getAdapter();
                mConnectionState = STATE_DISCONNECTED;
                dataNotSaved = 0;
                arrB6.clear();
                disconnect();
                close();
                Log.d("UART","裝置斷線");
            } else {
                //重連機制
                callbackErrorTimes++;
                Log.d(TAG, "onConnectionStateChange received: " + status);
                //intentAction = "GATT_STATUS_133";
                mConnectionState = STATE_DISCONNECTED;
                intentAction = BroadcastActions.ACTION_GATT_DISCONNECTED;
                Intent mainIntent = new Intent(BroadcastIntents.PRIMARY);
                mainIntent.putExtra("status", intentAction);
                sendBroadcast(mainIntent);
                mBluetoothAdapter = mBluetoothManager.getAdapter();
                dataNotSaved = 0;
                arrB6.clear();
                disconnect();
                close();// 防止出现status 133
                SharedPreferences share = getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE);
                //val mBluetoothDeviceAddress = share.getString("mac", "noValue")
                mBluetoothDeviceAddress = share.getString("mac", "noValue");
                if (!mBluetoothDeviceAddress.equals("noValue") && callbackErrorTimes < 5) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            connect(mBluetoothDeviceAddress);
                        }
                    };
                    errHandler.postDelayed(runnable,1000);
                }
            }
            Log.d("UARTCallBackStatus",Integer.toString(status));
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.w(TAG, "mBluetoothGatt = " + mBluetoothGatt);
                enableTXNotification();
                //broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
                //sendToMainBroadcast(ACTION_GATT_SERVICES_DISCOVERED);
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
    public boolean initailze() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }
        if (mBluetoothAdapter == null) {
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            if (mBluetoothAdapter == null) {
                Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
                return false;
            }
        }
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        /* 寫在mainfest的不用註冊
        mPrimaryReceiver = new PrimaryReceiver();
        IntentFilter filter = new IntentFilter("Main");
        registerReceiver(mPrimaryReceiver,filter);
        */


        //20180102    Andy
        // Create sound pool
        mPreference = getSharedPreferences(SavePreferences.SETTING_KEY, 0);
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        soundsMap = new HashMap<>();
        //20180122
        soundsMap.put(SOUND5, soundPool.load(this, R.raw.tvoc_over20000, 1));
        soundsMap.put(SOUND4, soundPool.load(this, R.raw.tvoc_over5500, 1));
        soundsMap.put(SOUND3, soundPool.load(this, R.raw.tvoc_over2200, 1));
        soundsMap.put(SOUND2, soundPool.load(this, R.raw.tvoc_over660, 1));
        soundsMap.put(SOUND1, soundPool.load(this, R.raw.tvoc_over220, 1));

        mVibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        //showWithVibrate = mPreference.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false);

        //20180104   Andy
        //alertId = soundPool.load(this, R.raw.babuchimam, 1);
        //alertId2 = soundPool2.load(this, R.raw.ballballbusabahuwa, 1);
        if (!mIsReceiverRegistered) {
            if (mReceiver == null) {
                mReceiver = new MyBroadcastReceiver();
                registerReceiver(mReceiver, new IntentFilter(BroadcastIntents.UART_SERVICE));
                mIsReceiverRegistered = true;
                //LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver.get(), makeGattUpdateIntentFilter());
            }
        }
        realm = Realm.getDefaultInstance(); // opens "myrealm.realm"
        try {
            Log.d("REALMUART",String.valueOf(realm.getConfiguration().getSchemaVersion()));
        } finally {
            realm.close();
        }


        registerReceiver(mBluetoothStateBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        //20180124
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
        //mBluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
        //mBluetoothAdapter = mBluetoothManager.getAdapter();
        //initailze();
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }
        // Previously connected device.  Try to reconnect.
        // 會慢一點點連上去，但是可以保持一條主從的關係，如果拿掉會一直新增新的CLient Server到後期就會錯誤
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress) && mBluetoothGatt != null && mBluetoothAdapter != null) {
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
        initailze();
        //mBluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
        //mBluetoothAdapter = mBluetoothManager.getAdapter();
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
        disableTXNotification();
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
            callbackErrorTimes++;
            return;
        }
        BluetoothGattCharacteristic TxChar = RxService.getCharacteristic(TX_CHAR_UUID);
        if (TxChar == null) {
            showMessage("Tx characteristic not found!");
            //broadcastUpdate(DEVICE_DOES_NOT_SUPPORT_UART);
            sendToMainBroadcast(DEVICE_DOES_NOT_SUPPORT_UART);
            callbackErrorTimes++;
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(TxChar, true);
        BluetoothGattDescriptor descriptor = TxChar.getDescriptor(CCCD);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
        callbackErrorTimes = 0;

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
        //unregisterReceiver(mPrimaryReceiver);
        disconnect();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mBluetoothStateBroadcastReceiver);
        close();
        realm.close();
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
                case BroadcastActions.ACTION_CONNECT_DEVICE:
                    if (!connect(intent.getStringExtra("mac"))) {
                        initailze();
                        connect(intent.getStringExtra("mac"));
                    }
                    break;
                case "close":
                    disconnect();
                    close();
                    break;
                case "message":
                    break;
//                case "checkItems":
//                    Log.d(TAG, "checkItems");
//                    writeRXCharacteristic(CallingTranslate.INSTANCE.GetHistorySampleItems());
//                    break;
//                case "callItems":
//                    Log.d(TAG, "callItems");
//                    writeRXCharacteristic(CallingTranslate.INSTANCE.GetHistorySample(++NowItem));
//                    break;
                case "setSampleRate":
//                    int SampleTime = intent.getIntExtra("SampleTime", 1);
//                        /*  設定 0xB6的設定
//                        * input [0] = sample rate
//                        * input [1] = sensor-on time range Sensor開啟時間長度
//                        * input [2] = sensor to get sample 第幾秒取資料
//                        *
//                        * input [3] = pump on time pump開啟時間點
//                        * input [4] = pumping time pump開啟時間長度
//                        */
//                    int[] param = {SampleTime, SampleTime*30, /*SampleTime*30-*/2, /*SampleTime*30-*/1, 2, 0, 0};
//                    Log.d(TAG, "setSampleRate");
//                    writeRXCharacteristic(CallingTranslate.INSTANCE.SetSampleRate(param));
                    break;
                case BroadcastActions.ACTION_GET_SAMPLE_RATE:   //"getSampleRate":
                    Log.d(TAG, BroadcastActions.ACTION_GET_SAMPLE_RATE);
//                    if (intent.getStringExtra("callFromConnect").equals("yes"))
//                        CallFromConnect=true;
//                    else
//                        CallFromConnect=false;
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
//                case ACTION_GATT_SERVICES_DISCOVERED:
//                    enableTXNotification();
//                    break;
                case ACTION_DATA_AVAILABLE:
                    dataAvaliable(intent);
                    break;
                case DEVICE_DOES_NOT_SUPPORT_UART:
                    showMessage("Device Does Not support UART. Disconnecting");
                    //if (callbackErrorTimes < 5) { enableTXNotification(); }
                    break;
                //20180130
                case BroadcastActions.INTENT_KEY_PUMP_ON:
                    writeRXCharacteristic(CallingTranslate.INSTANCE.PumpOnCall(65002));
                    break;
                //20180130
                case BroadcastActions.INTENT_KEY_PUMP_OFF:
                    writeRXCharacteristic(CallingTranslate.INSTANCE.PumpOnCall(1));
                    break;
                case BroadcastActions.INTENT_KEY_LED_OFF:
                    writeRXCharacteristic(CallingTranslate.INSTANCE.SetLedOn(false));
                    break;
                case BroadcastActions.INTENT_KEY_LED_ON:
                    writeRXCharacteristic(CallingTranslate.INSTANCE.SetLedOn(true));
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
    @SuppressLint("NewApi")
    public void dataAvaliable(Intent intent) {
        final byte[] txValue = intent.getByteArrayExtra(EXTRA_DATA);

        Log.e(TAG, "Get command response:" + Byte.toString(txValue[0]));

        switch (txValue[0]) {
            case (byte) 0xEA:
                //當要印ByteArray的時候可以用
                break;
            default:
                //當要印ByteArray的時候可以用
                ArrayList dataArray = new ArrayList();
                for (int index = 0; index < txValue.length; index++){
                    dataArray.add(txValue[index]);
                }
                Log.d("UARTDATAAVA",dataArray.toString());
                //Log.d("UARTDATAAVA",new String(txValue, StandardCharsets.UTF_8));
        }
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
                    //Toast.makeText(getApplicationContext(), getText(R.string.Number_of_data) + Integer.toString(NowItem) + getText(R.string.Loading_fail), Toast.LENGTH_LONG).show();
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
            case (byte) 0xB9:
                Log.d(TAG, "cmd:0xB9 feedback");
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
            Intent mainIntent = new Intent(BroadcastIntents.PRIMARY);
            switch (txValue[2]) {
                case (byte) 0xB0:
                    RString = CallingTranslate.INSTANCE.GetAllSensor(txValue);
                    mainIntent.putExtra("status", "B0");
                    mainIntent.putExtra("TEMPValue", RString.get(0));
                    mainIntent.putExtra("HUMIValue", RString.get(1));
                    mainIntent.putExtra("TVOCValue", RString.get(2));
                    mainIntent.putExtra("ECO2Value", RString.get(3));
                    mainIntent.putExtra("PM25Value", RString.get(4));
                    mainIntent.putExtra("BatteryLife", RString.get(5));
                    mainIntent.putExtra("PreheatCountDown", RString.get(6));
                    sendBroadcast(mainIntent);

                    // 20180226 add
                    if(isFirstB0) {
                        isFirstB0 = false;

                        writeRXCharacteristic(CallingTranslate.INSTANCE.GetLedStateCMD());
                    }


                    if (Integer.valueOf(RString.get(2)) < 221){
                        //20180122  Andy
                            countsound220 = 0;
                            countsound660 = 0;
                            countsound2200 = 0;
                            countsound5500 = 0;
                            countsound20000 = 0;

                            //Log.e("歸零TVOC220計數變數:", Integer.toString(countsound220));
                            //Log.e("歸零TVOC660計數變數:", Integer.toString(countsound660));
                    }
                    else if ( Integer.valueOf(RString.get(2))  >= 220 && (Integer.valueOf(RString.get(2))  < 660)) {
                        //20180122  Andy
                        BEBEBEBE1(RString);
                    }
                    else if ( (Integer.valueOf(RString.get(2))  >= 660) && (Integer.valueOf(RString.get(2))  < 2200)) {
                        //20180122  Andy
                        BEBEBEBE2(RString);
                    }
                    else if ( (Integer.valueOf(RString.get(2))  >= 2200) && (Integer.valueOf(RString.get(2))  < 5500)) {
                        //20180122  Andy
                        BEBEBEBE3(RString);
                    }
                    else if ( (Integer.valueOf(RString.get(2))  >= 5500) && (Integer.valueOf(RString.get(2))  < 20000)) {
                        //20180122  Andy
                        BEBEBEBE4(RString);
                    }
                    else {
                        //20180122  Andy
                        BEBEBEBE5(RString);
                    }

                    break;
                case (byte) 0xB1:
                    RString = CallingTranslate.INSTANCE.ParserGetInfo(txValue);
                    break;
                case (byte) 0xB2:
                    RString = CallingTranslate.INSTANCE.ParserGetSampleRate(txValue);
                    SharedPreferences share = getSharedPreferences("ASMSetting", MODE_PRIVATE);
                    String setting0 = share.getString("sample_rate", "2");
                    String setting1 = share.getString("sensor_on_time_range", "60");
                    String setting2 = share.getString("sensor_to_get_sample", "2");
                    String setting3 = share.getString("pump_on_time", "1");
                    String setting4 = share.getString("pumping_time_range", "2");
                    share.edit().putString("sample_rate", "2").putString("sensor_on_time_range", "60").putString("sensor_to_get_sample", "2").putString("pump_on_time", "1").putString("pumping_time_range", "2").apply();
                    //當寫入完畢時會回吐B2 ok的CMD所作的處理
                    if (RString.size() >= 5) {
                        Log.d("0xB2Compare", setting0 + ":" + RString.get(0) + " " + setting1 + ":" + RString.get(1) + " " + setting2 + ":" + RString.get(2) + " " + setting3 + ":" + RString.get(3) + " " + setting4 + ":" + RString.get(4));
                        if (setting0.equals(RString.get(0))
                                && setting1.equals(RString.get(1))
                                && setting2.equals(RString.get(2))
                                && setting3.equals(RString.get(3))
                                && setting4.equals(RString.get(4)))
                        {
                            Log.d("0xB2", "True");
                        } else {
                            share.edit().putString("sample_rate", "2").putString("sensor_on_time_range", "60").putString("sensor_to_get_sample", "2").putString("pump_on_time", "1").putString("pumping_time_range", "2").apply();
                            int[] param = {2, 2*30, 2, 1, 2, 0, 0};
                            Log.d(TAG, "setSampleRate");
                            writeRXCharacteristic(CallingTranslate.INSTANCE.SetSampleRate(param));
                        }
                        setSampleRateTime(Integer.parseInt(RString.get(0)));
                        writeRXCharacteristic(CallingTranslate.INSTANCE.GetHistorySampleItems());
                    }
                    break;
                case (byte) 0xB4:
                    RString = CallingTranslate.INSTANCE.ParserGetHistorySampleItems(txValue);
                    myDeviceData.clear();
                    //取得裝置目前B5的Index
                    setMaxItems(Integer.parseInt(RString.get(0)));//MAX Items
                    //************** 2017/12/03 "尊重原創 留原始文字 方便搜尋" 更改成從String撈文字資料(中文) *************************//
                    //Toast.makeText(getApplicationContext(),"共有資料"+Integer.toString(getMaxItems())+"筆",Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(),"讀取資料中請稍候",Toast.LENGTH_LONG).show();
                    //Toast.makeText(getApplicationContext(), getText(R.string.Total_Data) + Integer.toString(getMaxItems()) + getText(R.string.Total_Data_Finish), Toast.LENGTH_LONG).show();
                    //setCorrectTime(Integer.parseInt(RString.get(8)));
                    setCorrectTime(0);
                    Log.d("0xB4",RString.toString());
                    String b4correctTime = RString.get(3);
                    //取得當前時間
                    // String time=getDateTime();
                    // setGetDataTime(time);
                    Log.d("UART", "total item "+Integer.toString(getMaxItems()));
                    // Log.d("UART", "getItem 1");
                    //如果目前Index大於0
                    if (getMaxItems() > 0) {
                        //mainIntent.putExtra("status", "MAXPROGRESSITEM");
                        //mainIntent.putExtra("MAXPROGRESSITEM", Integer.toString(getMaxItems()));
                        //sendBroadcast(mainIntent);
                        Toast.makeText(getApplicationContext(), getText(R.string.Loading_Data), Toast.LENGTH_LONG).show();
                        Log.d("UART", "getItem 1");
                        NowItem = 1;
                        counter = 0;
                        //將時間秒數寫入設定為 00  或  30
                        timeSetNowToThirty(b4correctTime);
                        //Realm 資料庫
                        Realm realm = Realm.getDefaultInstance();
                        //計算有幾筆未上傳
                        RealmQuery condition = realm.where(AsmDataModel.class).equalTo("UpLoaded","0");
                        RealmResults notupload = condition.findAll();
                        if (notupload != null) {
                            Log.d("NOTUPLOAD",String.valueOf(notupload.size()));
                        }
                        //將資料庫最大時間與現在時間換算成Count
                        Number maxCreatedTime = realm.where(AsmDataModel.class).max("Created_time");
                        if (maxCreatedTime == null) { maxCreatedTime = Calendar.getInstance().getTimeInMillis() - TimeUnit.DAYS.toMillis(2); }
                        if (maxCreatedTime != null) {
                            //Long lastRowSaveTime = realm.where(AsmDataModel.class).equalTo("Created_time", maxCreatedTime.longValue())
                            //.findAll().first().getCreated_time().longValue();
                            Long nowTime = getMyDate().getTime();
                            Log.d("0xB4countLast",  new Date(maxCreatedTime.longValue()).toString());
                            Log.d("0xB4countLast",  new Date(nowTime).toString());
                            Long countForItemTime = nowTime - maxCreatedTime.longValue();
                            Log.d("0xB4", countForItemTime.toString());
                            countForItem = Math.min((int)(countForItemTime / (60L * 1000L)),getMaxItems());
                            //當小於0的時候讓它等於0
                            if (countForItem < 0) { countForItem = 0; }
                            Log.d("0xB4countItem", Long.toString(countForItem));
                            //Toast.makeText(getApplicationContext(), getText(R.string.Total_Data) + Long.toString(countForItem) + getText(R.string.Total_Data_Finish), Toast.LENGTH_LONG).show();
                        }
                        if (countForItem >= 1){
                            NowItem = countForItem;
                            writeRXCharacteristic(CallingTranslate.INSTANCE.GetHistorySample(NowItem));
                            downloading = true;
                        } else {
                            downloading = false;
                            downloadComplete = true;
                            Toast.makeText(getApplicationContext(), getText(R.string.Loading_Completely), Toast.LENGTH_LONG).show();
                        }
                        mainIntent.putExtra("status", BroadcastActions.INTENT_KEY_GET_HISTORY_COUNT);
                        mainIntent.putExtra(BroadcastActions.INTENT_KEY_GET_HISTORY_COUNT, Integer.toString(countForItem));
                        sendBroadcast(mainIntent);

                    } else if (getMaxItems() <= 0) {
                        //0xB6裡的Log會用到
                        timeSetNowToThirty(b4correctTime);
                        downloadComplete = true;
                    }
                    break;
                case (byte) 0xB5:
                    RString = CallingTranslate.INSTANCE.ParserGetHistorySampleItem(txValue);
                    //getDateTime(getMyDate().getTime()-getCorrectTime()*60*1000);
                    Log.d("0xB5Index",RString.get(0));
                    if (Integer.parseInt(RString.get(0)) == NowItem) {//將資料存入MyData
                        int nowItemReverse = countForItem - NowItem + 1;
                        Log.d("0XB5",Integer.toString(nowItemReverse));
                        Log.d("0XB5",Integer.toString(NowItem));
                        mainIntent.putExtra("status", BroadcastActions.INTENT_KEY_LOADING_DATA);
                        mainIntent.putExtra(BroadcastActions.INTENT_KEY_LOADING_DATA,Integer.toString(nowItemReverse));
                        sendBroadcast(mainIntent);
                        Log.d("UART:ITEM ", Integer.toString(NowItem));
                        myDeviceData.add(new myData(RString.get(1), RString.get(2), RString.get(3), RString.get(4), getDateTime(getMyDate().getTime() - getSampleRateUnit() * counter * 30 * 1000 - getCorrectTime() * 30 * 1000)));
                        //Realm 資料庫
                        Realm realm = Realm.getDefaultInstance();
                        Number num = realm.where(AsmDataModel.class).max("id");
                        int nextID;
                        if(num == null) {
                            nextID = 1;
                        } else {
                            nextID = num.intValue() + 1;
                        }
                        realm.executeTransaction(r -> {
                            AsmDataModel asmData = r.createObject(AsmDataModel.class,nextID);
                            asmData.setTEMPValue(RString.get(1));
                            asmData.setHUMIValue(RString.get(2));
                            asmData.setTVOCValue(RString.get(3));
                            asmData.setECO2Value(RString.get(4));
                            asmData.setPM25Value(RString.get(5));
                            asmData.setCreated_time((getMyDate().getTime() - countForItem * getSampleRateUnit() * 30 * 1000) + getSampleRateUnit() * counterB5 * 30 * 1000 + getCorrectTime() * 30 * 1000);
                            Log.d("RealmTimeB5", RString.toString());
                            Log.d("RealmTimeB5", new Date((getMyDate().getTime() - countForItem * getSampleRateUnit() * 30 * 1000) + getSampleRateUnit() * counterB5 * 30 * 1000 + getCorrectTime() * 30 * 1000).toString());
                        });
                        realm.close();
                        NowItem--;
                        counterB5++;

                        //if (NowItem >= getMaxItems()) {
                        if (NowItem <= 0) {
                            NowItem = 1;
                            downloading = false;
                            downloadComplete = true;
                            counterB5 = 1;
                            //************** 2017/12/03 "尊重原創 留原始文字 方便搜尋" 更改成從String撈中英文字資料 ***************************//
                            //Toast.makeText(getApplicationContext(),"讀取完成",Toast.LENGTH_LONG).show();
                            //*****************************************************************************************************************//
                            Toast.makeText(getApplicationContext(), getText(R.string.Loading_Completely), Toast.LENGTH_LONG).show();
//                            mainIntent.putExtra("status", "B5");
//                            Bundle data = new Bundle();
//                            data.putParcelableArrayList("resultSet", myDeviceData);
//                            mainIntent.putExtra("result", data);
//                            sendBroadcast(mainIntent);
                        } else {
                            //NowItem++;
                            //counter++;
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
//                        mainIntent.putExtra("TEMPValue", RString.get(0));
//                        mainIntent.putExtra("HUMIValue", RString.get(1));
//                        mainIntent.putExtra("TVOCValue", RString.get(2));
//                        mainIntent.putExtra("eCO2Value", RString.get(3));
//                        //mainIntent.putExtra("PM25", RString.get(4));
//                        mainIntent.putExtra("BatteryLife", RString.get(5));
//                        mainIntent.putExtra("flag",RString.get(6));
                        sendBroadcast(mainIntent);
                        HashMap hashMapInB6 = new HashMap();
                        hashMapInB6.put("TEMPValue",RString.get(0));
                        hashMapInB6.put("HUMIValue",RString.get(1));
                        hashMapInB6.put("TVOCValue",RString.get(2));
                        hashMapInB6.put("ECO2Value",RString.get(3));
                        hashMapInB6.put("PM25Value",RString.get(4));
                        hashMapInB6.put("BatteryLife",RString.get(5));
                        hashMapInB6.put("CreatedTime",timeSetForB6());
                        arrB6.add(hashMapInB6);
                        //在下載資料時因為沒寫入資料庫需要記住B6幾筆未寫入
                        dataNotSaved++;
                    if (!downloading && dataNotSaved != 0 && downloadComplete) {
                        //將時間秒數寫入設定為 00  或  30
                        Log.d("0xB6OldTime",new Date(getMyDate().getTime()).toString());
                        Log.d("0xB6",arrB6.toString());
                        //如果來了10筆就用現在時間退10筆
                        for (int i = 0; i < arrB6.size(); i++) {
                            //Realm 資料庫
                            Realm realm = Realm.getDefaultInstance();
                            Number num = realm.where(AsmDataModel.class).max("id");
                            int nextID;
                            if (num == null) {
                                nextID = 1;
                            } else {
                                nextID = num.intValue() + 1;
                            }
                            Number maxCreatedTime = realm.where(AsmDataModel.class).max("Created_time");
                            if (maxCreatedTime == null) { maxCreatedTime = Calendar.getInstance().getTimeInMillis() - TimeUnit.DAYS.toMillis(2); }
                            Log.d("0xB6DBLastTime",new Date(maxCreatedTime.longValue()).toString());
                            int count = i;
                            realm.executeTransaction(r -> {
                                AsmDataModel asmData = r.createObject(AsmDataModel.class, nextID);
                                asmData.setTEMPValue(arrB6.get(count).get("TEMPValue").toString());
                                asmData.setHUMIValue(arrB6.get(count).get("HUMIValue").toString());
                                asmData.setTVOCValue(arrB6.get(count).get("TVOCValue").toString());
                                asmData.setECO2Value(arrB6.get(count).get("ECO2Value").toString());
                                asmData.setPM25Value(arrB6.get(count).get("PM25Value").toString());
                                Long time = Long.parseLong(arrB6.get(count).get("CreatedTime").toString());
                                asmData.setCreated_time(time);
                                //asmData.setCreated_time(getMyDate().getTime() + getSampleRateUnit() * (count) * 30 * 1000 + getCorrectTime() * 30 * 1000);
                                Log.d("RealmTimeB6", arrB6.toString());
                                Log.d("RealmTimeB6", new Date(time).toString());
                                //Log.d("RealmTimeB6", new Date(getMyDate().getTime() + getSampleRateUnit() * (count) * 30 * 1000 + getCorrectTime() * 30 * 1000).toString());
                            });
                            realm.close();
                        }
                        //寫入完畢後將未寫入筆數設為0
                        dataNotSaved = 0;
                        arrB6.clear();
                        //timeSetNowToThirty();
                    }
                    break;

                case (byte) 0xB9:           // 取得裝置ＬＥＤ燈開或關
                    int ledState = txValue[3];

                    if(ledState == 1) {
                        mPreference.edit().putBoolean(SavePreferences.SETTING_LED_SWITCH,
                                false).apply();
                    } else {
                        mPreference.edit().putBoolean(SavePreferences.SETTING_LED_SWITCH,
                                true).apply();
                    }

                    Log.e(TAG, "LED Status: " + ledState);
                    break;
            }
        }
    }

    private void sendToMainBroadcast(String value) {
        Intent mainIntent = new Intent(BroadcastIntents.PRIMARY);
        mainIntent.putExtra("status", value);
        sendBroadcast(mainIntent);
    }

    private void sendCharToMainBroadcast(BluetoothGattCharacteristic characteristic) {
        if (TX_CHAR_UUID.equals(characteristic.getUuid())) {
            Intent mainIntent = new Intent(BroadcastIntents.PRIMARY);
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

    private void timeSetNowToThirty(String afterB6Sec) {
        //取得當前時間
        //將時間秒數寫入設定為 00  或  30
        //Long dateSecMil = new Date().getTime();
        Long dateSecMil = Calendar.getInstance().getTimeInMillis() + Calendar.getInstance().getTimeZone().getRawOffset() - Long.parseLong(afterB6Sec) * 1000;
        Long dateSecChange = (dateSecMil / 1000)/60 * (1000*60);
        //Log.d("0xB4",dateSecChange.toString());
        Date date = new Date(dateSecChange);
        Log.d("timeSetNowToThirty",date.toString());
        setMyDate(date);
    }

    private Long timeSetForB6() {
        Long dateSecMil = Calendar.getInstance().getTimeInMillis() + Calendar.getInstance().getTimeZone().getRawOffset();
        Long dateSecChange = (dateSecMil / 1000) / 60 * (1000 * 60);
        Date date = new Date(dateSecChange);
        Log.d("timeSetForB6",date.toString());
        return dateSecChange;
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



    //20180102   Andy
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void BEBEBEBE1(ArrayList<String> BEBERString) {
        //20180124
        ArrayList<String> bebe1RString = BEBERString;
        SharedPreferences mPreference = this.getApplication().getSharedPreferences(SavePreferences.SETTING_KEY, 0);
        if ((countsound220 == 5 || countsound220 == 0)) {
            //20180102   Andy叫叫ABC
            //mp = MediaPlayer.create (this, R.raw.pixiedust);
            //20171226  Andy
            countsound660 = 0;
            countsound2200 = 0;
            countsound5500 = 0;
            countsound20000 = 0;
            Log.e("更新TVOC計數變數: 220:", Integer.toString(countsound220) +
                    "660:" + Integer.toString(countsound660) + "2200:" + Integer.toString(countsound2200) +
                    "20000:" + Integer.toString(countsound20000));

            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false)) {
                //mp.start();
                //20171220   Andy
                try {
                    //alertId = soundPool.load(this, R.raw.babuchimam, 1);
                    //Thread.sleep(150);
                    //soundPool.play(alertId, 1F, 1F, 0, 0, 1.0f);
                    //叫一聲
                    playSound(SOUND1, 1.0f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false)) {
                //if ((countsound800 == 5 || countsound800 == 0)) {
                if (mVibrator != null) {
                    // 震动 1s
                    mVibrator.vibrate(1000);
                }
                //}

            }
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)) {

                if (isAppIsInBackground(nowActivity))
                    try {
                        makeNotificationShow(R.drawable.history_face_icon_02,
                                getString(R.string.warning_title_Yellow),
                                getString(R.string.text_message_air_mid),
                                bebe1RString.get(2));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (countsound220 == 5) {
            countsound220 = 0;
        }
        countsound220 = countsound220 + 1;
        Log.e("TVOC220計數變數:", Integer.toString(countsound220));
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void BEBEBEBE2(ArrayList<String> BEBERString){
        //20180124
        ArrayList<String> bebe2RString=BEBERString;
        SharedPreferences mPreference=this.getApplication().getSharedPreferences(SavePreferences.SETTING_KEY, 0);
        if ((countsound660 == 5 || countsound660 == 0)) {

            countsound220 = 0;
            countsound2200 = 0;
            countsound5500 = 0;
            countsound20000 = 0;
            Log.e("更新TVOC計數變數: 220:", Integer.toString(countsound220) +
                    "660:" + Integer.toString(countsound660) + "2200:" + Integer.toString(countsound2200) +
                    "20000:" + Integer.toString(countsound20000));


            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false)) {
                //20171219   Andy
                //mp.start();
                //20171220   Andy
                try {
                    //Thread.sleep(150);
                    //soundPool2.play(alertId2, 1F, 1F, 0, 0, 1.0f);

                    //叫兩聲
                    playSound(SOUND2, 1.0f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false)) {
                if (mVibrator != null) {
                    // 震动 2s
                    mVibrator.vibrate(2000);
                }
            }
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)) {
                if (isAppIsInBackground(nowActivity))
                    try {
                        makeNotificationShow(R.drawable.history_face_icon_03,
                                getString(R.string.warning_title_Orange),
                                getString(R.string.text_message_air_Medium_Orange),
                                bebe2RString.get(2));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        if (countsound660 == 5) {
            countsound660 = 0;
        }
        countsound660 = countsound660 + 1;
        Log.e("TVOC660計數變數:",Integer.toString(countsound660));

    }

    //20180122   Andy
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void BEBEBEBE3(ArrayList<String> BEBERString) {
        //20180124
        ArrayList<String> bebe3RString = BEBERString;
        SharedPreferences mPreference = this.getApplication().getSharedPreferences(SavePreferences.SETTING_KEY, 0);
        if ((countsound2200 == 5 || countsound2200 == 0)) {

            countsound220 = 0;
            countsound660 = 0;
            countsound5500 = 0;
            countsound20000 = 0;
            Log.e("更新TVOC計數變數: 220:", Integer.toString(countsound220) +
                    "660:" + Integer.toString(countsound660) + "2200:" + Integer.toString(countsound2200) +
                    "20000:" + Integer.toString(countsound20000));

            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false)) {
                //mp.start();
                //20171220   Andy
                try {
                    //alertId = soundPool.load(this, R.raw.babuchimam, 1);
                    //Thread.sleep(150);
                    //soundPool.play(alertId, 1F, 1F, 0, 0, 1.0f);
                    playSound(SOUND3, 1.0f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false)) {
                //if ((countsound800 == 5 || countsound800 == 0)) {
                if (mVibrator == null) {
                } else {
                    // 震动 1s
                    mVibrator.vibrate(3000);
                }
                //}

            }

            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)) {

                if (isAppIsInBackground(nowActivity)) {
                    try {
                        makeNotificationShow(R.drawable.history_face_icon_04,
                                getString(R.string.warning_title_Red),
                                getString(R.string.text_message_air_bad),
                                bebe3RString.get(2));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (countsound2200 == 5) {
            countsound2200 = 0;
        }
        countsound2200 = countsound2200 + 1;
        Log.e("TVOC2200計數變數:", Integer.toString(countsound2200));
    }
    //20180122   Andy
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void BEBEBEBE4(ArrayList<String> BEBERString) {
        //20180124
        ArrayList<String> bebe4RString=BEBERString;
        SharedPreferences mPreference = this.getApplication().getSharedPreferences(SavePreferences.SETTING_KEY, 0);
        if ((countsound5500 == 5 || countsound5500 == 0)) {
            countsound220 = 0;
            countsound660 = 0;
            countsound2200 = 0;
            countsound20000 = 0;
            Log.e("更新TVOC計數變數: 220:", Integer.toString(countsound220) +
                    "660:" + Integer.toString(countsound660) + "2200:" + Integer.toString(countsound2200) +
                    "20000:" + Integer.toString(countsound20000));

            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false)) {
                //mp.start();
                //20171220   Andy
                try {
                    //alertId = soundPool.load(this, R.raw.babuchimam, 1);
                    //Thread.sleep(150);
                    //soundPool.play(alertId, 1F, 1F, 0, 0, 1.0f);
                    playSound(SOUND4, 1.0f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false)) {
                //if ((countsound800 == 5 || countsound800 == 0)) {
                if (mVibrator != null) {
                    // 震动 1s
                    mVibrator.vibrate(4000);
                }
                //}

            }
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)) {

                if (isAppIsInBackground(nowActivity)) {
                    try {
                        makeNotificationShow(R.drawable.history_face_icon_05,
                            getString(R.string.warning_title_Purple),
                            getString(R.string.text_message_air_Serious_Purple),
                                bebe4RString.get(2));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (countsound5500 == 5) {
            countsound5500 = 0;
        }
        countsound5500 = countsound5500 + 1;
        Log.e("TVOC5500計數變數:", Integer.toString(countsound5500));
    }
    //20180122   Andy
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void BEBEBEBE5(ArrayList<String> BEBERString) {
        //20180124
        ArrayList<String> bebe5RString = BEBERString;
        SharedPreferences mPreference = this.getApplication().getSharedPreferences(SavePreferences.SETTING_KEY, 0);
        if ((countsound20000 == 5 || countsound20000 == 0)) {
            countsound220 = 0;
            countsound660 = 0;
            countsound2200 = 0;
            countsound5500 = 0;
            Log.e("更新TVOC計數變數: 220:", Integer.toString(countsound220) +
                    "660:" + Integer.toString(countsound660) + "2200:" + Integer.toString(countsound2200) +
                    "20000:" + Integer.toString(countsound20000));

            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_SOUND, false)) {
                //mp.start();
                //20171220   Andy
                try {
                    playSound(SOUND5, 1.0f);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_VIBERATION, false)) {
                //if ((countsound800 == 5 || countsound800 == 0)) {
                if (mVibrator != null) {
                    // 震动 1s
                    mVibrator.vibrate(5000);
                }
                //}

            }
            if (mPreference.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)) {

                if (isAppIsInBackground(nowActivity)) {
                    try {
                        makeNotificationShow(R.drawable.history_face_icon_06,
                                getString(R.string.warning_title_Brown),
                                getString(R.string.text_message_air_Extreme_Dark_Purple),
                                bebe5RString.get(2));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (countsound20000 == 5) {
            countsound20000 = 0;
        }
        countsound20000 = countsound20000 + 1;
        Log.e("TVOC20000計數變數:", Integer.toString(countsound20000));
    }

    private void makeNotificationShow(int iconID, String title, String text, String value) {
        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.bigText(getString(R.string.text_message_air_Extreme_Dark_Purple));
        @SuppressLint("ResourceAsColor") Notification notification = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), iconID))
                .setContentTitle(title)
                .setStyle(bigStyle)
                .setPriority(Notification.PRIORITY_DEFAULT)
                .setAutoCancel(true) // 點擊完notification自動消失
                .build();
        Intent intent = new Intent(this, MainActivity.class);
        //當使用者點擊通知Bar時，切換回MainActivity
        PendingIntent pi = PendingIntent.getActivity(this, REQUEST_CODE,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.contentIntent = pi;
        //20180109   Andy
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationHelper = new NotificationHelper(this);
            notificationHelper.set_TCOC_Value(Integer.parseInt(value));//RString.get(2)));
            Notification.Builder NB = notificationHelper.getNotification1(title, text);
            notificationHelper.notify(REQUEST_CODE, NB);
        } else {
            //送到手機的通知欄
            notificationManager.notify(1, notification);

            //20180209
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            //獲取電源管理器對象
            PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK, "bright");
            //獲取PowerManager.WakeLock對象,後面的參數|表示同時傳入兩個值,最後的是LogCat裡用的Tag

            wl.acquire(2 * 1000L);
            //點亮屏幕
            wl.release();
            Log.e("休眠狀態下","喚醒螢幕");

        }
    }

    public NotificationHelper notificationHelper = null;

    public void playSound(int sound, float fSpeed) {
        AudioManager mgr = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        assert mgr != null;
        float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
        float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volume = streamVolumeCurrent / streamVolumeMax;


        soundPool.play(soundsMap.get(sound), volume, volume, 1, 0, fSpeed);
    }

    private final BroadcastReceiver mBluetoothStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            //final int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);

            final String stateString = "[Broadcast] Action received: " + BluetoothAdapter.ACTION_STATE_CHANGED + ", state changed to " + state2String(state);

            switch (state) {
                case BluetoothAdapter.STATE_TURNING_OFF:
                case BluetoothAdapter.STATE_OFF:
                    disconnect();
                    break;
            }
            Log.d("UARTSERVICE",stateString);
        }

        private String state2String(final int state) {
            switch (state) {
                case BluetoothAdapter.STATE_TURNING_ON:
                    return "TURNING ON";
                case BluetoothAdapter.STATE_ON:
                    return "ON";
                case BluetoothAdapter.STATE_TURNING_OFF:
                    return "TURNING OFF";
                case BluetoothAdapter.STATE_OFF:
                    return "OFF";
                default:
                    return "UNKNOWN (" + state + ")";
            }
        }
    };

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

}
