package com.microjet.airqi2.CustomAPI

import java.io.*
import java.util.*

class CSVWriter {

    private var writer: Writer? = null

    private var folderName: String? = null
    private var fileName: String? = null
    private var separator: String? = null


    constructor(folderName: String, fileName: String) {
        this.folderName = folderName
        this.fileName = fileName
        this.separator = SEMICOLON_SEPARATOR
    }

    constructor(folderName: String, fileName: String, separator: String) {
        this.folderName = folderName
        this.fileName = fileName
        this.separator = separator
    }


    @Throws(IOException::class)
    private fun createCsvFile() {
        val createPath = File(android.os.Environment.getExternalStorageDirectory().toString() + "/" + folderName)
        createPath.mkdir()
        val path = createPath.path

        try {
            val csvFile = File("$path/$fileName.csv")
            csvFile.createNewFile()

            writer = BufferedWriter(OutputStreamWriter(FileOutputStream(csvFile), "UTF-8"))

        } catch (e: IOException) {
            e.printStackTrace()
            throw e
        }

    }

    private fun createCsvFileIfDontExists(): Boolean {
        return if (writer == null) {
            try {
                createCsvFile()
                true
            } catch (e: IOException) {
                e.printStackTrace()
                false
            }

        } else true
    }

    fun writeLine(csvText: Array<String>): Boolean {
        if (!createCsvFileIfDontExists()) {
            return false
        }

        val textWithSeparator = StringBuilder()
        for (text in csvText) {
            textWithSeparator.append(text + separator!!)
        }

        try {
            writer!!.write(textWithSeparator.toString() + "\n")
            writer!!.flush()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }

        return true
    }

    fun close(): Boolean {

        return try {
            writer!!.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }

    }

    companion object {

        val SEMICOLON_SEPARATOR = ";"
        val COMMA_SEPARATOR = ","
    }


}