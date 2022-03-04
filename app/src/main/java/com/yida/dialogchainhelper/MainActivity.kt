package com.yida.dialogchainhelper

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import com.yida.DialogChainLib.DialogChainHelper
import com.yida.DialogChainLib.ShowDialogConfig
import com.yida.dialogchainhelper.dialogfragment.DialogFragment1
import com.yida.dialogchainhelper.dialogfragment.DialogFragment3
import com.yida.dialogchainhelper.dialogfragment.DialogFragment5

class MainActivity : AppCompatActivity() {

    lateinit var dialogChainHelper:DialogChainHelper


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        dialogChainHelper = DialogChainHelper(this)
        findViewById<TextView>(R.id.layText).setOnClickListener {
            testNewApi()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        dialogChainHelper.unRegister()
    }


    fun testNewApi(){
        dialogChainHelper.addDialogFragment {
            level=1
            showDialogFragment={
                val dialogFragment=DialogFragment1()
                dialogFragment.show(supportFragmentManager,"1")
                dialogFragment
            }
        }

        dialogChainHelper.addDialogFragment {
            level=3
            showDialogFragment={
                val dialogFragment=DialogFragment3()
                dialogFragment.show(supportFragmentManager,"3")
                dialogFragment
            }
        }

        dialogChainHelper.addPopWindow {
            level=2
            showPopWindow={
                val imageView=ImageView(this@MainActivity)
                imageView.layoutParams=ViewGroup.LayoutParams(200,200)
                imageView.setImageResource(R.drawable.ic_launcher_background)
                val pop=PopupWindow(imageView)
                pop.width=300
                pop.height=300
                val view =findViewById<TextView>(R.id.layText)
                pop.isOutsideTouchable = true;
                pop.showAsDropDown( view)
                pop
            }
        }


        dialogChainHelper.addDialogFragment {
            level=5
            showDialogFragment={
                val dialogFragment=DialogFragment5()
                dialogFragment.show(supportFragmentManager,"5")
                dialogFragment
            }
        }
        dialogChainHelper.addDialog {
            level=7
            showDialog={
                 val dialog=mockDialog(7)
                dialog.show()
                dialog
            }
        }
        dialogChainHelper.addDialog {
            showDialog={
                val dialog=mockDialog(4)
                dialog.show()
                dialog
            }

            level=4

        }
        dialogChainHelper.addDialog {
            level=6
            showDialog={
                var dialog=mockDialog(6)
                dialog.show()
                dialog
            }
        }
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