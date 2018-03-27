package com.microjet.airqi2.BlueTooth.DFU.Fragment

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.content.Intent
import android.support.v4.app.DialogFragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AlertDialog
import android.util.Log
import com.microjet.airqi2.BlueTooth.DFU.DfuService
import com.microjet.airqi2.R
import no.nordicsemi.android.dfu.DfuBaseService

/**
 * Created by B00055 on 2018/3/26.
 */
class UploadCancelFragment : DialogFragment() {

    private var mListener: CancelFragmentListener? = null

    interface CancelFragmentListener {
        fun onCancelUpload()
    }

    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)

        try {
            mListener = activity as CancelFragmentListener?
        } catch (e: ClassCastException) {
            Log.d(TAG, "The parent Activity must implement CancelFragmentListener interface")
        }

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(activity!!).setTitle(R.string.dfu_confirmation_dialog_title).setMessage(R.string.dfu_upload_dialog_cancel_message).setCancelable(false)
                .setPositiveButton(R.string.yes, { dialog, whichButton ->
                    val manager = LocalBroadcastManager.getInstance(activity!!)
                    val pauseAction = Intent(DfuBaseService.BROADCAST_ACTION)
                    pauseAction.putExtra(DfuBaseService.EXTRA_ACTION, DfuBaseService.ACTION_ABORT)
                    manager.sendBroadcast(pauseAction)

                    mListener!!.onCancelUpload()
                }).setNegativeButton(R.string.no, { dialog, which -> dialog.cancel() }).create()
    }

    override fun onCancel(dialog: DialogInterface?) {
        val manager = LocalBroadcastManager.getInstance(activity!!)
        val pauseAction = Intent(DfuBaseService.BROADCAST_ACTION)
        pauseAction.putExtra(DfuBaseService.EXTRA_ACTION, DfuBaseService.ACTION_RESUME)
        manager.sendBroadcast(pauseAction)
    }

    companion object {
        private val TAG = "UploadCancelFragment"

        val instance: UploadCancelFragment
            get() = UploadCancelFragment()
    }
}
