package com.microjet.airqi2.BlueTooth.DFU

/**
 * Created by B00055 on 2018/4/12.
 */
import android.app.ActivityManager
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.Context
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import com.microjet.airqi2.BleEvent
import com.microjet.airqi2.R
import no.nordicsemi.android.dfu.*
import org.greenrobot.eventbus.EventBus
import java.io.File

class DFUProcessClass() {
    private val mDfuProgressListener = object : DfuProgressListenerAdapter() {
        override fun onDeviceConnecting(deviceAddress: String?) {
            //    mProgressBar?.setTitle("Now status")
            mProgressBar?.setMessage("Device Connected")
            //    mProgressBar!!.isIndeterminate = true
            //    mTextPercentage!!.setText(R.string.dfu_status_connecting)
        }

        override fun onDfuProcessStarting(deviceAddress: String?) {
            mProgressBar!!.isIndeterminate = true
            //   mProgressBar?.setTitle("Now status")
            //   mProgressBar?.setMessage("")
            //    mTextPercentage!!.setText(R.string.dfu_status_starting)
        }

        override fun onEnablingDfuMode(deviceAddress: String?) {
            mProgressBar!!.isIndeterminate = true
            //mTextPercentage!!.setText(R.string.dfu_status_switching_to_dfu)
        }

        override fun onFirmwareValidating(deviceAddress: String?) {
            mProgressBar!!.isIndeterminate = true
            // mProgressBar?.setTitle("Now status")
            mProgressBar?.setMessage("Data validating")
            //mTextPercentage!!.setText(R.string.dfu_status_validating)
        }

        override fun onDeviceDisconnecting(deviceAddress: String?) {
            mProgressBar!!.isIndeterminate = true
            mProgressBar?.setMessage("Device disconnecting")
            //mTextPercentage!!.setText(R.string.dfu_status_disconnecting)
        }

        override fun onDfuCompleted(deviceAddress: String?) {
            //mTextPercentage!!.setText(R.string.dfu_status_completed)
            mProgressBar?.setMessage("Dfu Completed")
            if (mResumed) {
                // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
                Handler().postDelayed({
                    //    onTransferCompleted()//清除資訊用
                    //    showDownloadDialog("DFU Successful")
                    // if this activity is still open and upload process was completed, cancel the notification
                    val manager = mContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(DfuBaseService.NOTIFICATION_ID)
                }, 200)
            } else {
                // Save that the DFU process has finished
                mDfuCompleted = true
            }

            EventBus.getDefault().post(BleEvent("dfu complete"))//使用event 通知

            Log.d("AirAction", "Dfu Completed")
        }

        /*
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
        }*/
        override fun onDfuAborted(deviceAddress: String?) {
            //mTextPercentage!!.setText(R.string.dfu_status_aborted)
            // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
            Handler().postDelayed({
                //    onUploadCanceled()

                // if this activity is still open and upload process was completed, cancel the notification
                val manager = mContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(DfuBaseService.NOTIFICATION_ID)
            }, 200)
        }

        override fun onProgressChanged(deviceAddress: String?, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
            mProgressBar!!.isIndeterminate = false
            mProgressBar?.setMessage(mContext!!.getString(R.string.text_setting_FW_updating))
            mProgressBar!!.progress = percent
            //mTextPercentage!!.text = getString(R.string.dfu_uploading_percentage, percent)
            /*
            if (partsTotal > 1)
                mTextUploading!!.text = getString(R.string.dfu_status_uploading_part, currentPart, partsTotal)
            else
                mTextUploading!!.setText(R.string.dfu_status_uploading)
                */
        }

        override fun onError(deviceAddress: String?, error: Int, errorType: Int, message: String?) {
            if (mResumed) {
                //showErrorMessage(message)

                // We have to wait a bit before canceling notification. This is called before DfuService creates the last notification.
                Handler().postDelayed({
                    // if this activity is still open and upload process was completed, cancel the notification
                    val manager = mContext!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(DfuBaseService.NOTIFICATION_ID)
                }, 200)
            } else {
                mDfuError = message
                mProgressBar?.setTitle(message)
            }
        }
    }

    private val PREFS_DEVICE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_DEVICE_NAME"
    private val PREFS_FILE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_NAME"
    private val PREFS_FILE_TYPE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_TYPE"
    private val PREFS_FILE_SCOPE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_SCOPE"
    private val PREFS_FILE_SIZE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_SIZE"
    private val SETTINGS_KEEP_BOND = "settings_keep_bond"

