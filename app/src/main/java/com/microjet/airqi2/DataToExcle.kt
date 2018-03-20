package com.microjet.airqi2

import jxl.Workbook
import jxl.format.Colour
import jxl.write.*
import jxl.write.biff.RowsExceededException
import java.io.File
import java.io.IOException

/**
 * Created by B00170 on 2018/3/20.
 */

class DataToExcle {
    internal lateinit var path: String
    internal lateinit var fileName: String
    internal var labelNames: Array<String>? = null
    internal var mWritableWorkbook: WritableWorkbook? = null
    internal var mWorkbook: Workbook? = null

    internal lateinit var EXCEL_PATH: String

    /** 開啟一個可寫入的excel檔案，注意最後必須調用closeExcelWritableWorkbook以完成寫入。  */
    fun createExcelWritableWorkbook(path: String, fileName: String
    ): Boolean {
        this.path = path
        this.fileName = fileName + ".xls"

        EXCEL_PATH = this.path + "/" + this.fileName
        try {
            // 打开文件
            val dir = File(path)
            if (!dir.exists())
            // true if this file exists, false otherwise.
            {
                dir.mkdir()// true if the directory has been created, false
                // otherwise
            }// 如果資料夾不存在則創立一個資料夾..

            mWritableWorkbook = Workbook.createWorkbook(File(EXCEL_PATH))
            return true
        } catch (e: Exception) {
            println(e)
            return false
        }

    }


    //    public boolean getExcelBook() {
    //
    //        if(mWritableWorkbook!=null){
    //            try {
    //                mWritableWorkbook.close();
    //            } catch (WriteException e) {
    //                e.printStackTrace();
    //                return false;
    //            } catch (IOException e) {
    //                e.printStackTrace();
    //                return false;
    //            }
    //        }
    //
    //        try {
    //            mWorkbook = Workbook.getWorkbook(new File(EXCEL_PATH));
    //            return true;
    //        } catch (BiffException e) {
    //            e.printStackTrace();
    //        } catch (IOException e) {
    //            e.printStackTrace();
    //        }
    //        return false;
    //    }

    /** 關閉excel檔案，很重要!最後一定要執行  */
    fun close(): Boolean {
        if (mWritableWorkbook != null) {
            try {
                wrriteExcelWritableWorkbook()
                mWritableWorkbook!!.close()
                return true
            } catch (e: WriteException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        if (mWorkbook != null) {
            mWorkbook!!.close()
        }
        return false
    }

    fun getOrCreateExcelSheet(sheetName: String): WritableSheet? {
        // 產生excel中的工作表
        if (mWritableWorkbook != null) {
            var sheet: WritableSheet? = mWritableWorkbook!!.getSheet(sheetName)
            if (sheet == null) {
                sheet = mWritableWorkbook!!.createSheet(sheetName, 0)
            }
            return sheet
        }
        return null
    }

    /**放置文字至指定工作表中。 */
    fun putData(sheet: WritableSheet, c: Int, r: Int,
                lableString: String, mWritableCellFormat: WritableCellFormat?): Boolean {
        val label: Label
        if (mWritableCellFormat != null) {
            label = Label(c, r, lableString, mWritableCellFormat)
        } else {
            label = Label(c, r, lableString)
        }
        try {
            sheet.addCell(label)

            return true
        } catch (e: RowsExceededException) {
            e.printStackTrace()

        } catch (e: WriteException) {
            e.printStackTrace()
        }

        return false
    }


    /**放置數字至指定工作表中。 */
    fun putData(sheet: WritableSheet, c: Int, r: Int,
                numberDouble: Double, mWritableCellFormat: WritableCellFormat?): Boolean {
        val number: jxl.write.Number
        if (mWritableCellFormat != null) {
            number = jxl.write.Number(c, r, numberDouble,
                    mWritableCellFormat)
        } else {
            number = jxl.write.Number(c, r, numberDouble)
        }
        try {
            sheet.addCell(number)

            return true
        } catch (e: RowsExceededException) {
            e.printStackTrace()

        } catch (e: WriteException) {
            e.printStackTrace()

        }

        return false
    }

    private fun wrriteExcelWritableWorkbook(): Boolean {
        try {
            mWritableWorkbook!!.write()
            return true
        } catch (e: IOException) {
            // TODO Auto-generated catch block
            e.printStackTrace()
        }

        return false
    }

    companion object {

        internal var mWritableCellFormat: WritableCellFormat? = null

        /**
         * 設定表格的樣式。參考:http://jexcelapi.sourceforge.net/resources/javadocs/current/
         * docs/
         */
        fun putWritableCellFormat(
                writableFont: WritableFont.FontName, paintSize: Int, mColour: Colour): WritableCellFormat {
            if (mWritableCellFormat == null) {
                mWritableCellFormat = WritableCellFormat()
            }
            val chFont11w = WritableFont(writableFont, paintSize)
            /*
         * WritableCellFormat cellFormat1 = new WritableCellFormat ();
         * cellFormat1.setFont(chFont11w);
         * cellFormat1.setBackground(Colour.DARK_GREEN);
         * cellFormat1.setAlignment(Alignment.CENTRE);
         * cellFormat1.setBorder(Border.ALL, BorderLineStyle.THIN,
         * Colour.GRAY_80);
         */

            try {
                chFont11w.colour = mColour
            } catch (e: WriteException) {
                e.printStackTrace()
            }

            mWritableCellFormat!!.setFont(chFont11w)
            return mWritableCellFormat as WritableCellFormat
        }
    }
}

