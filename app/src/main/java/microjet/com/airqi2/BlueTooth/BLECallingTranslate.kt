package microjet.com.airqi2.BlueTooth

import android.util.Log

/**
 * Created by B00055 on 2017/11/29.
 */
class CallingTranslate {

    private fun getCheckSum(CMD: ByteArray): Byte {
        val j = CMD.size
        val checkSum : Byte
        var temp = 0
        val max = 0xFF.toByte()
        for (i in 0 until j)
            temp+=CMD[i]
        checkSum = (max - temp).toByte()
        return checkSum
    }

    fun getDeviceID(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.DeviceID)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.DeviceID, checkSum)
    }

    fun setDeviceID(inputID: Byte): ByteArray {
        val valueHandler = byteArrayOf(Command_List.WriteCmd, Command_List.SetIDLens, Command_List.DeviceID, inputID)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.WriteCmd, Command_List.SetIDLens, Command_List.DeviceID, inputID, checkSum)
    }

    fun SelfTestCall(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.SelfTest)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.SelfTest, checkSum)
    }

    fun TemperatureCall(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.Temperature)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.Temperature, checkSum)
    }

    fun HumidityCall(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.Humidity)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.Humidity, checkSum)
    }

    fun TVOCCall(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.TVOC)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.TVOC, checkSum)
    }

    fun CO2Call(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.CO2)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.CO2, checkSum)
    }

    fun Temperature_RAWCall(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.Temperature_RAW)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.Temperature_RAW, checkSum)
    }

    fun Humidity_RAWCall(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.Humidity_RAW)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.Humidity_RAW, checkSum)
    }

    fun TVOC_RAWCall(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.TVOC_RAW)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.TVOC_RAW, checkSum)
    }

    fun GetTVOC_BaselineCall(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.TVOC_Baseline)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.TVOC_Baseline, checkSum)
    }

    fun WriteTVOC_BaselineCall(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.WriteCmd, Command_List.SetTVOCLens, 0x00.toByte(), 0x81.toByte(), 0xca.toByte(), 0x19.toByte(), 0x6d.toByte(), 0xdb.toByte())
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.WriteCmd, Command_List.SetTVOCLens, 0x00.toByte(), 0x81.toByte(), 0xca.toByte(), 0x19.toByte(), 0x6d.toByte(), 0xdb.toByte(), checkSum)
    }

    fun GetADCData(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.GetADCDAtaLens, Command_List.GetADCData)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.GetADCDAtaLens, Command_List.GetADCData, checkSum)
    }

    fun ResetPumpFreq(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.ResetPumpFreq)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.ResetPumpFreq, checkSum)
    }

    fun GetPumpFreq(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.GetPumpFreq)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.GetPumpFreq, checkSum)
    }

    fun ResetDeviceFlash(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.ResetFlash)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.ResetFlash, checkSum)
    }

    fun PreHeater(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.PreHeater)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.PreHeater, checkSum)
    }

    fun GetBatteryLife(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.GetBatteryLife)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.GetBatteryLife, checkSum)
    }

    fun GetAllFromDevice(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.GetAllFromDevice)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.GetAllFromDevice, checkSum)
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
        val valueHandler = byteArrayOf(Command_List.WriteCmd, Command_List.WriteOneByteLens, Command_List.FanPower, b[3])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.WriteCmd, Command_List.WriteTwoBytesLens, Command_List.FanPower, b[3], checkSum)
    }

    /**
     * Return the command include CheckSum
     *
     *
     *
     * input [0] = sample rate
     * input [1] = sensor on time
     * input [2] = time to get sample
     * input [3] = pump on time
     * input [4] = pumping time
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
        val valueHandler = byteArrayOf(Command_List.WriteCmd, Command_List.WriteElevenBytesLens, Command_List.SetOrGetSampleRate, b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7], b[8], b[9], b[10])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.WriteCmd, Command_List.WriteElevenBytesLens, Command_List.SetOrGetSampleRate, b[0], b[1], b[2], b[3], b[4], b[5], b[6], b[7], b[8], b[9], b[10], checkSum)
    }

    /**
     * Return the command include CheckSum
     * REQUEST_DEVICE_STARTING_GETSAMPLE
     *
     *
     * input [0] = sample rate
     * input [1] = sensor on time
     * input [2] = time to get sample
     * input [3] = pump on time
     * input [4] = pumping time
     * input [5] = 每間隔多久取資料
     * input [6] = 期間內取幾次資料
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
        val valueHandler = byteArrayOf(Command_List.WriteCmd, Command_List.WriteSixBytesLens, Command_List.SetOrGetSampleRate, b[0], b[1], b[2], b[3], b[4], b[5])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.WriteCmd, Command_List.WriteSixBytesLens, Command_List.SetOrGetSampleRate, b[0], b[1], b[2], b[3], b[4], b[5], checkSum)
    }

    fun GetSampleRate(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.SetOrGetSampleRate)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.SetOrGetSampleRate, checkSum)
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
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.GetHistorySampleItems)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.GetHistorySampleItems, checkSum)
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
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.WriteTwoBytesLens, Command_List.GetHistorySample, b[2], b[3])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.WriteTwoBytesLens, Command_List.GetHistorySample, b[2], b[3], checkSum)
    }

    /**
     * Return the GetInfo command include CheckSum
     *
     *
     *
     *
     * @return the GetInfo command include CheckSum
     */
    fun GetInfo(): ByteArray {
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.GetInfo)
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.NormalLens, Command_List.GetInfo, checkSum)
    }
    /**
     * Return the command include CheckSum
     *
     * <p>input 0 for stop 1~255 for minutes
     * for example input 3 to set Device get sample every 3 minutes
     * </p>
     *
     * @param input 0 for  device stop sample
     * @return the command include CheckSum
     */


    /**
     * Return the command include CheckSum
     *
     *
     *
     * input for sample time
     * ex:99/11/30
     * 0x99 0x11 0x30
     *
     *
     * @param input 0 for  device stop sample
     * @return the command include CheckSum
     */
    fun CallDeviceStartingSample(inPut: Int): ByteArray {
        var input = inPut
        val b = ByteArray(4)
        if (input > 65535)
            input = 65535
        b[0] = (input and -0x1000000).ushr(24).toByte()//big
        b[1] = (input and 0x00ff0000).ushr(16).toByte()
        b[2] = (input and 0x0000ff00).ushr(8).toByte()
        b[3] = (input and 0x000000ff).toByte()//little
        val valueHandler = byteArrayOf(Command_List.ReadCmd, Command_List.WriteThreeBytesLens, Command_List.CallDeviceStartingGetSample, b[1], b[2], b[3])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.ReadCmd, Command_List.WriteThreeBytesLens, Command_List.CallDeviceStartingGetSample, b[1], b[2], b[3], checkSum)
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
        val valueHandler = byteArrayOf(Command_List.WriteCmd, Command_List.WriteTwoBytesLens, Command_List.SensorOn_OFF, b[2], b[3])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.WriteCmd, Command_List.WriteTwoBytesLens, Command_List.SensorOn_OFF, b[2], b[3], checkSum)
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
        val valueHandler = byteArrayOf(Command_List.WriteCmd, Command_List.PumpOnLens, Command_List.PumpOn, b[2], b[3])
        val checkSum = getCheckSum(valueHandler)
        return byteArrayOf(Command_List.WriteCmd, Command_List.PumpOnLens, Command_List.PumpOn, b[2], b[3], checkSum)
    }
}