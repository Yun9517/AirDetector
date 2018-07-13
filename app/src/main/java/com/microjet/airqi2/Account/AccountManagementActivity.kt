package com.microjet.airqi2.Account

import android.app.DialogFragment
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginResult
import com.microjet.airqi2.BleEvent
import com.microjet.airqi2.Fragment.CheckFragment
import com.microjet.airqi2.R
import com.microjet.airqi2.TvocNoseData
import kotlinx.android.synthetic.main.activity_login.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern


class AccountManagementActivity : AppCompatActivity() {
    val callbackManager = CallbackManager.Factory.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        login_buttonFB?.visibility = View.GONE
        login_buttonFB.setReadPermissions(Arrays.asList("public_profile", "email"))
        loginButtonFB()
        initActionBar()

        newAccount?.setOnClickListener {
            checkNetwork("registerShow")
        }

        forgotPassword?.setOnClickListener {
            checkNetwork("forgotPassword")
        }

        login?.setOnClickListener {
            checkNetwork("login")
        }


    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
    }

    override fun onPause() {
        super.onPause()
        EventBus.getDefault().unregister(this)
    }

    private fun checkNetwork(whichFun: String) {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = cm.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            when (whichFun) {
                "registerShow" -> registerShow()
                "forgotPassword" -> forgotPassword()
                "login" -> login()
            }
        } else {
            val newFrage = CheckFragment().newInstance(R.string.checkConnection, this, 1)
            newFrage.show(fragmentManager, "dialog")
        }
    }

    private fun login() {
        when (isEmail(email?.text.toString()) && email?.text.toString() != "") {
            true -> AccountLoginTask().execute(email?.text.toString(), password?.text.toString())
            false -> {
                val newFrage = CheckFragment().newInstance(R.string.errorMail_address, this, 1)
                newFrage.show(fragmentManager, "dialog")
            }
        }
    }

    private fun isEmail(strEmail: String?): Boolean {
        val strPattern = ("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$")
        val p = Pattern.compile(strPattern)
        val m = p.matcher(strEmail)
        return m.matches()
    }


    private fun loginButtonFB() {
        login_buttonFB.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                // App code
                val userId = loginResult.accessToken.userId
                val graphRequest = GraphRequest.newMeRequest(loginResult.accessToken) { `object`, response -> displayUserInfo(`object`, loginResult) }
                val parameters = Bundle()
                parameters.putString("fields", "first_name, last_name, email, id")
                graphRequest.parameters = parameters
                graphRequest.executeAsync()
            }

            override fun onCancel() {
                // App code
            }

            override fun onError(exception: FacebookException) {
                // App code
            }
        })
    }

    fun displayUserInfo(`object`: JSONObject, loginResult: LoginResult) {
        var first_name = ""
        var last_name = ""
        var email = ""
        var id = ""
        try {
            first_name = `object`.getString("first_name")
            last_name = `object`.getString("last_name")
            email = `object`.getString("email")
            id = `object`.getString("id")
            //AccountFBLoginTask().execute(loginResult.accessToken.token.toString(),"facebook")
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        Log.e("testFBandFUCK", first_name + "_" + last_name + "_" + email + "_" + id)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    @Subscribe
    fun onEvent(bleEvent: BleEvent) {
        /* 處理事件 */
        Log.d("AirAction", bleEvent.message)
        when (bleEvent.message) {
            "wait Dialog" -> {
                val newFrage = CheckFragment().newInstance(R.string.wait_Login, this, 0)
                newFrage.setCancelable(false)
                newFrage.show(fragmentManager, "dialog")
            }
            "close Wait Dialog" -> {
                val previousDialog = fragmentManager.findFragmentByTag("dialog")
                if (previousDialog != null) {
                    val dialog = previousDialog as DialogFragment
                    dialog.dismiss()
                }
            }
            "success Login" -> {
                writeUserData()
                AccountActivityShow()
            }
            "wrong Login" -> {
                val newFrage = CheckFragment().newInstance(R.string.errorPassword, this, 1)
                newFrage.show(fragmentManager, "dialog")
            }
        }
    }

    private fun writeUserData() {
        val share = getSharedPreferences("TOKEN", MODE_PRIVATE)
        share.edit().putString("token", TvocNoseData.cloudToken).apply()
        share.edit().putString("name", TvocNoseData.cloudName).apply()
        share.edit().putString("email", TvocNoseData.cloudEmail).apply()
        share.edit().putString("deviceLi", TvocNoseData.cloudDeviceArr).apply()
        TvocNoseData.cloudToken = ""
        TvocNoseData.cloudName = ""
        TvocNoseData.cloudEmail = ""
        TvocNoseData.cloudDeviceArr = ""
    }

    private fun AccountActivityShow() {
        val intent = Intent(this, AccountActiveActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun registerShow() {
        val i: Intent? = Intent(this, AccountRegisterActivity::class.java)
        startActivity(i)
    }

    private fun forgotPassword() {
        val i: Intent? = Intent(this, AccountForgetPasswordActivity::class.java)
        startActivity(i)
    }

    private fun initActionBar() {
        // 取得 actionBar
        val actionBar = supportActionBar
        // 設定顯示左上角的按鈕
        actionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home //對用戶按home icon的處理，本例只需關閉activity，就可返回上一activity，即主activity。
            -> {
                finish()
                return true
            }
            else -> {
            }
        }
        return super.onOptionsItemSelected(item)
    }

}