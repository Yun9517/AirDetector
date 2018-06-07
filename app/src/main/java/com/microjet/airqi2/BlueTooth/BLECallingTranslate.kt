package com.microjet.airqi2.BlueTooth

import android.util.Log
import com.microjet.airqi2.TvocNoseData
import java.util.ArrayList
import kotlin.experimental.and

/**
 * Created by B00055 on 2017/11/29.
 *
 */
object BLECallingTranslate {

    private fun getCheckSum(CMD: ByteArray): Byte {
        val j = CMD.size
        val checkSum: Byte
        var temp = 0
        val max = 0xFF.toByte()
        for (i in 0 until j)
            temp += CMD[i]
        checkSum = (max - temp).toByte()
        return checkSum
    }

    fun getDeviceID(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.DeviceID)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.DeviceID, checkSum)
    }

    fun setDeviceID(inputID: Byte): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.WriteCmd, BLECommand.SetIDLens, BLECommand.DeviceID, inputID)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.WriteCmd, BLECommand.SetIDLens, BLECommand.DeviceID, inputID, checkSum)
    }

    fun SelfTestCall(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.SelfTest)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.SelfTest, checkSum)
    }

    fun TemperatureCall(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.Temperature)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.Temperature, checkSum)
    }

    fun HumidityCall(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.Humidity)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.Humidity, checkSum)
    }

    fun TVOCCall(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.TVOC)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.TVOC, checkSum)
    }

    fun CO2Call(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.CO2)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.CO2, checkSum)
    }

    fun Temperature_RAWCall(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.Temperature_RAW)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.Temperature_RAW, checkSum)
    }

    fun Humidity_RAWCall(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.Humidity_RAW)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.Humidity_RAW, checkSum)
    }

    fun TVOC_RAWCall(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.TVOC_RAW)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.TVOC_RAW, checkSum)
    }

    fun GetTVOC_BaselineCall(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.TVOC_Baseline)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.TVOC_Baseline, checkSum)
    }

    fun WriteTVOC_BaselineCall(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.WriteCmd, BLECommand.SetTVOCLens, 0x00.toByte(), 0x81.toByte(), 0xca.toByte(), 0x19.toByte(), 0x6d.toByte(), 0xdb.toByte())
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.WriteCmd, BLECommand.SetTVOCLens, 0x00.toByte(), 0x81.toByte(), 0xca.toByte(), 0x19.toByte(), 0x6d.toByte(), 0xdb.toByte(), checkSum)
    }

    fun GetADCData(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.GetADCDAtaLens, BLECommand.GetADCData)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.GetADCDAtaLens, BLECommand.GetADCData, checkSum)
    }

    fun ResetPumpFreq(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.ResetPumpFreq)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.ResetPumpFreq, checkSum)
    }

    fun GetPumpFreq(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetPumpFreq)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetPumpFreq, checkSum)
    }

    fun ResetDeviceFlash(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.ResetFlash)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.ResetFlash, checkSum)
    }

    fun PreHeater(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.PreHeater)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.PreHeater, checkSum)
    }

    fun GetBatteryLife(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetBatteryLife)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetBatteryLife, checkSum)
    }

    fun GetAllFromDevice(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetAllFromDevice)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetAllFromDevice, checkSum)
    }

    fun SetFanPower(inPut: Int): ByteArray {
        var input = inPut
        val b = ByteArray(4)
        if (input > 65535)
            input = 65535
        b[0] = (input and -0x1000000).ushr(24).toByte()//big
        b[1] = (input and 0x00ff0000).ushr(16).toByte()
        b[2] = (input and 0x0000ff00).ushr(8).toByte()
        b[3] = (input and 0x000000ff).toByte()//little
        val valueHandler = byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteOneByteLens, BLECommand.FanPower, b[3])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteTwoBytesLens, BLECommand.FanPower, b[3], checkSum)
    }

    /**
     * Return the command include CheckSum
     *
     *
     *
     * input [0] = sample rate
     * input [1] = sensor-on time range Sensor開啟時間長度
     * input [2] = time to get sample 第幾秒取資料
     * input [3] = pump on time pump開啟時間點
     * input [4] = pumping time pump開啟時間長度
     *
     * input [5] = 每間隔多久取資料
     * input [6] = 期間內取幾次資料
     *
     * @param input for item of data
     * @return the command include CheckSum
     */
    fun SetSampleRate(input: IntArray): ByteArray {
        if (input.size != 7) {
            Log.e("SetSampleRate", "parameter length error")
        }
        val a = ByteArray(4)
        val b = ByteArray(11)
        for (i in input.indices) {
            a[0] = (input[i] and -0x1000000).ushr(24).toByte()//big
            a[1] = (input[i] and 0x00ff0000).ushr(16).toByte()
            a[2] = (input[i] and 0x0000ff00).ushr(8).toByte()
            a[3] = (input[i] and 0x000000ff).toByte()//little
            when (i) {
                0 -> b[0] = a[3]
                1 -> {
                    b[1] = a[2]
                    b[2] = a[3]
                }
                2 -> {
                    b[3] = a[2]
                    b[4] = a[3]
                }
                3 -> {
                    b[5] = a[2]
                    b[6] = a[3]
                }
                4 -> {
                    b[7] = a[2]
                    b[8] = a[3]
                }
                5 -> b[9] = a[3]
                6 -> b[10] = a[3]
            }
        }
        val valueHandler = byteArrayOf(BLECommand.WriteCmd, BLECommand.writeTwelveBytesLens, BLECommand.SetOrGetSampleRate, b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7], b[8], b[9], b[10])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.WriteCmd, BLECommand.writeTwelveBytesLens, BLECommand.SetOrGetSampleRate, b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7], b[8], b[9], b[10], checkSum)
    }

    fun GetSampleRate(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.SetOrGetSampleRate)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.SetOrGetSampleRate, checkSum)
    }

    // Device led control
    fun getLedStateCMD(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.SetLedOnOff)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.SetLedOnOff, checkSum)
    }

    // Device led control
    fun SetLedOn(value1: Boolean, value2: Boolean): ByteArray {
        val setVal1 = if (value1) {
            BLECommand.LedOn
        } else {
            BLECommand.LedOff
        }

        val setVal2 = if (value2) {
            BLECommand.LedOn
        } else {
            BLECommand.LedOff
        }
        val valueHandler = byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteTwoBytesLens, BLECommand.SetLedOnOff, setVal1, setVal2)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteTwoBytesLens, BLECommand.SetLedOnOff, setVal1, setVal2, checkSum)
    }

    /**
     * Return the command include CheckSum
     * REQUEST_DEVICE_STARTING_GETSAMPLE
     *
     *
     * input [0] = 年
     * input [1] = 月
     * input [2] = 日
     * input [3] = 時
     * input [4] = 分
     * input [5] = 秒
     *
     * @param input for item of data
     * @return the command include CheckSum
     */
    fun CallDeviceStartRecord(input: IntArray): ByteArray {
        if (input.size != 6) {
            Log.e("SetSampleRate", "parameter length error")
        }
        val a = ByteArray(4)
        val b = ByteArray(6)
        for (i in input.indices) {
            a[0] = (input[i] and -0x1000000).ushr(24).toByte()//big
            a[1] = (input[i] and 0x00ff0000).ushr(16).toByte()
            a[2] = (input[i] and 0x0000ff00).ushr(8).toByte()
            a[3] = (input[i] and 0x000000ff).toByte()//little
            when (i) {
                0 -> b[0] = a[3]
                1 -> b[1] = a[3]
                2 -> b[2] = a[3]
                3 -> b[3] = a[3]
                4 -> b[4] = a[3]
                5 -> b[5] = a[3]
            }
        }
        val valueHandler = byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteSixBytesLens, BLECommand.CallDeviceStartingGetSample, b[0], b[1], b[2], b[3], b[4], b[5])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteSixBytesLens, BLECommand.CallDeviceStartingGetSample, b[0], b[1], b[2], b[3], b[4], b[5], checkSum)
    }

    /**
     * Return the command include CheckSum
     *
     *
     *
     *
     *
     * @return the command include CheckSum
     */
    fun GetHistorySampleItems(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetHistorySampleItems)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetHistorySampleItems, checkSum)
    }

    /**
     * Return the command include CheckSum
     *
     *
     *
     *
     *
     * @param input for item of data
     * @return the command include CheckSum
     */
    fun GetHistorySample(inPut: Int): ByteArray {
        var input = inPut
        val b = ByteArray(4)
        if (input > 65535)
            input = 65535
        b[0] = (input and -0x1000000).ushr(24).toByte()//big
        b[1] = (input and 0x00ff0000).ushr(16).toByte()
        b[2] = (input and 0x0000ff00).ushr(8).toByte()
        b[3] = (input and 0x000000ff).toByte()//little
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.WriteTwoBytesLens, BLECommand.GetHistorySample, b[2], b[3])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.WriteTwoBytesLens, BLECommand.GetHistorySample, b[2], b[3], checkSum)
    }

    /**
     * Return the getInfo command include CheckSum
     *
     *
     *
     *
     * @return the getInfo command include CheckSum
     */
    fun getInfo(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetInfo)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetInfo, checkSum)
    }

    fun SensorOn_OffCall(inPut: Int): ByteArray {
        var input = inPut
        Log.e("SensorOn_OffCall", "Now input:" + Integer.toString(input))
        val b = ByteArray(4)
        if (input > 65535)
            input = 65535
        b[0] = (input and -0x1000000).ushr(24).toByte()//big
        b[1] = (input and 0x00ff0000).ushr(16).toByte()
        b[2] = (input and 0x0000ff00).ushr(8).toByte()
        b[3] = (input and 0x000000ff).toByte()//little
        val valueHandler = byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteTwoBytesLens, BLECommand.SensorOn_OFF, b[2], b[3])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteTwoBytesLens, BLECommand.SensorOn_OFF, b[2], b[3], checkSum)
    }

    fun PumpOnCall(inPut: Int): ByteArray {
        var input = inPut
        val b = ByteArray(4)
        if (input > 65535)
            input = 65535
        b[0] = (input and -0x1000000).ushr(24).toByte()//big
        b[1] = (input and 0x00ff0000).ushr(16).toByte()
        b[2] = (input and 0x0000ff00).ushr(8).toByte()
        b[3] = (input and 0x000000ff).toByte()//little
        val valueHandler = byteArrayOf(BLECommand.WriteCmd, BLECommand.PumpOnLens, BLECommand.PumpOn, b[2], b[3])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.WriteCmd, BLECommand.PumpOnLens, BLECommand.PumpOn, b[2], b[3], checkSum)
    }

    // 2018/05/08
    fun PM25FanCall(inPut: Int): ByteArray {
        var input = inPut
        val b = ByteArray(5)
        if (input > 65535)
            input = 65535
        b[0] = (input and -0x1000000).ushr(24).toByte()//big
        b[1] = (input and 0x00ff0000).ushr(16).toByte()
        b[2] = (input and 0x0000ff00).ushr(8).toByte()
        b[3] = (input and 0x000000ff).toByte()//little
        val valueHandler = byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteThreeBytesLens, BLECommand.PM25_On, b[2], b[3], b[4])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteThreeBytesLens, BLECommand.PM25_On, b[2], b[3], b[4], checkSum)

        /*var input1 = inPut.toByte()
        var input2 = 0.toByte()
        var input3 = 30.toByte()
        val valueHandler = byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteThreeBytesLens, BLECommand.PM25_On, input1, input2, input3)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteThreeBytesLens, BLECommand.PM25_On, input1, input2, input3, checkSum)*/
    }


    fun Parser(bytes: ByteArray): ArrayList<String> {
        // String[] ReturnValue=new String[];
        val ReturnValue = ArrayList<String>()
        var CountTemp = 0
        var value = 0
        var temp = 0x00;
        var i = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i = i + 1//point to DataLength
                CountTemp = (bytes[i] and 0xFF.toByte()).toInt()//取得DataLength的Int數值
                for (j in 0 until CountTemp - 1) {
                    i = i + 1//Point to DataValue
                    value = value shl 8
                    value = value + (bytes[i] and 0xFF.toByte())
                }
                i = i + 1//Point to Cmd's CheckSum;
                ReturnValue.add(Integer.toString(value))
            } else {//non_Stop

            }
            i++
        }

        return ReturnValue
    }

    fun ParserAll(bytes: ByteArray): ArrayList<String> {
        // String[] ReturnValue=new String[];
        val ReturnValue = ArrayList<String>()
        var CountTemp = 0
        var value = 0
        var i = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i = i + 1//point to DataLength
                CountTemp = (bytes[i] and 0xFF.toByte()).toInt()//取得DataLength的Int數值
                for (j in 0 until CountTemp - 1) {
                    i = i + 1//Point to DataValue
                    value = value shl 8
                    value = value + (bytes[i] and 0xFF.toByte())
                    if (j == 1) {//Temperature
                        var Tempvalue = -45 + 175.0f * value / 65535
                        val newTemp = "%.1f".format(Tempvalue)
                        //value -= 6
                        ReturnValue.add(newTemp.toString())
                        value = 0
                    } else if (j >= 3 && j % 2 == 1) {
                        ReturnValue.add(Integer.toString(value))
                        value = 0
                    }
                    //    i=i+1;//Point to DataValue
                    //    value=value<<8;
                    //    value=value+(bytes[i]&0xFF);

                }
                i = i + 1//Point to Cmd's CheckSum;
                // ReturnValue.add(Integer.toString(value));
            } else {//non_Stop

            }
            i++
        }

        return ReturnValue
    }

    //0xB5
    fun ParserGetHistorySampleItem(bytes: ByteArray): ArrayList<String> {
        val ReturnValue = ArrayList<String>()
        var CountTemp = 0
        var value = 0
        var i = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i = i + 1//point to DataLength
                CountTemp = (bytes[i] and 0xFF.toByte()).toInt()//取得DataLength的Int數值
                i = i + 1//point to CMD;
                for (j in 0 until CountTemp - 2) {
                    i = i + 1//Point to DataValue
                    //    stringHex+=Integer.toString( ( bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
                    value = value shl 8
                    value = value + bytes[i].toPositiveInt()
                    when (j) {
                        1//Item Index
                        -> {
                            //  ReturnValue.add(stringHex);
                            //   stringHex="";
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        3//Temperature
                        -> {
                            var Tempvalue = -45 + 175.0f * value / 65535
                            val newTemp = "%.1f".format(Tempvalue)
                            //value -= 6
                            ReturnValue.add(newTemp.toString())
                            value = 0
                        }
                        4//Humi
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        6//TVOC
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        8//CO2
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        10//PM25
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        12//RecectDataCheck
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i = i + 1//Point to Cmd's CheckSum;
                // ReturnValue.add(Integer.toString(value));
            }
            i++

        }
        return ReturnValue
    }

    fun ParserGetHistorySampleItems(bytes: ByteArray): ArrayList<String> {
        val ReturnValue = ArrayList<String>()
        var CountTemp = 0
        val stringHex = ""
        var value = 0
        var i = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i = i + 1//point to DataLength
                CountTemp = (bytes[i] and 0xFF.toByte()).toInt()//取得DataLength的Int數值
                i = i + 1//point to CMD;
                for (j in 0 until CountTemp - 2) {
                    i = i + 1//Point to DataValue
                    value = value shl 8
                    value = value + bytes[i].toPositiveInt()
                    //    stringHex+=Integer.toString( ( bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
                    when (j) {
                        1//Max Items
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        2//sample status
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        3//correct time
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        5//最後週期離現在多久 last data sec
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        6//samepleRate
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i = i + 1//Point to Cmd's CheckSum;
                // ReturnValue.add(Integer.toString(value));
            }
            i++

        }
        return ReturnValue
    }

    fun ParserGetSampleRate(bytes: ByteArray): ArrayList<String> {
        val ReturnValue = ArrayList<String>()
        var CountTemp = 0
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i = i + 1//point to DataLength
                CountTemp = (bytes[i] and 0xFF.toByte()).toInt()//取得DataLength的Int數值
                i = i + 1//point to CMD;
                for (j in 0 until CountTemp - 2) {
                    i = i + 1//Point to DataValue
                    value = value shl 8
                    value = value + bytes[i].toPositiveInt()
                    // stringHex += Integer.toString((bytes[i] and 0xff.toByte()) + 0x100, 16).substring(1)
                    when (j) {
                        0//sample rate
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        2//sensor on time
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        4//time to get sample
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        6//pump on time
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        8//pumping time
                        -> {
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                    /*暫時用不到
                    9//get data in cycle 間隔多久取資料
                    -> {
                        ReturnValue.add(Integer.toString(value))
                        value = 0
                    }
                    10//期間內取幾次資料
                    -> {
                        ReturnValue.add(Integer.toString(value))
                        value = 0
                    }*/
                        else -> {
                        }
                    }
                }
                i = i + 1//Point to Cmd's CheckSum;
                // ReturnValue.add(Integer.toString(value));
            }
            i++

        }
        return ReturnValue
    }

    fun ParserGetInfo(bytes: ByteArray): ArrayList<String> {
        // String[] ReturnValue=new String[];
        val ReturnValue = ArrayList<String>()
        var CountTemp = 0
        var stringHex = ""
        var i = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i = i + 1//point to DataLength
                CountTemp = (bytes[i] and 0xFF.toByte()).toInt()//取得DataLength的Int數值
                i = i + 1//point to CMD;
                for (j in 0 until CountTemp - 2) {
                    i = i + 1//Point to DataValue
                    stringHex += Integer.toString((bytes[i] and 0xff.toByte()) + 0x100, 16).substring(1)
                    when (j) {
                        5//MAC Address
                        -> {
                            ReturnValue.add(stringHex)
                            stringHex = ""
                        }
                        6//Device
                        -> {
                            ReturnValue.add(stringHex)
                            stringHex = ""
                        }
                        7//VOC sensor
                        -> {
                            ReturnValue.add(stringHex)
                            stringHex = ""
                        }
                        10//FW Version
                        -> {
                            ReturnValue.add(stringHex)
                            stringHex = ""
                        }
                        else -> {
                        }
                    }
                }
                i = i + 1//Point to Cmd's CheckSum;
                // ReturnValue.add(Integer.toString(value));
            }
            i++

        }

        return ReturnValue
    }

    //0xB6
    fun ParserGetAutoSendData(bytes: ByteArray): ArrayList<String> {
        val ReturnValue = ArrayList<String>()
        var CountTemp = 0
        var stringHex = ""
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i = i + 1//point to DataLength
                CountTemp = (bytes[i] and 0xFF.toByte()).toInt()//取得DataLength的Int數值
                i = i + 1//point to CMD;
                for (j in 0 until CountTemp - 2) {
                    i = i + 1//Point to DataValue
                    //    stringHex+=Integer.toString( ( bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
                    value = value shl 8
                    value = value + bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                        1 -> {//Temperature
                            var Tempvalue = -45 + 175.0f * value / 65535
                            val newTemp = "%.1f".format(Tempvalue)
                            //value -= 6
                            ReturnValue.add(newTemp.toString())
                            value = 0
                            stringHex = ""
                        }
                        2 -> {//Humidity
                            ReturnValue.add(bytes[i].toPositiveInt().toString())
                            value = 0
                        }
                        4 -> {//TVOC
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        6 -> {//CO2
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        8 -> {//PM25
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        9 -> {//battery life
                            ReturnValue.add(bytes[i].toPositiveInt().toString())
                            value = 0
                        }
                        10 -> {//flag 0 for old 1 for new
                            ReturnValue.add(bytes[i].toPositiveInt().toString())
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i = i + 1//Point to Cmd's CheckSum;
            }
            i++
        }
        Log.d("PARSERB6", ReturnValue.toString())
        return ReturnValue
    }

    fun GetAllSensor(bytes: ByteArray): ArrayList<String> {
        val ReturnValue = ArrayList<String>()
        var CountTemp = 0
        var stringHex = ""
        var i = 0
        var value = 0

        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i = i + 1//point to DataLength
                CountTemp = (bytes[i] and 0xFF.toByte()).toInt()//取得DataLength的Int數值
                i = i + 1//point to CMD;
                for (j in 0 until CountTemp - 2) {
                    i = i + 1//Point to DataValue
                    //    stringHex+=Integer.toString( ( bytes[i] & 0xff ) + 0x100, 16).substring( 1 );
                    value = value shl 8
                    value = value + bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                        1 -> {//Temperature
                            var Tempvalue = -45 + 175.0f * value / 65535
                            val newTemp = "%.1f".format(Tempvalue)
                            //value -= 6
                            ReturnValue.add(newTemp.toString())
                            value = 0
                            stringHex = ""
                        }
                        2 -> {//Humidity
                            ReturnValue.add(bytes[i].toPositiveInt().toString())
                            value = 0
                        }
                        4 -> {//TVOC
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        6 -> {//CO2
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        8 -> {//PM25
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        9 -> {//battery life
                            ReturnValue.add(bytes[i].toPositiveInt().toString())
                            value = 0
                        }
                        10 -> {//Preheater

                            ReturnValue.add(bytes[i].toPositiveInt().toString())
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i = i + 1//Point to Cmd's CheckSum;
            }
            i++
        }
        Log.d("PARSERB0", ReturnValue.toString())
        return ReturnValue
    }

    fun getPM25Rate(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetSetPM25)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetSetPM25, checkSum)
    }

    fun setPM25Rate(inPut: Int): ByteArray {
        var input1 = inPut.toByte()
        var input2 = 0.toByte()
        var input3 = 30.toByte()
        val valueHandler = byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteThreeBytesLens, BLECommand.GetSetPM25, input1, input2, input3)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteThreeBytesLens, BLECommand.GetSetPM25, input1, input2, input3, checkSum)
    }

    fun getAllSensorKeyValue(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                //Log.d("B0RawDataLength",dataLength.toString())
                i++//point to CMD;
                for (j in 0 until dataLength - 2) { // -2因為Data長度13要忽略StopCmd和ByteLength
                    i++//Point to DataValue
                    value = value.shl(8)
                    value += bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                        1 -> {//Temperature
                            var Tempvalue = -45 + 175.0f * value / 65535
                            val newTemp = "%.1f".format(Tempvalue)
                            returnValue.put(TvocNoseData.B0TEMP, newTemp)
                            value = 0
                        }
                        2 -> {//Humidity
                            returnValue.put(TvocNoseData.B0HUMI, value.toString())
                            value = 0
                        }
                        4 -> {//TVOC
                            returnValue.put(TvocNoseData.B0TVOC, value.toString())
                            value = 0
                        }
                        6 -> {//CO2
                            returnValue.put(TvocNoseData.B0ECO2, value.toString())
                            value = 0
                        }
                        8 -> {//PM25
                            returnValue.put(TvocNoseData.B0PM25, value.toString())
                            value = 0
                        }
                        9 -> {//battery life
                            returnValue.put(TvocNoseData.B0BATT, value.toString())
                            value = 0
                        }
                        10 -> {//Preheater
                            returnValue.put(TvocNoseData.B0PREH, value.toString())
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i++//Point to Cmd's CheckSum;
            }
            i++
        }
        return returnValue
    }

    fun parserGetInfoKeyValue(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var stringHex = ""
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                //var rawData = ""
                i++//point to CMD;
                for (j in 0 until dataLength - 2) {
                    i++//Point to DataValue
                    //stringHex += Integer.toString((bytes[i] and 0xff.toByte()) + 0x100, 16).substring(1)
                    stringHex += String.format("%02X", bytes[i])
                    //rawData += String.format("%02X ", bytes[i])
                    when (j) {
                        0//is PM25
                        -> {
                            Log.e("ParceDeviceInfo", "String Index: $stringHex")
                            returnValue[TvocNoseData.ISPM25] = stringHex
                            stringHex = ""
                        }
                        4//Reserved
                        -> {
                            Log.e("ParceDeviceInfo", "String Index: $stringHex")
                            returnValue[TvocNoseData.MAC] = stringHex
                            stringHex = ""
                        }
                        6//Device
                        -> {
                            Log.e("ParceDeviceInfo", "String Index: $stringHex")
                            returnValue[TvocNoseData.DEVICE] = stringHex
                            stringHex = ""
                        }
                        7//VOC sensor
                        -> {
                            Log.e("ParceDeviceInfo", "String Index: $stringHex")
                            returnValue[TvocNoseData.TVOCSENOR] = stringHex
                            stringHex = ""
                        }
                        10//FW Version
                        -> {
                            Log.e("ParceDeviceInfo", "String Index: $stringHex")
                            returnValue[TvocNoseData.FW] = stringHex
                            stringHex = ""
                        }
                        12//FW Serial
                        ->{
                            Log.e("ParceDeviceInfo", "String Index: $stringHex")
                            returnValue[TvocNoseData.FWSerial] = stringHex
                            stringHex = ""
                        }
                        else -> {
                        }
                    }
                }
                //Log.e("ParceDeviceInfo", "Raw Data: $rawData")
                i++//Point to Cmd's CheckSum;
            }
            i++

        }
        return returnValue
    }

    /*
    fun parserGetSampleRateKeyValue(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                i++//point to CMD;
                for (j in 0 until dataLength - 2) {
                    i++//Point to DataValue
                    value = value shl 8
                    value += bytes[i].toPositiveInt()
                    when (j) {
                        0//sample rate
                        -> {
                            returnValue.put()
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        2//sensor on time
                        -> {
                            returnValue
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        4//time to get sample
                        -> {
                            returnValue
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        6//pump on time
                        -> {
                            returnValue
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                        8//pumping time
                        -> {
                            returnValue
                            ReturnValue.add(Integer.toString(value))
                            value = 0
                        }
                    /*暫時用不到
                    9//get data in cycle 間隔多久取資料
                    -> {
                        ReturnValue.add(Integer.toString(value))
                        value = 0
                    }
                    10//期間內取幾次資料
                    -> {
                        ReturnValue.add(Integer.toString(value))
                        value = 0
                    }*/
                        else -> {
                        }
                    }
                }
                i++//Point to Cmd's CheckSum;
                // ReturnValue.add(Integer.toString(value));
            }
            i++

        }
        return returnValue
    }
    */
    fun ParserGetSampleRateKeyValue(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                //Log.d("B0RawDataLength",dataLength.toString())
                i++//point to CMD;
                for (j in 0 until dataLength - 2) {
                    i++//Point to DataValue
                    value = value.shl(8)
                    value += bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                        0//sample rate
                        -> {
                            returnValue.put(TvocNoseData.B2SR, value.toString())
                            value = 0
                        }
                        2//sensor_on_time_range
                        -> {
                            returnValue.put(TvocNoseData.SOTR, value.toString())
                            value = 0
                        }
                        4//time to get sample
                        -> {
                            returnValue.put(TvocNoseData.STGS, value.toString())
                            value = 0
                        }
                        6//pump on time
                        -> {
                            returnValue.put(TvocNoseData.POT, value.toString())
                            value = 0
                        }
                        8//pumping time
                        -> {
                            returnValue.put(TvocNoseData.PTR, value.toString())
                            value = 0
                        }
                    /*暫時用不到
                    9//get data in cycle 間隔多久取資料
                    -> {
                        ReturnValue.add(Integer.toString(value))
                        value = 0
                    }
                    10//期間內取幾次資料
                    -> {
                        ReturnValue.add(Integer.toString(value))
                        value = 0
                    }*/
                        else -> {
                        }
                    }
                }
                i++//Point to Cmd's CheckSum;
            }
            i++
        }
        return returnValue
    }

    fun getPM25KeyValue(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                //Log.d("B0RawDataLength",dataLength.toString())
                i++//point to CMD;
                for (j in 0 until dataLength - 2) { // -2因為Data長度13要忽略StopCmd和ByteLength
                    i++//Point to DataValue
                    value = value.shl(8)
                    value += bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                        0 -> {//PM25 SampleRate
                            returnValue.put(TvocNoseData.PM25SR, value.toString())
                            value = 0
                        }
                        2 -> {//GetSampleTime
                            returnValue.put(TvocNoseData.PM25GST, value.toString())
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i++//Point to Cmd's CheckSum;
            }
            i++
        }
        return returnValue
    }

    fun parserGetHistorySampleItemsKeyValue(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                //Log.d("B0RawDataLength",dataLength.toString())
                i++//point to CMD;
                for (j in 0 until dataLength - 2) { // -2因為Data長度13要忽略StopCmd和ByteLength
                    i++//Point to DataValue
                    value = value.shl(8)
                    value += bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                        1//Max Items
                        -> {
                            returnValue.put(TvocNoseData.MAXI, Integer.toString(value))
                            value = 0
                        }
                        2//sample status
                        -> {
                            returnValue.put(TvocNoseData.SS, Integer.toString(value))
                            value = 0
                        }
                        3//correct time
                        -> {
                            returnValue.put(TvocNoseData.CT, Integer.toString(value))
                            value = 0
                        }
                        5//最後週期離現在多久 last data sec
                        -> {
                            returnValue.put(TvocNoseData.LDS, Integer.toString(value))
                            value = 0
                        }
                        6//samepleRate
                        -> {
                            returnValue.put(TvocNoseData.B4SR, Integer.toString(value))
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i++//Point to Cmd's CheckSum;
            }
            i++
        }
        return returnValue
    }

    /*
    fun parserGetHistorySampleItemKeyValue(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                Log.d("RawDataLength",dataLength.toString())
                i++//point to CMD;
                for (j in 0 until dataLength - 2) { // -2因為Data長度13要忽略StopCmd和ByteLength
                    i++//Point to DataValue
                    value = value.shl(8)
                    value += bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                        1//Item Index
                        -> {
                            returnValue.put(TvocNoseData.II, value.toString())
                            value = 0
                        }
                        3//Temperature
                        -> {
                            var Tempvalue = -45 + 175.0f * value / 65535
                            val newTemp = "%.1f".format(Tempvalue)
                            returnValue.put(TvocNoseData.B5TEMP,newTemp)
                            value = 0
                        }
                        4//Humi
                        -> {
                            returnValue.put(TvocNoseData.B5HUMI, value.toString())
                            value = 0
                        }
                        6//TVOC
                        -> {
                            returnValue.put(TvocNoseData.B5TVOC, value.toString())
                            value = 0
                        }
                        8//CO2
                        -> {
                            returnValue.put(TvocNoseData.B5ECO2 ,value.toString())
                            value = 0
                        }
                        10//PM25
                        -> {
                            returnValue.put(TvocNoseData.B5PM25, value.toString())
                            value = 0
                        }
                        12//RecectDataCheck
                        -> {
                            returnValue.put(TvocNoseData.RDC ,value.toString())
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i++
            }
            i++
        }
        return returnValue
    }
    */

    fun getRTC(): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetSetRTC)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.NormalLens, BLECommand.GetSetRTC, checkSum)
    }

    fun setRTC(bytes: ByteArray): ByteArray {
        val valueHandler = byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteFiveBytesLens, BLECommand.GetSetRTC, 0x00.toByte(), bytes[4], bytes[5], bytes[6], bytes[7])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.WriteCmd, BLECommand.WriteFiveBytesLens, BLECommand.GetSetRTC, 0x00.toByte(), bytes[4], bytes[5], bytes[6], bytes[7], checkSum)
    }


    fun parserGetRTCKeyValue(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                //Log.d("B0RawDataLength",dataLength.toString())
                i++//point to CMD;
                for (j in 0 until dataLength - 2) { // -2因為Data長度13要忽略StopCmd和ByteLength
                    i++//Point to DataValue
                    value = value.shl(8)
                    value += bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                    //RTC
                        4 -> {
                            returnValue.put(TvocNoseData.RTC, value.toString())
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i++
            }
            i++
        }
        return returnValue
    }

    fun getAllSensorC0KeyValue(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                //Log.d("B0RawDataLength",dataLength.toString())
                i++//point to CMD;
                for (j in 0 until dataLength - 2) { // -2因為Data長度13要忽略StopCmd和ByteLength
                    i++//Point to DataValue
                    value = value.shl(8)
                    value += bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                        1 -> {//Temperature
                            var Tempvalue = -45 + 175.0f * value / 65535
                            val newTemp = "%.1f".format(Tempvalue)
                            returnValue.put(TvocNoseData.C0TEMP, newTemp)
                            value = 0
                        }
                        2 -> {//Humidity
                            returnValue.put(TvocNoseData.C0HUMI, value.toString())
                            value = 0
                        }
                        4 -> {//TVOC
                            returnValue.put(TvocNoseData.C0TVOC, value.toString())
                            value = 0
                        }
                        6 -> {//CO2
                            returnValue.put(TvocNoseData.C0ECO2, value.toString())
                            value = 0
                        }
                        8 -> {//PM25
                            returnValue.put(TvocNoseData.C0PM25, value.toString())
                            value = 0
                        }
                        9 -> {//battery life
                            returnValue.put(TvocNoseData.C0BATT, value.toString())
                            value = 0
                        }
                        10 -> {//Preheater
                            returnValue.put(TvocNoseData.C0PREH, value.toString())
                            value = 0
                        }
                        15 -> {
                            returnValue.put(TvocNoseData.C0TIME, value.toString())
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i++//Point to Cmd's CheckSum;
            }
            i++
        }
        return returnValue
    }

    fun getHistorySampleC5(inPut: Int): ByteArray {
        var input = inPut
        val b = ByteArray(4)
        if (input > 65535)
            input = 65535
        b[0] = (input and -0x1000000).ushr(24).toByte()//big
        b[1] = (input and 0x00ff0000).ushr(16).toByte()
        b[2] = (input and 0x0000ff00).ushr(8).toByte()
        b[3] = (input and 0x000000ff).toByte()//little
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.WriteTwoBytesLens, BLECommand.GetHistorySampleC5, b[2], b[3])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.WriteTwoBytesLens, BLECommand.GetHistorySampleC5, b[2], b[3], checkSum)
    }

    fun parserGetHistorySampleItemKeyValueC5(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                //Log.d("RawDataLength",dataLength.toString())
                i++//point to CMD;
                for (j in 0 until dataLength - 2) { // -2因為Data長度13要忽略StopCmd和ByteLength
                    i++//Point to DataValue
                    value = value.shl(8)
                    value += bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                        1//Item Index
                        -> {
                            returnValue.put(TvocNoseData.C5II, value.toString())
                            value = 0
                        }
                        3//Temperature
                        -> {
                            var Tempvalue = -45 + 175.0f * value / 65535
                            val newTemp = "%.1f".format(Tempvalue)
                            returnValue.put(TvocNoseData.C5TEMP, newTemp)
                            value = 0
                        }
                        4//Humi
                        -> {
                            returnValue.put(TvocNoseData.C5HUMI, value.toString())
                            value = 0
                        }
                        6//TVOC
                        -> {
                            returnValue.put(TvocNoseData.C5TVOC, value.toString())
                            value = 0
                        }
                        8//CO2
                        -> {
                            returnValue.put(TvocNoseData.C5ECO2, value.toString())
                            value = 0
                        }
                        10//PM25
                        -> {
                            returnValue.put(TvocNoseData.C5PM25, value.toString())
                            value = 0
                        }
                        15//C5TIME
                        -> {
                            returnValue.put(TvocNoseData.C5TIME, value.toLong().toString())
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i++
            }
            i++
        }
        return returnValue
    }

    fun ParserGetAutoSendDataKeyValueC6(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                //Log.d("RawDataLength",dataLength.toString())
                i++//point to CMD;
                for (j in 0 until dataLength - 2) { // -2因為Data長度13要忽略StopCmd和ByteLength
                    i++//Point to DataValue
                    value = value.shl(8)
                    value += bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                        1//Temperature
                        -> {
                            var Tempvalue = -45 + 175.0f * value / 65535
                            val newTemp = "%.1f".format(Tempvalue)
                            returnValue.put(TvocNoseData.C6TEMP, newTemp)
                            value = 0
                        }
                        2//Humi
                        -> {
                            returnValue.put(TvocNoseData.C6HUMI, value.toString())
                            value = 0
                        }
                        4//TVOC
                        -> {
                            returnValue.put(TvocNoseData.C6TVOC, value.toString())
                            value = 0
                        }
                        6//CO2
                        -> {
                            returnValue.put(TvocNoseData.C6ECO2, value.toString())
                            value = 0
                        }
                        8//PM25
                        -> {
                            returnValue.put(TvocNoseData.C6PM25, value.toString())
                            value = 0
                        }
                        9 -> {

                        }
                        10 -> {

                        }
                        15//C6TIME
                        -> {
                            returnValue.put(TvocNoseData.C6TIME, value.toString())
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i++
            }
            i++
        }
        return returnValue
    }

    fun getAllSensorD0KeyValue(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                //Log.d("B0RawDataLength",dataLength.toString())
                i++//point to CMD;
                for (j in 0 until dataLength - 2) { // -2因為Data長度13要忽略StopCmd和ByteLength
                    i++//Point to DataValue
                    value = value.shl(8)
                    value += bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                        1 -> {//PM10
                            returnValue.put(TvocNoseData.D0PM10, value.toString())
                            value = 0
                        }
                        15 -> {
                            returnValue.put(TvocNoseData.D0TIME, value.toString())
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i++//Point to Cmd's CheckSum;
            }
            i++
        }
        return returnValue
    }

    fun parserGetHistorySampleItemKeyValueD5(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                //Log.d("RawDataLength",dataLength.toString())
                i++//point to CMD;
                for (j in 0 until dataLength - 2) { // -2因為Data長度13要忽略StopCmd和ByteLength
                    i++//Point to DataValue
                    value = value.shl(8)
                    value += bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                        1 -> {//PM10
                            returnValue.put(TvocNoseData.D5INDEX, value.toString())
                            value = 0
                        }
                        3 -> {
                            returnValue.put(TvocNoseData.D5PM10, value.toString())
                            value = 0
                        }
                        15 -> {
                            returnValue.put(TvocNoseData.D5TIME, value.toString())
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i++
            }
            i++
        }
        return returnValue
    }

    fun getHistorySampleD5(inPut: Int): ByteArray {
        var input = inPut
        val b = ByteArray(4)
        if (input > 65535)
            input = 65535
        b[0] = (input and -0x1000000).ushr(24).toByte()//big
        b[1] = (input and 0x00ff0000).ushr(16).toByte()
        b[2] = (input and 0x0000ff00).ushr(8).toByte()
        b[3] = (input and 0x000000ff).toByte()//little
        val valueHandler = byteArrayOf(BLECommand.ReadCmd, BLECommand.WriteTwoBytesLens, BLECommand.GetHistorySampleD5, b[2], b[3])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(BLECommand.ReadCmd, BLECommand.WriteTwoBytesLens, BLECommand.GetHistorySampleD5, b[2], b[3], checkSum)
    }

    fun ParserGetAutoSendDataKeyValueD6(bytes: ByteArray): HashMap<String, String> {
        val returnValue = HashMap<String, String>()
        var i = 0
        var value = 0
        while (i < bytes.size) {
            if (bytes[i] == BLECommand.StopCmd) {
                i++//point to DataLength
                val dataLength = bytes[i].toInt()//取得DataLength的Int數值
                //Log.d("RawDataLength",dataLength.toString())
                i++//point to CMD;
                for (j in 0 until dataLength - 2) { // -2因為Data長度13要忽略StopCmd和ByteLength
                    i++//Point to DataValue
                    value = value.shl(8)
                    value += bytes[i].toPositiveInt()//(bytes[i] and 0xFF.toByte())
                    when (j) {
                        1 -> {//PM10
                            returnValue.put(TvocNoseData.D6PM10, value.toString())
                            value = 0
                        }
                        15 -> { //D6Time
                            returnValue.put(TvocNoseData.D6TIME, value.toString())
                            value = 0
                        }
                        else -> {
                        }
                    }
                }
                i++
            }
            i++
        }
        return returnValue
    }

    fun Byte.toPositiveInt() = toInt() and 0xFF
}