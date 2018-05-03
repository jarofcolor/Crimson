package real.hybrid

import android.content.Context
import android.webkit.WebView

class WebViewResult(private val context: Context,private val module: Module,private val page: String,private val params: String) : ModuleResult<WebView> {

    private lateinit var webView: WebView

    override fun result(): WebView {
        webView = HybridWebView(context)
        if(page.isEmpty()){
            webView.loadUrl("file:///${RealHybrid.getAppModulePath(context,module)}/index.html")
        }else {
            webView.loadUrl("file:///${RealHybrid.getAppModulePath(context,module)}/$page/index.html")
        }
        return webView
    }

    fun openWebPage(page:String,params:String){
        RealHybrid.startWebPage(context,module,params)
    }
}