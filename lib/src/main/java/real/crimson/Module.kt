package real.crimson

/**
 * [
 *  {"route":"real.xx","name","xx","type":"app","version":1},
 *  ......
 * ]
 */
class Module(val route: String, val name: String, type: String, val version: Int) {
    companion object {
        val modules = arrayListOf<Module>()
        fun register(module: Module) {
            val oldModule = modules.firstOrNull { it.route == module.route }
            if (oldModule != null)
                modules.remove(oldModule)
            modules.add(module)
        }

        fun unregister(module: Module) {
            modules.remove(module)
        }
    }

    var launcher: ModuleLauncher = if (type == "app") WebViewLauncher(this) else AssetsLauncher(this)
    var parser: ModuleParser = ModuleParser(this)
}