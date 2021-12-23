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
参数1： Dialog dialog
参数2： int dialogLevel（Dialog优先级）
  
```
        dialogChainHelper.addDialog2Chain(dialog2, 2)

```


- 添加Dialogfragment
参数1： Dialogfragment dialog
参数2:   () -> Unit show  ，这个参数在kotlin里是一个高阶函数，在Java里是一个lamba表达式，需要把dialog 的展示逻辑写出对应的高阶函数，作为传参，传进来
参数3： int dialogLevel（Dialog优先级）
参数4:FragmentManager fm（需要展示的DialogFragmnet，添加用的FragmentManager），目的是为了判断dialog的宿主的生命周期

```
        dialogChainHelper.addDialog2Chain(dialogFragment3, { dialogFragment3.show(supportFragmentManager, "dialogFragment3") }, 3, supportFragmentManager)

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

