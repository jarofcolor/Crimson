package real.crimson.demo

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
import real.crimson.JsMethodHandler
import real.crimson.Module
import real.crimson.Crimson
import real.crimson.WebViewResult
class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WebView.setWebContentsDebuggingEnabled(true)


        val module = Module("af,sdk", "sdk", "app", 1)
        Crimson.registerModule(module)

        //第一种，先解析所有模块
        val dialog = AlertDialog.Builder(this).setCancelable(false).setMessage("正在加载中...").show()
        Crimson.parseModules(this) {
            if (it.contains(module)) {
                //可频繁调用
                val moduleResult = Crimson.startModule(this, module.route)
                if (moduleResult != null) {
                    val result = moduleResult as WebViewResult
                    val webView = result.result()
                    webView.webViewClient = object : WebViewClient() {
                        override fun onPageFinished(view: WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            //加载完成关闭对话
                            if (dialog.isShowing)
                                dialog.dismiss()
                        }
                    }

                    //注册解析打开模块子页面的JS方法
                    val openPageHandler = object:JsMethodHandler(){
                        override fun onJsCall(methodName: String, params: String) {
                            //例如user子页面，此时params为user
                            result.openPage(params)
                        }
                    }
                    //操作完成后如果需要通知网页，则调用
                    openPageHandler.callback()
                    result.registerJsMethodHandler("openPage",openPageHandler)
                    val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    container.addView(webView, params)
                }
            }
        }

        //第二种，解析某个模块并加载，不要频繁调用
//        Crimson.startModule(this, "af,sdk", {
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
        modules.addAll(Crimson.modules())
        modules.forEach {
            Crimson.unregisterModule(it)
        }
    }
}

