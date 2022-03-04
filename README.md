### DialogChainHelper是为了解决多个弹窗同时弹出，影响体验的问题

---
效果图：![](https://s2.loli.net/2021/12/23/VoIGt8OBYPFcMk7.gif)



demo代码,队列展示6个dialog
```
        dialogChainHelper.addDialog2Chain(dialogFragment1, { dialogFragment1.show(supportFragmentManager, "dialogFragment1") }, 1, supportFragmentManager)
        dialogChainHelper.addDialog2Chain(dialog2, 2)
        dialogChainHelper.addDialog2Chain(dialog4, 4)
        dialogChainHelper.addDialog2Chain(dialogFragment5, { dialogFragment5.show(supportFragmentManager, "dialogFragment5") }, 5, supportFragmentManager)
        dialogChainHelper.addDialog2Chain(dialogFragment3, { dialogFragment3.show(supportFragmentManager, "dialogFragment3") }, 3, supportFragmentManager)
        dialogChainHelper.addDialog2Chain(dialog6, 6)
```
---


#### 框架特性

- 使用TreeMap存放弹窗，在当前弹窗消失时，自动弹出下一个弹窗
- 同时支持DialogFragment和普通Dialog，
  支持配置弹窗优先级
- 不需要手动设置DismissListener，避免回调地狱，在多个弹窗的场景下，代码量依旧非常少
- 不需要继承类和接口，接入方便


---
使用方法

- 添加依赖
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
	
dependencies {
	        implementation 'com.github.NByida:DialogChainHelper:Tag'
	}	
	
```

- 构建DialogChainHelper对象
```
        var dialogChainHelper = DialogChainHelper()
```

- 添加Dialog


```
       dialogChainHelper.addDialog {
            level=7
            showDialog={
                 val dialog=mockDialog(7)
                dialog.show()
                dialog
            }
        }

```
- 添加popwindow

```
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

```


- 添加Dialogfragment

```
      dialogChainHelper.addDialogFragment {
            level=1
            showDialogFragment={
                val dialogFragment=DialogFragment1()
                dialogFragment.show(supportFragmentManager,"1")
                dialogFragment
            }
        }

```

- 取消注册
  - 在activity destroy的时候，调用unRegister,清空dialog队列
 ```
    override fun onDestroy() {
        super.onDestroy()
        dialogChainHelper.unRegister()
    }
 ```
 ---
具体使用方法，可参照demo里com.yida.dialogchainhelper.MainActivity

