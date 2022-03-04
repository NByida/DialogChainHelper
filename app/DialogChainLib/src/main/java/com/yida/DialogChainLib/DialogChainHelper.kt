package com.yida.DialogChainLib


import android.app.Dialog
import android.content.DialogInterface
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.PopupWindow
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import java.lang.reflect.Field
import java.util.*

class DialogChainHelper(activity: FragmentActivity) {
    
    private var mDismissMessageField: Field? = null
    private var mOnDismissListenerField: Field? = null
    private var showWindowMap: TreeMap<Int, ShowConfig> = TreeMap()

    init {
        activity.supportFragmentManager
            .registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                        val showNext = dialogFragments.contains(f)
                        super.onFragmentDestroyed(fm, f)
                        if (showNext) {
                            removeCurrent()
                            currentShow = false
                            showNextWindow()
                        }
                    }
                }, true
            )
    }
    
    /**
     * 添加dialog到队列
     */
    fun addDialog(add: ShowDialogConfig.() -> Unit) {
        val dialogConfig = ShowDialogConfig()
        dialogConfig.add()
        addConfig(dialogConfig)
    }

    /**
     * 添加dialogFragment到队列
     */
    fun addDialogFragment(add: ShowDialogFragmentConfig.() -> Unit) {
        val dialogFragmentConfig = ShowDialogFragmentConfig()
        dialogFragmentConfig.add()
        addConfig(dialogFragmentConfig)
    }


    /**
     * 添加popWindow到队列
     */
    fun addPopWindow(add: ShowPopWindowConfig.() -> Unit) {
        val popWindowConfig = ShowPopWindowConfig()
        popWindowConfig.add()
        addConfig(popWindowConfig)
    }

    private fun addConfig(config: ShowConfig) {
        val level = config.level
        showWindowMap[level] = config
        if (!currentShow) {
            showNextWindow()
        }
    }

    private var dialogFragments = Collections.newSetFromMap(WeakHashMap<DialogFragment, Boolean>())


    var currentShow = false

    fun removeCurrent() {
        val firstKey = showWindowMap.firstKey()
        firstKey?.let {
            showWindowMap.remove(it)
        }
    }

    private fun showNextWindow() {
        if (showWindowMap.isEmpty()) {
            return
        }
        val key: Int = showWindowMap.firstKey() ?: return
        val nextConfig = showWindowMap[key] ?: return
        val currentCanShow = nextConfig.judgeCanShow.invoke()
        if (!currentCanShow) {
            removeCurrent()
            showNextWindow()
        }
        currentShow = true
        val judgeConfig2Show = {
            var show = {}
            when (nextConfig) {
                is ShowDialogConfig -> {
                    show = {
                        val dialog = nextConfig.showDialog.invoke()
                        //获取dialog的onDismissListener，D并调用
                        if (mDismissMessageField == null) {
                            mDismissMessageField =
                                HookUtils.getDeclaredField(Dialog::class.java, "mDismissMessage")
                        }
                        var oldDismiss = {}
                        if (mDismissMessageField != null) {
                            val mDismissMessage =
                                HookUtils.fieldGetValue(mDismissMessageField, dialog)
                            (mDismissMessage as? Message)?.let {
                                val listener = it.obj
                                (listener as? DialogInterface.OnDismissListener)?.let {
                                    oldDismiss = { it.onDismiss(dialog) }
                                }
                            }
                        }
                        dialog.setOnDismissListener {
                            oldDismiss.invoke()
                            removeCurrent()
                            currentShow = false
                            showNextWindow()
                            dialog.setOnDismissListener { oldDismiss.invoke() }
                        }
                    }
                }
                is ShowPopWindowConfig -> {
                    show = {
                        val popupWindow = nextConfig.showPopWindow.invoke()
                        var oldDismiss = {}
                        if (mOnDismissListenerField == null) {
                            mOnDismissListenerField =
                                HookUtils.getDeclaredField(
                                    PopupWindow::class.java,
                                    "mOnDismissListener"
                                )
                        }
                        if (mOnDismissListenerField != null) {
                            val dismissListener =
                                HookUtils.fieldGetValue(mOnDismissListenerField, popupWindow)
                            if (dismissListener is PopupWindow.OnDismissListener) {
                                oldDismiss = {
                                    dismissListener.onDismiss()
                                }
                            }
                        }
                        popupWindow.setOnDismissListener {
                            oldDismiss.invoke()
                            removeCurrent()
                            currentShow = false
                            showNextWindow()
                            popupWindow.setOnDismissListener { oldDismiss.invoke() }
                        }
                    }
                }
                is ShowDialogFragmentConfig -> {
                    show = {
                        val dialogFragment = nextConfig.showDialogFragment.invoke()
                        dialogFragments.add(dialogFragment)
                    }
                }
                else -> { }
            }
            judgeDelay(nextConfig.delayMills, show)
        }
        if (Looper.myLooper() != Looper.getMainLooper()) {
            getMainHandler().post { judgeConfig2Show.invoke() }
        } else {
            judgeConfig2Show.invoke()
        }
    }

    private fun judgeDelay(delay: Long, action: (() -> Unit)) {
        if (delay == 0L) {
            action.invoke()
        } else {
            getMainHandler().postDelayed({ action.invoke() }, delay)
        }
    }

    private fun getMainHandler(): Handler {
        if (handler == null) {
            return Handler(Looper.getMainLooper())
        }
        return handler!!
    }


    private var handler: Handler? = null

 


    fun unRegister() {
        dialogFragments.clear()
        showWindowMap.clear()
    }


}

