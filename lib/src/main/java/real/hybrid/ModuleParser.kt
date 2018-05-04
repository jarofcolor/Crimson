package real.hybrid

import android.content.Context
import android.net.Uri
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream


open class ModuleParser(private val module: Module) {
    //可实现自己的更新方法
    open fun update(context: Context, updateUrl: String): Boolean {
        try {
            val connection = URL(updateUrl).openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            //连接
            connection.connect()
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val ins = connection.inputStream
                val cacheFile = File(RealHybrid.getAppModuleCacheFile(context, module))
                if (!cacheFile.parentFile.exists()) {
                    cacheFile.parentFile.mkdirs()
                }
                val out = FileOutputStream(cacheFile)
                val array = ByteArray(4 * 1024)
                var len = 0
                while (ins.read(array).also { len = it } != -1) {
                    out.write(array, 0, len)
                }
                ins.close()
                out.close()
                return true
            }
        } catch (e: Exception) {
        }
        return true
    }

    //可实现自己的解析方法
    open fun parse(context: Context): Boolean {
        val assets = context.assets
        val list = assets.list("modules")
        val zipName = list.firstOrNull { it == "${module.name}_${module.version}.zip" }
        val zipNameSplit = zipName?.split("_")

        if (zipNameSplit == null || zipNameSplit.size != 2) {
            return false
        }

        val appDir = File(RealHybrid.getAppPath(context), "${module.name}${File.separator}${module.version}")

        val destPathCacheFile = File(RealHybrid.getAppCachePath(context), zipName)
        if (!destPathCacheFile.exists()) {
            val ins = assets.open("modules/$zipName")
            if (!destPathCacheFile.parentFile.exists()) {
                destPathCacheFile.parentFile.mkdirs()
            }
            if (!copyTargetToCache(ins, destPathCacheFile)) {
                destPathCacheFile.delete()
            }
        }

        if (!unzip(destPathCacheFile, appDir)) {
            return false
        }

        return true
    }


    private fun copyTargetToCache(ins: InputStream, destPathCacheFile: File): Boolean {
        return try {
            val out = FileOutputStream(destPathCacheFile)
            val array = ByteArray(4 * 1028)
            var len = 0
            while (ins.read(array).also { len = it } != -1) {
                out.write(array,0,len)
            }
            out.close()
            ins.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun unzip(src: File, destPathDir: File): Boolean {
        return try {
            val zipFile = ZipFile(src)
            val zipIn = ZipInputStream(FileInputStream(src))
            var zipEntry: ZipEntry? = null
            while (zipIn.nextEntry.let { zipEntry = it;it != null }) {
                val outFile = File(destPathDir, zipEntry!!.name)

                if(zipEntry!!.isDirectory){
                    outFile.mkdirs()
                    continue
                }
                if (!outFile.parentFile.exists()) {
                    outFile.parentFile.mkdirs()
                }

                val ins = zipFile.getInputStream(zipEntry)
                if (outFile.exists()) {
                    //Todo 是否有更好更快的方法解决解压文件是否一致而无需重新解压？
                    if (outFile.length().toInt() == ins.available()) {
                        continue
                    }else {
                        outFile.delete()
                    }
                }
                val out = FileOutputStream(outFile)
                val array = ByteArray(4 * 1024)
                var len = 0
                while (ins.read(array).also { len = it } != -1) {
                    out.write(array,0,len)
                }
                ins.close()
                out.close()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun deleteDir(dir: File) {
        if (dir.isFile) {
            dir.delete()
            return
        }
        dir.listFiles().forEach {
            deleteDir(dir)
        }
        dir.delete()
    }
}