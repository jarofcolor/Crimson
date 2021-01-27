package real.droid.crimson

import android.os.Build
import android.webkit.WebView

abstract class JsMethodHandler(val nativeMethodName:String,val jsMethodName:String) {
    abstract fun onJsCall(handler: JsMethodHandler, methodName: String, params: String)

    /**
     * 调用由网页传入的JavaScript方法
     */
    fun callback(params: String = "") {
        val script = "javascript:$jsMethodName(\"$params\")"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView?.evaluateJavascript(script) {
                //ignored 所有的采用异步回调
            }
        } else {
            webView?.loadUrl(script)
        }
    }

    internal var webView: WebView? = null
}