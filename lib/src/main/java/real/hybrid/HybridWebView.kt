package real.hybrid

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView

@SuppressLint("SetJavaScriptEnabled")
class HybridWebView(context: Context?) : WebView(context) {

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
    }


}