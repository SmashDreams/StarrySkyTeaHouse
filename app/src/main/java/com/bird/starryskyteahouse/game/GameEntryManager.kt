package com.bird.starryskyteahouse.game

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException

class GameEntryManager(private val mContext: Context) {
    fun isGameInstalled(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                mContext.packageManager.getPackageInfo(
                    GameEntryContract.PACKAGE_NAME,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                mContext.packageManager.getPackageInfo(GameEntryContract.PACKAGE_NAME, 0)
            }
            true
        } catch (exception: PackageManager.NameNotFoundException) {
            false
        }
    }

    fun openGame(): Boolean {
        val launchIntent = mContext.packageManager
            .getLaunchIntentForPackage(GameEntryContract.PACKAGE_NAME)
            ?: return false
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        mContext.startActivity(launchIntent)
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
        mContext.startActivity(installIntent)
    }

    @Throws(IOException::class)
    private fun copyBundledApkToCache(): Uri {
        val installDir = File(mContext.cacheDir, GameEntryContract.INSTALL_CACHE_DIR)
        if (!installDir.exists() && !installDir.mkdirs()) {
            throw IOException("Cannot create installer cache directory")
        }
        val target = File(installDir, GameEntryContract.ASSET_FILE_NAME)
        if (!target.exists()) {
            mContext.assets.open(GameEntryContract.ASSET_FILE_NAME).use { input ->
                target.outputStream().use { output -> input.copyTo(output) }
            }
        }
        return FileProvider.getUriForFile(
            mContext,
            GameEntryContract.FILE_PROVIDER_AUTHORITY,
            target
        )
    }
}
