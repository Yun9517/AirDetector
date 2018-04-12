package com.microjet.airqi2.BlueTooth.DFU

/**
 * Created by B00055 on 2018/4/12.
 */
import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.os.Handler
import com.microjet.airqi2.R
import no.nordicsemi.android.dfu.DfuBaseService
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter
import no.nordicsemi.android.dfu.DfuServiceInitiator
import no.nordicsemi.android.dfu.DfuServiceListenerHelper

class DFUProcessClass (){
    private val mDfuProgressListener = object : DfuProgressListenerAdapter() {
        override fun onDeviceConnecting(deviceAddress: String?) {
        //    mProgressBar!!.isIndeterminate = true
        //    mTextPercentage!!.setText(R.string.dfu_status_connecting)
        }

        override fun onDfuProcessStarting(deviceAddress: String?) {
        //    mProgressBar!!.isIndeterminate = true
        //    mTextPercentage!!.setText(R.string.dfu_status_starting)
        }

        override fun onEnablingDfuMode(deviceAddress: String?) {
            //mProgressBar!!.isIndeterminate = true
            //mTextPercentage!!.setText(R.string.dfu_status_switching_to_dfu)
        }

        override fun onFirmwareValidating(deviceAddress: String?) {
            //mProgressBar!!.isIndeterminate = true
            //mTextPercentage!!.setText(R.string.dfu_status_validating)
        }

        override fun onDeviceDisconnecting(deviceAddress: String?) {
            //mProgressBar!!.isIndeterminate = true
            //mTextPercentage!!.setText(R.string.dfu_status_disconnecting)
        }

        override fun onDfuCompleted(deviceAddress: String?) {
            //mTextPercentage!!.setText(R.string.dfu_status_completed)
            if (mResumed) {
                // let's wait a bit until we cancel the notification. When canceled immediately it will be recreated by service again.
                Handler().postDelayed({
                    onTransferCompleted()
                //    showDownloadDialog("DFU Successful")
                    // if this activity is still open and upload process was completed, cancel the notification
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.cancel(DfuBaseService.NOTIFICATION_ID)
                }, 200)
            } else {
                // Save that the DFU process has finished
                mDfuCompleted = true
            }
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
                onUploadCanceled()

                // if this activity is still open and upload process was completed, cancel the notification
                val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.cancel(DfuBaseService.NOTIFICATION_ID)
            }, 200)
        }

        override fun onProgressChanged(deviceAddress: String?, percent: Int, speed: Float, avgSpeed: Float, currentPart: Int, partsTotal: Int) {
            //mProgressBar!!.isIndeterminate = false
            //mProgressBar!!.progress = percent
            //mTextPercentage!!.text = getString(R.string.dfu_uploading_percentage, percent)
            if (partsTotal > 1)
                mTextUploading!!.text = getString(R.string.dfu_status_uploading_part, currentPart, partsTotal)
            else
                mTextUploading!!.setText(R.string.dfu_status_uploading)
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
            }
        }
    }

    private var mContext: Context? = null
    /** Flag set to true in [.onRestart] and to false in [.onPause].  */
    private var mResumed: Boolean = false
    /** Flag set to true if DFU operation was completed while [.mResumed] was false.  */
    private var mDfuCompleted: Boolean = false
    /** The error message received from DFU service while [.mResumed] was false.  */
    private var mDfuError: String? = null

    private var mFileType: Int = 0

    var myDeviceName:String?=null
    var myDeviceAddress:String?=null
    init {}
    constructor(input: Context):this(){//第二建構元
        mContext=input
        //    callback=callbackInput
    }

    fun DFUAction(DeviceName:String,DeviceAddress:String){
        mFileType=DfuBaseService.TYPE_AUTO
        myDeviceName=DeviceName
        myDeviceAddress=DeviceAddress


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

}