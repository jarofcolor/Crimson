# Crimson 

### 项目介绍

本项目是对原生和网页混合开发的一个轻量级封装,框架的理念是将网页文档存储在本地，然后通过简单的方式加载它。同时，框架随时可更新本地静态资源包，从而使项目具有和网页一样方便的更新能力。框架采用模块化思想，可以将项目分成多个模块分开更新加载，做到真正的按需配置。

### 设计思路

设计思路大概可分为以下几点

+ 模块化，比如user模块，discovery模块等
+ 可更新，从网络下载更新
+ 版本控制，从服务器返回数据确认使用某个版本
+ 简易的JavaScript桥，方便和网页之间通信

结构：

```
        +++ module parser (用来解析静态资源包 zip)
module
        +++ module launcher （用来启动模块，返回 module result）

```

将项目整体划分为多个 module，每个module配置有一个 parser 和 一个 launcher，parser用来解析释放静态资源包（以.zip结尾的压缩文件），launcher则用来启动这个模块，启动后返回一个ModuleResult对象，该对象将会持有我们后续操作所需要的结果，比如一个WebView对象，得到WebView对象后，将其添加到UI视图中便可以显示出来

ModuleResult是一个真正结果的代理对象，可以通过该对象执行一些操作，比如添加一个JavaScript方法，从而轻易的实现和网页的互相通信

代码示例

```kotlin

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WebView.setWebContentsDebuggingEnabled(true)


        val module = Module("af,sdk", "sdk", "app", 1)
        RealHybrid.registerModule(module)

        //第一种，先解析所有模块
        val dialog = AlertDialog.Builder(this).setCancelable(false).setMessage("正在加载中...").show()
        RealHybrid.parseModules(this, {
            if (it.contains(module)) {
                //可频繁调用
                val moduleResult = RealHybrid.startModule(this, module.route)
                if (moduleResult != null) {
                    val result = moduleResult as WebViewResult
                    val webView = result.result()
                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            //加载完成关闭对话
                            if (dialog.isShowing)
                                dialog.dismiss()
                        }
                    }

                    //注册解析打开模块子页面的JS方法
                    val openPageHandler = object:JsMethodHandler(){
                        override fun onJsCall(methodName: String, params: String) {
                            //例如user子页面，此时params为user
                            result.openPage(params)
                        }
                    }
                    //操作完成后如果需要通知网页，则调用
                    openPageHandler.callback()
                    result.registerJsMethodHandler("openPage",openPageHandler)
                    val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    container.addView(webView, params)
                }
            }
        })

        //第二种，解析某个模块并加载，不要频繁调用
//        RealHybrid.startModule(this, "af,sdk", {
//            // 更新成功或失败
//        }, {
//            if (it != null) {
//                val result = (it as WebViewResult)
//                val webView = result.result()
//                webView.webViewClient = object : WebViewClient() {
//                    override fun onPageFinished(view: WebView?, url: String?) {
//                        super.onPageFinished(view, url)
//                        if (dialog.isShowing)
//                            dialog.dismiss()
//                    }
//                }
//                val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//                container.addView(webView, params)
//            }
//        })
    }
    
```

### 项目结构

静态资源包结构

```
+++ user.zip
     +++ score 积分墙
           +++ index.html
     +++ vip
     +++ index.html
```

将资源包文件放入 assets/modules 即可，资源包会在parse过程中自动释放，释放到每个app存储区的files目录，相关代码如下


Crimson.kt
   
```kotlin
   
    fun getAppPath(context: Context): String {
        return "${context.filesDir.absolutePath}${File.separator}modules"
    }

    fun getAppModulePath(context: Context, module: Module): String {
        return "${getAppPath(context)}${File.separator}${module.name}${File.separator}${module.version}"
    }

```


由于框架并未实现如何从网络获得更新地址，这一部分则需要开发者自己实现，通过传递当前版本给服务器获取是否需要更新，然后通过setUpdate方法标识需要更新就可以


 Crimson.kt
 
 ```kotlin
    fun setUpdate(context: Context, module: Module, update: String?) {
        if (update == null)
            updates.remove(module)
        else
            updates[module] = update
    }
```  
将需要更新的模块的下载地址通过该方法设置进去，框架便会自动下载释放资源

### 简易的JavaScript桥

相关代码 CrimsonWebView.kt
```kotlin
    inner class InnerJavaScriptInterface {
        @JavascriptInterface
        fun call(methodName: String, callbackName: String, params: String) {
            if (methodName.isEmpty()) return
            val handler = jsMethodHandles[methodName]
            handler?.callbackName = callbackName
            handler?.webView = this@CrimsonWebView
            handler?.onJsCall(methodName, params)
        }
    }
    
    
     this.addJavascriptInterface(InnerJavaScriptInterface(), "crimson")
```

客户调需要注册相应处理函数
```kotlin
webViewResult.registerJsMethodHandler("openPage",openPageHandler)
```

多参数时，建议传递json字符串方便解析

为了简洁，客户端只注册处理函数，并只作为接收者，不具备主动调用能力，所以需要主动调用的地方，可先由网页调用传入相关函数，如上文，当网页调用call时，传入的callbackName将使openPageHandler具有调用相关JavaScript方法的能力，该句柄可以随时在其它地方主动调用

```kotlin
openPageHandler.callback("xx")
```






