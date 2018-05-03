package real.hybrid

import android.content.Context
import android.webkit.WebView

class WebViewResult(private val context: Context, private val module: Module?, private val page: String) : ModuleResult<WebView> {

    private lateinit var webView: WebView

    override fun result(): WebView {
        webView = HybridWebView(context)

        if (module == null) {
            webView.loadUrl(page)
            return webView
        }

        if (page.isEmpty()) {
            webView.loadUrl("file:///${RealHybrid.getAppModulePath(context, module)}/index.html")
        } else {
            webView.loadUrl("file:///${RealHybrid.getAppModulePath(context, module)}/$page/index.html")
        }
        return webView
    }

    fun openPage(page: String, params: String) {
        if (module != null)
            RealHybrid.startPage(context, module, params)
    }

    fun openWebPage(page: String, url: String) {
        RealHybrid.startWebPage(context, url)
    }
}