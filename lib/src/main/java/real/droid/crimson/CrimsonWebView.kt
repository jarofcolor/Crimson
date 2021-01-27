package real.droid.crimson

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView

@SuppressLint("SetJavaScriptEnabled", "ViewConstructor", "JavascriptInterface")
class CrimsonWebView(context: Context, crossDomain: Boolean = true) : WebView(context) {
    init {
        val webSettings = this.settings
        webSettings.domStorageEnabled = true
        webSettings.databaseEnabled = true
        webSettings.blockNetworkImage = false
        if (Build.VERSION.SDK_INT >= 21) {
            webSettings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        webSettings.javaScriptEnabled = true
        webSettings.setSupportZoom(true)
        webSettings.allowFileAccess = true
        webSettings.cacheMode = WebSettings.LOAD_DEFAULT
        webSettings.loadWithOverviewMode = true

        try {
            if (crossDomain)
                if (Build.VERSION.SDK_INT >= 16) {
                    val clazz = webSettings.javaClass
                    val method = clazz.getMethod(
                            "setAllowUniversalAccessFromFileURLs", Boolean::class.javaPrimitiveType)//利用反射机制去修改设置对象
                    method?.invoke(webSettings, true)
                }
        } catch (e: Exception) {
        }


        this.addJavascriptInterface(InnerJavaScriptInterface(), "crimson")
    }

    private val jsMethodHandles = hashMapOf<String, JsMethodHandler>()
    private val mainHandler = Handler(Looper.getMainLooper())

    inner class InnerJavaScriptInterface {
        /**
         * @param [methodName]] 本地方法名
         * @see JsMethodHandler
         * @param [params]] JavaScript调用本地方法时的传入参数
         *
         * 本方法在网页上使用 window.crimson.call("xx",'xx","xxx")
         */
        @JavascriptInterface
        fun call(methodName: String, params: String) {
            if (methodName.isEmpty()) return
            val handler = jsMethodHandles[methodName]
            mainHandler.post { handler?.onJsCall(handler, methodName, params) }
        }
    }

    fun registerJsMethodHandler(handler: JsMethodHandler) {
        if (!jsMethodHandles.containsKey(handler.nativeMethodName)) {
            jsMethodHandles[handler.nativeMethodName] = handler
            handler.webView = this
        }
    }

    fun unregisterJsMethodHandler(methodName: String) {
        jsMethodHandles.remove(methodName)
    }
}