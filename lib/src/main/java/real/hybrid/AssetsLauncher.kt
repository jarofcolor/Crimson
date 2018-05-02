package real.hybrid

import android.content.Context
import java.io.File

class AssetsLauncher(module: Module) : ModuleLauncher(module) {
    override fun launch(context: Context, params: String): ModuleResult<FilePickerResult> {
        val module = object :ModuleResult<FilePickerResult> {
            override fun result(): FilePickerResult {
                return FilePickerResult(context, File(context.filesDir,"modules${File.separator}").absolutePath)
            }
        }
        return module
    }

}