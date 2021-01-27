# Crimson

## 项目介绍

本项目是对原生和网页混合开发的一个轻量级封装,框架的理念是将网页存储在本地，然后通过简单的方式加载它。同时，框架随时可更新本地静态资源包，从而使项目具有和网页一样方便的更新能力。框架采用模块化思想，可以将项目分成多个模块分开更新加载，做到真正的按需配置。

## 如何导入

``` gradle
dependencies {
    implementation 'real.droid:crimson:1.0.0'
}
```

## 如何使用

首先将网页内容压缩包以name_version.zip命名，然后放置到assets/modules目录下，具体可查看示例，然后按照以下说明调用代码。

### 1、注册module

``` kotlin
    val module = Module("m.demo", "demo", "app", 1)
    Crimson.registerModule(module)
```

### 2、解析module

```kotlin
    Crimson.parseModules()
```

### 3、启动module

```kotlin
    Crimson.startModule(context, module.route) 
```

### 4、启动后注册JS处理函数

```kotlin
    val moduleResult = Crimson.startModule(this, module.route)
    val result = moduleResult as WebViewResult
    //创建JS处理器
    val receiveMsgHandler = object : JsMethodHandler("receiveMsg","setResult") {
        override fun onJsCall(handler: JsMethodHandler,methodName: String, params: String) {
            Toast.makeText(this@MainActivity, "收到来自网页的内容:$params", Toast.LENGTH_SHORT).show()
            //通知结果给网页
            handler.callback("客户端返回的数据：" + System.currentTimeMillis())
        }
    }
    //注册一个处理器
    result.registerJsMethodHandler(receiveMsgHandler)
```

上面示例中，js代码通过`window.crimson.call("receiveMsg","这是一条来自网页的数据")`传递数据到客户端，客户端在接收数据后，通过`handler.callback("客户端返回的数据：" + System.currentTimeMillis())`将数据传递到网页，网页中需要有

``` javascript
function setResult(data){
    console.log(data)
}
```

来接收客户端的数据

### 5、完整示例

```kotlin
    val module = Module("m.demo", "demo", "app", 1)
    Crimson.registerModule(module)

    val dialog = AlertDialog.Builder(this).setCancelable(false).setMessage("正在加载中...").show()
    Crimson.parseModules(this) {
        if (it.contains(module)) {
            val moduleResult = Crimson.startModule(this, module.route)
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

                //创建JS处理器
                val receiveMsgHandler = object : JsMethodHandler("receiveMsg","setResult") {
                    override fun onJsCall(handler: JsMethodHandler,methodName: String, params: String) {
                        Toast.makeText(this@MainActivity, "收到来自网页的内容:$params", Toast.LENGTH_SHORT).show()
                        //通知结果给网页
                        handler.callback("收到客户端的返回：" + System.currentTimeMillis())
                    }
                }
                //注册一个处理器
                result.registerJsMethodHandler(receiveMsgHandler)
                val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                container.addView(webView, params)
            }
        }
    }
```

## 设计思路

设计思路大概可分为以下几点

+ 模块化，比如user模块，discovery模块等
+ 可更新，从网络下载更新
+ 版本控制，从服务器返回数据确认使用某个版本
+ 简易的JavaScript桥，方便和网页之间通信

结构：

``` no
        +++ module parser (用来解析静态资源包 zip)
module
        +++ module launcher （用来启动模块，返回 module result）

```

将项目整体划分为多个 module，每个module配置有一个 parser 和 一个 launcher，parser用来解析释放静态资源包（以.zip结尾的压缩文件），launcher则用来启动这个模块，启动后返回一个ModuleResult对象，该对象将会持有我们后续操作所需要的结果，比如一个WebView对象，得到WebView对象后，将其添加到UI视图中便可以显示出来

ModuleResult是一个真正结果的代理对象，可以通过该对象执行一些操作，比如添加一个JavaScript方法，从而轻易的实现和网页的互相通信