package real.hybrid

import android.content.Context
import android.webkit.WebView

class WebViewLauncher(private val module: Module) : ModuleLauncher(module) {

    override fun launch(context: Context, params: String): ModuleResult<WebView> {
        return WebViewResult(context,module,"")
    }

    fun startPage(context: Context, page: String):WebViewResult{
        return WebViewResult(context,module,page)
    }

    fun startWebPage(context: Context, url:String):WebViewResult{
        return WebViewResult(context,null,url)
    }
}