    private var mContext: Context? = null
    /** Flag set to true in [.onRestart] and to false in [.onPause].  */
    private var mResumed: Boolean = false
    /** Flag set to true if DFU operation was completed while [.mResumed] was false.  */
    private var mDfuCompleted: Boolean = false
    /** The error message received from DFU service while [.mResumed] was false.  */
    private var mDfuError: String? = null

    private var mFileType: Int = 0

    private var mStatusOk: Boolean = false
    private var mScope: Int? = null
    var myDeviceName: String? = null
    var myDeviceAddress: String? = null
    private var mFilePath: String? = null
    private var mProgressBar: ProgressDialog? = null

    init {
    }

    constructor(input: Context) : this() {//第二建構元
        mContext = input
    }

    fun DFUAction(DeviceName: String, DeviceAddress: String) {
        DfuServiceListenerHelper.registerProgressListener(mContext, mDfuProgressListener)
        if (mContext != null) {
            mProgressBar = ProgressDialog(mContext)
            mProgressBar?.setMessage("DFUing")
            mProgressBar?.isIndeterminate = false//功能不知道
            mProgressBar?.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            mProgressBar?.setCancelable(true)//
            mProgressBar?.max = 100
            mProgressBar?.show()
        }
        mFileType = DfuBaseService.TYPE_AUTO
        myDeviceName = DeviceName
        myDeviceAddress = DeviceAddress

        val file = File(mContext!!.cacheDir, "FWupdate.zip")
        if (file.exists()) {
            mjupdateFileInfo(file.name, file.length())
            mFilePath = file.path
            if (isDfuServiceRunning()) {//確保dfu service只跑一個
                //   showUploadCancelDialog()
                return
            }
            if (!mStatusOk) {
                Toast.makeText(this.mContext!!, R.string.dfu_file_status_invalid_message, Toast.LENGTH_LONG).show()
                return
            }
            saveCurrentState()
        }
    }


    private fun saveCurrentState() {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this.mContext!!)
        val editor = preferences.edit()
        editor.putString(PREFS_DEVICE_NAME, myDeviceName)// mSelectedDevice!!.name)
        // editor.putString(PREFS_FILE_NAME, mFileNameView!!.text.toString())
        // editor.putString(PREFS_FILE_TYPE, mFileTypeView!!.text.toString())
        // editor.putString(PREFS_FILE_SCOPE, mFileScopeView!!.text.toString())
        editor.apply()
        val keepBond = preferences.getBoolean(SETTINGS_KEEP_BOND, false)
        val forceDfu = preferences.getBoolean(DfuSettingsConstants.SETTINGS_ASSUME_DFU_NODE, true)
        val enablePRNs = preferences.getBoolean(DfuSettingsConstants.SETTINGS_PACKET_RECEIPT_NOTIFICATION_ENABLED, Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        val value = preferences.getString(DfuSettingsConstants.SETTINGS_NUMBER_OF_PACKETS, DfuServiceInitiator.DEFAULT_PRN_VALUE.toString())
        var numberOfPackets: Int
        try {
            numberOfPackets = Integer.parseInt(value)
        } catch (e: NumberFormatException) {
            numberOfPackets = DfuServiceInitiator.DEFAULT_PRN_VALUE
        }
        val starter = DfuServiceInitiator(myDeviceAddress!!)//mSelectedDevice!!.address)
                .setDeviceName(myDeviceName)//mSelectedDevice!!.name)
                .setKeepBond(keepBond)
                .setForceDfu(forceDfu)
                .setPacketsReceiptNotificationsEnabled(enablePRNs)
                .setPacketsReceiptNotificationsValue(numberOfPackets)
                .setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true)
                .setForeground(false)
        if (mFileType == DfuBaseService.TYPE_AUTO) {
            starter.setZip(mFilePath!!)// starter.setZip(mFileStreamUri, mFilePath)
            if (mScope != null)
                starter.setScope(mScope!!)
        } else {
            //   starter.setBinOrHex(mFileType, mFileStreamUri, mFilePath).setInitFile(mInitFileStreamUri, mInitFilePath)
        }
        starter.start(this.mContext!!, DFUService::class.java)
    }

    private fun isDfuServiceRunning(): Boolean {
        val manager = mContext!!.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
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
    private fun mjupdateFileInfo(fileName: String, fileSize: Long, fileType: Int = 0) {

        val extension = if (mFileType == DfuBaseService.TYPE_AUTO) "(?i)ZIP" else "(?i)HEX|BIN" // (?i) =  case insensitive
        mStatusOk = MimeTypeMap.getFileExtensionFromUrl(fileName).matches(extension.toRegex())

    }
}