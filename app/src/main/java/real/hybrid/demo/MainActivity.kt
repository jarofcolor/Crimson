package real.hybrid.demo

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import kotlinx.android.synthetic.main.activity_main.*
import real.hybrid.Module
import real.hybrid.RealHybrid
import real.hybrid.WebViewResult

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WebView.setWebContentsDebuggingEnabled(true)


        val module = Module("af,sdk", "sdk", "app", 1)
        RealHybrid.registerModule(module)

        //第一种，先解析所有模块
        val dialog = AlertDialog.Builder(this).setCancelable(false).setMessage("正在加载中...").show()
        RealHybrid.parseModules(this, {
            if (it.contains(module)) {
                //可频繁调用
                val moduleResult = RealHybrid.startModule(this, module.route)
                if (moduleResult != null) {
                    val result = moduleResult as WebViewResult
                    val webView = result.result()
                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            if (dialog.isShowing)
                                dialog.dismiss()
                        }
                    }
                    val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    container.addView(webView, params)
                }
            }
        })

        //第二种，解析某个模块并加载，不要频繁调用
//        RealHybrid.startModule(this, "af,sdk", {
//            // 更新成功或失败
//        }, {
//            if (it != null) {
//                val result = (it as WebViewResult)
//                val webView = result.result()
//                webView.webViewClient = object : WebViewClient() {
//                    override fun onPageFinished(view: WebView?, url: String?) {
//                        super.onPageFinished(view, url)
//                        if (dialog.isShowing)
//                            dialog.dismiss()
//                    }
//                }
//                val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
//                container.addView(webView, params)
//            }
//        })
    }

    override fun onDestroy() {
        super.onDestroy()
        val modules = arrayListOf<Module>()
        modules.addAll(RealHybrid.modules())
        modules.forEach {
            RealHybrid.unregisterModule(it)
        }
    }
}

