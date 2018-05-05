package real.crimson

import android.os.Build
import android.webkit.WebView

abstract class JsMethodHandler {
    abstract fun onJsCall(methodName: String, params: String)

    /**
     * 调用由网页传入的JavaScript方法
     */
    fun callback(params: String = "") {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView?.evaluateJavascript("javascript:$callbackName($params)", {
                //ignored 所有的采用异步回调
            })
        } else {
            webView?.loadUrl("javascript:$callbackName($params)")
        }
    }

    internal var webView: WebView? = null
    internal var callbackName: String? = null
}