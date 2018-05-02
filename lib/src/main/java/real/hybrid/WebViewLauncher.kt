package real.hybrid

import android.content.Context
import android.webkit.WebView

class WebViewLauncher(module: Module) : ModuleLauncher(module) {

    override fun launch(context: Context, params: String): ModuleResult<WebView> {
        return WebViewResult(context)
    }

    fun startWebPage(context: Context, page: String, params: String = ""):WebViewResult{
        return WebViewResult(context)
    }

    inner class WebViewResult(private val context: Context) : ModuleResult<WebView> {
        override fun result(): WebView {
            return HybridWebView(context)
        }
    }
}