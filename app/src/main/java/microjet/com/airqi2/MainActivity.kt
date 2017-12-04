package microjet.com.airqi2

import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.media.Image
import android.os.Bundle
import android.os.IBinder
import android.support.design.widget.NavigationView
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v4.view.GravityCompat
import android.support.v4.view.ViewPager
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.DisplayMetrics
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import me.kaelaela.verticalviewpager.VerticalViewPager
import microjet.com.airqi2.BlueTooth.DeviceListActivity
import microjet.com.airqi2.BlueTooth.UartService
import microjet.com.airqi2.CustomAPI.FragmentAdapter
import microjet.com.airqi2.CustomAPI.Utils
import microjet.com.airqi2.Definition.RequestPermission
import microjet.com.airqi2.Fragment.MainFragment
import microjet.com.airqi2.Fragment.TVOCFragment
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import java.util.*


class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    private val mContext = this@MainActivity

    // Fragment 容器
    private val mFragmentList = ArrayList<Fragment>()

    // ViewPager
    private var mPageVp: VerticalViewPager? = null

    // var viewPager = VerticalViewPager()
    // ViewPager目前頁面
    private var currentIndex: Int = 0

    // Drawer & NavigationBar
    private var mDrawerLayout : DrawerLayout? = null
    private var mDrawerToggle : ActionBarDrawerToggle? = null

    // 電池電量數值
    private var batValue: Int = 0

    // 藍芽icon in actionbar
    private var btIcon: MenuItem? = null
    //電量icon
    private var battreyIcon:MenuItem?=null

/*
    //20171124 Andy月曆的方法聆聽者
    var dateSetListener : DatePickerDialog.OnDateSetListener? = null
    var cal = Calendar.getInstance()
*/




    //Richard 171124
    private var nvDrawerNavigation : NavigationView? = null
    private var mDevice: BluetoothDevice? = null
    private var mBluetoothLeService: UartService? = null
    private val REQUEST_SELECT_DEVICE = 1
    private val REQUEST_SELECT_SAMPLE = 2
    //private var mBluetoothManager : BluetoothManager? = null
    //private var mBluetoothAdapter : BluetoothAdapter? = null

    //20171128   Andy SQLlite
    internal lateinit var dbrw: SQLiteDatabase
    internal lateinit var dbhelper: AndyAirDBhelper
    internal var tablename = "Andyairtable"


    internal var colstT = arrayOf("編號","時間","溫度", "濕度", "揮發", "二氧")// };
    internal var columT = arrayOf("_id", "collection_time","temper", "hum", "tvoc", "co2")//,"CO2"};
    internal var co10T = ""
    internal var co11T = ""
    internal var co12T = ""
    internal var co13T = ""
    internal var co14T = ""
    internal var co15T = ""

    internal var coTTDBTEST = ""
    internal var SaveToDB = arrayOf("2017/11/30","10", "20", "30", "40")
    internal var idTTDB: Long = 4
    internal var c: Cursor? = null
    internal var values : ContentValues? = null
    internal var IDID = ""
    internal var Count: Long = 0
    internal var idTTDBStr = ""


    //UArtService實體
    private var mService : UartService? = null

    private var mIsReceiverRegistered : Boolean = false
    private var mReceiver : MyBroadcastReceiver? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        uiFindViewById()
        viewPagerInit()
        initActionBar()

        val dm = DisplayMetrics()
        this@MainActivity.windowManager.defaultDisplay.getMetrics(dm)
        Log.v("MainActivity", "Resolution: " + dm.heightPixels + "x" + dm.widthPixels)

        // 電池電量假資料
     //   batValue = 30


//20171128 Andy SQL
        //*********************************************************************************************
