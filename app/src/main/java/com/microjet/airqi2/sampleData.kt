package com.microjet.airqi2

/**
 * Created by B00175 on 2018/6/25.
 */
object sampleData {

    val arr = arrayListOf<AsmDataModel>()

    init {
        prepareData()
    }

    private fun prepareData() {
        for (i in 0 .. 600) {
            var asm = AsmDataModel()
            asm.tempValue = "23.2"
            asm.humiValue = "41"
            asm.tvocValue = (218 + i).toString()
            asm.ecO2Value = "1171"
            asm.pM25Value = (3 + i ).toString()
            asm.pM10Value = 3
            asm.longitude = (121.42034 - (i / 1000F)).toFloat()
            asm.latitude = (24.959635 - (i / 1000F)).toFloat()
            asm.created_time = 1528992024000 + i * 60000
            arr.add(asm)
        }
    }

    fun getData(): List<AsmDataModel> {
        changeData()
        return arr
    }

    private fun changeData() {
        var item = arr[3]
        item.longitude = 121.412718f
        item.latitude = 24.960936f
        item.tvocValue = "5000"

    }
}