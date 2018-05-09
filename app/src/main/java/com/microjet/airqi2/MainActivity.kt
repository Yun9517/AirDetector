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
import android.drm.DrmStore
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.location.LocationListener
import android.location.LocationManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.*
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
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
import android.widget.ExpandableListView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.microjet.airqi2.Account.AccountActiveActivity
import com.microjet.airqi2.Account.AccountManagementActivity
import com.microjet.airqi2.BlueTooth.BLECallingTranslate
import com.microjet.airqi2.BlueTooth.DFU.DFUProcessClass
import com.microjet.airqi2.BlueTooth.DeviceListActivity
import com.microjet.airqi2.BlueTooth.UartService
import com.microjet.airqi2.CustomAPI.FragmentAdapter
import com.microjet.airqi2.CustomAPI.GetNetWork
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.Definition.RequestPermission
import com.microjet.airqi2.Definition.SavePreferences
import com.microjet.airqi2.Fragment.ChartFragment
import com.microjet.airqi2.Fragment.MainFragment
import com.microjet.airqi2.GestureLock.DefaultPatternCheckingActivity
import com.microjet.airqi2.URL.AirActionTask
import com.microjet.airqi2.engieeringMode.EngineerModeActivity
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_feature_dfu.*
import kotlinx.android.synthetic.main.activity_fetch_data_main.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.drawer_header.*
import layout.ExpandableListAdapter
import layout.ExpandedMenuModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.abs


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private val TAG = MainActivity::class.java.simpleName
    private val DEFINE_FRAGMENT_TVOC = 1
    private val DEFINE_FRAGMENT_CO2 = 2
    private val DEFINE_FRAGMENT_TEMPERATURE = 3
    private val DEFINE_FRAGMENT_HUMIDITY = 4
    private val DEFINE_FRAGMENT_PM25 = 5

    // Fragment 容器
    private val mFragmentList = ArrayList<Fragment>()

    // var viewPager = VerticalViewPager()
    // ViewPager目前頁面
    private var currentIndex: Int = 0

    // Drawer & NavigationBar
    private var mDrawerToggle: ActionBarDrawerToggle? = null

    // 電池電量數值
    private var batValue : Int = 0

    // 藍芽icon in actionbar
    private var bleIcon: MenuItem? = null
    //電量icon
    private var battreyIcon: MenuItem? = null
    //private var menuItem: MenuItem? = null
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
    private var drawerDeviceAddress: String? = null

    // ***** 2018/03/12 Drawer Show Device Name ******************************* //
    private var drawerDeviceName: String? = null

    // ***** 2018/03/12 Drawer Show Account Name ****************************** //
    private var drawerAccountName: String? = null


    //private var mMainReceiver: BroadcastReceiver? = null
    private var preheatCountDownInt = 0

    private var topMenu: Menu? = null
    // Code to manage Service lifecycle.
    private var mDeviceAddress: String? = null
    private var mUartService: UartService? = null
    //private var longi = 121.4215f
    //private var lati = 24.959742f
    private var locationListener: LocationListener? = null
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
            if (!MyApplication.getSharePreferenceManualDisconn()) {
                mUartService?.connect(mDeviceAddress)
            }
            mUartService?.initFuseLocationProviderClient()
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            mUartService = null
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
    private var blueToothstateStr = "BluetoothAdapter.STATE_OFF"

    //20180411
    private val soundPool2= SoundPool(1, AudioManager.STREAM_MUSIC, 100)
    private var alertId = 0
    private var lowPowerCont: Int = 0
    private var warningClass: WarningClass? = null
    val mContext = this@MainActivity

    //20180423
    private var points = java.util.ArrayList<ImageView>()

    // 2018/05/03 ExpandableListView
    private var mMenuAdapter: ExpandableListAdapter? = null
    private val listDataHeader: ArrayList<ExpandedMenuModel> = ArrayList<ExpandedMenuModel>()
    private val listDataChild: HashMap<ExpandedMenuModel, ArrayList<String>> = HashMap<ExpandedMenuModel, ArrayList<String>>()
    var set_NavigationView: NavigationView ? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.e(TAG, "call onCreate")
        uiFindViewById()
        viewPagerInit()
        initActionBar()
        initpoint()

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
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //20180411   建立警告物件
        warningClass = WarningClass(this)
        alertId = soundPool2.load(this, R.raw.low_power, 1)

        // 2018/05/03 ExpandableListView
        set_NavigationView =  findViewById<View>(R.id.naviView) as NavigationView

        setupDrawerContent(set_NavigationView)

        prepareListData()

        mMenuAdapter = ExpandableListAdapter(this, listDataHeader, listDataChild, navigationmenu!!)
        navigationmenu!!.setAdapter(mMenuAdapter)
        // 2018/05/09 Expandable View, hide original indicator
        navigationmenu.setGroupIndicator(null)
        //*****************************************************//
        navigationmenu.setOnGroupClickListener(ExpandableListView.OnGroupClickListener { parent, v, groupPosition, id ->
            when (groupPosition) {
                0 -> {
                    when(connState) {
                        BleConnection.CONNECTED -> { blueToothDisconnect() }
                        BleConnection.DISCONNECTED -> { blueToothConnect() }
                    }
                }
                1 -> {
                    accountShow()
                }
                2 -> {
                    if (parent.isGroupExpanded(groupPosition)) {
                        parent.collapseGroup(groupPosition)
                    } else {
                        parent.expandGroup(groupPosition)
                        parent.setOnChildClickListener(ExpandableListView.OnChildClickListener { parent, v, groupPosition, childPosition, id ->
                            when (childPosition) {
                                0 -> {
                                    airmapShow()
                                }
                                1 -> {
                                    publicMapShow()
                                }
                            }
                            parent.collapseGroup(groupPosition)
                        })
                    }
                }
                3 -> {
                    if (parent.isGroupExpanded(groupPosition)) {
                        parent.collapseGroup(groupPosition)
                    }
                    else {
                        parent.expandGroup(groupPosition)
                        parent.setOnChildClickListener(ExpandableListView.OnChildClickListener { parent, v, groupPosition, childPosition, id ->
                            when (childPosition) {
                                0 -> { knowledgeShow() }
                                1 -> { qandaShow() }
                                2 -> { tourShow() }
                            }
                            parent.collapseGroup(groupPosition)
                        })
                    }
                }
                4 -> { settingShow() }
                5 -> { aboutShow() }
            }
            true
        })
    }

    @SuppressLint("WifiManagerLeak")
    override fun onStart() {
        super.onStart()
        Log.e(TAG, "call onStart")
        //val serviceIntent: Intent? = Intent(this, UartService::class.java)
        //startService(serviceIntent)
        //checkUIState()
        requestPermissionsForBluetooth()
        //checkBluetooth()
        val share = getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE)
        //val mBluetoothDeviceAddress = share.getString("mac", "noValue")
        mDeviceAddress = share.getString("mac", "noValue")
        if (mDeviceAddress != "noValue" && connState == BleConnection.DISCONNECTED) {
            val gattServiceIntent = Intent(this, UartService::class.java)
            bindService(gattServiceIntent, mServiceConnection, Context.BIND_AUTO_CREATE)
            if (!MyApplication.getSharePreferenceManualDisconn()) {
                mUartService?.connect(mDeviceAddress)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        Log.e(TAG, "call onResume")
        if (mUartService == null) {
            connState = BleConnection.DISCONNECTED
        }
        checkUIState()
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
        Log.e(TAG, "call onPause")
    }

    override fun onStop() {
        super.onStop()
        checkUIState()
        Log.e(TAG, "call onStop")
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
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(myBroadcastReceiver)
            mIsReceiverRegistered = false
        }

        unregisterReceiver(mBluetoothStateReceiver)
        mUartService?.close()
        if (mUartService != null) {
            unbindService(mServiceConnection)
        }
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
        viewPager.offscreenPageLimit = 5
        naviView.menu?.findItem(R.id.nav_setting)?.isVisible = false
        lightIcon?.setImageResource(R.drawable.app_android_icon_light)
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

        mTvocFg.configFragment(DEFINE_FRAGMENT_TVOC)
        mEco2Fg.configFragment(DEFINE_FRAGMENT_CO2)
        mTempFg.configFragment(DEFINE_FRAGMENT_TEMPERATURE)
        mHumiFg.configFragment(DEFINE_FRAGMENT_HUMIDITY)
        mPM25Fg.configFragment(DEFINE_FRAGMENT_PM25)

        mFragmentList.add(mMainFg)
        mFragmentList.add(mTvocFg)
        mFragmentList.add(mEco2Fg)
        mFragmentList.add(mTempFg)
        mFragmentList.add(mHumiFg)
        mFragmentList.add(mPM25Fg)

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

                val share = getSharedPreferences("MACADDRESS", Activity.MODE_PRIVATE)
                val name = share.getString("name", "")

                if (name == "TVOC_NOSE") {
                    banDownDraw = position == 4     // 如果 position = 4，banDownDraw = true，反之 banDownDraw = false
                }
                //Log.e("offset:", offset.toString() + "")

                indicator.visibility = View.VISIBLE

                collapseIndicatorAnim(250)

                indicator.visibility = View.INVISIBLE
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
        bleIcon!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        battreyIcon!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        lightIcon = findViewById(R.id.imgLight)

        return super.onCreateOptionsMenu(menu)
    }

    private var clickCount = 0

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

        //點選ActionBAR會返回
            android.R.id.home -> {
                //checkUIState()
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
        //萬一DFU失敗時為Preference的Address加1
        val share = getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE)
        val realAddress = share.getString("mac", "noValue")

        val i: Intent? = Intent(this, AboutActivity::class.java)
                .putExtra("ADDRESS", realAddress)
                .putExtra("DEVICE_NAME",show_Device_Name?.text.toString())
        startActivity(i)
    }

    private fun airmapShow() {
        val share = getSharedPreferences(SavePreferences.SETTING_KEY, Context.MODE_PRIVATE)
        val isPrivacy = share.getBoolean(SavePreferences.SETTING_MAP_PRIVACY, false)
        if(isPrivacy) {
            DefaultPatternCheckingActivity.startAction(this@MainActivity,
                    DefaultPatternCheckingActivity.START_ACTION_MODE_NORMAL)
        } else {
            val i: Intent? = Intent(this, AirMapActivity::class.java)
            startActivity(i)
        }
    }

    // 20171127 Raymond 新增：知識庫activity
    private fun knowledgeShow() {
        //blueToothDisconnect()
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
        val shareToken = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        val myToken = shareToken.getString("token", "")
        if (GetNetWork.isFastGetNet) {
            if (myToken == "") {
                Log.e("主葉面看偷肯", myToken)
                val i: Intent? = Intent(this, AccountManagementActivity::class.java)
                //text_Account_status.setText(R.string.account_Deactivation)
                startActivity(i)
            } else {
                Log.e("主葉面!=空字串看偷肯", myToken)
                val i: Intent? = Intent(this, AccountActiveActivity::class.java)
                //text_Account_status.setText(R.string.account_Activation)
                startActivity(i)
            }
        } else {
            //showDialog("請連接網路")
            showDialog(getString(R.string.checkConnection))
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
            R.id.nav_air_map -> publicMapShow()
            R.id.nav_tour -> tourShow()
            R.id.nav_knowledge -> knowledgeShow()
            R.id.nav_qanda -> qandaShow()
            R.id.nav_getData -> {
            }
            R.id.nav_setting -> settingShow()
            R.id.nav_personal_map -> airmapShow()
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
            waitLayout?.findViewById<TextView>(R.id.textView15)?.text = resources.getString(R.string.text_message_heating) + preheatCountDownInt.toString() + "秒"
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
                    200 -> {
                        battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bat_charge_6)
                        icon = battreyIcon?.icon as AnimationDrawable
                        if (icon.isRunning) {
                            icon.stop()
                        }
                    }
                    in 180..199 -> {
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
                    100 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x6)
                    in 76..99 -> battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x5)
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
                val allowNotify = mPreference!!.getBoolean(SavePreferences.SETTING_ALLOW_NOTIFY, false)
                val useLowBattNotify = mPreference.getBoolean(SavePreferences.SETTING_BATTERY_SOUND, false)
                //20180206
                lowPowerCont++
                if (allowNotify && useLowBattNotify && lowPowerCont >= 10)//&&(countsound220==5||countsound220==0))
                {
                    lowPowerCont = 0
                    soundPool2!!.play(alertId, 1F, 1F, 0, 0, 1F)
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
                    dataAvaliable(intent)
                }
            }
            //Log.d("MainActivity", "OnReceive: $action")
            //checkUIState()
        }
    }

    @Synchronized
    private fun checkUIState() {
        if (connState == BleConnection.CONNECTED) {
            battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_x3)
            bleIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bluetooth_connect)
            img_bt_status?.setImageResource(R.drawable.app_android_icon_connect)
            val share = getSharedPreferences("MACADDRESS", Activity.MODE_PRIVATE)
            show_Dev_address?.text = share.getString("mac", "")
            show_Device_Name?.text = share.getString("name", "")
            val shareMSG = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
            text_Account_status?.text = shareMSG.getString("name", "")
            /*naviView.menu?.findItem(R.id.nav_add_device)?.isVisible = false
            naviView.menu?.findItem(R.id.nav_disconnect_device)?.isVisible = true*/
            naviView.menu?.findItem(R.id.nav_setting)?.isVisible = true
            naviView.menu?.findItem(R.id.nav_getData)?.isVisible = false
            // 2018/05/03 ExpandableListView - Modify text by BLE status
            listDataHeader[0].iconName = getString(R.string.UART_Disconnecting)
        } else {
            battreyIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.icon_battery_disconnect)
            bleIcon?.icon = AppCompatResources.getDrawable(mContext, R.drawable.bluetooth_disconnect)
            img_bt_status?.setImageResource(R.drawable.app_android_icon_disconnect)
            show_Dev_address?.text = ""
            show_Device_Name?.text = getString(R.string.No_Device_Connect)
            /*naviView.menu?.findItem(R.id.nav_add_device)?.isVisible = true
            naviView.menu?.findItem(R.id.nav_disconnect_device)?.isVisible = false*/
            naviView.menu?.findItem(R.id.nav_setting)?.isVisible = false
            naviView.menu?.findItem(R.id.nav_getData)?.isVisible = false
            heatingPanelHide()
            // 2018/05/03 ExpandableListView - Modify text by BLE status
            listDataHeader[0].iconName = getString(R.string.text_navi_add_device)
        }
        // 2018/05/03 ExpandableListView - use notify to change drawer text
        mMenuAdapter!!.notifyDataSetInvalidated()
        // **************************************************************** //
        Log.d("MAINcheckUIState", connState.toString())
    }

    // 2018/05/03 ExpandableListView
    private fun prepareListData() {
        val drawer_AddADDWII_01 =  ExpandedMenuModel()
        drawer_AddADDWII_01.iconName = getString(R.string.text_navi_add_device)
        drawer_AddADDWII_01.iconImg = R.drawable.ic_bluetooth_searching_black_24dp
        // Adding data header
        listDataHeader.add(drawer_AddADDWII_01)

        val drawer_Account_02 = ExpandedMenuModel()
        drawer_Account_02.iconName = getString(R.string.text_navi_accountManagement)
        drawer_Account_02.iconImg = R.drawable.ic_account_box_black_24dp
        listDataHeader.add(drawer_Account_02)

        val drawer_Map_03 = ExpandedMenuModel()
        drawer_Map_03.iconName = getString(R.string.text_navi_title_map)
        drawer_Map_03.iconImg = R.drawable.ic_place_black_24dp
        listDataHeader.add(drawer_Map_03)

        val drawer_Help_04 = ExpandedMenuModel()
        drawer_Help_04.iconName = getString(R.string.text_navi_title_help)
        drawer_Help_04.iconImg = R.drawable.ic_help_black_24dp
        listDataHeader.add(drawer_Help_04)

        val drawer_Setting_05 = ExpandedMenuModel()
        drawer_Setting_05.iconName = getString(R.string.text_navi_setting)
        drawer_Setting_05.iconImg = R.drawable.ic_settings_black_24dp
        listDataHeader.add(drawer_Setting_05)

        val drawer_About_06 = ExpandedMenuModel()
        drawer_About_06.iconName = getString(R.string.text_navi_about)
        drawer_About_06.iconImg = R.drawable.ic_phone_android_black_24dp
        listDataHeader.add(drawer_About_06)

        // Adding child data

        val child_Map_03 = ArrayList<String>()
        child_Map_03.add(getString(R.string.text_navi_personal))
        child_Map_03.add(getString(R.string.text_navi_air_map))

        val child_Help_04 = ArrayList<String>()
        child_Help_04.add(getString(R.string.text_navi_knowledge))
        child_Help_04.add(getString(R.string.text_navi_q_and_a))
        child_Help_04.add(getString(R.string.text_navi_tour))

        listDataChild.put(listDataHeader.get(2), child_Map_03)
        listDataChild.put(listDataHeader.get(3), child_Help_04)
        //navigationmenu.expandableListAdapter.getGroupView(0).scrollIndicators = 0
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
                    blueToothstateStr = "BluetoothAdapter.STATE_TURNING_OFF"
                }
                BluetoothAdapter.STATE_OFF -> {
                    blueToothstateStr = "BluetoothAdapter.STATE_OFF"
                }
                BluetoothAdapter.STATE_ON -> {
                    blueToothstateStr = "BluetoothAdapter.STATE_ON"
                }
            }
            Log.v(TAG, "mBluetoothStateReceiver: $blueToothstateStr")
        }
    }

    private fun checkLoginState() {
        val shareToken = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
        val myToken = shareToken.getString("token", "")
        if (myToken == "") {
            text_Account_status.text = getString(R.string.account_Deactivation)
        } else {
            val myName = shareToken.getString("name", "")
            text_Account_status.text = myName
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
    private fun dataAvaliable(intent: Intent) {
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
                        var hashMap = BLECallingTranslate.parserGetInfoKeyValue(txValue)
                        MyApplication.putDeviceVersion(hashMap["FW"].toString())
                        MyApplication.putDeviceSerial(hashMap["FWSerial"].toString())
                        CheckFWversion("20"+hashMap["FW"].toString()+hashMap["FWSerial"].toString(),"00"+hashMap["DEV"].toString())
                        Log.d("PARSERB1", hashMap.toString())
                    }
                    0xB2.toByte() -> {

                        var hashMap = BLECallingTranslate.ParserGetSampleRateKeyValue(txValue)
                        checkSampleRate(hashMap)
                        mUartService?.writeRXCharacteristic(BLECallingTranslate.GetHistorySampleItems())
                        Log.d("0xB2", hashMap.toString())
                    }
                    0xB4.toByte() -> {
                        getMaxItems(txValue)
                    }
                    0xB5.toByte() -> {
                        //saveToRealm(txValue)
                    }
                    0xB9.toByte() -> {
                        val mPreference = this.application.getSharedPreferences(SavePreferences.SETTING_KEY, 0)
                        val ledState = txValue[3].toInt()
                        if (txValue.size > 5) {
                            if (ledState == 1) {
                                mPreference.edit().putBoolean(SavePreferences.SETTING_LED_SWITCH,
                                        false).apply()
                            } else {
                                mPreference.edit().putBoolean(SavePreferences.SETTING_LED_SWITCH,
                                        true).apply()
                            }
                            Log.e(TAG, "LED Status: $ledState")
                        }
                        //Log.d("0xB9",hashMap.toString())
                    }
                    0xBA.toByte() -> {
                        MyApplication.setSharePreferenceManualDisconn(true)
                        Log.e("0xBA", "Manual Disconnect from Device.........")
                    }
                    0xE0.toByte() -> {
                        var hashMap = BLECallingTranslate.getPM25KeyValue(txValue)
                        if (hashMap[TvocNoseData.PM25SR] != "5" || hashMap[TvocNoseData.PM25GST] != "30") {
                            mUartService?.writeRXCharacteristic(BLECallingTranslate.setPM25Rate(5))
                        }
                        Log.d("0xE0", hashMap.toString())
                    }
                    0xBB.toByte() -> {
                        var hashMap = BLECallingTranslate.parserGetRTCKeyValue(txValue)
                        Log.d("0xBB", hashMap.toString())
                    }
                    0xC0.toByte() -> {
                        var hashMap = BLECallingTranslate.getAllSensorC0KeyValue(txValue)
                        heatingPanelControl(hashMap[TvocNoseData.C0PREH]!!)
                        batValue = hashMap[TvocNoseData.C0BATT]!!.toInt()
                        displayConnetedBatteryLife(hashMap[TvocNoseData.C0BATT]!!.toInt())
                        val rtcTime = hashMap[TvocNoseData.C0TIME]!!.toLong()
                        connectionInitMethod(rtcTime)
                        Log.d("0xC0", hashMap.toString())

                    }
                    0xC5.toByte() -> {
                        putC5ToObject(txValue)
                    }
                    0xC6.toByte() -> {
                        if (isFirstC6) {
                            isFirstC6 = false
                            mUartService?.writeRXCharacteristic(BLECallingTranslate.getHistorySampleC5(1))
                        }
                        var hashMap = BLECallingTranslate.ParserGetAutoSendDataKeyValueC6(txValue)
                        saveToRealmC6(hashMap)
                        warningClass!!.judgeValue(hashMap[TvocNoseData.C6TVOC]!!.toInt(),hashMap[TvocNoseData.C6PM25]!!.toInt())
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

    private fun getMaxItems(tx: ByteArray) {
        var hashMap = BLECallingTranslate.parserGetHistorySampleItemsKeyValue(tx)
        var sampleRateTime = 0
        var correctTime = 0
        sampleRateTime = hashMap[TvocNoseData.B4SR]!!.toInt()
        maxItem = hashMap[TvocNoseData.MAXI]!!.toInt()
        correctTime = hashMap[TvocNoseData.CT]!!.toInt()
        Log.d("UART", "total item " + Integer.toString(maxItem))
        if (maxItem > 0) {
            if (Build.BRAND != "OPPO") {
                Toast.makeText(applicationContext, getText(R.string.Loading_Data), Toast.LENGTH_SHORT).show()
            }
            //Realm 資料庫
            val realm = Realm.getDefaultInstance()
            //將資料庫最大時間與現在時間換算成Count
            var maxCreatedTime = realm.where(AsmDataModel::class.java).max("Created_time")
            if (maxCreatedTime == null) {
                maxCreatedTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2)
            }
            val nowTime = System.currentTimeMillis()//Calendar.getInstance().timeInMillis
            Log.d("0xB4countLast", Date(nowTime).toString())
            Log.d("0xB4countLast", Date(maxCreatedTime.toLong()).toString())
            val countForItemTime = nowTime - maxCreatedTime.toLong()
            Log.d("0xB4countItemTime", countForItemTime!!.toString())
            countForItem = Math.min((countForItemTime!! / (60L * 1000L)).toInt(), maxItem)
            Log.d("0xB4countItem", java.lang.Long.toString(countForItem.toLong()))
            if (Build.BRAND != "OPPO") {
                //Toast.makeText(applicationContext, getText(R.string.Total_Data).toString() + java.lang.Long.toString(countForItem.toLong()) + getText(R.string.Total_Data_Finish), Toast.LENGTH_SHORT).show()
            }
            if (countForItem >= 1) {
                //NowItem = countForItem
                //mUartService?.writeRXCharacteristic(BLECallingTranslate.getHistorySampleC5(countForItem))
                //downloading = true
                //downloadComplete = false;
                val mainIntent = Intent(BroadcastIntents.PRIMARY)
                mainIntent.putExtra("status", BroadcastActions.INTENT_KEY_GET_HISTORY_COUNT)
                mainIntent.putExtra(BroadcastActions.INTENT_KEY_GET_HISTORY_COUNT, Integer.toString(maxItem))
                sendBroadcast(mainIntent)
            } else {
                if (Build.BRAND != "OPPO") {
                    //Toast.makeText(applicationContext, getText(R.string.Loading_Completely), Toast.LENGTH_SHORT).show()
                }
            }
        }

        Log.d("getMaxItems", hashMap.toString())
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
        var hashMap = BLECallingTranslate.parserGetHistorySampleItemKeyValueC5(tx)
        val share = getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE)
        if (hashMap[TvocNoseData.C5TIME]!!.toLong() > 1514736000) {
            if (!lock) {
                indexMap.put("UTCBlockHead", hashMap[TvocNoseData.C5II]!!.toInt())
                lock = true
            }
        }
        if (hashMap[TvocNoseData.C5TIME]!!.toLong() == 0L) {
            if (lock) {
                indexMap.put("UTCBlockEnd", hashMap[TvocNoseData.C5II]!!.toInt())
                val indexCopy = indexMap.clone() as HashMap<String, Int>
                arr1.add(indexCopy)
                indexMap.clear()
                lock = false
            }
        }

        mDeviceAddress = share.getString("mac", "noValue")
        hashMap.put(TvocNoseData.C5MACA, mDeviceAddress!!)
        hashMap.put(TvocNoseData.C5LATI, TvocNoseData.lati.toString())
        hashMap.put(TvocNoseData.C5LONGI, TvocNoseData.longi.toString())
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
                indexMap.put("UTCBlockEnd", maxItem)
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
        class SaveRealmTask: AsyncTask<Void, Void, Void>() {
            override fun doInBackground(vararg params: Void?): Void? {
                try {
                    if (arr1.size == 0) {
                        val hash = HashMap<String, Int>()
                        hash["UTCBlockHead"] = 1
                        hash["UTCBlockEnd"] = maxItem
                        arr1.add(hash)
                    }
                    for (i in 0 until arr1.size) {
                        val head = arr1[i]["UTCBlockHead"]!! - 1
                        val end = arr1[i]["UTCBlockEnd"]!! - 1
                        var count = 0
                        for (y in head..end) {
                            val realm = Realm.getDefaultInstance()
                            val time = if (arrIndexMap[y][TvocNoseData.C5TIME]!!.toLong() > 1514736000)
                            {
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
                                    asmData.created_time = time //(arrIndexMap[head][TvocNoseData.C5TIME]!!.toLong() - 60 * count) * 1000//+ Calendar.getInstance().timeZone.rawOffset//getMyDate().getTime() - countForItem * getSampleRateUnit() * 30 * 1000 + (getSampleRateUnit() * counterB5 * 30 * 1000).toLong() + (getCorrectTime() * 30 * 1000).toLong()
                                    asmData.macAddress = arrIndexMap[y][TvocNoseData.C5MACA].toString()
                                    asmData.latitude = arrIndexMap[y][TvocNoseData.C5LATI]?.toFloat()
                                    asmData.longitude = arrIndexMap[y][TvocNoseData.C5LONGI]?.toFloat()
                                    Log.d("0xC5", asmData.toString())
                                }
                            }
                            realm.close()
                            count++
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }

            override fun onPostExecute(result: Void?) {
                super.onPostExecute(result)
                if (Build.BRAND != "OPPO") {
                    Toast.makeText(applicationContext, getText(R.string.Loading_Completely), Toast.LENGTH_SHORT).show()
                }
                //為了發版先將UploadTask要用的東西先放這
                val share_token = getSharedPreferences("TOKEN", Context.MODE_PRIVATE)
                val token = share_token.getString("token", "")
                val share = getSharedPreferences("MACADDRESS", Activity.MODE_PRIVATE)
                val macAddressForDB = share.getString("mac", "noValue")
                if (token != "") {
                    Thread(Runnable { UploadTask().execute(macAddressForDB, token) }).start()
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
    }
    @Subscribe
    fun onEvent(bleEvent: BleEvent ){
        /* 處理事件 */
        Log.d("AirAction", bleEvent.message)
        when (bleEvent.message) {
            "New FW Arrival "->{
                showDownloadDialog(bleEvent.message!!)
            }
            "Download Success"->{
                /*
                val intent = Intent()
                intent.putExtra("ADDRESS",show_Dev_address?.text.toString())
                intent.putExtra("DEVICE_NAME",show_Device_Name?.text.toString())
                intent.setClass(this, DFUActivity::class.java)
                startActivity(intent)*/
                var dfup= DFUProcessClass(this)
                val share = getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE)
                val mDeviceAddress = share.getString("mac", "noValue")
                if (mDeviceAddress!= "noValue") {
                    dfup.DFUAction("", mDeviceAddress)
                }
            }
        }
    }
    private fun showDownloadDialog(msg: String) {
        val Dialog = android.app.AlertDialog.Builder(this).create()
        //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
        //Dialog.setTitle("提示")
        Dialog.setTitle(getString(R.string.remind))
        Dialog.setMessage(msg+"\t請確定裝置與電源連接正常，手機儘量接近裝置，以利FW更新。")
        Dialog.setCancelable(false)//讓返回鍵與空白無效
        //Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")

        Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.Reject))//否
        { dialog, _ ->
            dialog.dismiss()
        }
        Dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Accept))//是
        { dialog, _ ->

            dialog.dismiss()

            val fwVer = ""

            val aat = AirActionTask(this.mContext)
            aat.execute("downloadFWFile")
        }
        Dialog.show()
    }

    fun CheckFWversion(Version:String,DeviceType:String) {
        if (batValue > 100) {
            val aat = AirActionTask(this.mContext, Version, DeviceType)
            val myResponse = aat.execute("postFWVersion")
            Log.v("AirActionTask", "OVER")
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

    private fun publicMapShow() {
        val i: Intent? = Intent(this, PublicMapActivity::class.java)
        startActivity(i)
    }
}


