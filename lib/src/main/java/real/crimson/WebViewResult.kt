package real.crimson

import android.content.Context
import android.webkit.WebView

class WebViewResult(private val context: Context, private val module: Module?, private val page: String, private val crossDomain: Boolean = true) : ModuleResult<WebView> {

    private lateinit var webView: CrimsonWebView

    override fun result(): WebView {
        webView = CrimsonWebView(context, crossDomain)

        webView.post {
            when {
                module == null -> webView.loadUrl(page)
                page.isEmpty() -> webView.loadUrl("file:///${Crimson.getAppModulePath(context, module)}/index.html")
                else -> webView.loadUrl("file:///${Crimson.getAppModulePath(context, module)}/$page/index.html")
            }
        }
        return webView
    }

    fun openPage(page: String) {
        if (module != null)
            Crimson.startPage(context, module,page)
    }

    fun openWebPage(page: String, url: String) {
        Crimson.startWebPage(context, url)
    }


    fun registerJsMethodHandler(methodName: String, handler: JsMethodHandler) {
        webView.registerJsMethodHandler(methodName, handler)
    }

    fun unregisterJsMethodHandler(methodName: String) {
        webView.unregisterJsMethodHandler(methodName)
    }
}