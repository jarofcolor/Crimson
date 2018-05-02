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
    private val updates = ConcurrentHashMap<Module, Boolean>()
    private val threadPool = Executors.newFixedThreadPool(10)
    private val mainHandler = Handler(Looper.getMainLooper())
    fun registerModule(module: Module) {
        Module.register(module)
    }

    fun unregisterModule(module: Module) {
        Module.unregister(module)
    }

    fun setUpdate(module: Module, update: Boolean = false) {
        updates[module] = update
    }

    fun startModule(context: Context, url: String, params: String = "", call: (ModuleResult<*>?) -> Unit) {
        val module = modules().find { it.route == url }
        if (module == null) {
            call(null)
            return
        }
        val parser = module.parser
        val run = {
            if (parser?.update() == true) {
                setUpdate(module, false)
            }
            if (parser?.parse(context) == true) {
                mainHandler.post { module.launcher?.launch(context, params) }
            } else {
                mainHandler.post { call(null) }
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

    fun getAppCachePath(context: Context): String {
        return "${context.filesDir.absolutePath}${File.separator}caches"
    }
}