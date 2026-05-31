package jp.deadend.noname.skk

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.PrintWriter
import java.lang.Thread.UncaughtExceptionHandler
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build

internal class MyUncaughtExceptionHandler(val context: Context) : UncaughtExceptionHandler {
    private val mDefaultUEH: UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()
    private val mVersionName: String

    init {
        val packInfo: PackageInfo? = try {
            context.packageManager.getPackageInfo(context.packageName, 0)
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
            dlog("MyUncaughtExceptionHandler: name not found")
            null
        }

        mVersionName = packInfo?.versionName ?: "unknown"
    }

    override fun uncaughtException(th: Thread, t: Throwable) {
        try {
            saveState(t)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }

        mDefaultUEH?.uncaughtException(th, t)
    }

    @Throws(FileNotFoundException::class)
    private fun saveState(e: Throwable) {
        val dir = context.getExternalFilesDir(null) ?: return
        val file = File(dir, context.getString(R.string.strace_file_name))
        val pw = PrintWriter(FileOutputStream(file))

        pw.println("以下は、AndroidTUTCodeが強制終了したときの情報です。")
        pw.println("内容を作者に連絡していただくと、問題の修正に役立つかもしれません。")
        pw.println()
        pw.println("Device:  " + Build.DEVICE)
        pw.println("Model:   " + Build.MODEL)
        pw.println("SDK:     " + Build.VERSION.SDK_INT)
        pw.println("Version: " + mVersionName)
        pw.println()

        e.printStackTrace(pw)
        pw.close()
    }
}
