package real.crimson

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebView
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

object Crimson {

    private val tasks = ConcurrentHashMap<Module, () -> Unit>()
    private val updates = ConcurrentHashMap<Module, String>()
    private val threadPool = Executors.newCachedThreadPool()
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
     * @param [context] context object
     * @param [module]
     * @param [update] 更新地址，为空时设置 null
     */
    fun setUpdate(context: Context, module: Module, update: String?) {
        if (update == null)
            updates.remove(module)
        else
            updates[module] = update
    }

    /**
     * 一次性初始化解析所有模块
     *  @param [call] 返回所有初始化成功的模块
     */
    fun parseModules(context: Context, call: (ArrayList<Module>) -> Unit) {
        val run = {
            val loadedModules = arrayListOf<Module>()
            modules().forEach { module ->
                val parser = module.parser
                val updateUrl = updates[module]
                if (updateUrl != null && parser.update(context, updateUrl)) {
                    setUpdate(context, module, null)
                }
                if (parser.parse(context)) {
                    loadedModules.add(module)
                }
            }
            mainHandler.post {
                call(loadedModules)
            }

            Unit
        }

        threadPool.execute(run)
    }

    /**
     * 模块已经加载好的启动方式,需要先调用parseModules
     */
    fun startModule(context: Context, route: String): ModuleResult<*>? {
        val module = modules().find { it.route == route } ?: return null
        return module.launcher.launch(context)
    }

    /**
     * 每个模块单独更新的启动方式
     */
    fun startModule(context: Context, route: String, update: (Boolean) -> Unit, result: (ModuleResult<*>?) -> Unit) {
        val module = modules().find { it.route == route }
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
                setUpdate(context, module, null)
            }
            if (parser.parse(context)) {
                mainHandler.post { result(module.launcher.launch(context)) }
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
    fun startPage(context: Context, module: Module, page: String): ModuleResult<WebView>? {
        if (module.launcher is WebViewLauncher) {
            return (module.launcher as WebViewLauncher).startPage(context, page)
        }
        return null
    }

    fun startWebPage(context: Context, url: String): ModuleResult<WebView>? {
        return WebViewResult(context, null, url, false)
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