// ------------------------------------------------------------------------------------------------------------------------------------------------

        //dbhelper = AndyAirDBhelper(this)
        //dbrw = dbhelper.writableDatabase
        //Toast.makeText(this,AndyAirDBhelper.database17 + "資料庫是否建立?" + dbrw.isOpen + "版本" + dbrw.version,Toast.LENGTH_LONG).show()
        //AddedSQLlite(60000)
        //SearchSQLlite()



        //20171128 Andy SQL
        //*********************************************************************************************
// ------------------------------------------------------------------------------------------------------------------------------------------------
        //查詢CO2資料
        //查詢CO2資料
        //查詢CO2資料
        /*
            c = dbrw.query(tablename, columT, null, null, null, null, null)

            //Toast.makeText(MainActivity.this, "現在位置:"+c.getPosition(), 3000).show();
            //Toast.makeText(MainActivity.this, "現在ColumnIndex:"+ c.getString(c.getColumnIndex(columT[0])), 3000).show();


            // 排版
            //co10T += colstT[0] + "\n";
            //co11T += colstT[1] + "\n";
            //co12T += colstT[2] + "\n";
            // co13T += colstT[3] + "\n";
            co14T += colstT[4] + "\n"




            if (c!!.getCount() > 0) {
                //Toast.makeText(MainActivity.this, "測試是否有進去!!  " + c.getCount() + "筆紀錄",Toast.LENGTH_LONG).show();
                c!!.moveToFirst()

                for (i in 0 until c!!.getCount()) {
                    Toast.makeText(this@MainActivity, "測試是否進For!!  " + c!!.getCount() + "第" + i + "筆紀錄", Toast.LENGTH_LONG).show()
                    co10T += c!!.getString(c!!.getColumnIndex(columT[0])) + "\n"
                    // sqlite比較不嚴僅，都用getString()取值即可
                    co14T += c!!.getString(4) + "\n"
                    Toast.makeText(this@MainActivity, "將新增資料庫CO2第 [ " + (i + 1) + " ]筆CO2:" + c!!.getString(0 + 1) +"ppm", Toast.LENGTH_LONG).show()
                    c!!.moveToNext()
                }

                Count = c!!.getCount().toLong()
                //c.close();
                val CountString = Count.toString()
                Toast.makeText(this@MainActivity, "共有" + CountString + "筆CO2紀錄", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@MainActivity, "資料庫無查CO2資料", Toast.LENGTH_LONG).show()
            }
    */
//*********************************************************************************************
// ------------------------------------------------------------------------------------------------------------------------------------------------

