package com.yida.dialogchainhelper.dialogfragment

import android.view.LayoutInflater
import android.view.ViewGroup
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.view.Gravity
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.yida.dialogchainhelper.R

class DialogFragment1 : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val textView = TextView(context)
        textView.width = 300
        textView.height = 300
        textView.gravity = Gravity.CENTER
        textView.setBackgroundColor(ContextCompat.getColor(context!!, R.color.white))
        textView.setTextColor(ContextCompat.getColor(context!!, R.color.black))
        textView.text = "我是DialogFragment:1"
        Log.e("yida", "onCreateView")
        return textView
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e("yida", "onDestroyView:" + this.javaClass.simpleName)
    }
}