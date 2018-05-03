package real.hybrid

import android.content.Context
import android.webkit.WebView

class WebViewLauncher(private val module: Module) : ModuleLauncher(module) {

    override fun launch(context: Context, params: String): ModuleResult<WebView> {
        return WebViewResult(context,module,"",params)
    }

    fun startWebPage(context: Context, page: String, params: String = ""):WebViewResult{
        return WebViewResult(context,module,page,params)
    }
}