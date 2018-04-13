package com.microjet.airqi2.BlueTooth.DFU

import android.Manifest
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningServiceInfo
import android.app.LoaderManager.LoaderCallbacks
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.support.annotation.NonNull
import android.support.v4.app.ActivityCompat
import android.support.v4.app.DialogFragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.microjet.airqi2.BlueTooth.DFU.Adapter.FileBrowserAppsAdapter
import com.microjet.airqi2.BlueTooth.DFU.Fragment.UploadCancelFragment
import com.microjet.airqi2.BlueTooth.DFU.Fragment.ZipInfoFragment
import com.microjet.airqi2.BlueTooth.DFU.Settings.SettingFragment
import com.microjet.airqi2.BlueTooth.DFU.Settings.SettingsActivity
import com.microjet.airqi2.BlueTooth.Scanner.ScannerFragment
import com.microjet.airqi2.CustomAPI.FileHelper
import com.microjet.airqi2.R
import com.microjet.airqi2.URL.AirActionTask
import no.nordicsemi.android.dfu.DfuBaseService
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper
import no.nordicsemi.android.dfu.DfuSettingsConstants.*

import java.io.File
/**
 * Created by B00055 on 2018/3/26.
 */
class DFUActivity : AppCompatActivity() , LoaderCallbacks<Cursor>, ScannerFragment.OnDeviceSelectedListener,
UploadCancelFragment.CancelFragmentListener, PermissionRationaleFragment.PermissionDialogListener {
    private val TAG = "DfuActivity"

    private val PREFS_DEVICE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_DEVICE_NAME"
    private val PREFS_FILE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_NAME"
    private val PREFS_FILE_TYPE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_TYPE"
    private val PREFS_FILE_SCOPE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_SCOPE"
    private val PREFS_FILE_SIZE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_SIZE"

    private val DATA_DEVICE = "device"
    private val DATA_FILE_TYPE = "file_type"
    private val DATA_FILE_TYPE_TMP = "file_type_tmp"
    private val DATA_FILE_PATH = "file_path"
    private val DATA_FILE_STREAM = "file_stream"
    private val DATA_INIT_FILE_PATH = "init_file_path"
    private val DATA_INIT_FILE_STREAM = "init_file_stream"
    private val DATA_STATUS = "status"
    private val DATA_SCOPE = "scope"
    private val DATA_DFU_COMPLETED = "dfu_completed"
    private val DATA_DFU_ERROR = "dfu_error"

    private val EXTRA_URI = "uri"

    private val PERMISSION_REQ = 25
    private val ENABLE_BT_REQ = 0
    private val SELECT_FILE_REQ = 1
    private val SELECT_INIT_FILE_REQ = 2

    private var mDeviceNameView: TextView? = null
    private var mFileNameView: TextView? = null
    private var mFileTypeView: TextView? = null
    private var mFileScopeView: TextView? = null
    private var mFileSizeView: TextView? = null
    private var mFileStatusView: TextView? = null
    private var mTextPercentage: TextView? = null
    private var mTextUploading: TextView? = null
    private var mProgressBar: ProgressBar? = null

    private var mSelectFileButton: Button? = null
    private var mUploadButton:Button? = null
    private var mConnectButton:Button? = null

    private var mSelectedDevice: BluetoothDevice? = null
    private var mFilePath: String? = null
    private var mFileStreamUri: Uri? = null
    private var mInitFilePath: String? = null
    private var mInitFileStreamUri: Uri? = null
    private var mFileType: Int = 0
    private var mFileTypeTmp: Int = 0 // This value is being used when user is selecting a file not to overwrite the old value (in case he/she will cancel selecting file)
    private var mScope: Int? = null
    private var mStatusOk: Boolean = false
    /** Flag set to true in [.onRestart] and to false in [.onPause].  */
    private var mResumed: Boolean = false
    /** Flag set to true if DFU operation was completed while [.mResumed] was false.  */
    private var mDfuCompleted: Boolean = false
    /** The error message received from DFU service while [.mResumed] was false.  */
    private var mDfuError: String? = null

    /**
     * The progress listener receives events from the DFU Service.
     * If is registered in onCreate() and unregistered in onDestroy() so methods here may also be called
     * when the screen is locked or the app went to the background. This is because the UI needs to have the
     * correct information after user comes back to the activity and this information can't be read from the service
     * as it might have been killed already (DFU completed or finished with error).
     */
    private val mDfuProgressListener = object : DfuProgressListenerAdapter() {
        override fun onDeviceConnecting(deviceAddress: String?) {
            mProgressBar!!.isIndeterminate = true
            mTextPercentage!!.setText(R.string.dfu_status_connecting)
        }

        override fun onDfuProcessStarting(deviceAddress: String?) {
            mProgressBar!!.isIndeterminate = true
            mTextPercentage!!.setText(R.string.dfu_status_starting)
        }

        override fun onEnablingDfuMode(deviceAddress: String?) {
            mProgressBar!!.isIndeterminate = true
            mTextPercentage!!.setText(R.string.dfu_status_switching_to_dfu)
        }

        override fun onFirmwareValidating(deviceAddress: String?) {
            mProgressBar!!.isIndeterminate = true
            mTextPercentage!!.setText(R.string.dfu_status_validating)
        }

        override fun onDeviceDisconnecting(deviceAddress: String?) {
            mProgressBar!!.isIndeterminate = true
            mTextPercentage!!.setText(R.string.dfu_status_disconnecting)
        }

        override fun onDfuCompleted(deviceAddress: String?) {
            mTextPercentage!!.setText(R.string.dfu_status_completed)
            if (mResumed) {
                // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
                Handler().postDelayed({
                    onTransferCompleted()
                    showDownloadDialog("DFU Successful")
                    // if this activity is still open and upload process was completed, cancel the notification
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(DfuBaseService.NOTIFICATION_ID)
                }, 200)
            } else {
                // Save that the DFU process has finished
                mDfuCompleted = true
            }
            val file=File(cacheDir, "FWupdate.zip")
            if( file.exists()) {
               file.delete()
            }
        }
        private fun showDownloadDialog(msg: String) {
            val Dialog = android.app.AlertDialog.Builder(this@DFUActivity).create()
            //必須是android.app.AlertDialog.Builder 否則alertDialog.show()會報錯
            //Dialog.setTitle("提示")
            Dialog.setTitle(getString(R.string.remind))
            Dialog.setMessage(msg)
            Dialog.setCancelable(false)//讓返回鍵與空白無效
            //Dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "确定")
            Dialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.Accept))//是
            { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            Dialog.show()
        }
        override fun onDfuAborted(deviceAddress: String?) {
            mTextPercentage!!.setText(R.string.dfu_status_aborted)
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            Handler().postDelayed({
                onUploadCanceled()

                // if this activity is still open and upload process was completed, cancel the notification
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(DfuBaseService.NOTIFICATION_ID)
            }, 200)
        }

        override fun onProgressChanged(deviceAddress: String?, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
            mProgressBar!!.isIndeterminate = false
            mProgressBar!!.progress = percent
            mTextPercentage!!.text = getString(R.string.dfu_uploading_percentage, percent)
            if (partsTotal > 1)
                mTextUploading!!.text = getString(R.string.dfu_status_uploading_part, currentPart, partsTotal)
            else
                mTextUploading!!.setText(R.string.dfu_status_uploading)
        }

        override fun onError(deviceAddress: String?, error: Int, errorType: Int, message: String?) {
            if (mResumed) {
                showErrorMessage(message)

                // We have to wait a bit before canceling notification. This is called before DfuService creates the last notification.
                Handler().postDelayed({
                    // if this activity is still open and upload process was completed, cancel the notification
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(DfuBaseService.NOTIFICATION_ID)
                }, 200)
            } else {
                mDfuError = message
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feature_dfu)
        isBLESupported()
        if (!isBLEEnabled()) {
            showBLEDialog()
        }
        setGUI()
        // Try to create sample files
        if (FileHelper.newSamplesAvailable(this)) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                FileHelper.createSamples(this)
            } else {
                val dialog = PermissionRationaleFragment.getInstance(R.string.permission_sd_text, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                dialog.show(supportFragmentManager, null)
            }
        }

        // restore saved state
        mFileType = DfuBaseService.TYPE_AUTO // Default
        if (savedInstanceState != null) {
            mFileType = savedInstanceState.getInt(DATA_FILE_TYPE)
            mFileTypeTmp = savedInstanceState.getInt(DATA_FILE_TYPE_TMP)
            mFilePath = savedInstanceState.getString(DATA_FILE_PATH)
            mFileStreamUri = savedInstanceState.getParcelable(DATA_FILE_STREAM)
            mInitFilePath = savedInstanceState.getString(DATA_INIT_FILE_PATH)
            mInitFileStreamUri = savedInstanceState.getParcelable(DATA_INIT_FILE_STREAM)
            mSelectedDevice = savedInstanceState.getParcelable(DATA_DEVICE)
            mStatusOk = mStatusOk || savedInstanceState.getBoolean(DATA_STATUS)
            mScope = if (savedInstanceState.containsKey(DATA_SCOPE)) savedInstanceState.getInt(DATA_SCOPE) else null
            mUploadButton!!.setEnabled(mSelectedDevice != null && mStatusOk)
            mDfuCompleted = savedInstanceState.getBoolean(DATA_DFU_COMPLETED)
            mDfuError = savedInstanceState.getString(DATA_DFU_ERROR)
        }

        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener)
        val file=File(cacheDir, "FWupdate.zip")

        myDeviceAddress = intent.extras.getString("ADDRESS")
        myDeviceName = intent.extras.getString("DEVICE_NAME")
        if( file.exists()) {
            mjupdateFileInfo(file.name, file.length(), 0)
            mFilePath=file.path
        }
    }

    var myDeviceName:String?=null
    var myDeviceAddress:String?=null
    override fun onDestroy() {
        super.onDestroy()
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener)
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        outState!!.putInt(DATA_FILE_TYPE, mFileType)
        outState.putInt(DATA_FILE_TYPE_TMP, mFileTypeTmp)
        outState.putString(DATA_FILE_PATH, mFilePath)
        outState.putParcelable(DATA_FILE_STREAM, mFileStreamUri)
        outState.putString(DATA_INIT_FILE_PATH, mInitFilePath)
        outState.putParcelable(DATA_INIT_FILE_STREAM, mInitFileStreamUri)
        outState.putParcelable(DATA_DEVICE, mSelectedDevice)
        outState.putBoolean(DATA_STATUS, mStatusOk)
        if (mScope != null) outState.putInt(DATA_SCOPE, mScope!!)
        outState.putBoolean(DATA_DFU_COMPLETED, mDfuCompleted)
        outState.putString(DATA_DFU_ERROR, mDfuError)
    }

    private fun setGUI() {
    //    val toolbar = findViewById<Toolbar>(R.id.toolbar_actionbar)
    //    setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        mDeviceNameView = findViewById(R.id.device_name)
        mFileNameView = findViewById(R.id.file_name)
        mFileTypeView = findViewById(R.id.file_type)
        mFileScopeView = findViewById(R.id.file_scope)
        mFileSizeView = findViewById(R.id.file_size)
        mFileStatusView = findViewById(R.id.file_status)
        mSelectFileButton = findViewById(R.id.action_select_file)
        mUploadButton = findViewById<Button>(R.id.action_upload)
        mConnectButton = findViewById<Button>(R.id.action_connect)
        mTextPercentage = findViewById(R.id.textviewProgress)
        mTextUploading = findViewById(R.id.textviewUploading)
        mProgressBar = findViewById(R.id.progressbar_file)

        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        if (isDfuServiceRunning()) {
            // Restore image file information
            mDeviceNameView!!.text = preferences.getString(PREFS_DEVICE_NAME, "")
            mFileNameView!!.text = preferences.getString(PREFS_FILE_NAME, "")
            mFileTypeView!!.text = preferences.getString(PREFS_FILE_TYPE, "")
            mFileScopeView!!.text = preferences.getString(PREFS_FILE_SCOPE, "")
            mFileSizeView!!.text = preferences.getString(PREFS_FILE_SIZE, "")
            mFileStatusView!!.setText(R.string.dfu_file_status_ok)
            mStatusOk = true
            showProgressBar()
        }
    }

    override fun onResume() {
        super.onResume()
        mResumed = true
        if (mDfuCompleted)
            onTransferCompleted()
        if (mDfuError != null)
            showErrorMessage(mDfuError)
        if (mDfuCompleted || mDfuError != null) {
            // if this activity is still open and upload process was completed, cancel the notification
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.cancel(DfuBaseService.NOTIFICATION_ID)
            mDfuCompleted = false
            mDfuError = null
        }
    }

    override fun onPause() {
        super.onPause()
        mResumed = false
    }

    override fun onRequestPermission(permission: String?) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQ)
     //   TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

   // fun onRequestPermission(permission: String) {
   //     ActivityCompat.requestPermissions(this, arrayOf(permission), PERMISSION_REQ)
   // }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQ -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have been granted the Manifest.permission.WRITE_EXTERNAL_STORAGE permission. Now we may proceed with exporting.
                    FileHelper.createSamples(this)
                } else {
                    Toast.makeText(this, R.string.no_required_permission, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun isBLESupported() {
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast(R.string.no_ble)
            finish()
        }
    }

    private fun isBLEEnabled(): Boolean {
        val manager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = manager.adapter
        return adapter != null && adapter.isEnabled
    }

    private fun showBLEDialog() {
        val enableIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(enableIntent, ENABLE_BT_REQ)
    }

    private fun showDeviceScanningDialog() {
        val dialog = ScannerFragment.getInstance(null) // Device that is advertising directly does not have the GENERAL_DISCOVERABLE nor LIMITED_DISCOVERABLE flag set.
        dialog.show(supportFragmentManager, "scan_fragment")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings_and_about, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        //    R.id.action_about -> { //       val fragment = AppHelpFragment.getInstance(R.string.dfu_about_text)
        //        fragment.show(supportFragmentManager, "help_fragment")
        //    }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
            }
        }
        return true
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (resultCode != Activity.RESULT_OK)
            return

        when (requestCode) {
            SELECT_FILE_REQ -> {
                // clear previous data
                mFileType = mFileTypeTmp
                mFilePath = null
                mFileStreamUri = null

                // and read new one
                val uri = data.data
                /*
			 * The URI returned from application may be in 'file' or 'content' schema. 'File' schema allows us to create a File object and read details from if
			 * directly. Data from 'Content' schema must be read by Content Provider. To do that we are using a Loader.
			 */
                if (uri!!.scheme == "file") {
                    // the direct path to the file has been returned
                    val path = uri.path
                    val file = File(path)
                    mFilePath = path

                    updateFileInfo(file.name, file.length(), mFileType)
                } else if (uri.scheme == "content") {
                    // an Uri has been returned
                    mFileStreamUri = uri
                    // if application returned Uri for streaming, let's us it. Does it works?
                    // FIXME both Uris works with Google Drive app. Why both? What's the difference? How about other apps like DropBox?
                    val extras = data.extras
                    if (extras != null && extras.containsKey(Intent.EXTRA_STREAM))
                        mFileStreamUri = extras.getParcelable(Intent.EXTRA_STREAM)

                    // file name and size must be obtained from Content Provider
                    val bundle = Bundle()
                    bundle.putParcelable(EXTRA_URI, uri)
                    loaderManager.restartLoader(SELECT_FILE_REQ, bundle, this)
                }
            }
            SELECT_INIT_FILE_REQ -> {
                mInitFilePath = null
                mInitFileStreamUri = null

                // and read new one
                val uri = data.data
                /*
			 * The URI returned from application may be in 'file' or 'content' schema. 'File' schema allows us to create a File object and read details from if
			 * directly. Data from 'Content' schema must be read by Content Provider. To do that we are using a Loader.
			 */
                if (uri!!.scheme == "file") {
                    // the direct path to the file has been returned
                    mInitFilePath = uri.path
                    mFileStatusView!!.setText(R.string.dfu_file_status_ok_with_init)
                } else if (uri.scheme == "content") {
                    // an Uri has been returned
                    mInitFileStreamUri = uri
                    // if application returned Uri for streaming, let's us it. Does it works?
                    // FIXME both Uris works with Google Drive app. Why both? What's the difference? How about other apps like DropBox?
                    val extras = data.extras
                    if (extras != null && extras.containsKey(Intent.EXTRA_STREAM))
                        mInitFileStreamUri = extras.getParcelable(Intent.EXTRA_STREAM)
                    mFileStatusView!!.setText(R.string.dfu_file_status_ok_with_init)
                }
            }
            else -> {
            }
        }
    }

    override fun onCreateLoader(id: Int, args: Bundle): Loader<Cursor> {
        val uri = args.getParcelable<Uri>(EXTRA_URI)
        /*
		 * Some apps, f.e. Google Drive allow to select file that is not on the device. There is no "_data" column handled by that provider. Let's try to obtain
		 * all columns and than check which columns are present.
		 */
        // final String[] projection = new String[] { MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DATA };
        return CursorLoader(this, uri, null, null, null, null)/* all columns, instead of projection */
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mFileNameView!!.text = null
        mFileTypeView!!.text = null
        mFileSizeView!!.text = null
        mFilePath = null
        mFileStreamUri = null
        mStatusOk = false
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (data != null && data.moveToNext()) {
            /*
			 * Here we have to check the column indexes by name as we have requested for all. The order may be different.
			 */
            val fileName = data.getString(data.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)/* 0 DISPLAY_NAME */)
            val fileSize = data.getInt(data.getColumnIndex(MediaStore.MediaColumns.SIZE) /* 1 SIZE */)
            var filePath: String? = null
            val dataIndex = data.getColumnIndex(MediaStore.MediaColumns.DATA)
            if (dataIndex != -1)
                filePath = data.getString(dataIndex /* 2 DATA */)
            if (!TextUtils.isEmpty(filePath))
                mFilePath = filePath

            updateFileInfo(fileName, fileSize.toLong(), mFileType)
        } else {
            mFileNameView!!.text = null
            mFileTypeView!!.text = null
            mFileSizeView!!.text = null
            mFilePath = null
            mFileStreamUri = null
            mFileStatusView!!.setText(R.string.dfu_file_status_error)
            mStatusOk = false
        }
    }

    /**
     * Updates the file information on UI
     *
     * @param fileName file name
     * @param fileSize file length
     */
    private fun updateFileInfo(fileName: String, fileSize: Long, fileType: Int) {
        mFileNameView!!.text = fileName
        when (fileType) {
            DfuBaseService.TYPE_AUTO -> mFileTypeView!!.text = resources.getStringArray(R.array.dfu_file_type)[0]
            DfuBaseService.TYPE_SOFT_DEVICE -> mFileTypeView!!.text = resources.getStringArray(R.array.dfu_file_type)[1]
            DfuBaseService.TYPE_BOOTLOADER -> mFileTypeView!!.text = resources.getStringArray(R.array.dfu_file_type)[2]
            DfuBaseService.TYPE_APPLICATION -> mFileTypeView!!.text = resources.getStringArray(R.array.dfu_file_type)[3]
        }
        mFileSizeView!!.text = getString(R.string.dfu_file_size_text, fileSize)
        mFileScopeView!!.text = getString(R.string.not_available)
        val extension = if (mFileType == DfuBaseService.TYPE_AUTO) "(?i)ZIP" else "(?i)HEX|BIN" // (?i) =  case insensitive
        mStatusOk = MimeTypeMap.getFileExtensionFromUrl(fileName).matches(extension.toRegex())
        val statusOk = mStatusOk
        mFileStatusView!!.setText(if (statusOk) R.string.dfu_file_status_ok else R.string.dfu_file_status_invalid)
        mUploadButton!!.setEnabled(mSelectedDevice != null && statusOk)

        // Ask the user for the Init packet file if HEX or BIN files are selected. In case of a ZIP file the Init packets should be included in the ZIP.
        if (statusOk) {
            if (fileType != DfuBaseService.TYPE_AUTO) {
                mScope = null
                mFileScopeView!!.text = getString(R.string.not_available)
                AlertDialog.Builder(this).setTitle(R.string.dfu_file_init_title).setMessage(R.string.dfu_file_init_message)
                        .setNegativeButton(R.string.no, { dialog, which ->
                            mInitFilePath = null
                            mInitFileStreamUri = null
                        }).setPositiveButton(R.string.yes, { dialog, which ->
                            val intent = Intent(Intent.ACTION_GET_CONTENT)
                            intent.type = DfuBaseService.MIME_TYPE_OCTET_STREAM
                            intent.addCategory(Intent.CATEGORY_OPENABLE)
                            startActivityForResult(intent, SELECT_INIT_FILE_REQ)
                        }).show()
            } else {
                AlertDialog.Builder(this).setTitle(R.string.dfu_file_scope_title).setCancelable(false)
                        .setSingleChoiceItems(R.array.dfu_file_scope, 0, { dialog, which ->
                            when (which) {
                                0 -> mScope = null
                                1 -> mScope = DfuServiceInitiator.SCOPE_SYSTEM_COMPONENTS
                                2 -> mScope = DfuServiceInitiator.SCOPE_APPLICATION
                            }
                        }).setPositiveButton(R.string.ok, { dialogInterface, i ->
                            val index: Int
                            if (mScope == null) {
                                index = 0
                            } else if (mScope == DfuServiceInitiator.SCOPE_SYSTEM_COMPONENTS) {
                                index = 1
                            } else {
                                index = 2
                            }
                            mFileScopeView!!.text = resources.getStringArray(R.array.dfu_file_scope)[index]
                        }).show()
            }
        }
    }

    /**
     * Called when the question mark was pressed
     *
     * @param view a button that was pressed
     */
    fun onSelectFileHelpClicked(view: View) {
        AlertDialog.Builder(this).setTitle(R.string.dfu_help_title).setMessage(R.string.dfu_help_message).setPositiveButton(R.string.ok, null)
                .show()
    }

    /**
     * Called when Select File was pressed
     *
     * @param view a button that was pressed
     */
    fun onSelectFileClicked(view: View) {
        mFileTypeTmp = mFileType
        var index = 0
        when (mFileType) {
            DfuBaseService.TYPE_AUTO -> index = 0
            DfuBaseService.TYPE_SOFT_DEVICE -> index = 1
            DfuBaseService.TYPE_BOOTLOADER -> index = 2
            DfuBaseService.TYPE_APPLICATION -> index = 3
        }
        // Show a dialog with file types
        AlertDialog.Builder(this).setTitle(R.string.dfu_file_type_title)
                .setSingleChoiceItems(R.array.dfu_file_type, index, { dialog, which ->
                    when (which) {
                        0 -> mFileTypeTmp = DfuBaseService.TYPE_AUTO
                        1 -> mFileTypeTmp = DfuBaseService.TYPE_SOFT_DEVICE
                        2 -> mFileTypeTmp = DfuBaseService.TYPE_BOOTLOADER
                        3 -> mFileTypeTmp = DfuBaseService.TYPE_APPLICATION
                    }
                }).setPositiveButton(R.string.ok, { dialog, which -> openFileChooser() }).setNeutralButton(R.string.dfu_file_info, { dialog, which ->
                    val fragment = ZipInfoFragment()
                    fragment.show(supportFragmentManager, "help_fragment")
                }).setNegativeButton(R.string.cancel, null).show()
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = if (mFileTypeTmp == DfuBaseService.TYPE_AUTO) DfuBaseService.MIME_TYPE_ZIP else DfuBaseService.MIME_TYPE_OCTET_STREAM
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        if (intent.resolveActivity(packageManager) != null) {
            // file browser has been found on the device
            startActivityForResult(intent, SELECT_FILE_REQ)
        } else {
            // there is no any file browser app, let's try to download one
            val customView = layoutInflater.inflate(R.layout.app_file_browser, null)
            val appsList = customView.findViewById<ListView>(android.R.id.list)
            appsList.setAdapter(FileBrowserAppsAdapter(this))
            appsList.setChoiceMode(ListView.CHOICE_MODE_SINGLE)
            appsList.setItemChecked(0, true)
            AlertDialog.Builder(this).setTitle(R.string.dfu_alert_no_filebrowser_title).setView(customView)
                    .setNegativeButton(R.string.no, { dialog, which -> dialog.dismiss() }).setPositiveButton(R.string.ok, { dialog, which ->
                        val pos = appsList.getCheckedItemPosition()
                        if (pos >= 0) {
                            val query = resources.getStringArray(R.array.dfu_app_file_browser_action)[pos]
                            val storeIntent = Intent(Intent.ACTION_VIEW, Uri.parse(query))
                            startActivity(storeIntent)
                        }
                    }).show()
        }
    }

    /**
     * Callback of UPDATE/CANCEL button on DfuActivity
     */
    fun onUploadClicked(view: View) {
        if (isDfuServiceRunning()) {
            showUploadCancelDialog()
            return
        }

        // Check whether the selected file is a HEX file (we are just checking the extension)
        if (!mStatusOk) {
            Toast.makeText(this, R.string.dfu_file_status_invalid_message, Toast.LENGTH_LONG).show()
            return
        }

        // Save current state in order to restore it if user quit the Activity
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val editor = preferences.edit()
        editor.putString(PREFS_DEVICE_NAME,myDeviceName)// mSelectedDevice!!.name)
        editor.putString(PREFS_FILE_NAME, mFileNameView!!.text.toString())
        editor.putString(PREFS_FILE_TYPE, mFileTypeView!!.text.toString())
        editor.putString(PREFS_FILE_SCOPE, mFileScopeView!!.text.toString())
        editor.putString(PREFS_FILE_SIZE, mFileSizeView!!.text.toString())
        editor.apply()
        showProgressBar()
        val keepBond = preferences.getBoolean(SettingFragment.SETTINGS_KEEP_BOND, false)
        val forceDfu = preferences.getBoolean(SETTINGS_ASSUME_DFU_NODE, false)
        val enablePRNs = preferences.getBoolean(SETTINGS_PACKET_RECEIPT_NOTIFICATION_ENABLED, Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        val value = preferences.getString(SETTINGS_NUMBER_OF_PACKETS, DfuServiceInitiator.DEFAULT_PRN_VALUE.toString())
        var numberOfPackets: Int
        try {
            numberOfPackets = Integer.parseInt(value)
        } catch (e: NumberFormatException) {
            numberOfPackets = DfuServiceInitiator.DEFAULT_PRN_VALUE
        }

        val starter = DfuServiceInitiator(myDeviceAddress)//mSelectedDevice!!.address)
                .setDeviceName(myDeviceName)//mSelectedDevice!!.name)
                .setKeepBond(keepBond)
                .setForceDfu(forceDfu)
                .setPacketsReceiptNotificationsEnabled(enablePRNs)
                .setPacketsReceiptNotificationsValue(numberOfPackets)
                .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)
        if (mFileType == DfuBaseService.TYPE_AUTO) {
            starter.setZip( mFilePath)// starter.setZip(mFileStreamUri, mFilePath)
            if (mScope != null)
                starter.setScope(mScope!!)
        } else {
            starter.setBinOrHex(mFileType, mFileStreamUri, mFilePath).setInitFile(mInitFileStreamUri, mInitFilePath)
        }
        starter.start(this, DFUService::class.java)
    }

    private fun showUploadCancelDialog() {
        val manager = LocalBroadcastManager.getInstance(this)
        val pauseAction = Intent(DfuBaseService.BROADCAST_ACTION)
        pauseAction.putExtra(DfuBaseService.EXTRA_ACTION, DfuBaseService.ACTION_PAUSE)
        manager.sendBroadcast(pauseAction)

        val fragment = UploadCancelFragment()
        fragment.show(supportFragmentManager, TAG)
    }

    /**
     * Callback of CONNECT/DISCONNECT button on DfuActivity
     */
    fun onConnectClicked(view: View) {
        if (isBLEEnabled()) {
            showDeviceScanningDialog()
        } else {
            showBLEDialog()
        }
    }

   override fun onDeviceSelected(device: BluetoothDevice, name: String) {
        mSelectedDevice = device
        mUploadButton!!.setEnabled(mStatusOk)
        mDeviceNameView!!.text = name ?: getString(R.string.not_available)
    }

   override fun onDialogCanceled() {
        // do nothing
    }

    private fun showProgressBar() {
        mProgressBar!!.visibility = View.VISIBLE
        mTextPercentage!!.visibility = View.VISIBLE
        mTextPercentage!!.text = null
        mTextUploading!!.setText(R.string.dfu_status_uploading)
        mTextUploading!!.visibility = View.VISIBLE
        mConnectButton!!.setEnabled(false)
        mSelectFileButton!!.isEnabled = false
        mUploadButton!!.setEnabled(true)
        mUploadButton!!.setText(R.string.dfu_action_upload_cancel)
    }

    private fun onTransferCompleted() {
        clearUI(true)
        showToast(R.string.dfu_success)

    }

    fun onUploadCanceled() {
        clearUI(false)
        showToast(R.string.dfu_aborted)
    }

   override fun onCancelUpload() {
        mProgressBar!!.isIndeterminate = true
        mTextUploading!!.setText(R.string.dfu_status_aborting)
        mTextPercentage!!.text = null
    }

    private fun showErrorMessage(message: String?) {
        clearUI(false)
        showToast("Upload failed: " + message!!)
    }

    private fun clearUI(clearDevice: Boolean) {
        mProgressBar!!.visibility = View.INVISIBLE
        mTextPercentage!!.visibility = View.INVISIBLE
        mTextUploading!!.visibility = View.INVISIBLE
        mConnectButton!!.setEnabled(true)
        mSelectFileButton!!.isEnabled = true
        mUploadButton!!.setEnabled(false)
        mUploadButton!!.setText(R.string.dfu_action_upload)
        if (clearDevice) {
            mSelectedDevice = null
            mDeviceNameView!!.setText(R.string.dfu_default_name)
        }
        // Application may have lost the right to these files if Activity was closed during upload (grant uri permission). Clear file related values.
        mFileNameView!!.text = null
        mFileTypeView!!.text = null
        mFileScopeView!!.text = null
        mFileSizeView!!.text = null
        mFileStatusView!!.setText(R.string.dfu_file_status_no_file)
        mFilePath = null
        mFileStreamUri = null
        mInitFilePath = null
        mInitFileStreamUri = null
        mStatusOk = false
    }

    private fun showToast(messageResId: Int) {
        Toast.makeText(this, messageResId, Toast.LENGTH_SHORT).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun isDfuServiceRunning(): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (DFUService::class.java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    /**
     * Updates the file information on UI
     *
     * @param fileName file name
     * @param fileSize file length
     */
    private fun mjupdateFileInfo(fileName: String, fileSize: Long, fileType: Int) {
        mFileNameView!!.text = fileName
        when (fileType) {
            DfuBaseService.TYPE_AUTO -> mFileTypeView!!.text = resources.getStringArray(R.array.dfu_file_type)[0]
            DfuBaseService.TYPE_SOFT_DEVICE -> mFileTypeView!!.text = resources.getStringArray(R.array.dfu_file_type)[1]
            DfuBaseService.TYPE_BOOTLOADER -> mFileTypeView!!.text = resources.getStringArray(R.array.dfu_file_type)[2]
            DfuBaseService.TYPE_APPLICATION -> mFileTypeView!!.text = resources.getStringArray(R.array.dfu_file_type)[3]
        }
        mFileSizeView!!.text = getString(R.string.dfu_file_size_text, fileSize)
        mFileScopeView!!.text = getString(R.string.not_available)
        val extension = if (mFileType == DfuBaseService.TYPE_AUTO) "(?i)ZIP" else "(?i)HEX|BIN" // (?i) =  case insensitive
        mStatusOk = MimeTypeMap.getFileExtensionFromUrl(fileName).matches(extension.toRegex())
        val statusOk = mStatusOk
        mFileStatusView!!.setText(if (statusOk) R.string.dfu_file_status_ok else R.string.dfu_file_status_invalid)
        mUploadButton!!.setEnabled(statusOk)//mSelectedDevice != null &&

                // Ask the user for the Init packet file if HEX or BIN files are selected. In case of a ZIP file the Init packets should be included in the ZIP.
        if (statusOk) {
            if (fileType != DfuBaseService.TYPE_AUTO) {
                mScope = null
                mFileScopeView!!.text = getString(R.string.not_available)
                AlertDialog.Builder(this).setTitle(R.string.dfu_file_init_title).setMessage(R.string.dfu_file_init_message)
                        .setNegativeButton(R.string.no, { dialog, which ->
                            mInitFilePath = null
                            mInitFileStreamUri = null
                        }).setPositiveButton(R.string.yes, { dialog, which ->
                            val intent = Intent(Intent.ACTION_GET_CONTENT)
                            intent.type = DfuBaseService.MIME_TYPE_OCTET_STREAM
                            intent.addCategory(Intent.CATEGORY_OPENABLE)
                            startActivityForResult(intent, SELECT_INIT_FILE_REQ)
                        }).show()
            } else {
                AlertDialog.Builder(this).setTitle(R.string.dfu_file_scope_title).setCancelable(false)
                        .setSingleChoiceItems(R.array.dfu_file_scope, 0, { dialog, which ->
                            when (which) {
                                0 -> mScope = null
                                1 -> mScope = DfuServiceInitiator.SCOPE_SYSTEM_COMPONENTS
                                2 -> mScope = DfuServiceInitiator.SCOPE_APPLICATION
                            }
                        }).setPositiveButton(R.string.ok, { dialogInterface, i ->
                            val index: Int
                            if (mScope == null) {
                                index = 0
                            } else if (mScope == DfuServiceInitiator.SCOPE_SYSTEM_COMPONENTS) {
                                index = 1
                            } else {
                                index = 2
                            }
                            mFileScopeView!!.text = resources.getStringArray(R.array.dfu_file_scope)[index]
                        }).show()
            }
        }
    }
}