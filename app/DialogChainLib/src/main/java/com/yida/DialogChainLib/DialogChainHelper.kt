package com.yida.DialogChainLib


import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.content.DialogInterface
import android.os.Message
import android.util.Log
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import java.lang.IllegalStateException
import java.lang.reflect.Field
import java.util.*

class DialogChainHelper {

    private var dialogChain: TreeMap<Int, DialogStub> = TreeMap()

    data class DialogStub(val dialogWrap: DialogWrap, val level: Int, val show: () -> Unit)
    data class DialogWrap(var dialogFragment: DialogFragment?, var dialog: Dialog?)

    private var fmRegisterList = arrayListOf<String>()
    private var fmShowList = arrayListOf<String>()

    private var mDismissMessageField: Field? = null
    private var mDismissedField: Field? = null
    private var mDestroyedField: Field? = null
    private var mHostField: Field? = null


    fun addDialog2Chain(dialog: Dialog, level: Int) {
        assertDestroy(dialog.context)
        dialogChain[level] = DialogStub(DialogWrap(null, dialog), level) {
            val activity = getActivity(dialog.context)
            if (activity != null && !activity.isDestroyed && !activity.isFinishing) {
                dialog.show()
                return@DialogStub
            }
            Log.d("addDialog2Chain", "dialog host activity is destory, skip dialog: ${level}")
            showNextDialog()
        }
        if (mDismissMessageField == null) {
            mDismissMessageField = HookUtils.getDeclaredField(Dialog::class.java, "mDismissMessage")
        }
        var oldDismiss = {}
        if (mDismissMessageField != null) {
            val mDismissMessage = HookUtils.fieldGetValue(mDismissMessageField, dialog)
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
            showNextDialog()
            dialog.setOnDismissListener { oldDismiss.invoke() }
        }
        checkShowDialogRightNow()
    }

    fun addDialog2Chain(
        dialogFragment: DialogFragment,
        show: () -> Unit = {},
        level: Int,
        fm: FragmentManager
    ) {
        dialogChain[level] = DialogStub(DialogWrap(dialogFragment, null), level) {
            if (judgeCanAddFragment(fm)) {
                show()
            } else {
                showNextDialog()
            }
        }
        if (!fmRegisterList.contains("${fm.hashCode()}")) {
            fm.registerFragmentLifecycleCallbacks(
                object : FragmentManager.FragmentLifecycleCallbacks() {
                    override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                        super.onFragmentViewDestroyed(fm, f)
                        if ((f is DialogFragment) && fragmentInList(f)) {
                            removeCurrent()
                            showNextDialog()
                            if (fmShowList.contains("${f.hashCode()}")) {
                                fmShowList.remove("${f.hashCode()}")
                            }
                        }
                    }
                }, true
            )
            fmRegisterList.add("${fm.hashCode()}")
        }
        checkShowDialogRightNow()
    }

    fun unRegister() {
        fmRegisterList.clear()
        dialogChain.clear()
        fmShowList.clear()
    }

    private fun checkShowDialogRightNow() {
        if (dialogChain.isNotEmpty()) {
            val dialogWrap = dialogChain[dialogChain.firstKey()]
            val dialog = dialogWrap?.dialogWrap?.dialog
            val dialogFragment = dialogWrap?.dialogWrap?.dialogFragment
            //当前dialog 已经被关闭
            if (dialogFragment == null) {
                if (dialog == null || !dialog.isShowing) {
                    showNextDialog()
                    return
                }
            }
            //当前dialogFragment未被添加
            if (dialogFragment?.dialog == null) {
                showNextDialog()
                return
            }
            //当前dialogFragment 已经被关闭 dismissed
            if (mDismissedField == null) {
                mDismissedField =
                    HookUtils.getDeclaredField(DialogFragment::class.java, "mDismissed")
            }
            val mDismissed = HookUtils.fieldGetValue(
                mDismissedField,
                dialogFragment
            )
            if (mDismissedField == null || mDismissed == null) {
                Log.e("Fatal error", "mDismissed  is null , reflect failed")
            }
            (mDismissed as? Boolean)?.let {
                if (mDismissed) {
                    showNextDialog()
                }
            }
        }
    }


    private fun showNextDialog() {
        if (dialogChain.isEmpty()) {
            unRegister()
            return
        }
        val dialogWrap = dialogChain[dialogChain.firstKey()]
        val dialog = dialogWrap?.dialogWrap?.dialog
        val dialogFragment = dialogWrap?.dialogWrap?.dialogFragment
        dialog?.let {
            dialogWrap.show.invoke()
            Log.i("dialog", "show dialog:${dialogChain.firstKey()}")
            return
        }
        if (dialogFragment == null) {
            Log.i("dialog", "show dialogFragment:${dialogChain.firstKey()}")
            dialogWrap?.show?.invoke()
        } else {
            if (!fmShowList.contains("${dialogFragment.hashCode()}")) {
                Log.i("dialog", "show dialogFragment2:${dialogChain.firstKey()}")
                dialogWrap.show.invoke()
                fmShowList.add("${dialogFragment.hashCode()}")
            }
        }
    }


    private fun judgeCanAddFragment(fragmentManager: FragmentManager): Boolean {
        if (mDestroyedField == null) {
            mDestroyedField = HookUtils.getDeclaredField(
                fragmentManager.javaClass,
                "mDestroyed"
            )
        }
        if (mHostField == null) {
            mHostField = HookUtils.getDeclaredField(
                fragmentManager.javaClass,
                "mHost"
            )
        }
        val mDestroyed = HookUtils.fieldGetValue(mDestroyedField, fragmentManager)
        val mHost = HookUtils.fieldGetValue(mHostField, fragmentManager)
        if (mDestroyed != null && mHost != null) {
            if (mDestroyed is Boolean && !mDestroyed) {
                Log.d("addDialog2Chain", "judgeCanAddFragment true")
                return true
            }
        } else {
            Log.e("Fatal error", "mDestroyed or mHost is null , reflect failed")
        }
        Log.d("addDialog2Chain", "judgeCanAddFragment false")
        return false
    }


    private fun removeCurrent() {
        if (!dialogChain.isEmpty()) {
            dialogChain.remove(dialogChain.firstKey())
        }
    }


    private fun fragmentInList(dialogFragment: DialogFragment): Boolean {
        for (index: Int in dialogChain.keys) {
            if (dialogChain[index]?.dialogWrap?.dialogFragment == dialogFragment) {
                return true
            }
        }
        return false
    }

    private fun assertDestroy(mContext: Context?) {
        if (mContext == null) {
            throw IllegalStateException("context cannot be null")
        }
        val activity = getActivity(mContext)
        activity?.let {
            if (it.isFinishing || it.isDestroyed) {
                throw IllegalStateException(" cannot add dialog in a destroy activity")
            }
        }
    }

    private fun getActivity(mContext: Context): Activity? {
        var context: Context? = mContext
        while (context !is Activity && mContext is ContextWrapper) {
            context = (mContext).baseContext
        }
        return if (context is Activity) {
            context
        } else null
    }
}

