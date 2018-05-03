package real.hybrid

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
            modules.add(module)
        }

        fun unregister(module: Module) {
            modules.remove(module)
        }
    }

    var launcher: ModuleLauncher = if (type == "app") WebViewLauncher(this) else AssetsLauncher(this)
    var parser: ModuleParser = ModuleParser(this)
}