/*
        //20171124 Andy月曆實現
        // create an OnDateSetListener
        // when you click on the button, show DatePickerDialog that is set with OnDateSetListener
        dateSetListener = object : DatePickerDialog.OnDateSetListener {
            override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int,
                                   dayOfMonth: Int) {
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDateInView()
            }
        }
        */

        setupDrawerContent(nvDrawerNavigation)
    }


    private val mMessageReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // on background handler thread, and verified CONNECTIVITY_INTERNAL
            // permission above.
            when (intent.action) {
                "com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTED",
                "com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTING"
                -> {
                    nvDrawerNavigation?.menu?.findItem(R.id.nav_add_device)?.isVisible = true
                    nvDrawerNavigation?.menu?.findItem(R.id.nav_disconnect_device)?.isVisible = false
                }
                "com.nordicsemi.nrfUART.ACTION_GATT_CONNECTED" -> {
                    nvDrawerNavigation?.menu?.findItem(R.id.nav_add_device)?.isVisible = false
                    nvDrawerNavigation?.menu?.findItem(R.id.nav_disconnect_device)?.isVisible = true
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        val serviceIntent :Intent? = Intent(this, UartService::class.java)
        startService(serviceIntent)
        Log.d("MAIN","START")
    }

    override fun onResume() {
        super.onResume()
        if (!mIsReceiverRegistered) {
            if (mReceiver == null)
                mReceiver = MyBroadcastReceiver()
            registerReceiver(mReceiver, IntentFilter("mainActivity"))
            mIsReceiverRegistered = true
        }
        requestPermissionsForBluetooth()
        checkConnection()


        //bindService(serviceIntent, mServiceConnection ,Context.BIND_AUTO_CREATE)
        //LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,makeGattUpdateIntentFilter())
        //val mainIntent = Intent("Main")
        //sendBroadcast(mainIntent)

    }

    override fun onPause() {
        super.onPause()
        if (mIsReceiverRegistered) {
            unregisterReceiver(mReceiver)
            mReceiver = null
            mIsReceiverRegistered = false
        }
    }

    override fun onDestroy() {
        val serviceIntent :Intent? = Intent("Main")
        serviceIntent!!.putExtra("status", "disconnect")
        sendBroadcast(serviceIntent)

        val intent :Intent? = Intent(this, UartService::class.java)
        stopService(intent)

        super.onDestroy()
        //LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver)
        //unbindService(mServiceConnection)
    }
//123
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
        } else {
            checkBluetooth()
        }
    }


    private fun checkBluetooth() {
        // 若手機不支援BLE則離開APP
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Utils.toastMakeTextAndShow(mContext, resources.getString(R.string.ble_not_supported),
                    Toast.LENGTH_SHORT)
            finish()
        }

        // 偵測手機是否內建藍芽·若有則偵測藍芽是否開啟
        val bluetoothManager = (getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)!!
        val mBluetoothAdapter = bluetoothManager.adapter

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Utils.toastMakeTextAndShow(mContext, resources.getString(R.string.ble_not_supported),
                    Toast.LENGTH_SHORT)
            finish()
        } else {
            if(!mBluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                enableBtIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                mContext.startActivity(enableBtIntent)
            }
        }
    }

    //20171128 Andy SQL
    private fun AddedSQLlite(intData: Int)
    {
        //////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        values  = ContentValues()
        //idTTDB = c!!.getCount().toLong()
        //Toast.makeText(this,"我要查比數:"+idTTDB,Toast.LENGTH_LONG).show()


        if (SaveToDB[0] !== "" && SaveToDB[1] !== "" && SaveToDB[2] !== "" && SaveToDB[3] !== "" && idTTDB >= 0) {//****************************************************************************
            //Toast.makeText(this@MainActivity, "資料滿4筆，我將要存到資料庫去!!!!!", Toast.LENGTH_LONG).show()
            //cv.put(columT[0],c.getPosition());
            values !!.put(columT[1], SaveToDB[0])
            values !!.put(columT[2], SaveToDB[1])
            values !!.put(columT[3], SaveToDB[2])
            values !!.put(columT[4], SaveToDB[3])
            values !!.put(columT[5], SaveToDB[4])
            //新增一筆五個測項資料到資料庫中

            idTTDB = dbrw.insert(tablename, null, values )
            Toast.makeText(this@MainActivity, "資料滿5，這筆資料內容:" + SaveToDB[0]+","+SaveToDB[1]+","+SaveToDB[2]+","+SaveToDB[3]+","+","+SaveToDB[4], Toast.LENGTH_LONG).show()


            //Toast.makeText(this@MainActivity, "資料滿4，這筆資料內容:" + SaveToDB[0]+","+SaveToDB[1]+","+SaveToDB[2]+","+SaveToDB[3]+",", Toast.LENGTH_LONG).show()

        } else {
            //Toast.makeText(this@MainActivity, "溫度、濕度、TVOC、CO2未滿，不新增資料庫", Toast.LENGTH_LONG).show()
        }
        //新增一筆四個測項資料到資料庫中
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////一次新增四個測項資料///////////////////////////////////////////////////一次新增四個測項資料//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    }
//****************************************************************************************************************************************************
//----------------------------------------------------------------------------------------------------------------------------------------------------------------------


    private fun SearchSQLlite() {
        //****************************************************************************************************************************************************
//------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //查詢CO2資料
        //查詢CO2資料
        //查詢CO2資料
        c = dbrw.query(tablename, columT, null, null, null, null, null)

        //Toast.makeText(MainActivity.this, "現在位置:"+c.getPosition(), 3000).show();
        //Toast.makeText(MainActivity.this, "現在ColumnIndex:"+ c.getString(c.getColumnIndex(columT[0])), 3000).show();


        // 排版
        co10T += colstT[0] + "\n";
        co11T += colstT[1] + "\n";
        co12T += colstT[2] + "\n";
        co13T += colstT[3] + "\n";
        co14T += colstT[4] + "\n"


        if (c!!.getCount() > 0) {
            //Toast.makeText(MainActivity.this, "測試是否有進去!!  " + c.getCount() + "筆紀錄",Toast.LENGTH_LONG).show();
            c!!.moveToFirst()

            for (i in 0 until c!!.getCount()) {
                //Toast.makeText(this@MainActivity, "測試是否進For!!  " + c!!.getCount() + "第" + i + "筆紀錄", Toast.LENGTH_LONG).show()
                //co10T += c!!.getString(c!!.getColumnIndex(columT[0])) + "\n"
                //co11T += c!!.getString(c!!.getColumnIndex(columT[1])) + "\n"
                //co12T += c!!.getString(c!!.getColumnIndex(columT[2])) + "\n"
                //co13T += c!!.getString(c!!.getColumnIndex(columT[3])) + "\n"
                //co14T += c!!.getString(c!!.getColumnIndex(columT[4])) + "\n"
                // sqlite比較不嚴僅，都用getString()取值即可
                co10T += c!!.getString(0) + "\n"
                //co11T += c!!.getString(1) + "\n"
                //co12T += c!!.getString(2) + "\n"
                //co13T += c!!.getString(3) + "\n"
                //co14T += c!!.getString(4) + "\n"
                //Toast.makeText(this@MainActivity, "增資料庫CO2第 [ " + (i + 1) + " ]筆CO2:" + c!!.getString(0 + 1) +"ppm", Toast.LENGTH_LONG).show()

                Count = c!!.getCount().toLong()
                //c.close();
                val CountString = Count.toString()
                //Toast.makeText(this@MainActivity, "共有" + CountString + "筆紀錄，第["+(i+1)+"]筆資料內容", Toast.LENGTH_LONG).show()
                /*
            Toast.makeText(this@MainActivity, "資料庫ID第 [ " + (i + 1) + " ]筆: NO" + c!!.getString(0)  +"\n"
                    +"資料庫溫度第 [ " + (i + 1) + " ]筆:" + c!!.getString(1) +"C \n"
                    +"資料庫濕度第 [ " + (i + 1) + " ]筆:" + c!!.getString(2) +"% \n"
                    +"資料庫CO2第 [ " + (i + 1) + " ]筆:" + c!!.getString(3) +"ppm \n"
                    +"資料庫TVOC第 [ " + (i + 1) + " ]筆:" + c!!.getString(4) +"ppb", Toast.LENGTH_LONG).show()

            c!!.moveToNext()
            */
            }
            //Toast.makeText(MainActivity.this, "現在位置:"+c.getPosition(), 3000).show();
            //Toast.makeText(MainActivity.this, "現在ColumnIndex:"+ c.getString(c.getColumnIndex(columT[0])), 3000).show();


            // 排版
            co10T += colstT[0] + "\n";
            co11T += colstT[1] + "\n";
            co12T += colstT[2] + "\n";
            co13T += colstT[3] + "\n";
            co14T += colstT[4] + "\n"
            co15T += colstT[5] + "\n"

            if (c!!.getCount() > 0) {
                //Toast.makeText(MainActivity.this, "測試是否有進去!!  " + c.getCount() + "筆紀錄",Toast.LENGTH_LONG).show();
                c!!.moveToFirst()

                for (i in 0 until c!!.getCount()) {
                    //Toast.makeText(this@MainActivity, "測試是否進For!!  " + c!!.getCount() + "第" + i + "筆紀錄", Toast.LENGTH_LONG).show()
                    //co10T += c!!.getString(c!!.getColumnIndex(columT[0])) + "\n"
                    //co11T += c!!.getString(c!!.getColumnIndex(columT[1])) + "\n"
                    //co12T += c!!.getString(c!!.getColumnIndex(columT[2])) + "\n"
                    //co13T += c!!.getString(c!!.getColumnIndex(columT[3])) + "\n"
                    //co14T += c!!.getString(c!!.getColumnIndex(columT[4])) + "\n"
                    // sqlite比較不嚴僅，都用getString()取值即可
                    //co10T += c!!.getString(0) + "\n"
                    //co11T += c!!.getString(1) + "\n"
                    //co12T += c!!.getString(2) + "\n"
                    //co13T += c!!.getString(3) + "\n"
                    //co14T += c!!.getString(4) + "\n"
                    //Toast.makeText(this@MainActivity, "增資料庫CO2第 [ " + (i + 1) + " ]筆CO2:" + c!!.getString(0 + 1) +"ppm", Toast.LENGTH_LONG).show()

                    Count = c!!.getCount().toLong()
                    //c.close();
                    val CountString = Count.toString()
                    Toast.makeText(this@MainActivity, "共有" + CountString + "筆紀錄，第[" + (i + 1) + "]筆資料內容", Toast.LENGTH_LONG).show()

                    Toast.makeText(this@MainActivity, "資料庫ID第 [ " + (i + 1) + " ]筆: NO" + c!!.getString(0) + "\n"
                            + "資料庫時間第 [ " + (i + 1) + " ]筆:" + c!!.getString(1) + " \n"
                            + "資料庫溫度第 [ " + (i + 1) + " ]筆:" + c!!.getString(2) + "C \n"
                            + "資料庫濕度第 [ " + (i + 1) + " ]筆:" + c!!.getString(3) + "% \n"
                            + "資料庫CO2第 [ " + (i + 1) + " ]筆:" + c!!.getString(4) + "ppm \n"
                            + "資料庫TVOC第 [ " + (i + 1) + " ]筆:" + c!!.getString(5) + "ppb", Toast.LENGTH_LONG).show()

                    c!!.moveToNext()
                }


            } else {
                Toast.makeText(this@MainActivity, "資料庫無查CO2資料", Toast.LENGTH_LONG).show()
            }
        }
    }

    private var myMenu : Menu? = null
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        myMenu=menu
        val menuItem : MenuItem? = menu!!.findItem(R.id.batStatus)
        btIcon = menu!!.findItem(R.id.bleStatus)
        battreyIcon=menu?.findItem(R.id.batStatus)
        menuItem!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item!!.itemId) {
            R.id.batStatus -> {
                dialogShow(getString(R.string.text_battery_title),
                        getString(R.string.text_battery_value) + batValue + "%")
            }

/*
            R.id.calendarView -> {
                DatePickerDialog(this@MainActivity, R.style.MyDatePickerDialogTheme,
                        dateSetListener,
                        // set DatePickerDialog to point to today's date when it loads up
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)).show()
            }
            */
        //  R.id.Andy_calendarView ->{
        //   CalendarShow("月曆","月曆選擇")
        // }


        //點選ActionBAR會返回
            android.R.id.home -> {
                mDrawerToggle!!.onOptionsItemSelected(item)
            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun uiFindViewById() {
        mPageVp = this.findViewById(R.id.id_page_vp)
        mDrawerLayout = this.findViewById(R.id.drawer_layout)
        nvDrawerNavigation = this.findViewById(R.id.navigation)
        nvDrawerNavigation?.menu?.findItem(R.id.nav_setting)?.isVisible = false
    }

    @Suppress("DEPRECATION")
    private fun viewPagerInit() {
        // 加入 Fragment 成員
        val mMainFg = MainFragment()
        val mTvocFg = TVOCFragment()

        mFragmentList.add(mMainFg)
        mFragmentList.add(mTvocFg)

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
                currentIndex = position
            }
        })
    }

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
        // 將 actionBar 和 DrawerLayout 取得關聯
        mDrawerToggle = ActionBarDrawerToggle(this, mDrawerLayout, R.string.text_drawer_open, R.string.text_drawer_close)
        // 同步 actionBarDrawerToggle
        mDrawerToggle!!.syncState()
        // 設定 DrawerLayout 監聽事件
        mDrawerLayout!!.addDrawerListener(mDrawerToggle!!)
    }

    private fun dialogShow(title : String, content : String) {
        val i : Intent? = Intent(this, CustomDialogActivity::class.java)
        val bundle : Bundle? = Bundle()
        bundle!!.putString("dialogTitle", title)
        bundle.putString("dialogContent", content)

        i!!.putExtras(bundle)
        startActivity(i)
    }

    // 20171127 Peter 新增：AboutActivity, AirMapActivity
    private fun aboutShow() {
        val i : Intent? = Intent(this, AboutActivity::class.java)
        startActivity(i)
    }

    private fun airmapShow(){
        val i : Intent? = Intent(this, AirMapActivity::class.java)
        startActivity(i)
    }

    // 20171127 Raymond 新增：知識庫activity
    private fun knowledgeShow() {
        val i : Intent? = Intent(this, KnowledgeActivity::class.java)
        startActivity(i)
    }

    private fun settingShow() {
        val i : Intent? = Intent(this, SettingActivity::class.java)
        startActivityForResult(i,REQUEST_SELECT_SAMPLE)
        //startActivity(i)
    }

    private fun tourShow() {
        val i : Intent? = Intent(this, TourActivity::class.java)
        startActivity(i)
    }


