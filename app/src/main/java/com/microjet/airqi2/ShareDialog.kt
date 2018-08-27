package com.microjet.airqi2

import android.app.Dialog
import android.app.DialogFragment
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.ImageView
import java.io.File

/**
 * Created by ray650128 on 2018/8/20.
 *
 */
class ShareDialog : DialogFragment() {

    private var uriString = ""

    private var mContext: Context? = null

    companion object {
        val TAG = ShareDialog::class.java.simpleName
        val EXTRA_FILE_PATH = "URI"
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bundle = arguments
        if(bundle != null) {
            uriString = bundle.getString(EXTRA_FILE_PATH)
        }

        return inflater?.inflate(R.layout.layout_share, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(view != null) {
            view.findViewById<ImageView>(R.id.imgBtnClose).setOnClickListener {
                dismiss()
            }

            view.findViewById<Button>(R.id.btnScreenshotShare).setOnClickListener {
                val imageFile = File(uriString)

                try {
                    val fileProviderId = "${mContext!!.packageName}.fileprovider"
                    val photoURI = FileProvider.getUriForFile(mContext!!, fileProviderId, imageFile)
                    loge("Share path: ${photoURI.path}")

                    val intent = Intent(Intent.ACTION_SEND)

                    intent.data = photoURI
                    intent.type = "image/*"
                    intent.putExtra(Intent.EXTRA_STREAM, photoURI)  //圖片的實體路徑

                    val chooser = Intent.createChooser(intent, getString(R.string.text_share_screenshot_title))

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

            view.findViewById<Button>(R.id.btnApplicationShare).setOnClickListener {
                try {
                    val shareBody = getString(R.string.text_share_app_content)
                    val sharingIntent = Intent(android.content.Intent.ACTION_SEND)
                    sharingIntent.type = "text/plain"
                    sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody)
                    startActivity(Intent.createChooser(sharingIntent,getString(R.string.text_share_app_title)))
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                dismiss()
            }

            view.findViewById<Button>(R.id.btnFourGridReport).setOnClickListener {
                val uri = Uri.parse("https://api.mjairql.com/api/v1/fourGridReport")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                startActivity(intent)

                dismiss()
            }

            view.findViewById<Button>(R.id.btnScreenshotReport).setOnClickListener {
                val imageFile = File(uriString)

                try {
                    val photoURI = FileProvider.getUriForFile(mContext!!, "${mContext!!.packageName}.fileprovider", imageFile)
                    loge("Share path: ${photoURI.path}")

                    val mailURI = "service@addwii.com"
                    val intent = Intent(Intent.ACTION_SEND)

                    intent.data = photoURI
                    intent.type = "application/image"
                    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mailURI))  //圖片的實體路徑
                    intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.text_button_feedback_the_screenshot))  //圖片的實體路徑
                    intent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.text_feedback_content),
                            Build.BRAND, Build.MODEL,
                            Build.VERSION.RELEASE, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE))

                    intent.putExtra(Intent.EXTRA_STREAM, photoURI)  //圖片的實體路徑

                    val chooser = Intent.createChooser(intent, getString(R.string.text_send_email_title))

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

            view.findViewById<Button>(R.id.btnFacebookDiscussion).setOnClickListener {
                val fbApp = try {
                    activity.packageManager.getPackageInfo("com.facebook.katana", 0)
                } catch (e: Exception) {
                    null
                }

                loge("Facebook app: $fbApp")

                val facebookUri = Uri.parse(
                        if (fbApp != null) "fb://group/214211646107561"
                        else "https://www.facebook.com/groups/214211646107561/"
                )

                val intent = Intent(Intent.ACTION_VIEW, facebookUri)
                startActivity(intent)

                dismiss()
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    private fun loge(message: String) {
        Log.e(TAG, message)
    }
}