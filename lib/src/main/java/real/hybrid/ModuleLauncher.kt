package real.hybrid

import android.content.Context

abstract class ModuleLauncher(module: Module) {
    abstract fun launch(context: Context, params: String): ModuleResult<*>
}