/*
    //20171124 Andy叫出月曆的方法
    private fun updateDateInView() {
        val myFormat = "yyyy/MM/dd" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.TAIWAN)
        Toast.makeText(this,sdf.format(cal.getTime()),Toast.LENGTH_LONG).show()

    }
    */


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
            R.id.nav_add_device -> blueToothShow()
            R.id.nav_disconnect_device -> blueToothdisconnect()
            R.id.nav_about -> {
                aboutShow()
            /*
               val intent: Intent? = Intent("Main")
               intent!!.putExtra("status", "callDeviceStartSample")
               sendBroadcast(intent)*/
               //    aboutShow()
           }
           R.id.nav_air_map -> airmapShow()
           R.id.nav_tour -> tourShow()
       //R.id.nav_second_fragment -> fragmentClass = SecondFragment::class.java
           R.id.nav_knowledge ->{
               knowledgeShow()
               /*
               val intent: Intent? = Intent("Main")
               intent!!.putExtra("status", "getSampleRate")
               sendBroadcast(intent)*/
             //  knowledgeShow()
           }
            R.id.nav_getData->{
                val intent: Intent? = Intent("Main")
                intent!!.putExtra("status", "getSampleRate")
                sendBroadcast(intent)
            }
           R.id.nav_setting -> settingShow()
       //R.id.nav_third_fragment -> fragmentClass = ThirdFragment::class.java
       //else -> fragmentClass = FirstFragment::class.java
       }

