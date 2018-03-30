package com.microjet.airqi2.CustomAPI

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.BaseColumns
import android.provider.MediaStore
import android.widget.Toast
import com.microjet.airqi2.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by B00055 on 2018/3/26.
 */
object FileHelper {
    private val TAG = "FileHelper"

    private val PREFS_SAMPLES_VERSION = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_SAMPLES_VERSION"
    private val CURRENT_SAMPLES_VERSION = 4

    val NORDIC_FOLDER = "Nordic Semiconductor"
    val UART_FOLDER = "UART Configurations"
    val BOARD_FOLDER = "Board"
    val BOARD_NRF6310_FOLDER = "nrf6310"
    val BOARD_PCA10028_FOLDER = "pca10028"
    val BOARD_PCA10036_FOLDER = "pca10036"

    fun newSamplesAvailable(context: Context): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val version = preferences.getInt(PREFS_SAMPLES_VERSION, 0)
        return version < CURRENT_SAMPLES_VERSION
    }

    fun createSamples(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val version = preferences.getInt(PREFS_SAMPLES_VERSION, 0)
        if (version == CURRENT_SAMPLES_VERSION)
            return

        /*
		 * Copy example HEX files to the external storage. Files will be copied if the DFU Applications folder is missing
		 */
        val root = File(Environment.getExternalStorageDirectory(), "Nordic Semiconductor")
        if (!root.exists()) {
            root.mkdir()
        }
        val board = File(root, "Board")
        if (!board.exists()) {
            board.mkdir()
        }
        val nrf6310 = File(board, "nrf6310")
        if (!nrf6310.exists()) {
            nrf6310.mkdir()
        }
        val pca10028 = File(board, "pca10028")
        if (!pca10028.exists()) {
            pca10028.mkdir()
        }

        // Remove old files. Those will be moved to a new folder structure
        File(root, "ble_app_hrs_s110_v6_0_0.hex").delete()
        File(root, "ble_app_rscs_s110_v6_0_0.hex").delete()
        File(root, "ble_app_hrs_s110_v7_0_0.hex").delete()
        File(root, "ble_app_rscs_s110_v7_0_0.hex").delete()
        File(root, "blinky_arm_s110_v7_0_0.hex").delete()
        File(root, "dfu_2_0.bat").delete() // This file has been migrated to 3.0
        File(root, "dfu_3_0.bat").delete() // This file has been migrated to 3.1
        File(root, "dfu_2_0.sh").delete() // This file has been migrated to 3.0
        File(root, "dfu_3_0.sh").delete() // This file has been migrated to 3.1
        File(root, "README.txt").delete() // This file has been modified to match v.3.0+

        var oldCopied = false
        var newCopied = false

        // nrf6310 files
        var f = File(nrf6310, "ble_app_hrs_s110_v6_0_0.hex")
        if (!f.exists()) {
            copyRawResource(context, R.raw.ble_app_hrs_s110_v6_0_0, f)
            oldCopied = true
        }
        f = File(nrf6310, "ble_app_rscs_s110_v6_0_0.hex")
        if (!f.exists()) {
            copyRawResource(context, R.raw.ble_app_rscs_s110_v6_0_0, f)
            oldCopied = true
        }
        f = File(nrf6310, "ble_app_hrs_s110_v7_0_0.hex")
        if (!f.exists()) {
            copyRawResource(context, R.raw.ble_app_hrs_s110_v7_0_0, f)
            oldCopied = true
        }
        f = File(nrf6310, "ble_app_rscs_s110_v7_0_0.hex")
        if (!f.exists()) {
            copyRawResource(context, R.raw.ble_app_rscs_s110_v7_0_0, f)
            oldCopied = true
        }
        f = File(nrf6310, "blinky_arm_s110_v7_0_0.hex")
        if (!f.exists()) {
            copyRawResource(context, R.raw.blinky_arm_s110_v7_0_0, f)
            oldCopied = true
        }
        // PCA10028 files
        f = File(pca10028, "blinky_s110_v7_1_0.hex")
        if (!f.exists()) {
            copyRawResource(context, R.raw.blinky_s110_v7_1_0, f)
            oldCopied = true
        }
        f = File(pca10028, "blinky_s110_v7_1_0_ext_init.dat")
        if (!f.exists()) {
            copyRawResource(context, R.raw.blinky_s110_v7_1_0_ext_init, f)
            oldCopied = true
        }
        f = File(pca10028, "ble_app_hrs_dfu_s110_v7_1_0.hex")
        if (!f.exists()) {
            copyRawResource(context, R.raw.ble_app_hrs_dfu_s110_v7_1_0, f)
            oldCopied = true
        }
        f = File(pca10028, "ble_app_hrs_dfu_s110_v7_1_0_ext_init.dat")
        if (!f.exists()) {
            copyRawResource(context, R.raw.ble_app_hrs_dfu_s110_v7_1_0_ext_init, f)
            oldCopied = true
        }
        File(root, "ble_app_hrs_dfu_s110_v8_0_0.zip").delete() // name changed
        f = File(pca10028, "ble_app_hrs_dfu_s110_v8_0_0_sdk_v8_0.zip")
        if (!f.exists()) {
            copyRawResource(context, R.raw.ble_app_hrs_dfu_s110_v8_0_0_sdk_v8_0, f)
            newCopied = true
        }
        f = File(pca10028, "ble_app_hrs_dfu_s110_v8_0_0_sdk_v9_0.zip")
        if (!f.exists()) {
            copyRawResource(context, R.raw.ble_app_hrs_dfu_s110_v8_0_0_sdk_v9_0, f)
            newCopied = true
        }
        f = File(pca10028, "ble_app_hrs_dfu_all_in_one_sdk_v9_0.zip")
        if (!f.exists()) {
            copyRawResource(context, R.raw.ble_app_hrs_dfu_all_in_one_sdk_v9_0, f)
            newCopied = true
        }

        if (oldCopied)
            Toast.makeText(context, R.string.dfu_example_files_created, Toast.LENGTH_SHORT).show()
        else if (newCopied)
            Toast.makeText(context, R.string.dfu_example_new_files_created, Toast.LENGTH_SHORT).show()

        // Scripts
        newCopied = false
        f = File(root, "dfu_3_1.bat")
        if (!f.exists()) {
            copyRawResource(context, R.raw.dfu_win_3_1, f)
            newCopied = true
        }
        f = File(root, "dfu_3_1.sh")
        if (!f.exists()) {
            copyRawResource(context, R.raw.dfu_mac_3_1, f)
            newCopied = true
        }
        f = File(root, "README.txt")
        if (!f.exists()) {
            copyRawResource(context, R.raw.readme, f)
        }
        if (newCopied)
            Toast.makeText(context, R.string.dfu_scripts_created, Toast.LENGTH_SHORT).show()

        // Save the current version
        preferences.edit().putInt(PREFS_SAMPLES_VERSION, CURRENT_SAMPLES_VERSION).apply()
    }

    /**
     * Copies the file from res/raw with given id to given destination file. If dest does not exist it will be created.
     *
     * @param context  activity context
     * @param rawResId the resource id
     * @param dest     destination file
     */
    private fun copyRawResource(context: Context, rawResId: Int, dest: File) {
        try {
            val readIs = context.resources.openRawResource(rawResId)
            val fos = FileOutputStream(dest)

            val buf = ByteArray(1024)
            var read: Int
            try {
                read = readIs.read(buf)
                while (read > 0) {
                    fos.write(buf, 0, read)
                    read = readIs.read(buf)
                }
            } finally {
                readIs.close()
                fos.close()
            }
        } catch (e: IOException) {
            //   DebugLogger.e(TAG, "Error while copying HEX file " + e.toString())
        }

    }

    fun getContentUri(context: Context, file: File): Uri? {
        val filePath = file.absolutePath
        val uri = MediaStore.Files.getContentUri("external")
        val cursor = context.contentResolver.query(
                uri,
                arrayOf(BaseColumns._ID),
                MediaStore.Files.FileColumns.DATA + "=? ",
                arrayOf(filePath), null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                return Uri.withAppendedPath(uri, id.toString())
            } else {
                if (file.exists()) {
                    val values = ContentValues()
                    values.put(MediaStore.Files.FileColumns.DATA, filePath)
                    return context.contentResolver.insert(uri, values)
                } else {
                    return null
                }
            }
        } finally {
            cursor!!.close()
        }
    }
}
