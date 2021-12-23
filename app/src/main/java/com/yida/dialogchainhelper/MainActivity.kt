package com.yida.dialogchainhelper

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.widget.TextView
import com.yida.DialogChainLib.DialogChainHelper
import com.yida.dialogchainhelper.dialogfragment.DialogFragment1
import com.yida.dialogchainhelper.dialogfragment.DialogFragment3
import com.yida.dialogchainhelper.dialogfragment.DialogFragment5

class MainActivity : AppCompatActivity() {

    lateinit var dialogChainHelper:DialogChainHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mockData()
        dialogChainHelper = DialogChainHelper()
        findViewById<TextView>(R.id.layText).setOnClickListener {
            showDialog()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogChainHelper.unRegister()
    }

    fun showDialog() {
        dialogChainHelper.addDialog2Chain(dialogFragment1, { dialogFragment1.show(supportFragmentManager, "dialogFragment1") }, 1, supportFragmentManager)
        dialogChainHelper.addDialog2Chain(dialog2, 2)
        dialogChainHelper.addDialog2Chain(dialog4, 4)
        Handler().postDelayed({
            dialogChainHelper.addDialog2Chain(dialogFragment5, { dialogFragment5.show(supportFragmentManager, "dialogFragment5") }, 5, supportFragmentManager)
        },1000)
        dialogChainHelper.addDialog2Chain(dialogFragment3, { dialogFragment3.show(supportFragmentManager, "dialogFragment3") }, 3, supportFragmentManager)
        dialogChainHelper.addDialog2Chain(dialog6, 6)
    }


    lateinit var dialog4: Dialog
    lateinit var dialog2: Dialog
    lateinit var dialog6: Dialog

    var dialogFragment1 = DialogFragment1()
    var dialogFragment3 = DialogFragment3()
    var dialogFragment5 = DialogFragment5()

    private fun mockData() {
        dialog4 = mockDialog(4)
        dialog2 = mockDialog(2)
        dialog6 = mockDialog(6)
    }


    private fun mockDialog(index: Int): Dialog {
        val dialog = Dialog(this)
        val textView = TextView(this)
        textView.width=1000
        textView.height=500
        textView.gravity=Gravity.CENTER
        textView.setText("dialog:${index}")
        dialog.setContentView(textView)
        dialog.setCancelable(true)
        return dialog
    }
}