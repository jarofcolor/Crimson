package real.crimson

import android.content.Context

class AssetsResult(private val context: Context, private val module: Module) : ModuleResult<AssetsFilePicker> {

    private val picker = AssetsFilePicker(context, Crimson.getAppModulePath(context, module))
    override fun result(): AssetsFilePicker {
        return picker
    }


    fun getAssetPath(name: String): String {
        return picker.getAssetFilePath(name)
    }
}