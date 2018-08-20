package com.microjet.airqi2

import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.*
import android.widget.Button
import java.io.File

/**
 * Created by ray650128 on 2018/8/20.
 *
 */
class ShareDialog : DialogFragment() {

    private var uriString = ""

    private var mContext: Context? = null

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bundle = arguments
        if(bundle != null) {
            uriString = bundle.getString("URI")
        }

        return inflater?.inflate(R.layout.layout_share, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(view != null) {
            view.findViewById<Button>(R.id.btnShareScreenshot).setOnClickListener {
                val imageFile = File(uriString)

                try {
                    val photoURI = FileProvider.getUriForFile(mContext!!, "${mContext!!.packageName}.fileprovider", imageFile)
                    Log.e("SHARE", photoURI.path)

                    val intent = Intent(Intent.ACTION_SEND)

                    intent.data = photoURI
                    intent.type = "image/*"
                    intent.putExtra(Intent.EXTRA_STREAM, photoURI)  //圖片的實體路徑

                    val chooser = Intent.createChooser(intent, "Share")

                    //給目錄臨時的權限
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    // Verify the intent will resolve to at least one activity
                    if (intent.resolveActivity(mContext!!.packageManager) != null) {
                        startActivityForResult(chooser, 0)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                dismiss()
            }

            view.findViewById<Button>(R.id.btnShareApplication).setOnClickListener {
                dismiss()
            }

            view.findViewById<Button>(R.id.btnReport).setOnClickListener {
                dismiss()
            }

            view.findViewById<Button>(R.id.btnScreenshotReport).setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }
}