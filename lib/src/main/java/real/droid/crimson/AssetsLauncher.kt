package real.droid.crimson

import android.content.Context

class AssetsLauncher(private val module: Module) : ModuleLauncher(module) {
    override fun launch(context: Context): ModuleResult<AssetsFilePicker> {
        return AssetsResult(context, module)
    }
}