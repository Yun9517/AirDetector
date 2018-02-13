package com.microjet.airqi2

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.*
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

import android.graphics.BitmapFactory
import android.graphics.drawable.AnimationDrawable
import android.location.LocationManager
import android.media.AudioManager
import android.media.SoundPool
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBar
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.app.NotificationCompat
import android.support.v7.content.res.AppCompatResources
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.widget.*
import io.realm.Realm
import kotlinx.android.synthetic.main.frg_main.*
import com.microjet.airqi2.BlueTooth.DeviceListActivity
import com.microjet.airqi2.BlueTooth.UartService
import com.microjet.airqi2.BlueTooth.UartService.mConnectionState
import com.microjet.airqi2.CustomAPI.CustomViewPager
import com.microjet.airqi2.CustomAPI.FragmentAdapter
import com.microjet.airqi2.CustomAPI.Utils
import com.microjet.airqi2.Definition.BroadcastActions
import com.microjet.airqi2.Definition.BroadcastIntents
import com.microjet.airqi2.Definition.RequestPermission
import com.microjet.airqi2.Definition.SavePreferences
import com.microjet.airqi2.Fragment.*
import io.fabric.sdk.android.services.settings.IconRequest.build
import kotlinx.android.synthetic.main.frg_humidity.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    public val mContext = this@MainActivity

    // Fragment 容器
    private val mFragmentList = ArrayList<Fragment>()

    // ViewPager
    private var mPageVp: CustomViewPager? = null

    // var viewPager = VerticalViewPager()
    // ViewPager目前頁面
    private var currentIndex: Int = 0

    // Drawer & NavigationBar
    private var mDrawerLayout: DrawerLayout? = null
    private var mDrawerToggle: ActionBarDrawerToggle? = null

    // 電池電量數值
    private var batValue: Int = 0

    // 藍芽icon in actionbar
    private var bleIcon: MenuItem? = null
    //電量icon
    private var battreyIcon: MenuItem? = null
    private var menuItem: MenuItem? = null


    private var connState = false
