package com.microjet.airqi2.GestureLock

import android.text.TextUtils

import com.microjet.airqi2.MyApplication
import com.microjet.airqi2.R

class PatternHelper {

    var message: String? = null
        private set
    private var tmpPwd: String? = null
    private var times: Int = 0
    var isFinish: Boolean = false
        private set
    var isOk: Boolean = false
        private set

    private val reDrawMsg: String
        get() = MyApplication.applicationResText(R.string.text_set_pattern_again)

    private val settingSuccessMsg: String
        get() = MyApplication.applicationResText(R.string.text_set_pattern_success)

    private val checkingSuccessMsg: String
        get() = MyApplication.applicationResText(R.string.text_unlock_success)

    private val sizeErrorMsg: String
        get() = String.format(MyApplication.applicationResText(R.string.text_redraw_again), MAX_SIZE)

    private val diffPreErrorMsg: String
        get() = MyApplication.applicationResText(R.string.text_redraw_again1)

    private val pwdErrorMsg: String
        get() = String.format(MyApplication.applicationResText(R.string.text_pw_error), remainTimes)

    private val fromStorage: String
        get() {
            val result = SharedPreferencesUtil.getInstance()!!.getString(GESTURE_PWD_KEY)
            return SecurityUtil.decrypt(result)
        }

    private val remainTimes: Int
        get() = if (times < 5) MAX_TIMES - times else 0

    internal fun validateForSetting(hitList: List<Int>?) {
        this.isFinish = false
        this.isOk = false

        if (hitList == null || hitList.size < MAX_SIZE) {
            this.tmpPwd = null
            this.message = sizeErrorMsg
            return
        }

        //1. draw first time
        if (TextUtils.isEmpty(this.tmpPwd)) {
            this.tmpPwd = convert2String(hitList)
            this.message = reDrawMsg
            this.isOk = true
            return
        }

        //2. draw second times
        if (this.tmpPwd == convert2String(hitList)) {
            this.message = settingSuccessMsg
            saveToStorage(this.tmpPwd!!)
            this.isOk = true
            this.isFinish = true
        } else {
            this.tmpPwd = null
            this.message = diffPreErrorMsg
        }
    }

    internal fun validateForChecking(hitList: List<Int>?) {
        this.isOk = false

        if (hitList == null || hitList.size < MAX_SIZE) {
            this.times++
            this.isFinish = this.times >= MAX_SIZE
            this.message = pwdErrorMsg
            return
        }

        val storagePwd = fromStorage
        if (!TextUtils.isEmpty(storagePwd) && storagePwd == convert2String(hitList)) {
            this.message = checkingSuccessMsg
            this.isOk = true
            this.isFinish = true
        } else {
            this.times++
            this.isFinish = this.times >= MAX_SIZE
            this.message = pwdErrorMsg
        }
    }

    private fun convert2String(hitList: List<Int>): String {
        return hitList.toString()
    }

    private fun saveToStorage(gesturePwd: String) {
        val encryptPwd = SecurityUtil.encrypt(gesturePwd)
        SharedPreferencesUtil.getInstance()!!.saveString(GESTURE_PWD_KEY, encryptPwd)
    }

    companion object {
        private val MAX_SIZE = 4
        private val MAX_TIMES = 5
        private val GESTURE_PWD_KEY = "gesture_pwd_key"
    }
}