//        try {
//            fragment = fragmentClass.newInstance() as Fragment
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//
//        // Insert the fragment by replacing any existing fragment
//        val fragmentManager = supportFragmentManager
//        fragmentManager.beginTransaction().replace(R.id.flContent, fragment).commit()

       // Highlight the selected item has been done by NavigationView
       //menuItem.isChecked = true
       // Set action bar title

       //title = menuItem.title
       // Close the navigation drawer
       // ******************************************************//
        //    2017/11/28 Peter Title文字 不會隨著點選抽屜改變
        //title = menuItem.title
        // ******************************************************//

        // Close the navigation drawer
        mDrawerLayout?.closeDrawer(GravityCompat.START)
    }

    //menuItem點下去後StartActivityResult等待回傳
    private fun blueToothShow() {
        val i : Intent? = Intent(this,
                DeviceListActivity::class.java)
        startActivity(i)
        //startActivityForResult(i,REQUEST_SELECT_DEVICE)
    }

    private fun blueToothdisconnect() {
        val serviceIntent :Intent? = Intent("Main")
        serviceIntent!!.putExtra("status", "disconnect")
        sendBroadcast(serviceIntent)
        //stopService(serviceIntent)
    }

    //視回傳的code執行相對應的動作
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_SELECT_DEVICE ->
                //When the DeviceListActivity return, with the selected device address
                //得到Address後將Address後傳遞至Service後啟動 171129
                if (resultCode == Activity.RESULT_OK && data != null) {
                    //val deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE)
                    //mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress)
                    //val serviceIntent :Intent? = Intent(this, UartService::class.java)
                    //serviceIntent?.putExtra(BluetoothDevice.EXTRA_DEVICE, deviceAddress)
                    //startService(serviceIntent)
                    //Log.d("MAINActivity", "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mBluetoothLeService)
                    print("MainActivity")
                }
            REQUEST_SELECT_SAMPLE->{
                if ( data != null) {
                   var value= data.getIntExtra("choseCycle",0)
                    val intent: Intent? = Intent("Main")
                    intent!!.putExtra("status", "setSampleRate")

                    when(value){
                        0->{//2min
                            intent.putExtra("SampleTime",2)
                        }
                        1->{//10min
                            intent.putExtra("SampleTime",10)
                        }
                        2->{//15min
                            intent.putExtra("SampleTime",15)
                        }
                        3->{//20min
                            intent.putExtra("SampleTime",20)
                        }
                        4->{//30min
                            intent.putExtra("SampleTime",30)
                        }
                    }
                    sendBroadcast(intent)
                }
            }
            else -> {
                print("test")
            }
        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        var intentF = IntentFilter()
        intentF.addAction("com.nordicsemi.nrfUART.ACTION_GATT_CONNECTED")
        intentF.addAction("com.nordicsemi.nrfUART.ACTION_GATT_DISCONNECTED")
        intentF.addAction("com.nordicsemi.nrfUART.ACTION_GATT_SERVICES_DISCOVERED")
        intentF.addAction("com.nordicsemi.nrfUART.ACTION_DATA_AVAILABLE")
        intentF.addAction("com.nordicsemi.nrfUART.EXTRA_DATA")
        intentF.addAction("com.nordicsemi.nrfUART.DEVICE_DOES_NOT_SUPPORT_UART")
        return intentF
    }



    private val mServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, rawBinder: IBinder) {
            mService = (rawBinder as UartService.LocalBinder).serverInstance
        }

        override fun onServiceDisconnected(classname: ComponentName) {

        }
    }

        private fun updateUI(intent : Intent) {
            when (intent.getStringExtra("status")) {
                "ACTION_GATT_CONNECTED", "ACTION_GATT_CONNECTING"
                -> {
                    nvDrawerNavigation?.menu?.findItem(R.id.nav_add_device)?.isVisible = false
                    nvDrawerNavigation?.menu?.findItem(R.id.nav_disconnect_device)?.isVisible = true
                    nvDrawerNavigation?.menu?.findItem(R.id.nav_setting)?.isVisible = true
                    nvDrawerNavigation?.menu?.findItem(R.id.nav_getData)?.isVisible = true
                    nvDrawerNavigation?.getHeaderView(0)?.findViewById<TextView>(R.id.txt_devname)?.text=getText(R.string.Already_Connected)
                    nvDrawerNavigation?.getHeaderView(0)?.findViewById<ImageView>(R.id.img_bt_status)?.setImageResource(R.drawable.app_android_icon_connect)
                    btIcon!!.icon = resources.getDrawable(R.drawable.bluetooth_connect)
                    //battreyIcon?.icon= resources.getDrawable(R.drawable.battery_icon_low)
                    val intent: Intent? = Intent("Main")
                    intent!!.putExtra("status", "callDeviceStartSample")
                    sendBroadcast(intent)

                }
                "ACTION_GATT_DISCONNECTED", "ACTION_GATT_DISCONNECTING"
                -> {
                    nvDrawerNavigation?.menu?.findItem(R.id.nav_add_device)?.isVisible = true
                    nvDrawerNavigation?.menu?.findItem(R.id.nav_disconnect_device)?.isVisible = false
                    nvDrawerNavigation?.menu?.findItem(R.id.nav_setting)?.isVisible = false
                    nvDrawerNavigation?.menu?.findItem(R.id.nav_getData)?.isVisible = false
                    nvDrawerNavigation?.getHeaderView(0)?.findViewById<TextView>(R.id.txt_devname)?.text=getText(R.string.No_Device_Connect)
                    nvDrawerNavigation?.getHeaderView(0)?.findViewById<ImageView>(R.id.img_bt_status)?.setImageResource(R.drawable.app_android_icon_disconnect)
                    btIcon?.icon = resources.getDrawable(R.drawable.bluetooth_disconnect)
                    battreyIcon?.icon= resources.getDrawable(R.drawable.battery_icon_disconnect)
                }
                "B5"->{
                    var bundle= intent.getBundleExtra("result")
                    var data= bundle.getParcelableArrayList<myData>("resultSet")
                    val mFragmentAdapter :FragmentAdapter=mPageVp?.adapter as FragmentAdapter
                    (mFragmentAdapter.getItem(1)as TVOCFragment).AddedSQLlite(data)

                }
                "B6"->{
                    intent.getStringExtra("TVOCValue")
                    batValue=intent.getStringExtra("BatteryLife").toInt()
                    if (batValue>100){
                        battreyIcon?.icon= resources.getDrawable(R.drawable.battery_icon_charge)
                        batValue = batValue - 100
                        //myMenu?.findItem(R.id.batStatus)?.icon=getDrawable(R.drawable.battery_icon_charge)
                    }
                    else if(batValue in 66..100){
                        battreyIcon?.icon= resources.getDrawable(R.drawable.battery_icon_full)
                        //myMenu?.findItem(R.id.batStatus)?.icon=getDrawable(R.drawable.battery_icon_full)
                    }
                    else if(batValue in 23..65){
                        battreyIcon?.icon= resources.getDrawable(R.drawable.battery_icon_2grid)
                        //myMenu?.findItem(R.id.batStatus)?.icon=getDrawable(R.drawable.battery_icon_2grid)
                    }
                    else if (batValue in 10..32){
                        battreyIcon?.icon= resources.getDrawable(R.drawable.battery_icon_1grid)
                        //myMenu?.findItem(R.id.batStatus)?.icon=getDrawable(R.drawable.battery_icon_1grid)
                    }
                    else {
                        battreyIcon?.icon= resources.getDrawable(R.drawable.battery_icon_low)
                    }
                   // (mPageVp?.adapter?.getItemPosition(0) as MainFragment).setBar1CurrentValue(intent.getStringExtra("TVOCValue").toFloat())
                    val mFragmentAdapter :FragmentAdapter=mPageVp?.adapter as FragmentAdapter
                    (mFragmentAdapter.getItem(0)as MainFragment).setBar1CurrentValue(intent.getStringExtra("TVOCValue"))
                }
            }
            checkBluetooth()
        }


        inner class MyBroadcastReceiver : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                updateUI(intent)
            }

        }
        private fun checkConnection() {
            if (UartService.mConnectionState == 0) {
                val mainIntent = Intent("Main")
                mainIntent.putExtra("status", "ACTION_GATT_DISCONNECTED")
                sendBroadcast(mainIntent)
            }else{
                val mainIntent = Intent("Main")
                mainIntent.putExtra("status", "ACTION_GATT_CONNECTED")
                sendBroadcast(mainIntent)
            }
        }

    }



