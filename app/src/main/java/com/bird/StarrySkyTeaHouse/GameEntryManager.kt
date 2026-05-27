package com.bird.StarrySkyTeaHouse

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

class GameEntryManager(private val context: Context) {
    fun isGameInstalled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    GameEntryContract.PACKAGE_NAME,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(GameEntryContract.PACKAGE_NAME, 0)
            }
            true
        } catch (exception: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openGame(): Boolean {
        val launchIntent = context.packageManager
            .getLaunchIntentForPackage(GameEntryContract.PACKAGE_NAME)
            ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(launchIntent)
        return true
    }

    @Throws(IOException::class, ActivityNotFoundException::class, SecurityException::class)
    fun installBundledGame() {
        val apkUri = copyBundledApkToCache()
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, GameEntryContract.APK_MIME_TYPE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }

    @Throws(IOException::class)
    private fun copyBundledApkToCache(): Uri {
        val installDir = File(context.cacheDir, GameEntryContract.INSTALL_CACHE_DIR)
        if (!installDir.exists() && !installDir.mkdirs()) {
            throw IOException("Cannot create installer cache directory")
        }
        val target = File(installDir, GameEntryContract.ASSET_FILE_NAME)
        context.assets.open(GameEntryContract.ASSET_FILE_NAME).use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        }
        return FileProvider.getUriForFile(
            context,
            GameEntryContract.FILE_PROVIDER_AUTHORITY,
            target
        )
    }
}
