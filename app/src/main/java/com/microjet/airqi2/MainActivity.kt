package com.microjet.airqi2

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ApplicationErrorReport.TYPE_NONE
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.graphics.drawable.AnimationDrawable
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.SoundPool
import android.net.ConnectivityManager
import android.net.ConnectivityManager.TYPE_WIFI
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.Contacts.PhonesColumns.TYPE_MOBILE
import android.provider.Settings
import android.support.annotation.RequiresApi
import android.support.design.widget.NavigationView
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.content.res.AppCompatResources
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.*
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import com.microjet.airqi2.BlueTooth.DeviceListActivity
import com.microjet.airqi2.BlueTooth.UartService
import com.microjet.airqi2.CustomAPI.CustomViewPager
import com.microjet.airqi2.CustomAPI.FragmentAdapter
import com.microjet.airqi2.CustomAPI.GetNetWork
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.Definition.RequestPermission
import com.microjet.airqi2.Definition.SavePreferences
import com.microjet.airqi2.Fragment.*
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import com.microjet.airqi2.Fragment.ChartFragment
import com.microjet.airqi2.Fragment.MainFragment
import com.microjet.airqi2.R.id.info
import com.microjet.airqi2.R.id.text_Account_status
import io.realm.Realm
import kotlinx.android.synthetic.main.drawer_header.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException
import java.lang.Math.E
import java.lang.reflect.Array.get
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private val DEFINE_FRAGMENT_TVOC = 1
    private val DEFINE_FRAGMENT_CO2 = 2
    private val DEFINE_FRAGMENT_TEMPERATURE = 3
    private val DEFINE_FRAGMENT_HUMIDITY = 4
    private val DEFINE_FRAGMENT_PM25 = 5
    public val mContext = this@MainActivity

    // Fragment 容器
    private val mFragmentList = ArrayList<Fragment>()

    // ViewPager
    private var mPageVp : CustomViewPager? = null

    // var viewPager = VerticalViewPager()
    // ViewPager目前頁面
    private var currentIndex : Int = 0

    // Drawer & NavigationBar
    private var mDrawerLayout : DrawerLayout? = null
    private var mDrawerToggle : ActionBarDrawerToggle? = null

    // 電池電量數值
    private var batValue : Int = 0

    // 藍芽icon in actionbar
    private var bleIcon : MenuItem? = null
    //電量icon
    private var battreyIcon : MenuItem? = null
    //private var menuItem: MenuItem? = null
    private var lightIcon : ImageView? = null

    private var connState = BleConnection.DISCONNECTED

    //Richard 171124
    private var nvDrawerNavigation: NavigationView? = null
    // private var mDevice: BluetoothDevice? = null
    //private var mBluetoothLeService: UartService? = null
    private val REQUEST_SELECT_DEVICE = 1
    private val REQUEST_SELECT_SAMPLE = 2

    //UArtService實體
    //private var mService: UartService? = null

    private var mIsReceiverRegistered : Boolean = false
    //private var mReceiver: MyBroadcastReceiver? = null
    private var isGPSEnabled : Boolean = false
    private var mLocationManager : LocationManager? = null

    // ***** 2017/12/11 Drawer連線 會秀出 Mac Address ************************* //
    private var drawerDeviceAddress : String? = null

    // ***** 2018/03/12 Drawer Show Device Name ******************************* //
    private var drawerDeviceName : String? = null

    // ***** 2018/03/12 Drawer Show Account Name ****************************** //
    private var drawerAccountName : String? = null


    // 20171212 Raymond added Wait screen
    private var mWaitLayout : RelativeLayout? = null
    private var mainLayout : LinearLayout? = null
    //private var mMainReceiver: BroadcastReceiver? = null
    private var preheatCountDownInt = 0

    private var topMenu : Menu? = null

    //20180122
    private var soundPool : SoundPool? = null
    private var alertId = 0
    private var lowPowerCont : Int=0

    // Code to manage Service lifecycle.
    private var mDeviceAddress : String? = null
    private var mUartService : UartService? = null

    private var lati = 121.4215f
    private var longi = 24.959742f
    private var locationListener : LocationListener? = null

    // FragmentAdapter
    private lateinit var mFragmentAdapter : FragmentAdapter

    //
    //private val mPM25Fg = ChartFragment()
    /** 是否禁止右劃標記  */
    private var banDownDraw : Boolean = false
    /** 手指在螢幕上的最後x坐標  */
    private var mLastMotionY : Float = 0.toFloat()

    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            mUartService = (service as UartService.LocalBinder).serverInstance
            if (!mUartService!!.initailze()) {
                Log.e(TAG, "Unable to initialize Bluetooth")
                finish()
            }
            // Automatically connects to the device upon successful start-up initialization.
            mUartService?.connect(mDeviceAddress)
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mUartService = null
        }
    }

    enum class BleConnection {
        CONNECTING, CONNECTED, DISCONNECTING,DISCONNECTED,
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e(TAG, "call onCreate")

        uiFindViewById()

        viewPagerInit()

        initActionBar()

        val dm = DisplayMetrics()
        this@MainActivity.windowManager.defaultDisplay.getMetrics(dm)
        Log.v("MainActivity", "Resolution: " + dm.heightPixels + "x" + dm.widthPixels)

        Log.e("Conn", MyApplication.getConnectStatus())

        if (!mIsReceiverRegistered) {
            LocalBroadcastManager.getInstance(mContext).registerReceiver(MyBroadcastReceiver, makeGattUpdateIntentFilter())
            mIsReceiverRegistered = true
        }

        setupDrawerContent(nvDrawerNavigation)

        UartService.nowActivity = this
        registerReceiver(mBluetoothStateReceiver, makeBluetoothStateIntentFilter())
        //20180206
        soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 100)
        alertId = soundPool!!.load(this, R.raw.low_power, 1)


        //20180209
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Log.d("MAINACUUID", MyApplication.getPsuedoUniqueID())

    }

    @SuppressLint("WifiManagerLeak")
    override fun onStart() {
        super.onStart()
        Log.e(TAG, "call onStart")
        //val serviceIntent: Intent? = Intent(this, UartService::class.java)
        //startService(serviceIntent)

        checkUIState()
        requestPermissionsForBluetooth()
        //checkBluetooth()


        val share = getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE)
        //val mBluetoothDeviceAddress = share.getString("mac", "noValue")
        mDeviceAddress = share.getString("mac", "noValue")
        if (mDeviceAddress != "noValue" && connState == BleConnection.DISCONNECTED) {
            val gattServiceIntent = Intent(this, UartService::class.java)
            bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
            mUartService?.connect(mDeviceAddress)
        }

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location?) {
                Log.d("LocationListener1",location?.longitude.toString())
                Log.d("LocationListener1",location?.latitude.toString())

                lati = location?.latitude!!.toFloat()
                longi = location?.longitude!!.toFloat()

                val intent: Intent? = Intent(BroadcastIntents.PRIMARY)
                intent?.putExtra("status", BroadcastActions.INTENT_KEY_LOCATION_VALUE)
                val bundle: Bundle? = Bundle()
                bundle?.putFloat(BroadcastActions.INTENT_KEY_LATITUDE_VALUE,lati)
                bundle?.putFloat(BroadcastActions.INTENT_KEY_LONGITUDE_VALUE,longi)
                intent!!.putExtra("TwoValueBundle",bundle)
                sendBroadcast(intent)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onProviderEnabled(provider: String?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onProviderDisabled(provider: String?) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        //20180311




        getLocation()
    }


    override fun onResume() {
        super.onResume()
        Log.e(TAG, "call onResume")

        if (mUartService == null) {
            connState = BleConnection.DISCONNECTED
            checkUIState()
        }
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "call onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "call onStop")
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.removeUpdates(locationListener)
    }


    //20180202
    override fun onBackPressed() {
        //實現模擬home鍵功能
        //super.onBackPressed();//這句話一定要註解掉，不然又會去掉用系統初始的back處理方式
        val intent = Intent(Intent.ACTION_MAIN)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }




    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "call onDestroy")
        if (mIsReceiverRegistered) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(MyBroadcastReceiver)
            mIsReceiverRegistered = false
        }

        unregisterReceiver(mBluetoothStateReceiver)

        //val serviceIntent: Intent? = Intent(BroadcastIntents.PRIMARY)
        //serviceIntent!!.putExtra("status", "disconnect")
        //sendBroadcast(serviceIntent)

        //val intent: Intent? = Intent(this, UartService::class.java)
        //stopService(intent)
        mUartService?.close()
        if (mUartService != null) {
            unbindService(mServiceConnection)
        }
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
        val mBuilder = AlertDialog.Builder(this)
        mBuilder.setTitle(R.string.text_message_need_permission)
                .setMessage(R.string.text_message_need_permission)
                .setCancelable(false)
                .setPositiveButton(resources.getString(R.string.text_message_yes)
                ) { _, _ -> finish() }

        val mADialog = mBuilder.create()
        mADialog.show()
    }

    // 請求權限結果方法實作
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

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
        mPageVp = this.findViewById(R.id.id_page_vp)
        mPageVp!!.offscreenPageLimit = 5
        mDrawerLayout = this.findViewById(R.id.drawer_layout)
        nvDrawerNavigation = this.findViewById(R.id.navigation)
        nvDrawerNavigation?.menu?.findItem(R.id.nav_setting)?.isVisible = false

        // 20171212 Raymond added Wait screen
        mWaitLayout = this.findViewById(R.id.waitLayout)
        mainLayout = this.findViewById(R.id.mainLayout)
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
        val mEco2Fg = ChartFragment()
        val mTempFg = ChartFragment()
        val mPM25Fg = ChartFragment()

        mTvocFg.ConfigFragment(DEFINE_FRAGMENT_TVOC)
        mEco2Fg.ConfigFragment(DEFINE_FRAGMENT_CO2)
        mTempFg.ConfigFragment(DEFINE_FRAGMENT_TEMPERATURE)
        mHumiFg.ConfigFragment(DEFINE_FRAGMENT_HUMIDITY)
        mPM25Fg.ConfigFragment(DEFINE_FRAGMENT_PM25)

        mFragmentList.add(mMainFg)
        mFragmentList.add(mTvocFg)
        mFragmentList.add(mEco2Fg)
        mFragmentList.add(mTempFg)
        mFragmentList.add(mHumiFg)
        mFragmentList.add(mPM25Fg)

        mFragmentAdapter = FragmentAdapter(this.supportFragmentManager, mFragmentList)

        mPageVp!!.adapter = mFragmentAdapter
        mPageVp!!.isScrollable = true
        mPageVp!!.currentItem = 0
        mPageVp!!.setOnPageChangeListener(object : ViewPager.OnPageChangeListener {

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

                val share = getSharedPreferences("MACADDRESS", Activity.MODE_PRIVATE)
                val name = share.getString("name", "")

                if(name == "TVOC_NOSE") {
                    banDownDraw = position == 4     // 如果 position = 4，banDownDraw = true，反之 banDownDraw = false
                }
                //Log.e("offset:", offset.toString() + "")
            }

            override fun onPageSelected(position: Int) {
                Log.d("PageSelected", position.toString())
                currentIndex = position
            }
        })

        mFragmentAdapter.notifyDataSetChanged()
    }

    // 如果 banDownDraw = true，封鎖下滑的手勢
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        // TODO Auto-generated method stub
        if (banDownDraw) {
            when (ev.action) {
                MotionEvent.ACTION_DOWN -> {
                    mLastMotionY = ev.y
                    Log.e(TAG, "EV Y: $mLastMotionY")
                }
                MotionEvent.ACTION_MOVE -> {
                    if (ev.y - mLastMotionY < 0) {
                        mPageVp!!.isScrollable = false
                        Log.e(TAG, "EV Y: ${ev.y - mLastMotionY}")
                    }
                }
                MotionEvent.ACTION_UP -> {
                    mPageVp!!.isScrollable = true
                }
            }
        }
        return super.dispatchTouchEvent(ev);
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
        mDrawerToggle = ActionBarDrawerToggle(this, mDrawerLayout, R.string.text_drawer_open, R.string.text_drawer_close)
        // 同步 actionBarDrawerToggle
        mDrawerToggle!!.syncState()
        // 設定 DrawerLayout 監聽事件
        mDrawerLayout!!.addDrawerListener(mDrawerToggle!!)

    }

    private fun checkLastPM25Value(): Boolean {
        val realm = Realm.getDefaultInstance()
        val query = realm.where(AsmDataModel::class.java)
        val dbSize = query.findAll().size - 1
        val lastPM25val = query.findAll()[dbSize]!!.pM25Value

        return lastPM25val != "65535"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        topMenu = menu
        //menuItem= menu!!.findItem(R.id.batStatus)
        bleIcon = menu!!.findItem(R.id.bleStatus)
        battreyIcon = menu.findItem(R.id.batStatus)
        bleIcon!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        battreyIcon!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        lightIcon = findViewById(R.id.imgLight)

        return super.onCreateOptionsMenu(menu)
    }

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



        //點選ActionBAR會返回
            android.R.id.home -> {
                checkUIState()
                checkLoginState()
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
        val i: Intent? = Intent(this, AboutActivity::class.java)
        startActivity(i)
    }

    private fun airmapShow() {
        val i: Intent? = Intent(this, AirMapActivity::class.java)
        startActivity(i)
    }

    // 20171127 Raymond 新增：知識庫activity
    private fun knowledgeShow() {
        val i: Intent? = Intent(this, KnowledgeActivity::class.java)
        startActivity(i)
    }

    // 20171219 Raymond 新增：Q&A activity
    private fun qandaShow() {
        val i: Intent? = Intent(this, QandAActivity::class.java)
        startActivity(i)
    }

    private fun settingShow() {
        val i: Intent? = Intent(this, SettingActivity::class.java)
        startActivityForResult(i, REQUEST_SELECT_SAMPLE)
        //startActivity(i)
    }

    private fun tourShow() {
        val i: Intent? = Intent(this, TourActivity::class.java)
        startActivity(i)
    }

    private fun accountShow() {
        val shareToKen = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        val MyToKen = shareToKen.getString("token", "")
        if(GetNetWork.isFastGetNet) {
        if(MyToKen=="") {
                Log.e("主葉面看偷肯",MyToKen)
            val i: Intent? = Intent(this, AccountManagementActivity::class.java)
            //text_Account_status.setText(R.string.account_Deactivation)
            startActivity(i)
        }else{
                Log.e("主葉面!=空字串看偷肯",MyToKen)
            val i: Intent? = Intent(this, AccountActive::class.java)
            //text_Account_status.setText(R.string.account_Activation)
            startActivity(i)
        }
        }else{
            showDialog("請連接網路")
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
            R.id.nav_about ->  aboutShow()
            R.id.nav_accountManagement -> accountShow()
            R.id.nav_air_map -> airmapShow()
            R.id.nav_tour -> tourShow()
            R.id.nav_knowledge -> knowledgeShow()
            R.id.nav_qanda -> qandaShow()
            R.id.nav_getData -> {            }
            R.id.nav_setting -> settingShow()
        }
        mDrawerLayout?.closeDrawer(GravityCompat.START)
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
            startActivityForResult(i,REQUEST_SELECT_DEVICE)
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
            Log.d("MAIN","BLEDISCONNTED ERROR")
        }
        //stopService(serviceIntent)
        checkUIState()
    }

    private fun setGPSEnabled() {
        Toast.makeText(this, "無法取得定位，手機請開啟定位", Toast.LENGTH_SHORT).show()
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    private fun heatingPanelShow() {
        mWaitLayout!!.visibility = View.VISIBLE
        val va = createDropAnim(mWaitLayout!!, 0, 100)
        va.start()
    }

    private fun heatingPanelHide() {
        val origHeight: Int = mWaitLayout!!.height
        val va: ValueAnimator = createDropAnim(mWaitLayout!!, origHeight, 0)
        va.addUpdateListener { }
        va.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                mWaitLayout!!.visibility = View.INVISIBLE
            }
        })
        va.start()
    }

    @SuppressLint("SetTextI18n")
    private fun heatingPanelControl(preheatCountDownString : String) {
        if (mWaitLayout!!.visibility == View.INVISIBLE) {
            heatingPanelShow()
            mWaitLayout!!.bringToFront()
        }

        if (preheatCountDownString != "255") {
            preheatCountDownInt = (120 - preheatCountDownString.toInt())
            Log.v(TAG, "Preheat Count Down: $preheatCountDownInt")
            mWaitLayout?.findViewById<TextView>(R.id.textView15)?.text = resources.getString(R.string.text_message_heating) + preheatCountDownInt.toString() + "秒"
            //if (mWaitLayout!!.visibility == View.VISIBLE) {
            //    heatingPanelHide()
            //}
            //mWaitLayout!!.bringToFront()
        }
        //120秒預熱畫面消失
        if (preheatCountDownString == "255") {
            if (mWaitLayout!!.visibility == View.VISIBLE) {
                heatingPanelHide()
            }
            mWaitLayout!!.bringToFront()
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
                        print("MainActivity")
                        val result = mUartService?.connect(mDeviceAddress)
                        Log.d(TAG, "Connect request result=$result")
                    }
                }
            }

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
            else -> {
                print("test")
            }
        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BroadcastActions.ACTION_GATT_CONNECTED)
        intentFilter.addAction(BroadcastActions.EXTRA_DATA)
        //intentFilter.addAction(BroadcastActions.ACTION_GATT_SERVICES_DISCOVERED)
        intentFilter.addAction(BroadcastActions.ACTION_GATT_DISCONNECTED)
        intentFilter.addAction(BroadcastActions.ACTION_GET_NEW_DATA)
        intentFilter.addAction(BroadcastActions.ACTION_GET_RESULT)
        intentFilter.addAction(BroadcastActions.ACTION_GET_HISTORY_COUNT)
        intentFilter.addAction(BroadcastActions.ACTION_LOADING_DATA)
        intentFilter.addAction(BroadcastActions.ACTION_STATUS_HEATING)
        return intentFilter
    }

    private fun displayConnetedBatteryLife() {
        val icon: AnimationDrawable
        when (batValue) {
        // Charge
            in 101..200 -> {
                when (batValue) {
                    in 198..200 -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bat_charge_6)
                        icon = battreyIcon?.icon as AnimationDrawable
                        if (icon.isRunning) {
                            icon.stop()
                        }
                    }
                    in 180..198 -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bat_charge_5)
                        icon = battreyIcon?.icon as AnimationDrawable
                        if (!icon.isRunning) {
                            icon.start()
                        }
                    }
                    in 160..180 -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bat_charge_4)
                        icon = battreyIcon?.icon as AnimationDrawable
                        if (!icon.isRunning) {
                            icon.start()
                        }
                    }
                    in 140..160 -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bat_charge_3)
                        icon = battreyIcon?.icon as AnimationDrawable
                        if (!icon.isRunning) {
                            icon.start()
                        }
                    }
                    in 120..140 -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bat_charge_2)
                        icon = battreyIcon?.icon as AnimationDrawable
                        if (!icon.isRunning) {
                            icon.start()
                        }
                    }
                    in 101..120 -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bat_charge_1)
                        icon = battreyIcon?.icon as AnimationDrawable
                        if (!icon.isRunning) {
                            icon.start()
                        }
                    }
                }
            }
        // Normal
            in 10..100 -> {
                /*if(icon.isRunning) {
                    icon.stop()
                }*/
                when (batValue) {
                    in 96..100 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x6)
                    in 76..95 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x5)
                    in 56..75 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x4)
                    in 41..55 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x3)
                    in 21..40 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x2)
                    in 10..20 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x1)
                }
            }
        // Exception
            else -> {
                battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_low)

                //20180206
                val mPreference = this.application.getSharedPreferences(SavePreferences.SETTING_KEY, 0)
                //20180206
                lowPowerCont++
                if (mPreference.getBoolean(SavePreferences.SETTING_BATTERY_SOUND, false) && lowPowerCont >= 10)//&&(countsound220==5||countsound220==0))
                {
                    lowPowerCont=0
                    soundPool!!.play(alertId,1F , 1F, 0, 0, 1F)
                }
            }
        }
    }

    private val TAG = MainActivity::class.java.simpleName
    // inner class MyBroadcastReceiver : BroadcastReceiver()
    private val MyBroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n", "CommitTransaction")
        override fun onReceive(context: Context?, intent: Intent) {
            //updateUI(intent)
            checkBluetooth()
            val action = intent.action
            when (action) {
                BroadcastActions.ACTION_GATT_CONNECTED -> {
                    connState = BleConnection.CONNECTED
                    val bundle = intent.extras
                    drawerDeviceAddress = bundle.getString(BroadcastActions.INTENT_KEY_DEVICE_ADDR)
                    battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x3)

                    val share = getSharedPreferences("MACADDRESS", Activity.MODE_PRIVATE)
                    //val nameshare = getSharedPreferences("authorization", "Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImp0aSI6IjBlODQ3OWU2MjRhNzVjMThiOTAyYzYzZTI1NzgwMDY4MzU1NTNkNWEyNzNlOWMwMTVjNzYzZDBkOGM2NjNiNWMyNTI1ZGI0Y2I1ZDU5NTk3In0.eyJhdWQiOiIxIiwianRpIjoiMGU4NDc5ZTYyNGE3NWMxOGI5MDJjNjNlMjU3ODAwNjgzNTU1M2Q1YTI3M2U5YzAxNWM3NjNkMGQ4YzY2M2I1YzI1MjVkYjRjYjVkNTk1OTciLCJpYXQiOjE1MjEwMTMwNTIsIm5iZiI6MTUyMTAxMzA1MiwiZXhwIjoxNTUyNTQ5MDUyLCJzdWIiOiIzMTEiLCJzY29wZXMiOltdfQ.aL5qFGRYFGgRR25DYJvnmo7YotOr9AE7GpzHdkJ6UaCN87A0ejThEPTdoMW-CiRhdQ4Yslm7ICoz45vDR4Hzrn4MrBLcPmMRuEXFwasdHfL-kLev2d8XH2JzuPBJjwit2n482CpQezXraVOroL5D2Rnd5jWLza5_8Nj5yG-RKvRMY6nF9rMt2TBvhhemFVPJs55mFaFKwvWUWxXKr1mLdpYIzecCshzRhHFBFqzM_5ZMrCiLg-yz0jvnmWlfxgVzM1XDVe9T-hx3OV98Rx8jRBbf10auXQ_lqtOFcEKQpzRQuiN7XVO7pYvIR47-hn34csan0XspWO04TXK685-1vfygc2vN7rn87_FUeIxeosj8YLLdmM5xCshzDxzfKfgquTEOeReIcXlHBne7skOudvR6qW54UebFxSb7OImdjWBC3Y8IbcRQraTSXh3GfWWni7XonLDQGcx5V5OYxDHV-RYd64l_9ZlTV01AYVMJ5C_QMVgPA0UAnjm5mLudBLKCW4a_m6plc_ZXBEulPzRer0BIZP4Gl1HBBEeE1h2vr58ixPXIGgRvslnIr98FlcfqpS2vlI5VQQokDmCtBlJq8VaIuAi1VXtpRVnZ9wcR94ws_puuHLadHLWFzGviSGwUgpKeF5qQGqx4TTB81YY9bJjsY7DJIONgVdIPiNtbtGo")
                    val deviceName = share.getString("name", "")
                    //val accountName = share.getString("name","")

                    val shareMSG = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
                    val accountName = shareMSG.getString("name", "")
                    val myEmail= shareMSG.getString("email","")

                    drawerDeviceName = deviceName
                    drawerAccountName = accountName

                    // 判斷連線的裝置是TVOC_NOSE還是PM2.5_NOSE
                    /*if(name == "TVOC_NOSE") {
                        this@MainActivity.supportFragmentManager.beginTransaction().hide(mPM25Fg).commit()
                    } else {
                        this@MainActivity.supportFragmentManager.beginTransaction().show(mPM25Fg).commit()
                    }*/
                    // drawerDeviceAddress = intent.getStringExtra("macAddress")
                    //    updateUI(intent)
                }
                BroadcastActions.ACTION_GATT_DISCONNECTED -> {
                    connState = BleConnection.DISCONNECTED
                    battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_disconnect)
                    heatingPanelHide()
                    //    updateUI(intent)
                }
                BroadcastActions.ACTION_GET_NEW_DATA -> {
                    val bundle = intent.extras
                    //val tempVal = bundle.getString(BroadcastActions.INTENT_KEY_TEMP_VALUE)
                    //val humiVal = bundle.getString(BroadcastActions.INTENT_KEY_HUMI_VALUE)
                    //val tvocVal = bundle.getString(BroadcastActions.INTENT_KEY_TVOC_VALUE)
                    //val co2Val = bundle.getString(BroadcastActions.INTENT_KEY_CO2_VALUE)
                    //val pm25Val = bundle.getString(BroadcastActions.INTENT_KEY_PM25_VALUE).toInt()
                    batValue = bundle.getString(BroadcastActions.INTENT_KEY_BATTERY_LIFE).toInt()
                    val preheatCountDownString = bundle.getString(BroadcastActions.INTENT_KEY_PREHEAT_COUNT)
                    Log.v(TAG, "電池電量: $batValue%")
                    // 預熱畫面控制
                    heatingPanelControl(preheatCountDownString)
                    displayConnetedBatteryLife()
                }
            }
            Log.d("MainActivity", "OnReceive: $action")
            checkUIState()
        }


    }

    @Synchronized private fun checkUIState() {
        if (connState == BleConnection.CONNECTED) {
            nvDrawerNavigation?.menu?.findItem(R.id.nav_add_device)?.isVisible = false
            nvDrawerNavigation?.menu?.findItem(R.id.nav_disconnect_device)?.isVisible = true
            nvDrawerNavigation?.getHeaderView(0)?.findViewById<TextView>(R.id.show_Dev_address)?.text = drawerDeviceAddress
            nvDrawerNavigation?.getHeaderView(0)?.findViewById<TextView>(R.id.show_Device_Name)?.text = drawerDeviceName
            nvDrawerNavigation?.getHeaderView(0)?.findViewById<TextView>(R.id.text_Account_status)?.text = drawerAccountName
            nvDrawerNavigation?.getHeaderView(0)?.findViewById<ImageView>(R.id.img_bt_status)?.setImageResource(R.drawable.app_android_icon_connect)
            bleIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bluetooth_connect)
            nvDrawerNavigation?.menu?.findItem(R.id.nav_setting)?.isVisible = true
            nvDrawerNavigation?.menu?.findItem(R.id.nav_getData)?.isVisible = false
        } else {
            nvDrawerNavigation?.menu?.findItem(R.id.nav_add_device)?.isVisible = true
            nvDrawerNavigation?.menu?.findItem(R.id.nav_disconnect_device)?.isVisible = false
            nvDrawerNavigation?.getHeaderView(0)?.findViewById<TextView>(R.id.show_Dev_address)?.text = ""
            nvDrawerNavigation?.getHeaderView(0)?.findViewById<TextView>(R.id.show_Device_Name)?.text = getText(R.string.No_Device_Connect)
            nvDrawerNavigation?.getHeaderView(0)?.findViewById<ImageView>(R.id.img_bt_status)?.setImageResource(R.drawable.app_android_icon_disconnect)
            bleIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bluetooth_disconnect)
            nvDrawerNavigation?.menu?.findItem(R.id.nav_setting)?.isVisible = false
            nvDrawerNavigation?.menu?.findItem(R.id.nav_getData)?.isVisible = false
            lightIcon?.setImageResource(R.drawable.app_android_icon_light)
        }
        Log.d("MAINcheckUIState", connState.toString())
    }


    private fun makeBluetoothStateIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        return intentFilter
    }

    private val mBluetoothStateReceiver = object: BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF)
            var stateStr = "BluetoothAdapter.STATE_OFF"

            when (state) {
                BluetoothAdapter.STATE_TURNING_OFF -> {
                    stateStr = "BluetoothAdapter.STATE_TURNING_OFF"
                }
                BluetoothAdapter.STATE_OFF -> {
                    stateStr = "BluetoothAdapter.STATE_OFF"
                }
                BluetoothAdapter.STATE_ON -> {
                    stateStr = "BluetoothAdapter.STATE_ON"
//                    val share = getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE)
//                    val mBluetoothDeviceAddress = share.getString("mac", "noValue")
//
//                    if (mBluetoothDeviceAddress != "noValue" && !connState) {
//                        val mainintent = Intent(BroadcastIntents.PRIMARY)
//                        mainintent.putExtra("status", BroadcastActions.ACTION_CONNECT_DEVICE)
//                        mainintent.putExtra("mac", mBluetoothDeviceAddress)
//                        sendBroadcast(mainintent)
//                    }
                }
            }
            Log.v(TAG, "mBluetoothStateReceiver: $stateStr")
        }
    }