/*
    //20171124 Andy月曆的方法聆聽者
    var dateSetListener : DatePickerDialog.OnDateSetListener? = null
    var cal = Calendar.getInstance()
*/


    //Richard 171124
    private var nvDrawerNavigation: NavigationView? = null
    private var mDevice: BluetoothDevice? = null
    private var mBluetoothLeService: UartService? = null
    private val REQUEST_SELECT_DEVICE = 1
    private val REQUEST_SELECT_SAMPLE = 2

    //UArtService實體
    private var mService: UartService? = null

    private var mIsReceiverRegistered: Boolean = false
 //   private var mReceiver: MyBroadcastReceiver? = null
    private var isGPSEnabled: Boolean = false
    private var mLocationManager: LocationManager? = null

    // ***** 2017/12/11 Drawer連線 會秀出 Mac Address ************************ //
    private var drawerDeviceAddress: String? = null


    // 20171212 Raymond added Wait screen
    private var mWaitLayout: RelativeLayout? = null
    private var mainLayout: LinearLayout? = null
    private var mMainReceiver: BroadcastReceiver? = null
    private var preheatCountDownInt = 0

    private var topMenu: Menu? = null

    //20180122
    private var soundPool: SoundPool? = null
    private var alertId = 0
    private var lowPowerCont:Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(TAG, "call onCreate")

        uiFindViewById()
        viewPagerInit()
        initActionBar()

        val dm = DisplayMetrics()
        this@MainActivity.windowManager.defaultDisplay.getMetrics(dm)
        Log.v("MainActivity", "Resolution: " + dm.heightPixels + "x" + dm.widthPixels)

        // 電池電量假資料
        //   batValue = 30
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val serviceIntent: Intent? = Intent(this, UartService::class.java)
//            ContextCompat.startForegroundService(this, serviceIntent)
//            //startForegroundService(serviceIntent)
//        } else {

        //}


        if (!mIsReceiverRegistered) {
            LocalBroadcastManager.getInstance(mContext).registerReceiver(MyBroadcastReceiver, makeGattUpdateIntentFilter())
            mIsReceiverRegistered = true
        }

        //Use Realm
        val realm = Realm.getDefaultInstance() // opens "myrealm.realm"
        try {
            // ... Do something ...
        } finally {
            realm.close()
        }

        setupDrawerContent(nvDrawerNavigation)

        UartService.nowActivity=this
        registerReceiver(mBluetoothStateReceiver, makeBluetoothStateIntentFilter())
        //20180206
        soundPool = SoundPool(1, AudioManager.STREAM_MUSIC, 100)
        alertId = soundPool!!.load(this, R.raw.low_power, 1)
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "call onStart")
        val serviceIntent: Intent? = Intent(this, UartService::class.java)
        startService(serviceIntent)

        checkUIState()
        requestPermissionsForBluetooth()
        //checkBluetooth()

        val share = getSharedPreferences("MACADDRESS", Context.MODE_PRIVATE)
        val mBluetoothDeviceAddress = share.getString("mac", "noValue")

        if (mBluetoothDeviceAddress != "noValue" && !connState) {
            val mainintent = Intent(BroadcastIntents.PRIMARY)
            mainintent.putExtra("status", BroadcastActions.ACTION_CONNECT_DEVICE)
            mainintent.putExtra("mac", mBluetoothDeviceAddress)
            sendBroadcast(mainintent)
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "call onResume")

    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "call onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "call onStop")

    }


    //20180202
     override fun onBackPressed() {
        //實現模擬home鍵功能
        //super.onBackPressed();//這句話一定要註解掉，不然又會去掉用系統初始的back處理方式
        var intent: Intent = Intent(Intent.ACTION_MAIN)
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
         intent.addCategory(Intent.CATEGORY_HOME)
        startActivity(intent)
    }




    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "call onDestroy")
        if (mIsReceiverRegistered) {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(MyBroadcastReceiver)
            mIsReceiverRegistered = false
        }

        unregisterReceiver(mBluetoothStateReceiver)

        val serviceIntent: Intent? = Intent(BroadcastIntents.PRIMARY)
        serviceIntent!!.putExtra("status", "disconnect")
        sendBroadcast(serviceIntent)

        val intent: Intent? = Intent(this, UartService::class.java)
        stopService(intent)

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
                ) { dialog, which -> finish() }

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
        val mBluetoothManager = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)!!
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
                var intentOpenBluetoothSettings = Intent()
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
        val mTvocFg = TVOCFragment()
        val mEco2Fg = ECO2Fragment()
        val mTempFg = TempFragment()
        val mHumiFg = HumidiytFragment()

        mFragmentList.add(mMainFg)
        mFragmentList.add(mTvocFg)
        mFragmentList.add(mEco2Fg)
        mFragmentList.add(mTempFg)
        mFragmentList.add(mHumiFg)

        val mFragmentAdapter = FragmentAdapter(this.supportFragmentManager, mFragmentList)
        mPageVp!!.adapter = mFragmentAdapter
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

                Log.e("offset:", offset.toString() + "")
            }

            override fun onPageSelected(position: Int) {
                Log.d("PageSelected", position.toString())
                currentIndex = position
                if (currentIndex >= 1) {
                    when (currentIndex) {
                        1 -> {
                            val nowFragment = mFragmentAdapter.getItem(currentIndex) as TVOCFragment
                            nowFragment.sprTVOC?.setSelection(TvocNoseData.spinnerPosition)
                            nowFragment.btnTextChanged(TvocNoseData.spinnerPosition)
                            nowFragment.drawChart(TvocNoseData.spinnerPosition)
                        }
                        2 -> {
                            val nowFragment = mFragmentAdapter.getItem(currentIndex) as ECO2Fragment
                            nowFragment.sprTVOC?.setSelection(TvocNoseData.spinnerPosition)
                            nowFragment.btnTextChanged(TvocNoseData.spinnerPosition)
                            nowFragment.drawChart(TvocNoseData.spinnerPosition)
                        }
                        3 -> {
                            val nowFragment = mFragmentAdapter.getItem(currentIndex) as TempFragment
                            nowFragment.sprTVOC?.setSelection(TvocNoseData.spinnerPosition)
                            nowFragment.btnTextChanged(TvocNoseData.spinnerPosition)
                            nowFragment.drawChart(TvocNoseData.spinnerPosition)
                        }
                        4 -> {
                            val nowFragment = mFragmentAdapter.getItem(currentIndex) as HumidiytFragment
                            nowFragment.sprTVOC?.setSelection(TvocNoseData.spinnerPosition)
                            nowFragment.btnTextChanged(TvocNoseData.spinnerPosition)
                            nowFragment.drawChart(TvocNoseData.spinnerPosition)
                        }
                    }
                    //val mFragmentAdapter: FragmentAdapter = mPageVp?.adapter as FragmentAdapter
                //(mFragmentAdapter.getItem(1) as TVOCFragment).setImageBarSize()
                    //先測試下載功能是否OK
                    //(mFragmentAdapter.getItem(1) as TVOCFragment).getDeviceData()
                }
            }
        })
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

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        topMenu = menu
        //menuItem= menu!!.findItem(R.id.batStatus)
        bleIcon = menu!!.findItem(R.id.bleStatus)
        battreyIcon = menu.findItem(R.id.batStatus)
        bleIcon!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        battreyIcon!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

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
                mDrawerToggle!!.onOptionsItemSelected(item)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun dialogShow(title: String, content: String) {
        val i: Intent? = Intent(this, CustomDialogActivity::class.java)
        val bundle: Bundle? = Bundle()
        bundle!!.putString("dialogTitle", title)
        bundle.putString("dialogContent", content)

        i!!.putExtras(bundle)
        startActivity(i)
    }

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
            R.id.nav_air_map -> airmapShow()
            R.id.nav_tour -> tourShow()
        //R.id.nav_second_fragment -> fragmentClass = SecondFragment::class.java
            R.id.nav_knowledge -> {
                knowledgeShow()
                /*
               val intent: Intent? = Intent("Main")
               intent!!.putExtra("status", "getSampleRate")
               sendBroadcast(intent)*/
                //  knowledgeShow()
            }
            R.id.nav_qanda -> qandaShow()
            R.id.nav_getData -> {

            }
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
        val mBluetoothManager = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)!!
        val mBluetoothAdapter = mBluetoothManager.adapter
        if (!isGPSEnabled) {
            setGPSEnabled()
        } else if (isGPSEnabled && mBluetoothAdapter.isEnabled) {
            val i: Intent? = Intent(this,
                    DeviceListActivity::class.java)
            startActivity(i)
        }
        //startActivityForResult(i,REQUEST_SELECT_DEVICE)
    }

    private fun blueToothDisconnect() {
        if (connState) {
            val serviceIntent: Intent? = Intent(BroadcastIntents.PRIMARY)
            serviceIntent!!.putExtra("status", "disconnect")
            sendBroadcast(serviceIntent)
        }
        //stopService(serviceIntent)
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

    private fun heatingPanelControl(preheatCountDownString : String) {
        if (mWaitLayout!!.visibility == View.INVISIBLE) {
            heatingPanelShow()
            mWaitLayout!!.bringToFront()
        }

        preheatCountDownInt = (120 - preheatCountDownString.toInt())
        Log.v(TAG, "Preheat Count Down: " + preheatCountDownInt)
        mWaitLayout?.findViewById<TextView>(R.id.textView15)?.text = resources.getString(R.string.text_message_heating) + preheatCountDownInt.toString() + "秒"
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
            REQUEST_SELECT_DEVICE ->
                //When the DeviceListActivity return, with the selected device address
                //得到Address後將Address後傳遞至Service後啟動 171129
                if (resultCode == Activity.RESULT_OK && data != null) {
                    print("MainActivity")
                }
            REQUEST_SELECT_SAMPLE -> {
                if (data != null) {
                    var value = data.getIntExtra("choseCycle", 0)
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
        var intentFilter = IntentFilter()
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
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context?, intent: Intent) {
            //updateUI(intent)
            checkBluetooth()
            val action = intent.action
            when (action) {
                BroadcastActions.ACTION_GATT_CONNECTED -> {
                    connState = true
                    val bundle = intent.extras
                    drawerDeviceAddress = bundle.getString(BroadcastActions.INTENT_KEY_DEVICE_ADDR)
                    // drawerDeviceAddress = intent.getStringExtra("macAddress")
                    //    updateUI(intent)
                }
                BroadcastActions.ACTION_GATT_DISCONNECTED -> {
                    connState = false
                    checkUIState()
                    //    updateUI(intent)
                }
                BroadcastActions.ACTION_GET_NEW_DATA -> {
                    val bundle = intent.extras
                    val tempVal = bundle.getString(BroadcastActions.INTENT_KEY_TEMP_VALUE)
                    val humiVal = bundle.getString(BroadcastActions.INTENT_KEY_HUMI_VALUE)
                    val tvocVal = bundle.getString(BroadcastActions.INTENT_KEY_TVOC_VALUE)
                    val co2Val = bundle.getString(BroadcastActions.INTENT_KEY_CO2_VALUE)
                    batValue = bundle.getString(BroadcastActions.INTENT_KEY_BATTERY_LIFE).toInt()
                    val preheatCountDownString = bundle.getString(BroadcastActions.INTENT_KEY_PREHEAT_COUNT)
                    Log.v(TAG, "電池電量: $batValue%")
                    // 預熱畫面控制
                    heatingPanelControl(preheatCountDownString)
                    displayConnetedBatteryLife()
                }
            }
            Log.d("MainActivity","OnReceive: " + action)
            checkUIState()
        }


    }

    @Synchronized private fun checkUIState() {
        if (connState) {
            nvDrawerNavigation?.menu?.findItem(R.id.nav_add_device)?.isVisible = false
            nvDrawerNavigation?.menu?.findItem(R.id.nav_disconnect_device)?.isVisible = true
            nvDrawerNavigation?.getHeaderView(0)?.findViewById<TextView>(R.id.txt_devname)?.text = drawerDeviceAddress
            nvDrawerNavigation?.getHeaderView(0)?.findViewById<ImageView>(R.id.img_bt_status)?.setImageResource(R.drawable.app_android_icon_connect)
            bleIcon?.icon = resources.getDrawable(R.drawable.bluetooth_connect)
            nvDrawerNavigation?.menu?.findItem(R.id.nav_setting)?.isVisible = true
            nvDrawerNavigation?.menu?.findItem(R.id.nav_getData)?.isVisible = false
        }else {
            nvDrawerNavigation?.menu?.findItem(R.id.nav_add_device)?.isVisible = true
            nvDrawerNavigation?.menu?.findItem(R.id.nav_disconnect_device)?.isVisible = false
            nvDrawerNavigation?.getHeaderView(0)?.findViewById<TextView>(R.id.txt_devname)?.text = getText(R.string.No_Device_Connect)
            nvDrawerNavigation?.getHeaderView(0)?.findViewById<ImageView>(R.id.img_bt_status)?.setImageResource(R.drawable.app_android_icon_disconnect)
            bleIcon?.icon = resources.getDrawable(R.drawable.bluetooth_disconnect)
            battreyIcon?.icon = resources.getDrawable(R.drawable.icon_battery_disconnect)
            nvDrawerNavigation?.menu?.findItem(R.id.nav_setting)?.isVisible = false
            nvDrawerNavigation?.menu?.findItem(R.id.nav_getData)?.isVisible = false
        }
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
            Log.v(TAG, "mBluetoothStateReceiver: " + stateStr)
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
}





