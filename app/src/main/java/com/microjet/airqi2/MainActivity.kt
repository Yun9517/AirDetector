package com.microjet.airqi2

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AnimationDrawable
import android.location.LocationManager
import android.media.AudioManager
import android.media.SoundPool
import android.net.ConnectivityManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.content.res.AppCompatResources
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.webkit.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.microjet.airqi2.Account.AccountActiveActivity
import com.microjet.airqi2.Account.AccountManagementActivity
import com.microjet.airqi2.Account.AccountRetryActivity
import com.microjet.airqi2.Account.AccountTask.AccountCheckTokenTask
import com.microjet.airqi2.BlueTooth.BLECallingTranslate
import com.microjet.airqi2.BlueTooth.DeviceListActivity
import com.microjet.airqi2.BlueTooth.UartService
import com.microjet.airqi2.CustomAPI.FragmentAdapter
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.Definition.RequestPermission
import com.microjet.airqi2.Definition.SavePreferences
import com.microjet.airqi2.FireBaseCloudMessage.FirebaseNotifSettingTask
import com.microjet.airqi2.Fragment.ChartFragment
import com.microjet.airqi2.Fragment.MainFragment
import com.microjet.airqi2.Fragment.Pm10Fragment
import com.microjet.airqi2.GestureLock.DefaultPatternCheckingActivity
import com.microjet.airqi2.URL.AirActionTask
import com.microjet.airqi2.URL.AppMenuTask
import com.microjet.airqi2.URL.AppVersion
import com.microjet.airqi2.settingPage.SettingActivity
import com.microjet.airqi2.warringClass.WarringClass
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.drawer_header.*
import layout.ExpandableListAdapter
import layout.ExpandedMenuModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private val TAG = MainActivity::class.java.simpleName
    private val DEFINE_FRAGMENT_TVOC = 1
    private val DEFINE_FRAGMENT_PM25 = 2
    private val DEFINE_FRAGMENT_ECO2 = 3
    private val DEFINE_FRAGMENT_TEMPERATURE = 4
    private val DEFINE_FRAGMENT_HUMIDITY = 5
    private val DEFINE_FRAGMENT_PM10 = 6

    // Fragment 容器
    private val mFragmentList = ArrayList<Fragment>()

    // var viewPager = VerticalViewPager()
    // ViewPager目前頁面
    private var currentIndex: Int = 0

    // Drawer & NavigationBar
    private var mDrawerToggle: ActionBarDrawerToggle? = null

    // 電池電量數值
    private var batValue: Int = 0

    // 藍芽icon in actionbar
    private var bleIcon: MenuItem? = null
    //電量icon
    private var battreyIcon: MenuItem? = null
    //分享icon
    private var shareMap: MenuItem? = null

    private var getDrawerLayoutItem: MenuItem? = null

    private var lightIcon: ImageView? = null

    private var connState = BleConnection.DISCONNECTED

    // private var mDevice: BluetoothDevice? = null
    //private var mBluetoothLeService: UartService? = null
    private val REQUEST_SELECT_DEVICE = 1
    private val REQUEST_SELECT_SAMPLE = 2

    //UArtService實體
    //private var mService: UartService? = null

    private var mIsReceiverRegistered: Boolean = false
    //private var mReceiver: myBroadcastReceiver? = null
    private var isGPSEnabled: Boolean = false
    private var mLocationManager: LocationManager? = null

    // ***** 2017/12/11 Drawer連線 會秀出 Mac Address ************************* //
    //private var drawerDeviceAddress: String? = null

    // ***** 2018/03/12 Drawer Show Device Name ******************************* //
    //private var drawerDeviceName: String? = null

    // ***** 2018/03/12 Drawer Show Account Name ****************************** //
    //private var drawerAccountName: String? = null


    //private var mMainReceiver: BroadcastReceiver? = null
    private var preheatCountDownInt = 0

    private var topMenu: Menu? = null
    // Code to manage Service lifecycle.
    private var mDeviceAddress: String? = null
    //private var mUartService: UartService? = null
    //private var longi = 121.4215f
    //private var lati = 24.959742f
    //private var locationListener: LocationListener? = null
    // FragmentAdapter
    private lateinit var mFragmentAdapter: FragmentAdapter
    //private val mPM25Fg = ChartFragment()
    /** 是否禁止右劃標記  */
    private var banDownDraw: Boolean = false
    /** 手指在螢幕上的最後x坐標  */
    private var mLastMotionY: Float = 0.toFloat()
    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mUartService = (service as UartService.LocalBinder).service
            if (!mUartService!!.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            if (!myPref.getSharePreferenceManualDisconn()) {
                mDeviceAddress = myPref.getSharePreferenceMAC()
                if (mDeviceAddress != "noValue") {
                    mUartService?.connect(mDeviceAddress)
                }
            }
            /*if (myPref.getSharePreferenceServiceForeground()) {
                val serviceIntent = Intent(this@MainActivity, UartService::class.java)
                serviceIntent.action = "START_FOREGROUND"
                startService(serviceIntent)
            }*/
            mUartService?.initFuseLocationProviderClient()
            Log.d("mServiceConnection", "onServiceConnected")
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mUartService = null
            Log.d("mServiceConnection", "onServiceDisconnected")
        }
    }

    enum class BleConnection {
        CONNECTED,
        DISCONNECTED,
    }

    private var errorTime = 0
    private var isFirstC0 = true
    private var isFirstC6 = true
    private var countForItem = 0
    private var arrIndexMap = ArrayList<HashMap<String, String>>()
    private var lock = false
    private var arr1 = arrayListOf<HashMap<String, Int>>()
    private var indexMap = HashMap<String, Int>()
    private var maxItem = 0
    private var bluetoothStateStr = "BluetoothAdapter.STATE_OFF"

    //20180411
    private val soundPool2 = SoundPool(1, AudioManager.STREAM_MUSIC, 100)
    private var alertId = 0
    private var lowPowerCont: Int = 0
    val mContext = this@MainActivity

    // 2018/05/29 Add "introduction" & "ourStory", modify sequence. Thanks the original creator!
    private var experienceURL = ""
    private var introductionURL = ""
    private var buyURL = ""
    private var ourStoryURL = ""

    //20180423
    private var points = java.util.ArrayList<ImageView>()

    // 2018/05/03 ExpandableListView
    private var mMenuAdapter: ExpandableListAdapter? = null
    private val listDataHeader: ArrayList<ExpandedMenuModel> = ArrayList()
    private val listDataChild: HashMap<ExpandedMenuModel, ArrayList<String>> = HashMap()
    private var setNavigationView: NavigationView? = null

    private var mywarningclass: WarringClass? = null

    private lateinit var myPref: PrefObjects
    private var c6d6map = HashMap<String, String>()
    private var lati = 255f  //TvocNoseData.lati
    private var longi = 255f //TvocNoseData.longi
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    private var countForAndroidO: Int = 0
    private var triggerForAndroidOOn: Boolean = false

    //AccountCheckTokenTask使用
    private val getMainActivity: Activity = this

    companion object {
        var mUartService: UartService? = null
    }

    private var bindServiceSuccess: Boolean = false

    //2018/08/16
    private var scrollindex = 0
    private var scrollViewsArr = ArrayList<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e(TAG, "call onCreate")
        myPref = PrefObjects(this)
        Log.e("Firebase", FirebaseInstanceId.getInstance().token.toString())
        uiFindViewById()
        viewPagerInit()
        initActionBar()
        initpoint()
        CheckSWversion()
        checkUrl()
        mywarningclass = WarringClass(mContext)
        val dm = DisplayMetrics()
        this@MainActivity.windowManager.defaultDisplay.getMetrics(dm)
        Log.v("MainActivity", "Resolution: " + dm.heightPixels + "x" + dm.widthPixels)
        Log.e("Conn", MyApplication.getConnectStatus())
        if (!mIsReceiverRegistered) {
            LocalBroadcastManager.getInstance(mContext).registerReceiver(myBroadcastReceiver, makeGattUpdateIntentFilter())
            mIsReceiverRegistered = true
        }
        //setupDrawerContent(naviView)
        registerReceiver(mBluetoothStateReceiver, makeBluetoothStateIntentFilter())

        //20180209
        //window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //20180411   建立警告物件
        //  warningClass = WarningClass(this)
        alertId = soundPool2.load(this, R.raw.low_power, 1)

        //  mywarningclass=WarringClass(this)

        setExpandableDrawer()

        val checkResult = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        if (checkResult != ConnectionResult.SUCCESS) {
            Log.e("偵測是否成功", "結論失敗")
        } else {
            Log.e("偵測是否成功", "結論成功")
        }
        FirebaseMessaging.getInstance().subscribeToTopic("addwiinews")
        //FirebaseMessaging.getInstance().subscribeToTopic("addwiiNewsNotifi")
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        wvMapMain.loadUrl("https://mjairql.com/air_map/")
        wvMapMain.webViewClient = object : WebViewClient() {
            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                super.onReceivedError(view, request, error)
                when (error!!.errorCode) {
                    -2 -> {
                        Toast.makeText(MyApplication.applicationContext(), "請連接網路", Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                    }
                }

            }
        }
        wvMapMain.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                callback.invoke(origin, true, false)
            }
        }



        // 180816 暫時拿掉常駐，因此刪除SharePreference中的KEY
        if(myPref.getSharePreferenceServiceForeground()) {
            myPref.clearSharePreferenceServiceForeground()
        }
        //2018/08/16 跑馬燈
        scrollingMission()

    }

    @SuppressLint("WifiManagerLeak")
    override fun onStart() {
        super.onStart()
        Log.e(TAG, "call onStart")
        //val serviceIntent: Intent? = Intent(this, UartService::class.java)
        //startService(serviceIntent)
        requestPermissionsForBluetooth()
        //checkBluetooth()
        //20180802 Richard
        val gattServiceIntent = Intent(this, UartService::class.java)
        bindServiceSuccess = bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
        Log.d("bindServiceResult", bindServiceSuccess.toString())
        if (mUartService != null && bindServiceSuccess) {
            if (connState == BleConnection.DISCONNECTED) {
                if (!myPref.getSharePreferenceManualDisconn()) {
                    mDeviceAddress = myPref.getSharePreferenceMAC()
                    if (mDeviceAddress != "noValue") {
                        mUartService?.connect(mDeviceAddress)
                    }
                }
            }
        } else {
            connState = BleConnection.DISCONNECTED
        }
        //20180518
        val shareToken = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        val myToken = shareToken.getString("token", "")
        if (myToken != "") {
            FirebaseNotifSettingTask().execute(myToken)
            AccountCheckTokenTask(getMainActivity,"checkTokenByOnstart").execute(myToken)
        }
        if (wvMapMain.visibility == 0) {
            var ws = wvMapMain.settings
            ws.javaScriptEnabled = true
        }
        Log.d("QQQ", wvMapMain.visibility.toString())
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        Log.e(TAG, "call onResume")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            countForAndroidO = 0
            triggerForAndroidOOn = false
        }
        checkUIState()
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "call onPause")
        EventBus.getDefault().unregister(this)
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            countForAndroidO = 0
            triggerForAndroidOOn = true
        }
        if (wvMapMain.visibility == 0) {
            var ws = wvMapMain.settings
            ws.javaScriptEnabled = false
        }
        checkUIState()
        Log.e(TAG, "call onStop")
    }

    //20180202
    override fun onBackPressed() {
        closeDialogShow()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "call onDestroy")
        if (mIsReceiverRegistered) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(myBroadcastReceiver)
            mIsReceiverRegistered = false
        }

        unregisterReceiver(mBluetoothStateReceiver)
        mUartService?.close()
        if (mUartService != null) {
            unbindService(mServiceConnection)
        }
        wvMapMain.clearCache(true)

        //EventBus.getDefault().unregister(this)
    }

    // 20171130 add by Raymond 增加權限 Request
    // 允許權限後的方法實作
    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        if (requestCode == RequestPermission.REQ_CODE_ACCESS_FILE_LOCATION) {
            checkBluetooth()
        }
    }

    // 拒絕權限後的方法實作
    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (requestCode == RequestPermission.REQ_CODE_ACCESS_FILE_LOCATION) {
            val mBuilder = AlertDialog.Builder(this)
            mBuilder.setTitle(R.string.text_message_need_permission)
                    .setMessage(R.string.text_message_need_permission)
                    .setCancelable(false)
                    .setPositiveButton(resources.getString(R.string.text_message_yes)
                    ) { _, _ -> finish() }

            val mADialog = mBuilder.create()
            mADialog.show()
        }
    }

    // 請求權限結果方法實作
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            333 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    picture()
                    Log.e("CheckPerm", "Write External Storage Permission Granted...")
                }
                return
            }
        }
        EasyPermissions.onRequestPermissionsResult(requestCode,
                permissions,
                grantResults,
                this)
    }

    // 要求藍芽權限的方法實作
    @AfterPermissionGranted(RequestPermission.REQ_CODE_ACCESS_FILE_LOCATION)
    private fun requestPermissionsForBluetooth() {
        if (!EasyPermissions.hasPermissions(this, RequestPermission.PERMISSION_ACCESS_FINE_LOCATION)) {
            EasyPermissions.requestPermissions(this,
                    getString(R.string.text_need_bluetooth_perm),
                    RequestPermission.REQ_CODE_ACCESS_FILE_LOCATION,
                    RequestPermission.PERMISSION_ACCESS_FINE_LOCATION)
        }
//        else {
//            checkBluetooth()
//        }
    }


    private fun checkBluetooth() {
        // 偵測手機是否內建藍芽·若有則偵測藍芽是否開啟
        val mBluetoothManager = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
        val mBluetoothAdapter = mBluetoothManager.adapter

        // 若手機不支援BLE則離開APP
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toastMakeTextAndShow(mContext, resources.getString(R.string.ble_not_supported),
                    Toast.LENGTH_SHORT)
            finish()
        }

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Utils.toastMakeTextAndShow(mContext, resources.getString(R.string.ble_not_supported),
                    Toast.LENGTH_SHORT)
            finish()
        } else {
            if (!mBluetoothAdapter.isEnabled) {
                //val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                //enableBtIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                //mContext.startActivityForResult(enableBtIntent,5)
                val intentOpenBluetoothSettings = Intent()
                intentOpenBluetoothSettings.action = android.provider.Settings.ACTION_BLUETOOTH_SETTINGS
                startActivity(intentOpenBluetoothSettings)
            }
        }
    }


    private fun uiFindViewById() {
        viewPager.offscreenPageLimit = 6
        naviView.menu?.findItem(R.id.nav_setting)?.isVisible = false
        //lightIcon?.setImageResource(R.drawable.app_android_icon_light)
    }

    @Suppress("DEPRECATION")
    private fun viewPagerInit() {
        // 加入 Fragment 成員
        val mMainFg = MainFragment()
/*
        val mTvocFg = TVOCFragment()
        val mEco2Fg = ECO2Fragment()
        val mTempFg = TempFragment()
        val mHumiFg = HumidiytFragment()
*/

        val mHumiFg = ChartFragment()
        val mTvocFg = ChartFragment()
        //val mEco2Fg = ChartFragment()
        val mTempFg = ChartFragment()
        val mPM25Fg = ChartFragment()
        val mPM10Fg = Pm10Fragment()

        mTvocFg.configFragment(DEFINE_FRAGMENT_TVOC)
        //mEco2Fg.configFragment(DEFINE_FRAGMENT_ECO2)
        mTempFg.configFragment(DEFINE_FRAGMENT_TEMPERATURE)
        mHumiFg.configFragment(DEFINE_FRAGMENT_HUMIDITY)
        mPM25Fg.configFragment(DEFINE_FRAGMENT_PM25)
        mPM10Fg.configFragment(DEFINE_FRAGMENT_PM10)

        mFragmentList.add(mMainFg)
        mFragmentList.add(mTvocFg)
        mFragmentList.add(mPM25Fg)
        mFragmentList.add(mPM10Fg)
        //mFragmentList.add(mEco2Fg)
        mFragmentList.add(mTempFg)
        mFragmentList.add(mHumiFg)


        mFragmentAdapter = FragmentAdapter(this.supportFragmentManager, mFragmentList)

        viewPager.adapter = mFragmentAdapter
        viewPager.isScrollable = true
        viewPager.currentItem = 0
        viewPager.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            // state：滑動狀態（0，1，2）
            // 1：滑動中 2：滑動完畢 0：閒置。
            override fun onPageScrollStateChanged(state: Int) {
            }

            /**************************
             * position :目前頁面，以及你點擊滑動的頁面 offset:目前頁面偏移的百分比
             * offsetPixels:目前頁面偏移的像素位置
             */
            override fun onPageScrolled(position: Int, offset: Float,
                                        offsetPixels: Int) {

                val share = getSharedPreferences(SavePreferences.SETTING_KEY, Activity.MODE_PRIVATE)
                val name = share.getString("name", "")

                if (name == "TVOC_NOSE") {
                    banDownDraw = position == 4     // 如果 position = 4，banDownDraw = true，反之 banDownDraw = false
                }
                //Log.e("offset:", offset.toString() + "")

                indicator.visibility = View.VISIBLE

                collapseIndicatorAnim(250)

                indicator.visibility = View.INVISIBLE
                /*
                val bundle = Bundle()
                bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "VIEW_SCROLL")
                bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CLICK")
                mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                */
            }

            override fun onPageSelected(position: Int) {
                Log.d("PageSelected", position.toString())
                currentIndex = position
                setImageBackground(position)
            }
        })

        mFragmentAdapter.notifyDataSetChanged()
    }

    // 如果 banDownDraw = true，封鎖下滑的手勢
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (banDownDraw) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    mLastMotionY = ev.y
                    Log.e(TAG, "EV Y: $mLastMotionY")
                }
                MotionEvent.ACTION_MOVE -> {
                    if (ev.y - mLastMotionY < 0) {
                        viewPager.isScrollable = false
                        Log.e(TAG, "EV Y: ${ev.y - mLastMotionY}")
                    }
                }
                MotionEvent.ACTION_UP -> {
                    viewPager.isScrollable = true
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar

        // 設定 actionbar layout
        //actionBar!!.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        //actionBar.setCustomView(R.layout.custom_actionbar)

        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        // 將 actionBar 和 DrawerLayout 取得關聯
        mDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.text_drawer_open, R.string.text_drawer_close)
        // 同步 actionBarDrawerToggle
        mDrawerToggle!!.syncState()
        // 設定 DrawerLayout 監聽事件
        drawerLayout!!.addDrawerListener(mDrawerToggle!!)

    }

    /*private fun checkLastPM25Value(): Boolean {
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        val dbSize = query.findAll().size - 1
        val lastPM25val = query.findAll()[dbSize]!!.pM25Value

        return lastPM25val != "65535"
    }*/

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        topMenu = menu
        //menuItem= menu!!.findItem(R.id.batStatus)
        bleIcon = menu!!.findItem(R.id.bleStatus)
        battreyIcon = menu.findItem(R.id.batStatus)
        shareMap = menu.findItem(R.id.shareMap)
        bleIcon!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        battreyIcon!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        shareMap!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        lightIcon = findViewById(R.id.imgLight)

        return super.onCreateOptionsMenu(menu)
    }

    //private var clickCount = 0

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item!!.itemId) {
        //電池點選顯示對話方塊先關掉
//            R.id.batStatus -> {
//                if(connState) {
//                    when (batValue) {
//                        in 1..100 -> dialogShow(getString(R.string.text_battery_title),
//                                getString(R.string.text_battery_value) + batValue + "%")
//                        in 101..200 -> dialogShow(getString(R.string.text_battery_title),
//                                "充電中")
//                        else -> dialogShow(getString(R.string.text_battery_title),
//                                getString(R.string.text_battery_value) + "1 %")
//                    }
//                }
//            }

//            R.id.bleStatus -> {
//                if(clickCount > 10) {
//                    startActivity(Intent(this@MainActivity, EngineerModeActivity::class.java))
//                    clickCount = 0
//                } else {
//                    clickCount++
//                }
//            }
//            R.id.bleStatus -> {
//                startActivity(Intent(this@MainActivity, PhotoActivity::class.java))
//            }
            R.id.shareMap -> {
                checkPermissions()
            }
        //點選ActionBAR會返回
            android.R.id.home -> {
                //checkUIState()
                checkLoginState()
                getDrawerLayoutItem = item
                mDrawerToggle!!.onOptionsItemSelected(item)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    //選單內容
    /*private fun dialogShow(title: String, content: String) {
        val i: Intent? = Intent(this, CustomDialogActivity::class.java)
        val bundle: Bundle? = Bundle()
        bundle!!.putString("dialogTitle", title)
        bundle.putString("dialogContent", content)

        i!!.putExtras(bundle)
        startActivity(i)
    }*/

    // 20171127 Peter 新增：AboutActivity, AirMapActivity
    private fun aboutShow() {
        //萬一DFU失敗時為Preference的Address加1
        val realAddress = myPref.getSharePreferenceMAC()

        val i: Intent? = Intent(this, AboutActivity::class.java)
                .putExtra("ADDRESS", realAddress)
                .putExtra("DEVICE_NAME", show_Device_Name?.text.toString())
        startActivity(i)
    }

    private fun trailMapShow() {
        if (connState == BleConnection.CONNECTED) {
            val isPrivacy = myPref.getSharePreferencePrivacy()
            if (isPrivacy) {
                DefaultPatternCheckingActivity.startAction(this@MainActivity,
                        DefaultPatternCheckingActivity.START_ACTION_MODE_NORMAL)
            } else {
                val mLang = Locale.getDefault().language + "-" + Locale.getDefault().country

                val i: Intent? = Intent(this, if (mLang == "zh-CN") {
                    GoldenMapActivity::class.java
                } else {
                    AirMapActivity::class.java
                })
                startActivity(i)
            }
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "TRAIL_MAP")
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CLICK")
            mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        } else if (connState == BleConnection.DISCONNECTED) {
            val uri = Uri.parse("https://www.addwii.com/product/mobile-nose-%E9%9A%A8%E8%BA%AB%E7%A9%BA%E6%B1%A1%E9%BC%BB-%E9%87%91/")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            startActivity(intent)
            val bundle = Bundle()
            bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "TRAIL_MAP_PROMOTION")
            bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CLICK")
            mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
        }
    }

    // 20171127 Raymond 新增：知識庫activity
    private fun knowledgeShow() {
        //blueToothDisconnect()
        val i: Intent? = Intent(this, KnowledgeActivity::class.java)
        startActivity(i)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "KNOW_HOW")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CLICK")
        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    // 20171219 Raymond 新增：Q&A activity
    private fun qandaShow() {
        val i: Intent? = Intent(this, QandAActivity::class.java)
        startActivity(i)
    }

    private fun settingShow() {
        val i: Intent? = Intent(this, SettingActivity::class.java)

        i!!.putExtra("CONN", connState == BleConnection.CONNECTED)

        startActivityForResult(i, REQUEST_SELECT_SAMPLE)
        //startActivity(i)
    }

    private fun tourShow() {
        val i: Intent? = Intent(this, TourActivity::class.java)
        startActivity(i)
    }

    private fun accountShow() {
        val shareToken = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        val myToken = shareToken.getString("token", "")
        mDrawerToggle!!.onOptionsItemSelected(getDrawerLayoutItem)
        if (myToken == "") {
            Log.e("checkMyTokem", "myToken = " + myToken)
            val i: Intent? = Intent(this, AccountManagementActivity::class.java)
            startActivity(i)
        } else {
            Log.e("checkMyTokem", "myToken = " + myToken)
            checkMyTokem(myToken)
        }


    }

    private fun setupDrawerContent(navigationView: NavigationView?) {
        navigationView?.setNavigationItemSelectedListener { menuItem ->
            selectDrawerItem(menuItem)
            true
        }
    }

    private fun selectDrawerItem(menuItem: MenuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        //var fragment: Fragment? = null
        //val fragmentClass: Class<*>
        when (menuItem.itemId) {
            R.id.nav_add_device -> blueToothConnect()
            R.id.nav_disconnect_device -> blueToothDisconnect()
            R.id.nav_about -> aboutShow()
            R.id.nav_accountManagement -> accountShow()
            R.id.nav_air_map -> publicMapShow("http://mjairql.com/air_map/", getString(R.string.text_title_Manifest_AirMap))
            R.id.nav_tour -> tourShow()
            R.id.nav_knowledge -> knowledgeShow()
            R.id.nav_qanda -> qandaShow()
            R.id.nav_getData -> {
            }
            R.id.nav_setting -> settingShow()
            R.id.nav_personal_map -> trailMapShow()
        }
        drawerLayout?.closeDrawer(GravityCompat.START)
    }

    //menuItem點下去後StartActivityResult等待回傳
    private fun blueToothConnect() {
        checkBluetooth()
        mLocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        isGPSEnabled = mLocationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        // 偵測手機是否內建藍芽·若有則偵測藍芽是否開啟
        val mBluetoothManager = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
        val mBluetoothAdapter = mBluetoothManager.adapter
        if (!isGPSEnabled) {
            setGPSEnabled()
        } else if (isGPSEnabled && mBluetoothAdapter.isEnabled) {
            val i: Intent? = Intent(this,
                    DeviceListActivity::class.java)
            //startActivity(i)
            startActivityForResult(i, REQUEST_SELECT_DEVICE)
        }
        //startActivityForResult(i,REQUEST_SELECT_DEVICE)
    }

    private fun blueToothDisconnect() {
        if (connState == BleConnection.CONNECTED) {
            //val serviceIntent: Intent? = Intent(BroadcastIntents.PRIMARY)
            //serviceIntent!!.putExtra("status", "disconnect")
            //sendBroadcast(serviceIntent)
            mUartService?.disconnect()
        } else {
            Log.d("MAIN", "BLEDISCONNTED ERROR")
        }
        //stopService(serviceIntent)
        //checkUIState()
    }

    private fun setGPSEnabled() {
        Toast.makeText(this, "無法取得定位，手機請開啟定位", Toast.LENGTH_SHORT).show()
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    //預熱畫面三個方法
    private fun heatingPanelShow() {
        waitLayout!!.visibility = View.VISIBLE
        val va = createDropAnim(waitLayout!!, 0, 100)
        va.start()
    }

    private fun heatingPanelHide() {
        val origHeight: Int = waitLayout!!.height
        val va: ValueAnimator = createDropAnim(waitLayout!!, origHeight, 0)
        va.addUpdateListener { }
        va.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                waitLayout!!.visibility = View.INVISIBLE
            }
        })
        va.start()
    }

    @SuppressLint("SetTextI18n")
    private fun heatingPanelControl(preheatCountDownString: String) {
        if (waitLayout!!.visibility == View.INVISIBLE) {
            heatingPanelShow()
            waitLayout!!.bringToFront()
        }

        if (preheatCountDownString != "255") {
            preheatCountDownInt = (120 - preheatCountDownString.toInt())
            Log.v(TAG, "Preheat Count Down: $preheatCountDownInt")
            waitLayout?.findViewById<TextView>(R.id.textView15)?.text = String.format(getString(R.string.text_message_heating), preheatCountDownInt)
            //if (waitLayout!!.visibility == View.VISIBLE) {
            //    heatingPanelHide()
            //}
            //waitLayout!!.bringToFront()
        }
        //120秒預熱畫面消失
        if (preheatCountDownString == "255") {
            if (waitLayout!!.visibility == View.VISIBLE) {
                heatingPanelHide()
            }
            waitLayout!!.bringToFront()
        }
    }

    private fun createDropAnim(view: View, start: Int, end: Int): ValueAnimator {
        val va: ValueAnimator = ValueAnimator.ofInt(start, end)
        va.addUpdateListener { valueAnimator ->
            val value: Int = valueAnimator!!.animatedValue as Int     //根据时间因子的变化系数进行设置高度
            val layoutParams: ViewGroup.LayoutParams = view.layoutParams
            layoutParams.height = value
            view.layoutParams = layoutParams    //设置高度
        }
        return va
    }


    //視回傳的code執行相對應的動作
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SELECT_DEVICE -> {
                //When the DeviceListActivity return, with the selected device address
                //得到Address後將Address後傳遞至Service後啟動 171129
                if (resultCode == Activity.RESULT_OK && data != null) {
                    if (connState == BleConnection.DISCONNECTED) {
                        mDeviceAddress = data.extras.getString("MAC")
                        val gattServiceIntent = Intent(this, UartService::class.java)
                        bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
                        val result = mUartService?.connect(mDeviceAddress)
                        Log.d(TAG, "Connect request result=$result")
                    }
                }
            }
        /*
        REQUEST_SELECT_SAMPLE -> {
            if (data != null) {
                val value = data.getIntExtra("choseCycle", 0)
                val uuintent: Intent? = Intent(BroadcastIntents.PRIMARY)
                uuintent!!.putExtra("status", "setSampleRate")

                when (value) {//resolution 1= 30 second
                    0 -> {//30s
                        uuintent.putExtra("SampleTime", 1)//
                    }
                    1 -> {//10min
                        uuintent.putExtra("SampleTime", 20)
                    }
                    2 -> {//15min
                        uuintent.putExtra("SampleTime", 30)
                    }
                    3 -> {//20min
                        uuintent.putExtra("SampleTime", 40)
                    }
                    4 -> {//30min
                        uuintent.putExtra("SampleTime", 60)
                    }
                    else -> {
                        uuintent.putExtra("SampleTime", 1)
                    }
                }
                sendBroadcast(uuintent)

            }
        }
        5->{
            if (resultCode == Activity.RESULT_OK) {

            }
            else{ finish() }
        }
        */
            else -> {
                print("test")
            }
        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastActions.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GET_NEW_DATA)
        intentFilter.addAction(BroadcastActions.ACTION_DATA_AVAILABLE)
        /*
        intentFilter.addAction(BroadcastActions.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BroadcastActions.ACTION_EXTRA_DATA)
        intentFilter.addAction(BroadcastActions.ACTION_GET_RESULT)
        intentFilter.addAction(BroadcastActions.ACTION_GET_HISTORY_COUNT)
        intentFilter.addAction(BroadcastActions.ACTION_LOADING_DATA)
        intentFilter.addAction(BroadcastActions.ACTION_STATUS_HEATING)
        */
        return intentFilter
    }

    private fun displayConnetedBatteryLife(batValue: Int) {
        val icon: AnimationDrawable
        when (batValue) {
        // Charge
            in 101..200 -> {
                when (batValue) {
                    in 188..200 -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bat_charge_6)
                        icon = battreyIcon?.icon as AnimationDrawable
                        if (icon.isRunning) {
                            icon.stop()
                        }
                    }
                    in 176..187 -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bat_charge_5)
                        icon = battreyIcon?.icon as AnimationDrawable
                        if (!icon.isRunning) {
                            icon.start()
                        }
                    }
                    in 141..175 -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bat_charge_4)
                        icon = battreyIcon?.icon as AnimationDrawable
                        if (!icon.isRunning) {
                            icon.start()
                        }
                    }
                    in 121..140 -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bat_charge_3)
                        icon = battreyIcon?.icon as AnimationDrawable
                        if (!icon.isRunning) {
                            icon.start()
                        }
                    }
                    in 110..120 -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bat_charge_2)
                        icon = battreyIcon?.icon as AnimationDrawable
                        if (!icon.isRunning) {
                            icon.start()
                        }
                    }
                    in 101..109 -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bat_charge_1)
                        icon = battreyIcon?.icon as AnimationDrawable
                        if (!icon.isRunning) {
                            icon.start()
                        }
                    }
                }
            }
        // Normal
            in 0..100 -> {
                /*if(icon.isRunning) {
                    icon.stop()
                }*/
                when (batValue) {
                    in 88..100 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x6)
                    in 76..87 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x5)
                    in 61..75 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x4)
                    in 41..60 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x3)
                    in 21..40 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x2)
                    in 10..20 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x1)
                    else -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_low)

                        //20180206
                        val allowNotify = myPref.getSharePreferenceAllowNotify()
                        val useLowBattNotify = myPref.getSharePreferenceAllowNotifyLowBattery()
                        //20180206
                        lowPowerCont++
                        if (allowNotify && useLowBattNotify && lowPowerCont >= 10)//&&(countsound220==5||countsound220==0))
                        {
                            lowPowerCont = 0
                            soundPool2.play(alertId, 1F, 1F, 0, 0, 1F)
                        }
                    }
                }
            }
        }
    }

    private val myBroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n", "CommitTransaction")
        override fun onReceive(context: Context?, intent: Intent) {
            //checkBluetooth()
            val action = intent.action
            when (action) {
                BroadcastActions.ACTION_GATT_CONNECTED -> {
                    connState = BleConnection.CONNECTED
                    checkUIState()
                    Log.d(TAG, "OnReceive: $action")
                }
                BroadcastActions.ACTION_GATT_DISCONNECTED -> {
                    connState = BleConnection.DISCONNECTED
                    isFirstC0 = true
                    isFirstC6 = true
                    arr1.clear()
                    arrIndexMap.clear()
                    lock = false
                    checkUIState()
                    Log.d(TAG, "OnReceive: $action")
                    myPref.setSharePreferenceCheckFWVersion(false)
                }
            /*
            BroadcastActions.ACTION_GET_NEW_DATA -> {
                val bundle = intent.extras
                //val tempVal = bundle.getString(BroadcastActions.INTENT_KEY_TEMP_VALUE)
                //val humiVal = bundle.getString(BroadcastActions.INTENT_KEY_HUMI_VALUE)
                //val tvocVal = bundle.getString(BroadcastActions.INTENT_KEY_TVOC_VALUE)
                //val co2Val = bundle.getString(BroadcastActions.INTENT_KEY_CO2_VALUE)
                //val pm25Val = bundle.getString(BroadcastActions.INTENT_KEY_PM25_VALUE).toInt()
                //batValue = bundle.getString(BroadcastActions.INTENT_KEY_BATTERY_LIFE).toInt()
                //val preheatCountDownString = bundle.getString(BroadcastActions.INTENT_KEY_PREHEAT_COUNT)
                //Log.v(TAG, "電池電量: $batValue%")
                // 預熱畫面控制
                //heatingPanelControl(preheatCountDownString)
                //displayConnetedBatteryLife()
            }
            */
                BroadcastActions.ACTION_DATA_AVAILABLE -> {
                    dataAvailable(intent)
                }
            }
            //Log.d("MainActivity", "OnReceive: $action")
            //checkUIState()
        }
    }

    @Synchronized
    private fun checkUIState() {
        if (connState == BleConnection.CONNECTED) {
            this.title = getString(R.string.ADDWII)
            val share = getSharedPreferences(SavePreferences.SETTING_KEY, Activity.MODE_PRIVATE)
            show_Dev_address?.text = myPref.getSharePreferenceMAC()
            show_Device_Name?.text = myPref.getSharePreferenceName()
            val shareMSG = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
            checkLoginState()
            /*naviView.menu?.findItem(R.id.nav_add_device)?.isVisible = false
            naviView.menu?.findItem(R.id.nav_disconnect_device)?.isVisible = true*/
            naviView.menu?.findItem(R.id.nav_setting)?.isVisible = true
            naviView.menu?.findItem(R.id.nav_getData)?.isVisible = false
            // 2018/05/03 ExpandableListView - Modify text by BLE status
            if (wvMapMain.visibility == 0) {
                wvMapMain.visibility = View.INVISIBLE
            }
        } else {
            this.title = getString(R.string.app_name_air_map)
            show_Dev_address?.text = ""
            show_Device_Name?.text = getString(R.string.No_Device_Connect)
            /*naviView.menu?.findItem(R.id.nav_add_device)?.isVisible = true
            naviView.menu?.findItem(R.id.nav_disconnect_device)?.isVisible = false*/
            naviView.menu?.findItem(R.id.nav_setting)?.isVisible = false
            naviView.menu?.findItem(R.id.nav_getData)?.isVisible = false
            heatingPanelHide()
            // 2018/05/03 ExpandableListView - Modify text by BLE status
            if (wvMapMain.visibility == 4) {
                wvMapMain.visibility = View.VISIBLE
            }
        }
        setExpandableDrawer()
        // 2018/05/03 ExpandableListView - use notify to change drawer text
        mMenuAdapter!!.notifyDataSetInvalidated()
        // **************************************************************** //
        invalidateOptionsMenu()

        Log.d("MAINcheckUIState", connState.toString())
    }

    // 2018/05/03 ExpandableListView
    private fun prepareListData() {
        listDataHeader.clear()
        // Group List connState == BleConnection.CONNECTED
        val drawer_01_Add_Device_Connected = ExpandedMenuModel()
        drawer_01_Add_Device_Connected.iconName = getString(R.string.UART_Disconnecting)
        drawer_01_Add_Device_Connected.iconImg = R.drawable.drawer01_add_device
        drawer_01_Add_Device_Connected.icHeadRidInt = R.string.text_drawer_01_add_device

        val drawer_01_Add_Device_Disconnected = ExpandedMenuModel()
        drawer_01_Add_Device_Disconnected.iconName = getString(R.string.text_navi_add_device)
        drawer_01_Add_Device_Disconnected.iconImg = R.drawable.drawer01_add_device
        drawer_01_Add_Device_Disconnected.icHeadRidInt = R.string.text_drawer_01_add_device

        val drawer_02_mobile_nose = ExpandedMenuModel()
        drawer_02_mobile_nose.iconName = getString(R.string.text_drawer_02_mobile_nose)
        drawer_02_mobile_nose.iconImg = R.drawable.drawer02_mobile_nose
        drawer_02_mobile_nose.icHeadRidInt = R.string.text_drawer_02_mobile_nose

        val drawer_03_air_map = ExpandedMenuModel()
        drawer_03_air_map.iconName = getString(R.string.text_drawer_03_air_map)
        drawer_03_air_map.iconImg = R.drawable.drawer03_air_map
        drawer_03_air_map.icHeadRidInt = R.string.text_drawer_03_air_map

        val drawer_04_personal_trail = ExpandedMenuModel()
        drawer_04_personal_trail.iconName = getString(R.string.text_drawer_04_personal_trail)
        drawer_04_personal_trail.iconImg = R.drawable.drawer04_personal_track
        drawer_04_personal_trail.icHeadRidInt = R.string.text_drawer_04_personal_trail

        val drawer_05_knowledge = ExpandedMenuModel()
        drawer_05_knowledge.iconName = getString(R.string.text_drawer_05_knowledge)
        drawer_05_knowledge.iconImg = R.drawable.drawer05_knowledge_info
        drawer_05_knowledge.icHeadRidInt = R.string.text_drawer_05_knowledge

        val drawer_06_QA = ExpandedMenuModel()
        drawer_06_QA.iconName = getString(R.string.text_drawer_06_QA)
        drawer_06_QA.iconImg = R.drawable.drawer06_qa
        drawer_06_QA.icHeadRidInt = R.string.text_drawer_06_QA

        val drawer_07_setting = ExpandedMenuModel()
        drawer_07_setting.iconName = getString(R.string.text_drawer_07_setting)
        drawer_07_setting.iconImg = R.drawable.drawer07_setting
        drawer_07_setting.icHeadRidInt = R.string.text_drawer_07_setting

        // Child List
        val child_02_mobile_nose = ArrayList<String>()
        child_02_mobile_nose.add(getString(R.string.text_drawer_02_1_user_experience))
        child_02_mobile_nose.add(getString(R.string.text_drawer_02_2_introduction))
        child_02_mobile_nose.add(getString(R.string.text_drawer_02_3_purchase))
        child_02_mobile_nose.add(getString(R.string.text_drawer_02_4_our_story))

        val child_06_QA = ArrayList<String>()
        child_06_QA.add(getString(R.string.text_drawer_06_1_FAQ))
        child_06_QA.add(getString(R.string.text_drawer_06_2_guide_tour))

        val child_07_setting = ArrayList<String>()
        child_07_setting.add(getString(R.string.text_drawer_07_1_account_setting))
        child_07_setting.add(getString(R.string.text_drawer_07_2_general_setting))

        if (connState == BleConnection.CONNECTED) { //空氣地圖收起
            listDataHeader.add(drawer_01_Add_Device_Connected)
            listDataHeader.add(drawer_02_mobile_nose)
            listDataHeader.add(drawer_03_air_map)
            listDataHeader.add(drawer_04_personal_trail)
            listDataHeader.add(drawer_05_knowledge)
            listDataHeader.add(drawer_06_QA)
            listDataHeader.add(drawer_07_setting)
            listDataChild[listDataHeader[1]] = child_02_mobile_nose
            listDataChild[listDataHeader[5]] = child_06_QA
            listDataChild[listDataHeader[6]] = child_07_setting
        } else {
            listDataHeader.add(drawer_01_Add_Device_Disconnected)
            listDataHeader.add(drawer_02_mobile_nose)
            drawer_04_personal_trail.iconName = getString(R.string.text_drawer_04_personal_trail_advanced)
            listDataHeader.add(drawer_04_personal_trail)
            listDataHeader.add(drawer_05_knowledge)
            drawer_07_setting.iconName = getString(R.string.text_drawer_07_1_account_setting)
            listDataHeader.add(drawer_07_setting)
            listDataChild[listDataHeader[1]] = child_02_mobile_nose
        }

    }


    private fun makeBluetoothStateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        return intentFilter
    }

    private val mBluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)

            when (state) {
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    mUartService?.disconnect()
                    checkUIState()
                    bluetoothStateStr = "BluetoothAdapter.STATE_TURNING_OFF"
                }
                BluetoothAdapter.STATE_OFF -> {
                    bluetoothStateStr = "BluetoothAdapter.STATE_OFF"
                }
                BluetoothAdapter.STATE_ON -> {
                    bluetoothStateStr = "BluetoothAdapter.STATE_ON"
                }
            }
            Log.v(TAG, "mBluetoothStateReceiver: $bluetoothStateStr")
        }
    }

    private fun checkLoginState() {
        val shareToken = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        val myToken = shareToken.getString("token", "")
        if (myToken == "") {
            text_Account_status?.text = getString(R.string.account_Deactivation)
        } else {
            val myName = shareToken.getString("name", "")
            val myEmail = shareToken.getString("email", "")
            when (myName) {
                "空汙鼻使用者" -> text_Account_status?.text = myEmail
                else -> text_Account_status?.text = myName
            }
            Log.e("MainActivity取名字", myName)
        }
    }

    //20180312
    private fun showDialog(msg: String) {
        val dialog = android.app.AlertDialog.Builder(this@MainActivity).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        dialog.setTitle(getString(R.string.remind))
        dialog.setMessage(msg)
        dialog.setCancelable(false)//讓返回鍵與空白無效
        //Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
        dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.confirm)) { dialog, _ ->
            dialog.dismiss()
            //finish()
        }
        dialog.show()
    }


    @SuppressLint("NewApi")
    private fun dataAvailable(intent: Intent) {
        val txValue = intent.getByteArrayExtra(BroadcastActions.ACTION_EXTRA_DATA)
        when (txValue[0]) {
            0xE0.toByte() -> {
            }
            0xE1.toByte() -> {
            }
            0xEA.toByte() -> {
            }
            else -> {
            }
        }
        when (txValue[2]) {
            0xB1.toByte() -> Log.d(TAG, "cmd:0xB1 feedback")
            0xB2.toByte() -> Log.d(TAG, "cmd:0xB2 feedback")
            0xB4.toByte() -> Log.d(TAG, "cmd:0xB4 feedback")
            0xB5.toByte() -> Log.d(TAG, "cmd:0xB5 feedback")
            0xB9.toByte() -> Log.d(TAG, "cmd:0xB9 feedback")
            0xBA.toByte() -> Log.d(TAG, "cmd:0xBA feedback")
        }
        when (txValue[3]) {
            0xE0.toByte() -> {
                Log.d("UART feeback", "ok"); }
            0xE1.toByte() -> {
                Log.d("UART feedback", "Couldn't write in device"); return
            }
            0xE2.toByte() -> {
                Log.d("UART feedback", "Temperature sensor fail"); return
            }
            0xE3.toByte() -> {
                Log.d("UART feedback", "B0TVOC sensor fail"); return
            }
            0xE4.toByte() -> {
                Log.d("UART feedback", "Pump power fail"); return
            }
            0xE5.toByte() -> {
                Log.d("UART feedback", "Invalid value"); return
            }
            0xE6.toByte() -> {
                Log.d("UART feedback", "Unknown command"); return
            }
            0xE7.toByte() -> {
                Log.d("UART feedback", "Waiting timeout"); return
            }
            0xE8.toByte() -> {
                Log.d("UART feedback", "Checksum error"); return
            }
        }

        if (errorTime >= 3) {
            errorTime = 0
        }
        if (!Utils.checkCheckSum(txValue)) {
            errorTime += 1
        } else {
            if (txValue.size > 5) {
                when (txValue[2]) {
                    0xB0.toByte() -> {
                        //var hashMap = BLECallingTranslate.getAllSensorKeyValue(txValue)
                        //heatingPanelControl(hashMap[TvocNoseData.B0PREH]!!)
                        //displayConnetedBatteryLife(hashMap[TvocNoseData.B0BATT]!!.toInt())
                        /*
                        val ble = BleEvent()
                        ble.B0TEMP = hashMap[TvocNoseData.B0TEMP]!!
                        ble.B0HUMI = hashMap[TvocNoseData.B0HUMI]!!
                        ble.B0TVOC = hashMap[TvocNoseData.B0TVOC]!!
                        ble.B0ECO2 = hashMap[TvocNoseData.B0ECO2]!!
                        ble.B0PM25 = hashMap[TvocNoseData.B0PM25]!!
                        ble.B0PREH = hashMap[TvocNoseData.B0PREH]!!
                        ble.B0BATT = hashMap[TvocNoseData.B0BATT]!!
                        EventBus.getDefault().post(ble)
                        */
                        val now = (System.currentTimeMillis() / 1000)
                        connectionInitMethod(now)
                    }
                    0xB1.toByte() -> {
                        val hashMap = BLECallingTranslate.parserGetInfoKeyValue(txValue)
                        MyApplication.putDevicePMType(hashMap[TvocNoseData.ISPM25].toString())
                        MyApplication.putDeviceVersion(hashMap[TvocNoseData.FW].toString())
                        MyApplication.putDeviceSerial(hashMap[TvocNoseData.FWSerial].toString())
                        MyApplication.putDeviceType(hashMap[TvocNoseData.DEVICE].toString())
                        Log.d("PARSERB1", hashMap.toString())

                        // 儲存裝置資訊
                        if(!hashMap[TvocNoseData.FW].isNullOrEmpty()) {
                            myPref.setSharePreferenceDeviceVer(hashMap[TvocNoseData.FW]!!)
                        }
                        if(!hashMap[TvocNoseData.FWSerial].isNullOrEmpty()) {
                            myPref.setSharePreferenceDeviceSer(hashMap[TvocNoseData.FWSerial]!!)
                        }
                        if(!hashMap[TvocNoseData.DEVICE].isNullOrEmpty()) {
                            myPref.setSharePreferenceDeviceType(hashMap[TvocNoseData.DEVICE]!!)
                        }

                        showPm10OrNot()
                        val aat = AirActionTask(this, "20${hashMap[TvocNoseData.FW]}${hashMap[TvocNoseData.FWSerial]}", "${hashMap[TvocNoseData.DEVICE]}")
                        aat.execute("postFWVersion")
                        Log.v("AirActionTask", "OVER")
                    }
                    0xB2.toByte() -> {
                        val hashMap = BLECallingTranslate.ParserGetSampleRateKeyValue(txValue)
                        checkSampleRate(hashMap)
                        mUartService?.writeRXCharacteristic(BLECallingTranslate.GetHistorySampleItems())
                        Log.d("0xB2", hashMap.toString())
                    }
                    0xB4.toByte() -> {
                        setMaxAvailableItems(txValue)
                    }
                    0xB5.toByte() -> {
                        //saveToRealm(txValue)
                    }
                    0xB9.toByte() -> {
                        val ledState = txValue[3].toInt()
                        val ledState2 = txValue[4].toInt()
                        if (txValue.size > 5) {
                            if (ledState == 1) {
                                MyApplication.isOnlineLedOn = false
                                myPref.setSharePreferenceLedOn(false)
                            } else {
                                MyApplication.isOnlineLedOn = true
                                myPref.setSharePreferenceLedOn(true)
                            }

                            if (ledState2 == 1) {
                                MyApplication.isOfflineLedOn = false
                                myPref.setSharePreferenceDisconnectLedOn(false)
                            } else {
                                MyApplication.isOfflineLedOn = true
                                myPref.setSharePreferenceDisconnectLedOn(true)
                            }
                            Log.e(TAG, "LED Status: $ledState")
                        }
                        //Log.d("0xB9",hashMap.toString())
                    }
                    0xBA.toByte() -> {
                        myPref.setSharePreferenceManualDisconn(true)
                        mUartService?.disconnect()
                        Log.e("0xBA", "Manual Disconnect from Device.........")
                    }
                    0xE0.toByte() -> {
                        val hashMap = BLECallingTranslate.getPM25KeyValue(txValue)
                        if (hashMap[TvocNoseData.PM25SR] != "5" || hashMap[TvocNoseData.PM25GST] != "30") {
                            mUartService?.writeRXCharacteristic(BLECallingTranslate.setPM25Rate(5))
                        }
                        Log.d("0xE0", hashMap.toString())
                    }
                    0xBB.toByte() -> {
                        val hashMap = BLECallingTranslate.parserGetRTCKeyValue(txValue)
                        Log.d("0xBB", hashMap.toString())
                    }
                    0xC0.toByte() -> {
                        val hashMap = BLECallingTranslate.getAllSensorC0KeyValue(txValue)
                        heatingPanelControl(hashMap[TvocNoseData.C0PREH]!!)
                        batValue = hashMap[TvocNoseData.C0BATT]!!.toInt()

                        MyApplication.putDeviceChargeStatus((batValue > 100))

                        displayConnetedBatteryLife(hashMap[TvocNoseData.C0BATT]!!.toInt())
                        val rtcTime = hashMap[TvocNoseData.C0TIME]!!.toLong()
                        connectionInitMethod(rtcTime)
                        Log.d("0xC0", hashMap.toString())

                    }
                    0xC5.toByte() -> {
                        val pmType = MyApplication.getDevicePMType().toInt()
                        if (pmType < 2) {
                            putC5ToObject(txValue)
                        } else { // 當Type有PM10跑另一個多型
                            putC5ToObject(txValue, pmType)
                        }
                    }
                    0xC6.toByte() -> {
                        val hashMap = BLECallingTranslate.ParserGetAutoSendDataKeyValueC6(txValue)
                        if (isFirstC6) {
                            isFirstC6 = false
                            if (maxItem > 0) {
                                changeMaxItem(hashMap)
                                val mainIntent = Intent(BroadcastIntents.PRIMARY)
                                mainIntent.putExtra("status", BroadcastActions.INTENT_KEY_GET_HISTORY_COUNT)
                                mainIntent.putExtra(BroadcastActions.INTENT_KEY_GET_HISTORY_COUNT, Integer.toString(maxItem))
                                sendBroadcast(mainIntent)
                                mUartService?.writeRXCharacteristic(BLECallingTranslate.getHistorySampleC5(1))
                                Log.d("AppropriateItem", maxItem.toString())
                            }
                        }
                        val pmType = MyApplication.getDevicePMType().toInt()
                        if (pmType < 2) {
                            saveToRealmC6(hashMap)
                        } else {
                            c6d6map = hashMap
                        }

                        mywarningclass?.judgeValue(hashMap[TvocNoseData.C6TVOC]!!.toInt(), hashMap[TvocNoseData.C6PM25]!!.toInt())
                        // warningClass!!.judgeValue(hashMap[TvocNoseData.C6TVOC]!!.toInt(), hashMap[TvocNoseData.C6PM25]!!.toInt())
                    }
                    0xD5.toByte() -> {
                        putD5ToObject(txValue)
                    }
                    0xD6.toByte() -> {
                        val hashMap = BLECallingTranslate.ParserGetAutoSendDataKeyValueD6(txValue)
                        saveToRealmD6(c6d6map, hashMap)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (triggerForAndroidOOn) {
                                countForAndroidO++
                                if (countForAndroidO >= 60) {
                                    countForAndroidO = 0
                                    mUartService?.disconnect()
                                }
                            }
                        }
                    }
                }
            } else {
                Log.d("0xB2OK", txValue.size.toString())
            }
        }
    }

    /*private fun checkCheckSum(input: ByteArray): Boolean {
        var checkSum = 0x00
        var max = 0xFF.toByte()
        for (i in 0 until input.size) {
            checkSum += input[i]
        }
        var checkSumByte = checkSum.toByte()
        return checkSumByte == max

    }*/

    private fun setMaxAvailableItems(tx: ByteArray) {
        val hashMap = BLECallingTranslate.parserGetHistorySampleItemsKeyValue(tx)
        maxItem = hashMap[TvocNoseData.MAXI]!!.toInt()
    }

    private fun connectionInitMethod(rtcTime: Long) {
        if (isFirstC0) {
            isFirstC0 = false
            val hh = Handler()
            checkRTCSetted(rtcTime)
            hh.postDelayed({ mUartService?.writeRXCharacteristic(BLECallingTranslate.GetSampleRate()) }, 500)
            hh.postDelayed({ mUartService?.writeRXCharacteristic(BLECallingTranslate.getPM25Rate()) }, 750)
            hh.postDelayed({ mUartService?.writeRXCharacteristic(BLECallingTranslate.getLedStateCMD()) }, 1000)
            hh.postDelayed({ mUartService?.writeRXCharacteristic(BLECallingTranslate.getInfo()) }, 1250)
        }
    }

    private fun checkRTCSetted(rtcTime: Long) {
        val now = (System.currentTimeMillis() / 1000)
        val secondOffset = Math.abs(rtcTime - now)
        Log.d("RTCOffSet", secondOffset.toString())
        if (secondOffset > 5) { //如果rtc秒數offset大於5秒才重set
            Log.d("NowTime", now.toString())
            val nowByte = ByteBuffer.allocate(8).putLong(now).array()
            mUartService?.writeRXCharacteristic(BLECallingTranslate.setRTC(nowByte))
        }
    }

    private fun checkSampleRate(hash: HashMap<String, String>) {
        val share = getSharedPreferences(TvocNoseData.ASMS, Context.MODE_PRIVATE)
        val setting0 = share.getString(TvocNoseData.B2SR, "2")
        val setting1 = share.getString(TvocNoseData.SOTR, "60")
        val setting2 = share.getString(TvocNoseData.STGS, "2")
        val setting3 = share.getString(TvocNoseData.POT, "1")
        val setting4 = share.getString(TvocNoseData.PTR, "2")
        Log.d("0xB2Compare",
                setting0 + ":" + hash[TvocNoseData.B2SR] + " " +
                        setting1 + ":" + hash[TvocNoseData.SOTR] + " " +
                        setting2 + ":" + hash[TvocNoseData.STGS] + " " +
                        setting3 + ":" + hash[TvocNoseData.POT] + " " +
                        setting4 + ":" + hash[TvocNoseData.PTR])

        if (setting0 == hash[TvocNoseData.B2SR]
                && setting1 == hash[TvocNoseData.SOTR]
                && setting2 == hash[TvocNoseData.STGS]
                && setting3 == hash[TvocNoseData.POT]
                && setting4 == hash[TvocNoseData.PTR]) {
            Log.d("0xB2", "True")
        } else {
            share.edit()
                    .putString(TvocNoseData.B2SR, "2")
                    .putString(TvocNoseData.SOTR, "60")
                    .putString(TvocNoseData.STGS, "2")
                    .putString(TvocNoseData.POT, "1")
                    .putString(TvocNoseData.PTR, "2").apply()
            val param = intArrayOf(2, 2 * 30, 2, 1, 2, 0, 0)
            mUartService?.writeRXCharacteristic(BLECallingTranslate.SetSampleRate(param))
        }
    }


    private fun putC5ToObject(tx: ByteArray) {
        val hashMap = BLECallingTranslate.parserGetHistorySampleItemKeyValueC5(tx)
        val share = getSharedPreferences(SavePreferences.SETTING_KEY, Context.MODE_PRIVATE)
        if (hashMap[TvocNoseData.C5TIME]!!.toLong() > 1514736000) {
            if (!lock) {
                indexMap.put("UTCBlockHead", hashMap[TvocNoseData.C5II]!!.toInt())
                lock = true
            }
        }
        if (hashMap[TvocNoseData.C5TIME]!!.toLong() == 0L) {
            if (lock) {
                indexMap["UTCBlockEnd"] = hashMap[TvocNoseData.C5II]!!.toInt()
                val indexCopy = indexMap.clone() as HashMap<String, Int>
                arr1.add(indexCopy)
                indexMap.clear()
                lock = false
            }
        }
        /*
        val realm = Realm.getDefaultInstance()
        val latiLongiObj = realm.where(AsmDataModel::class.java).equalTo("Created_time", hashMap[TvocNoseData.C5TIME]?.toLong()).findFirst()
        if (latiLongiObj != null) {
            lati = latiLongiObj.latitude
            longi = latiLongiObj.longitude
        }
        realm.close()
        */
        mDeviceAddress = myPref.getSharePreferenceMAC()
        hashMap.put(TvocNoseData.C5MACA, mDeviceAddress!!)
        hashMap.put(TvocNoseData.C5LATI, lati.toString())
        hashMap.put(TvocNoseData.C5LONGI, longi.toString())
        arrIndexMap.add(hashMap)


        var nowItem = hashMap[TvocNoseData.C5II]!!.toInt()
        Log.d("C5ToObject", nowItem.toString())
        nowItem++

        if (nowItem > maxItem) { //|| nowItem == countForItem) {
            /*
            if (Build.BRAND != "OPPO") {
                Toast.makeText(applicationContext, getText(R.string.Loading_Completely), Toast.LENGTH_SHORT).show()
            }
            */
            //如果到大筆後仍然沒有解鎖，設邊界值給他
            if (lock) {
                indexMap["UTCBlockEnd"] = maxItem
                val indexCopy = indexMap.clone() as HashMap<String, Int>
                arr1.add(indexCopy)
                indexMap.clear()
                lock = false
            }
            saveToRealmC5()
        } else {
            val mainIntent = Intent(BroadcastIntents.PRIMARY)
            mainIntent.putExtra("status", BroadcastActions.INTENT_KEY_LOADING_DATA)
            mainIntent.putExtra(BroadcastActions.INTENT_KEY_LOADING_DATA, Integer.toString(nowItem))
            sendBroadcast(mainIntent)
            mUartService?.writeRXCharacteristic(BLECallingTranslate.getHistorySampleC5(nowItem))
        }
        Log.d("C5ARR", arr1.toString())
    }

    private fun saveToRealmC5() {
        class SaveRealmTask : AsyncTask<Void, Void, String>() {
            override fun doInBackground(vararg params: Void?): String {
                try {
                    val realm = Realm.getDefaultInstance()
                    if (arr1.size == 0) {
                        val hash = HashMap<String, Int>()
                        hash["UTCBlockHead"] = 1
                        hash["UTCBlockEnd"] = maxItem
                        arr1.add(hash)
                    }
                    for (i in 0 until arr1.size) {
                        val head = arr1[i]["UTCBlockHead"]!! - 1
                        val end = arr1[i]["UTCBlockEnd"]!! - 1
                        for ((count, y) in (head..end).withIndex()) {
                            val time = if (arrIndexMap[y][TvocNoseData.C5TIME]!!.toLong() > 1514736000) {
                                arrIndexMap[y][TvocNoseData.C5TIME]!!.toLong() * 1000
                            } else {
                                (arrIndexMap[head][TvocNoseData.C5TIME]!!.toLong() - 60 * count) * 1000
                            }
                            val query = realm.where(AsmDataModel::class.java).equalTo("Created_time", time).findAll()
                            if (query.isEmpty() && time > 1514736000000) {
                                realm.executeTransaction { r ->
                                    val asmData = r.createObject(AsmDataModel::class.java, TvocNoseData.getMaxID())
                                    asmData.tempValue = arrIndexMap[y][TvocNoseData.C5TEMP].toString()
                                    asmData.humiValue = arrIndexMap[y][TvocNoseData.C5HUMI].toString()
                                    asmData.tvocValue = arrIndexMap[y][TvocNoseData.C5TVOC].toString()
                                    asmData.ecO2Value = arrIndexMap[y][TvocNoseData.C5ECO2].toString()
                                    asmData.pM25Value = arrIndexMap[y][TvocNoseData.C5PM25].toString()
                                    asmData.pM10Value = if (arrIndexMap[y][TvocNoseData.D5PM10]?.toInt() != null) arrIndexMap[y][TvocNoseData.D5PM10]?.toInt() else 0
                                    asmData.created_time = time //(arrIndexMap[head][TvocNoseData.C5TIME]!!.toLong() - 60 * count) * 1000//+ Calendar.getInstance().timeZone.rawOffset//getMyDate().getTime() - countForItem * getSampleRateUnit() * 30 * 1000 + (getSampleRateUnit() * counterB5 * 30 * 1000).toLong() + (getCorrectTime() * 30 * 1000).toLong()
                                    asmData.macAddress = arrIndexMap[y][TvocNoseData.C5MACA].toString()
                                    asmData.latitude = arrIndexMap[y][TvocNoseData.C5LATI]?.toFloat()
                                    asmData.longitude = arrIndexMap[y][TvocNoseData.C5LONGI]?.toFloat()
                                    Log.d("0xC5", asmData.toString())
                                }
                            }
                        }
                    }
                    realm.close()
                    return "DBWriteFinished"
                } catch (e: Exception) {
                    e.printStackTrace()
                    return "DBWriteError"
                }

            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                when (result) {
                    "DBWriteFinished" -> {
                        Utils.toastMakeTextAndShow(this@MainActivity, getString(R.string.Loading_Completely), Toast.LENGTH_SHORT)
                        val headTime = arrIndexMap.first()[TvocNoseData.C5TIME]!!.toLong() * 1000
                        myPref.setSharePreferencePairedC5LastTime(mDeviceAddress, headTime)
                    }
                    "DBWriteError" -> {
                        Log.d("MAIN", "DBWriteError")
                    }
                }
            }
        }
        SaveRealmTask().execute()
    }

    private fun saveToRealmC6(hashmap: HashMap<String, String>) {
        val realm = Realm.getDefaultInstance()
        val time = hashmap[TvocNoseData.C6TIME]!!.toLong() * 1000
        val query = realm.where(AsmDataModel::class.java).equalTo("Created_time", time).findAll()
        if (query.isEmpty() && time > 1514736000000) {
            realm.executeTransaction { r ->
                val asmData = r.createObject(AsmDataModel::class.java, TvocNoseData.getMaxID())
                asmData.tempValue = hashmap[TvocNoseData.C6TEMP].toString()
                asmData.humiValue = hashmap[TvocNoseData.C6HUMI].toString()
                asmData.tvocValue = hashmap[TvocNoseData.C6TVOC].toString()
                asmData.ecO2Value = hashmap[TvocNoseData.C6ECO2].toString()
                asmData.pM25Value = hashmap[TvocNoseData.C6PM25].toString()
                asmData.created_time = hashmap[TvocNoseData.C6TIME]!!.toLong() * 1000//getMyDate().getTime() - countForItem * getSampleRateUnit() * 30 * 1000 + (getSampleRateUnit() * counterB5 * 30 * 1000).toLong() + (getCorrectTime() * 30 * 1000).toLong()
                asmData.macAddress = mDeviceAddress
                asmData.latitude = TvocNoseData.lati
                asmData.longitude = TvocNoseData.longi
                Log.d("0xC6", asmData.toString())
            }
        }
        realm.close()
        uploadData()
    }

    @Subscribe
    fun onEvent(bleEvent: BleEvent) {
        /* 處理事件 */
        Log.d("AirAction", bleEvent.message)
        when (bleEvent.message) {
            "new SW version" -> {
                val appPackageName = packageName
                val Dialog = android.app.AlertDialog.Builder(this).create()
                Dialog.setTitle(getString(R.string.remind))
                Dialog.setMessage(getString(R.string.new_Version_Notify))
                Dialog.setCancelable(false)//讓返回鍵與空白無效
                Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.Reject))//否
                { dialog, _ ->
                    dialog.dismiss()
                }
                Dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Accept))//是
                { dialog, _ ->
                    dialog.dismiss()
                    try {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$appPackageName")))
                    } catch (anfe: android.content.ActivityNotFoundException) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$appPackageName")))
                    }
                }
                Dialog.show()

            }
        // 2018/05/29 Add "introduction" & "ourStory", modify sequence. Thanks the original creator!
            "new URL get" -> {
                buyURL = bleEvent.buyProduct!!
                introductionURL = bleEvent.introduction!!
                experienceURL = bleEvent.userExp!!
                ourStoryURL = bleEvent.ourStory!!
            }
        // 2018/07/17 Add check Account Token effective
            "successToken" -> {
                val i: Intent? = Intent(this, AccountActiveActivity::class.java)
                startActivity(i)
            }
            "ErrorTokenWithOnstart" -> {
                cleanUserData()
            }
            "ErrorTokenWithButton" -> {
                cleanUserData()
                val i: Intent? = Intent(this, AccountManagementActivity::class.java)
                startActivity(i)
            }
            // 2018/07/20 Check FW Status
            "New FW Arrival"->{
                runOnUiThread({
                    listDataHeader[6].FWIndicator = R.drawable.point_pink
                    mMenuAdapter!!.notifyDataSetInvalidated()
                })
            }
        }
    }

    //初始化導航圓點的方法
    private fun initpoint() {
        val count = indicator.childCount//獲取布局中圓點數量
        for (i in 0 until count) {
            //將布局中的圓點加入到圓點集合中
            points.add(indicator.getChildAt(i) as ImageView)
        }
        //設置第一張圖片（也就是圖片數組的0下標）的圓點狀態為觸摸實心狀態
        points[0].setImageResource(R.drawable.viewpager_indicator_focused)
    }

    //設選中圖片對應的導航原點的狀態
    private fun setImageBackground(selectImage: Int) {
        for (i in points.indices) {
            //如果選中圖片的下標等於圓點集合中下標的id，則改變圓點狀態
            if (i == selectImage) {
                points[i].setImageResource(R.drawable.viewpager_indicator_focused)
            } else {
                points[i].setImageResource(R.drawable.viewpager_indicator_unfocused)
            }
        }
    }


    // 關閉動畫
    private fun collapseIndicatorAnim(duration: Long) {
        /*val mHideAction = TranslateAnimation(Animation.RELATIVE_TO_PARENT, 0f,
                Animation.RELATIVE_TO_PARENT, 1.0f, Animation.RELATIVE_TO_PARENT,
                0.0f, Animation.RELATIVE_TO_PARENT, 0.0f)
        mHideAction.duration = duration

        indicator.startAnimation(mHideAction)*/
        val fadeOut = AlphaAnimation(1f, 0f)
        fadeOut.interpolator = AccelerateInterpolator()
        fadeOut.startOffset = duration
        fadeOut.duration = duration

        indicator.startAnimation(fadeOut)
    }

    private fun publicMapShow(url: String, title: String) {
        val i: Intent? = Intent(this, PublicMapActivity::class.java)
        i!!.putExtra("URL", url)
        i!!.putExtra("TITLE", title)
        startActivity(i)
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "PUBLIC_MAP")
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CLICK")
        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
    }

    private fun CheckSWversion() {
        val check = BuildConfig.VERSION_NAME
        var release = 0
        var internal = 0
        var external = 0
        var temp = 0
        var string = ""
        val spot: Char = "."[0]
        check.forEach {
            if (it == spot) {
                when (temp) {
                    0 -> {
                        release = string.toInt()
                    }
                    1 -> {
                        internal = string.toInt()
                    }
                }
                string = ""
                temp++
            } else {
                string += Character.toString(it)
            }
        }
        external = string.toInt()
        val apv = AppVersion(release, internal, external)
        apv.execute()
    }

    private fun checkUrl() {
        AppMenuTask().execute()
    }

    private fun uploadData() {
        val swCloudVal: Boolean = myPref.getSharePreferenceCloudUploadStat()
        val swCloud3GVal: Boolean = myPref.getSharePreferenceCloudUpload3GStat()
        if (swCloudVal) {
            val shareToken = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
            val token = shareToken.getString("token", "")
            val macAddressForDB = myPref.getSharePreferenceMAC()
            val networkStat = MyApplication.getConnectStatus()
            when (networkStat) {
                "MOBILE" -> {
                    if (swCloud3GVal) {
                        if (token != "") {
                            UploadTask().execute(macAddressForDB, token)
                        }
                    }
                }
                "WIFI" -> {
                    if (token != "") {
                        UploadTask().execute(macAddressForDB, token)
                    }
                }
                else -> {
                    Log.d(TAG, networkStat)
                }
            }
        }
    }

    private fun showPm10OrNot() {
        val pmType = MyApplication.getDevicePMType().toInt()
        val ll1 = mFragmentAdapter.fragmentList[0].view?.findViewById<LinearLayout>(R.id.llayoutbtmline1)
        try {
            if (pmType < 2) {
                if (ll1!!.childCount == 3) { ll1.findViewById<LinearLayout>(R.id.show_PM10).visibility = View.GONE }
            } else {
                if (ll1!!.childCount == 3) { ll1.findViewById<LinearLayout>(R.id.show_PM10).visibility = View.VISIBLE }
            }
            Log.d("ViewPager", viewPager.adapter?.count.toString())
        } catch (e: Exception) {
            Log.d(TAG, e.toString())
        }
    }

    //多型代入PM10TYPE,一筆C5一筆D5
    private fun putC5ToObject(tx: ByteArray, pm10type: Int) {
        Log.d(TAG, "putC5ToObject---$pm10type")
        val hashMap = BLECallingTranslate.parserGetHistorySampleItemKeyValueC5(tx)
        if (hashMap[TvocNoseData.C5TIME]!!.toLong() > 1514736000) {
            if (!lock) {
                indexMap.put("UTCBlockHead", hashMap[TvocNoseData.C5II]!!.toInt())
                lock = true
            }
        }
        if (hashMap[TvocNoseData.C5TIME]!!.toLong() == 0L) {
            if (lock) {
                indexMap["UTCBlockEnd"] = hashMap[TvocNoseData.C5II]!!.toInt()
                val indexCopy = indexMap.clone() as HashMap<String, Int>
                arr1.add(indexCopy)
                indexMap.clear()
                lock = false
            }
        }
        /*
        val realm = Realm.getDefaultInstance()
        val latiLongiObj = realm.where(AsmDataModel::class.java).equalTo("Created_time", hashMap[TvocNoseData.C5TIME]?.toLong()).findFirst()
        if (latiLongiObj != null) {
            lati = latiLongiObj.latitude
            longi = latiLongiObj.longitude
        }
        realm.close()
        */
        mDeviceAddress = myPref.getSharePreferenceMAC()
        hashMap.put(TvocNoseData.C5MACA, mDeviceAddress!!)
        hashMap.put(TvocNoseData.C5LATI, lati.toString())
        hashMap.put(TvocNoseData.C5LONGI, longi.toString())
        arrIndexMap.add(hashMap)


        var nowItem = hashMap[TvocNoseData.C5II]!!.toInt()
        mUartService?.writeRXCharacteristic(BLECallingTranslate.getHistorySampleD5(nowItem))
        Log.d("C5ToObjectINDEX", nowItem.toString())
    }

    private fun putD5ToObject(tx: ByteArray) {
        val hashMap = BLECallingTranslate.parserGetHistorySampleItemKeyValueD5(tx)
        //原來有INDEX!!
        var d5Index = hashMap[TvocNoseData.D5INDEX]!!.toInt()
        val d5PM10 = hashMap[TvocNoseData.D5PM10]
        val d5TIME = hashMap[TvocNoseData.D5TIME]
        val arrIndex = d5Index - 1
        if (arrIndexMap[arrIndex][TvocNoseData.C5TIME] == d5TIME) { //有點沒必要的判斷，不過還是加上去了，聊勝於無
            arrIndexMap[arrIndex][TvocNoseData.D5PM10] = d5PM10.toString()
            Log.d("D5PM10", "$d5Index + $d5PM10")
        } else {
            Log.e(TAG, "putD5ToObject時間不準就慘啦")
        }
        d5Index++
        if (d5Index > maxItem) { //|| nowItem == countForItem) {
            if (lock) {
                indexMap["UTCBlockEnd"] = maxItem
                val indexCopy = indexMap.clone() as HashMap<String, Int>
                arr1.add(indexCopy)
                indexMap.clear()
                lock = false
            }
            saveToRealmC5()
        } else {
            val mainIntent = Intent(BroadcastIntents.PRIMARY)
            mainIntent.putExtra("status", BroadcastActions.INTENT_KEY_LOADING_DATA)
            mainIntent.putExtra(BroadcastActions.INTENT_KEY_LOADING_DATA, Integer.toString(d5Index))
            sendBroadcast(mainIntent)
            mUartService?.writeRXCharacteristic(BLECallingTranslate.getHistorySampleC5(d5Index))
        }
        Log.d("C5D5ARR", arr1.toString())
    }

    private fun saveToRealmD6(hash1: HashMap<String, String>, hash2: HashMap<String, String>) {
        var c6Time = if (hash1[TvocNoseData.C6TIME] != null) hash1[TvocNoseData.C6TIME]!!.toLong() * 1000 else 0L
        var d6Time = if (hash2[TvocNoseData.D6TIME] != null) hash2[TvocNoseData.D6TIME]!!.toLong() * 1000 else 0L
        if (c6Time == d6Time && d6Time != 0L) {
            val realm = Realm.getDefaultInstance()
            val query = realm.where(AsmDataModel::class.java).equalTo("Created_time", c6Time).findAll()
            if (query.isEmpty() && d6Time > 1514736000000) {
                realm.executeTransaction { r ->
                    val asmData = r.createObject(AsmDataModel::class.java, TvocNoseData.getMaxID())
                    asmData.tempValue = hash1[TvocNoseData.C6TEMP].toString()
                    asmData.humiValue = hash1[TvocNoseData.C6HUMI].toString()
                    asmData.tvocValue = hash1[TvocNoseData.C6TVOC].toString()
                    asmData.ecO2Value = hash1[TvocNoseData.C6ECO2].toString()
                    asmData.pM25Value = hash1[TvocNoseData.C6PM25].toString()
                    asmData.created_time = hash1[TvocNoseData.C6TIME]!!.toLong() * 1000//getMyDate().getTime() - countForItem * getSampleRateUnit() * 30 * 1000 + (getSampleRateUnit() * counterB5 * 30 * 1000).toLong() + (getCorrectTime() * 30 * 1000).toLong()
                    asmData.pM10Value = hash2[TvocNoseData.D6PM10]?.toInt()
                    asmData.macAddress = mDeviceAddress
                    asmData.latitude = TvocNoseData.lati
                    asmData.longitude = TvocNoseData.longi
                    Log.d(TAG, "SUCCESSD6" + asmData.toString())
                }
            }
            realm.close()
            uploadData()
        }
        c6d6map.clear()
    }

    private fun checkMyTokem(myToken: String?) {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            //if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) 判斷Android3.0版本以上運行
            //TASK改用並行
            AccountCheckTokenTask(getMainActivity,"checkTokenBybtEvent").executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, myToken)
        } else {
            val i: Intent? = Intent(this, AccountRetryActivity::class.java)
            startActivity(i)
        }
    }

    private fun cleanUserData() {
        //失效的Token，清除Apk內所有使用者資料
        val share = getSharedPreferences("TOKEN", MODE_PRIVATE)
        share.edit().putString("token", "").apply()
        share.edit().putString("name", "").apply()
        share.edit().putString("email", "").apply()
        share.edit().putString("deviceLi", "").apply()
        if (Build.BRAND != "OPPO") {
            Toast.makeText(MyApplication.applicationContext(), R.string.errorToken, Toast.LENGTH_SHORT).show()
        }
    }

    private fun changeMaxItem(hashmap: HashMap<String, String>) {
        val c6Time = hashmap[TvocNoseData.C6TIME]!!.toLong() * 1000
        var maxC5DevicePairedTime = myPref.getSharePreferencePairedC5LastTime(mDeviceAddress)
        val calculateMaxItem = Math.abs(((c6Time - maxC5DevicePairedTime) / 60000L).toInt()) //預熱時必會拿到負數，所以要用絕對值讓他無限大
        val appropriateMaxItems = Math.min(calculateMaxItem, maxItem) //取兩方最小值，如果預熱時基本上都會拿到maxItem
        maxItem = appropriateMaxItems
        Log.d("calculateMaxItem", calculateMaxItem.toString())
        Log.d("appropriateMaxItems", appropriateMaxItems.toString())
    }

    private fun closeDialogShow() {
        val dlg = android.app.AlertDialog.Builder(this).create()
        dlg.setTitle(getString(R.string.dialog_close_title))
        dlg.setMessage(getString(R.string.dialog_close_content))
        dlg.setCancelable(true)//讓返回鍵與空白無效
        dlg.setButton(DialogInterface.BUTTON_POSITIVE, getString(android.R.string.yes))//是
        { _, _ ->
            System.exit(0)
        }
        dlg.setButton(DialogInterface.BUTTON_NEGATIVE, getString(android.R.string.no))//否
        { _, _ ->
            dlg.dismiss()
            /*val intent = Intent(Intent.ACTION_MAIN)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addCategory(Intent.CATEGORY_HOME)
            startActivity(intent)*/
        }
        dlg.show()
    }

    private fun setExpandableDrawer(){
        // 2018/05/03 ExpandableListView
        /*setNavigationView = findViewById<View>(R.id.naviView) as NavigationView

        setupDrawerContent(setNavigationView)*/ // Original

        setupDrawerContent(naviView)

        // Set Drawer's content
        prepareListData()

        // Set Drawer's Adapter
        mMenuAdapter = ExpandableListAdapter(this, listDataHeader, listDataChild, navigationmenu!!)
        navigationmenu!!.setAdapter(mMenuAdapter)

        // 2018/05/09 Expandable View, hide original indicator
        navigationmenu.setGroupIndicator(null)

        // Set Drawer's OnClickListener
        navigationmenu.setOnGroupClickListener({ parent, _, groupPosition, _ ->
            when (listDataHeader[groupPosition].icHeadRidInt) {
                R.string.text_drawer_01_add_device -> {
                    when (connState) {
                        BleConnection.CONNECTED -> blueToothDisconnect()
                        BleConnection.DISCONNECTED -> blueToothConnect()
                    }
                    drawerLayout?.closeDrawer(GravityCompat.START)
                }
                R.string.text_drawer_02_mobile_nose -> {
                    if (parent.isGroupExpanded(groupPosition)) {
                        parent.collapseGroup(groupPosition)
                    } else {
                        parent.expandGroup(groupPosition)
                        parent.setOnChildClickListener({ parent, _, groupPosition, childPosition, _ ->
                            when (childPosition) {
                                0 -> {
                                    if (experienceURL.isNotEmpty()) {
                                        val uri = Uri.parse(experienceURL)
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        startActivity(intent)
                                        val bundle = Bundle()
                                        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "USER_EXP")
                                        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "CLICK");
                                        mFirebaseAnalytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
                                    }
                                }
                                1 -> {
                                    if (introductionURL.isNotEmpty()) {
                                        val uri = Uri.parse(introductionURL)
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        startActivity(intent)
                                    }
                                }
                                2 -> {
                                    if (buyURL.isNotEmpty()) {
                                        val uri = Uri.parse(buyURL)
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        startActivity(intent)
                                    }
                                }
                                3 -> {
                                    if (ourStoryURL.isNotEmpty()) {
                                        val uri = Uri.parse(ourStoryURL)
                                        val intent = Intent(Intent.ACTION_VIEW, uri)
                                        startActivity(intent)
                                    }
                                }
                            }
                            drawerLayout?.closeDrawer(GravityCompat.START)
                            parent.collapseGroup(groupPosition)
                        })
                    }
                }
                R.string.text_drawer_03_air_map -> {
                    publicMapShow("https://mjairql.com/air_map/", getString(R.string.text_title_Manifest_AirMap));drawerLayout?.closeDrawer(GravityCompat.START)
                }
                R.string.text_drawer_04_personal_trail -> {
                    trailMapShow(); drawerLayout?.closeDrawer(GravityCompat.START)
                }
                R.string.text_drawer_05_knowledge -> {
                    knowledgeShow(); drawerLayout?.closeDrawer(GravityCompat.START)
                }
                R.string.text_drawer_06_QA -> {
                    if (parent.isGroupExpanded(groupPosition)) {
                        parent.collapseGroup(groupPosition)
                    } else {
                        parent.expandGroup(groupPosition)
                        parent.setOnChildClickListener({ parent, _, groupPosition, childPosition, _ ->
                            when (childPosition) {
                                0 -> qandaShow()
                                1 -> tourShow()
                            }
                            drawerLayout?.closeDrawer(GravityCompat.START)
                            parent.collapseGroup(groupPosition)
                        })
                    }
                }
                R.string.text_drawer_07_setting -> {
                    if (connState == BleConnection.CONNECTED) {
                        if (parent.isGroupExpanded(groupPosition)) {
                            parent.collapseGroup(groupPosition)
                        } else {
                            parent.expandGroup(groupPosition)
                            parent.setOnChildClickListener({ parent, _, groupPosition, childPosition, _ ->
                                when (childPosition) {
                                    0 -> accountShow()
                                    1 -> settingShow()
                                }
                                drawerLayout?.closeDrawer(GravityCompat.START)
                                parent.collapseGroup(groupPosition)
                            })
                        }
                    } else {
                        accountShow()
                    }

                }
            }
            Log.e("789798", listDataHeader[groupPosition].icHeadRidInt.toString())
            true
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (connState == BleConnection.CONNECTED) {
            battreyIcon!!.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x3)
            bleIcon!!.icon = AppCompatResources.getDrawable(mContext, R.drawable.bluetooth_connect)
            img_bt_status!!.setImageResource(R.drawable.app_android_icon_connect)
            battreyIcon?.isVisible = true
            bleIcon?.isVisible = true
            shareMap?.isVisible = false
        } else {
            battreyIcon!!.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_disconnect)
            bleIcon!!.icon = AppCompatResources.getDrawable(mContext, R.drawable.bluetooth_disconnect)
            img_bt_status!!.setImageResource(R.drawable.app_android_icon_disconnect)
            battreyIcon?.isVisible = false
            bleIcon?.isVisible = false
            shareMap?.isVisible = true
        }
        val shareToken = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        if (myPref.getSharePreferenceMAC() == "noValue" && shareToken.getString("email", "") == "") {
            battreyIcon?.isVisible = false
            bleIcon?.isVisible = false
        }

        return super.onPrepareOptionsMenu(menu)
    }

    private fun checkwvMapShow() {
        if (connState == BleConnection.CONNECTED) {
            wvMapMain.visibility = View.INVISIBLE
        } else {
            if (myPref.getSharePreferenceMAC() == "noValue") {
                wvMapMain.visibility = View.VISIBLE
            } else {
                wvMapMain.visibility = View.INVISIBLE
            }
        }
    }

    fun screenShot(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        Log.d("YYY", "done")
        return bitmap
    }
    private fun checkPermissions() {
        when {
            ActivityCompat.checkSelfPermission(this@MainActivity, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ->
                ActivityCompat.requestPermissions(this@MainActivity,
                        arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),333)
            else -> {
                picture()
                Log.e("CheckPerm", "Permission Granted...")
            }
        }
    }


    @SuppressLint("SimpleDateFormat")
    private  fun picture(){
        val bitmap = screenShot(window.decorView.rootView)
        val now = System.currentTimeMillis()
        val folderName = "ADDWII Mobile Nose"
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_hh-mm-ss")

        val folderPath = File("${android.os.Environment.getExternalStorageDirectory()}/$folderName")
        folderPath.mkdir()

        val mPath = "${folderPath.absolutePath}/${simpleDateFormat.format(now)}.jpg"

        val imageFile = File(mPath)
        shareContent(imageFile)
        val outputStream = FileOutputStream(imageFile)
        val quality = 100
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        outputStream.flush()
        outputStream.close()

    }

    private fun shareContent(imageFile: File) {
        try {
            val photoURI = FileProvider.getUriForFile(this, "$packageName.fileprovider", imageFile)
            Log.e("SHARE", photoURI.path)

            val intent = Intent(Intent.ACTION_SEND)

            intent.data = photoURI
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_STREAM, photoURI)  //圖片的實體路徑

            val chooser = Intent.createChooser(intent, "Share")

            //給目錄臨時的權限
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            // Verify the intent will resolve to at least one activity
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(chooser, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scrollingMission() {
        Handler().postDelayed({
            if (TvocNoseData.scrollingList.isNotEmpty()) {
                setViewSingleLine()
            } else {
                scrollindex++
                if (scrollindex < 10) {
                    ScrollingTextTask().execute()
                    scrollingMission()
                }
            }
        }, 6000)
    }


    @SuppressLint("SetTextI18n")
    private fun setViewSingleLine() {
        scrollViewsArr.clear()//记得加这句话，不然可能会产生重影现象
        Log.e("scrollingList", TvocNoseData.scrollingList.toString())
        for (i in 0 until TvocNoseData.scrollingList.size) {
            //设置滚动的单个布局
            val vScrollivs = LayoutInflater.from(mContext).inflate(R.layout.item_view_single, null)
            //初始化布局的控件
            val tv1 = vScrollivs.findViewById<TextView>(R.id.tvScrollContent)
            /**
             * 設置監聽
             */

            tv1.setOnClickListener {
                val url = Uri.parse(TvocNoseData.scrollingList[i]["url"].toString())
                val intent = Intent(Intent.ACTION_VIEW, url)
                startActivity(intent)
            }

            //进行对控件赋值
            tv1?.text = "${TvocNoseData.scrollingList[i]["title"]}..."
            Log.e("scrollingList[title]", TvocNoseData.scrollingList[i]["title"].toString())
            //添加到循环滚动数组里面去
            scrollViewsArr.add(vScrollivs)
        }
        upview?.setViews(scrollViewsArr)
    }

}


