package com.yida.DialogChainLib

import android.app.Dialog
import android.widget.PopupWindow
import androidx.fragment.app.DialogFragment

open class ShowConfig{
    open var level:Int=1
    var delayMills:Long=0L
    var judgeCanShow: ()->Boolean={true}
}

class ShowDialogConfig:ShowConfig(){
    lateinit var showDialog:()->Dialog
}

class ShowDialogFragmentConfig:ShowConfig(){
    lateinit var showDialogFragment:()->DialogFragment
}

class ShowPopWindowConfig:ShowConfig(){
    lateinit var showPopWindow:()->PopupWindow
}

