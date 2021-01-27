package real.droid.crimson

import android.content.Context
import java.io.File

class AssetsFilePicker(context: Context, path: String) {
    private val root = File(path)

    fun getAssetFilePath(name:String):String{
         return File(root,name).absolutePath
    }
}