//private boolean isAppIsInBackground(Context context) {
//        var boolean isInBackground = true;
//        var ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
//            var List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
//            for (ActivityManager. android.app.ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
//                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
//                    for (String activeProcess : processInfo.pkgList) {
//                        if (activeProcess.equals(context.getPackageName())) {
//                            isInBackground = false;
//                        }
//                    }
//                }
//            }
//        } else {
//            var List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
//             ComponentName componentInfo = taskInfo.get(0).topActivity;
//            if (componentInfo.getPackageName().equals(context.getPackageName())) {
//                isInBackground = false;
//            }
//        }
//
//        return isInBackground;
//    }
/*
    //20180307
    private var client: OkHttpClient? = null
    private val hasBeenUpLoaded = java.util.ArrayList<Int>()
    private inner class postDataAsyncTasks : AsyncTask<String, Void, String>() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        override fun doInBackground(vararg params: String): String? {
            var return_body: RequestBody? = null
            var getResponeResult = java.lang.Boolean.parseBoolean(null)
            try {
                //取得getRequestBody
                return_body = getRequestBody()
                //呼叫getResponse取得結果
                if (return_body!!.contentLength() > 0) {
                    getResponeResult = getResponse(return_body)

                    if (getResponeResult) {
                        //呼叫updateDB_UpLoaded方法更改此次傳輸的資料庫資料欄位UpLoaded
                        val DBSucess = updateDB_UpLoaded()
                        if (DBSucess) {
                            Log.e("幹改進去", DBSucess.toString())
                        }
                        hasBeenUpLoaded.clear()
                    } else {
                        Log.e("幹改失敗拉!!", getResponeResult.toString())
                    }
                } else {
                    Log.e("幹太少筆啦!", return_body.contentLength().toString())
                }

            } catch (e: Exception) {
                Log.e("return_body_erro", e.toString())
            }
            return null
        }
    }
    //20180307
    private fun getRequestBody(): RequestBody? {
        //很重要同區域才可以叫到同一個東西
        val share = getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE)
        val DeviceAddress = share.getString("mac", "noValue")
        //        String serial = "";
        //        //確認唯一識別碼(https://blog.mosil.biz/2014/05/android-device-id-uuid/)
        //        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
        //            serial = Build.SERIAL;
        //        }
        //首先將要丟進陣列內的JSON物件存好內容後丟進陣列
        val realm = Realm.getDefaultInstance()


        val query2 = realm.where(AsmDataModel::class.java)
        val result5 = query2.equalTo("UpLoaded", "1").findAll()
        //        realm.executeTransaction((Realm realm1) -> {
        //
        //            for (int i = 0 ; i < result5.size() ; i++) {
        //
        //                result5.get(i).setUpLoaded("0");
        //
        //                Log.e("這個時間", String.valueOf(result5.toString()));
        //            }
        //
        //        });

        val query = realm.where(AsmDataModel::class.java)
        val result1 = query.equalTo("UpLoaded", "0").findAll()

        /*
        RealmQuery<AsmDataModel> query9 = realm.where(AsmDataModel.class);
        RealmResults<AsmDataModel> result7 =query9.distinct("Created_time");
        Log.e("幹",String.valueOf(result7.size()));
        Log.e("幹蝦小",String.valueOf(result1.size()));
*/

        Log.e("未上傳ID", result1.toString())
        Log.e("已上ID", result5.toString())
        Log.e("未上傳資料筆數", result1.size.toString())
        Log.e("未上傳資料", result1.toString().toString())
        Log.e("已上傳資料筆數", result5.size.toString())


        //MyApplication getUUID=new MyApplication();
        val UUID = MyApplication.getPsuedoUniqueID()
        //製造RequestBody的地方
        var body: RequestBody? = null

        //20170227
        val json_obj = JSONObject()            //用來當內層被丟進陣列內的JSON物件
        val json_arr = JSONArray()                //JSON陣列

        try {
            if (result1.size > 0) {
                for (i in result1.indices) {
                    //toltoSize++;
                    if (i == 6000) {
                        break
                    }
                    //                if (result1.get(i).getCreated_time().equals(result1.get(i + 1).getCreated_time())) {
                    //                    realm.beginTransaction();
                    //                    result1.get(i).deleteFromRealm();
                    //                    realm.commitTransaction();
                    //                    Log.e("資料相同時", result1.get(i).getCreated_time().toString() + "下筆資料" + result1.get(i).getCreated_time().toString());
                    //                }
                    hasBeenUpLoaded.add(result1[i]!!.dataId)
                    Log.i("text", "i=" + i + "\n")
                    val json_obj_weather = JSONObject()            //單筆weather資料
                    json_obj_weather.put("temperature", result1[i]!!.tempValue)
                    json_obj_weather.put("humidity", result1[i]!!.humiValue)
                    json_obj_weather.put("tvoc", result1[i]!!.tvocValue)
                    json_obj_weather.put("eco2", result1[i]!!.ecO2Value)
                    json_obj_weather.put("pm25", result1[i]!!.pM25Value)
                    json_obj_weather.put("longitude", "24.778289")
                    json_obj_weather.put("latitude", "120.988108")
                    json_obj_weather.put("timestamp", result1[i]!!.created_time)
                    Log.e("timestamp", "i=" + i + "timestamp=" + result1[i]!!.created_time!!.toString())
                    json_arr.put(json_obj_weather)
                    //Log.e("下一筆資料","這筆資料:"+result1.get(i).getCreated_time().toString()+"下一筆資料:"+result1.get(i+1).getCreated_time().toString());
                }
            } else {
                Log.e("未上傳資料筆數", result1.size.toString())
            }

            json_obj.put("uuid", UUID)
            json_obj.put("mac_address", DeviceAddress)
            json_obj.put("registration_id", "qooo123457")
            //再來將JSON陣列設定key丟進JSON物件
            json_obj.put("weather", json_arr)
            Log.e("全部資料", json_obj.toString())
            val mediaType = MediaType.parse("application/x-www-form-urlencoded")
            body = RequestBody.create(mediaType, "data=" + json_obj.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return body
    }

    //傳資料
    private fun getResponse(body: RequestBody): Boolean {
        var response: Response? = null
        var resonseReselt = java.lang.Boolean.parseBoolean(null)
        try {
            if (body.contentLength() > 0) {
                //丟資料
                val request = Request.Builder()
                        .url("https://mjairql.com/api/v1/upWeather")
                        .post(body)
                        .addHeader("content-type", "application/x-www-form-urlencoded")
                        .addHeader("cache-control", "no-cache")
                        .addHeader("postman-token", "a2fa2822-765d-209a-ec8c-82170c5171c0")
                        .build()
                try {
                    client = OkHttpClient.Builder()
                            .connectTimeout(0, TimeUnit.SECONDS)
                            .writeTimeout(0, TimeUnit.SECONDS)
                            .readTimeout(0, TimeUnit.SECONDS)
                            .build()
                    //上傳資料
                    response = client?.newCall(request)?.execute()
                    if (response!!.isSuccessful) {//正確回來
                        resonseReselt = true
                        Log.e("正確回來!!", response!!.body()!!.string())
                    } else {//錯誤回來
                        Log.e("錯誤回來!!", response!!.body()!!.string())
                        resonseReselt = false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e("回來處理有錯!", e.toString())

                }

            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return resonseReselt
    }

    private fun updateDB_UpLoaded(): Boolean {
        var dbSucessOrNot = java.lang.Boolean.parseBoolean(null)
        val realm = Realm.getDefaultInstance()
        try {
            realm.executeTransaction { realm1: Realm ->
                Log.e("正確回來TRY", hasBeenUpLoaded.size.toString())
                for (i in 0 until hasBeenUpLoaded.size) {
                    //realm.beginTransaction();
                    val aaa = realm1.where(AsmDataModel::class.java)
                            .equalTo("id", hasBeenUpLoaded.get(i))
                            .findFirst()
                    aaa!!.setUpLoaded("1")
                    Log.e("回來更新", aaa!!.getDataId()!!.toString() + "更新?" + aaa!!.getUpLoaded())
                }
                val query3 = realm.where(AsmDataModel::class.java)
                val result3 = query3.equalTo("UpLoaded", "1").findAll()
                Log.e("正確更改", result3.size.toString())
                Log.e("正確更改內容", result3.toString())
            }
            dbSucessOrNot = true
        } catch (e: Exception) {
            Log.e("dbSucessOrNot", e.toString())
            dbSucessOrNot = false
        }
        return dbSucessOrNot
    }
*/
    private fun getLocation() {
        checkGPSPermisstion()
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val criteria = Criteria()
        criteria.accuracy = Criteria.ACCURACY_MEDIUM
        criteria.powerRequirement = Criteria.POWER_MEDIUM
        val provider = locationManager.getBestProvider(criteria, true)
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider, 5000, 10f, locationListener)
        }
    }

    private fun checkGPSPermisstion() {
        val permission = ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
        Log.d("MAINAC", permission.toString())
        val permission1 = PackageManager.PERMISSION_GRANTED
        Log.d("MAINAC", permission1.toString())
    }

    private fun checkLoginState() {
        val shareToKen = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        val MyToKen = shareToKen.getString("token", "")
        if(MyToKen=="") {
            text_Account_status.setText(R.string.account_Deactivation)
        }else{
            text_Account_status.setText(R.string.account_Activation)
        }
    }

    private fun getNetWork (): Boolean  {
        var result = false
        try {
            val connManager: ConnectivityManager? = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo: NetworkInfo? = connManager!!.getActiveNetworkInfo() as NetworkInfo


            //判斷是否有網路
            //net = networkInfo.isConnected
            if (networkInfo == null || !networkInfo.isConnected()) {
                result = false
            } else {
                result = networkInfo.isAvailable()
}

        }catch (E: Exception) {
            Log.e("網路", E.toString())
        }
        return result
    }


    //20180312
    fun showDialog(msg:String){
        val Dialog = android.app.AlertDialog.Builder(this@MainActivity).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        Dialog.setTitle("提示")
        Dialog.setMessage(msg.toString())
        Dialog.setCancelable(false)//讓返回鍵與空白無效
        Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
        { dialog, _ ->
            dialog.dismiss()
            //finish()
        }
        Dialog.show()
    }


}









