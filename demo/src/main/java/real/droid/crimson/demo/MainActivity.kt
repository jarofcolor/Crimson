package real.droid.crimson.demo

import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import real.droid.crimson.Crimson
import real.droid.crimson.JsMethodHandler
import real.droid.crimson.Module
import real.droid.crimson.WebViewResult

class MainActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        WebView.setWebContentsDebuggingEnabled(true)


        val module = Module("m.demo", "demo", "app", 1)
        Crimson.registerModule(module)

        val dialog = AlertDialog.Builder(this).setCancelable(false).setMessage("正在加载中...").show()
        Crimson.parseModules(this) {
            if (it.contains(module)) {
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

                    //创建JS处理器
                    val receiveMsgHandler = object : JsMethodHandler("receiveMsg", "setResult") {
                        override fun onJsCall(handler: JsMethodHandler, methodName: String, params: String) {
                            Toast.makeText(this@MainActivity, "收到来自网页的内容:$params", Toast.LENGTH_SHORT).show()
                            //通知结果给网页
                            handler.callback("客户端返回的数据：" + System.currentTimeMillis())
                        }
                    }
                    result.registerJsMethodHandler(receiveMsgHandler)
                    val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                    container.addView(webView, params)
                }
            }
        }
    }
}

