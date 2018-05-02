package real.hybrid

/**
 * [
 *  {"route":"real.xx","name","xx","type":"app","version":1},
 *  ......
 * ]
 */
class Module(val route: String, val name: String, val type: String,val version:Int) {
    companion object {
        val modules = arrayListOf<Module>()
        fun register(module: Module) {
            modules.add(module)
        }

        fun unregister(module: Module) {
            modules.remove(module)
        }
    }

    var launcher: ModuleLauncher? = null
    var parser:ModuleParser? = null
}