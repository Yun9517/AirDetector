package microjet.com.airqi2

import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.TextView

/**
 * Created by ray650128 on 2017/11/24.
 */

class CustomDialogActivity : Activity() {

    var btn_confirm : Button? = null
    var text_close : TextView? = null
    var text_title : TextView? = null
    var text_content : TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.custom_dialog)

        uiFindViewById()
        uiSetAction()
        getText()
    }

    private fun uiFindViewById() {
        btn_confirm = this.findViewById(R.id.btn_confirm)

        text_close = this.findViewById(R.id.text_close)
        text_title = this.findViewById(R.id.text_title)
        text_content = this.findViewById(R.id.text_content)
    }

    private fun uiSetAction() {
        btn_confirm!!.setOnClickListener {
            this@CustomDialogActivity.finish()
        }

        text_close!!.setOnClickListener {
            this@CustomDialogActivity.finish()
        }
    }

    private fun getText() {
        val bundle : Bundle? = intent.extras

        val title : String? = bundle!!.getString("dialogTitle")
        val content : String? = bundle.getString("dialogContent")

        text_title!!.text = title
        text_content!!.text = content
    }
}