package real.droid.crimson

import android.content.Context

abstract class ModuleLauncher(module: Module) {
    abstract fun launch(context: Context): ModuleResult<*>
}