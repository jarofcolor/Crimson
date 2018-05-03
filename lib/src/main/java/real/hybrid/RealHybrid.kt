package real.hybrid

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

object RealHybrid {

    private val tasks = ConcurrentHashMap<Module, () -> Unit>()
    private val updates = ConcurrentHashMap<Module, String>()
    private val threadPool = Executors.newFixedThreadPool(10)
    private val mainHandler = Handler(Looper.getMainLooper())

    /**
     * 注册模块
     */
    fun registerModule(module: Module) {
        Module.register(module)
    }

    /**
     * 解除模块注册
     */
    fun unregisterModule(module: Module) {
        Module.unregister(module)
    }

    /**
     * 设置是否需要更新
     */
    fun setUpdate(context: Context, module: Module, update: String?) {
        if (update == null)
            updates.remove(module)
        else
            updates[module] = update
    }

    fun startModule(context: Context, url: String, params: String = "", update: (Boolean) -> Unit, result: (ModuleResult<*>?) -> Unit) {
        val module = modules().find { it.route == url }
        if (module == null) {
            result(null)
            return
        }
        val parser = module.parser
        val run = {
            val updateUrl = updates[module]
            if (updateUrl != null && parser.update(context, updateUrl)) {
                mainHandler.post {
                    update(true)
                }
            }
            if (parser.parse(context)) {
                mainHandler.post { result(module.launcher.launch(context, params)) }
            } else {
                mainHandler.post { result(null) }
            }
            tasks.remove(module)
            Unit
        }

        if (tasks.contains(module)) {
            return
        }
        threadPool.execute(run)
        tasks[module] = run
    }

    /**
     * 仅仅适用于Launcher为WebViewLauncher的Module
     */
    fun startWebPage(context: Context, module: Module, page: String, params: String = ""): ModuleResult<WebView>? {
        if (module.launcher is WebViewLauncher) {
            return (module.launcher as WebViewLauncher).startWebPage(context, page, params)
        }
        return null
    }

    fun modules(): List<Module> {
        return Module.modules
    }

    fun getAppPath(context: Context): String {
        return "${context.filesDir.absolutePath}${File.separator}modules"
    }

    fun getAppModulePath(context: Context, module: Module): String {
        return "${getAppPath(context)}${File.separator}${module.name}${File.separator}${module.version}"
    }

    fun getAppCachePath(context: Context): String {
        return "${context.filesDir.absolutePath}${File.separator}caches"
    }

    fun getAppModuleCacheFile(context: Context, module: Module): String {
        return "${getAppCachePath(context)}${File.separator}${module.name}_${module.version}.zip"
    }
}