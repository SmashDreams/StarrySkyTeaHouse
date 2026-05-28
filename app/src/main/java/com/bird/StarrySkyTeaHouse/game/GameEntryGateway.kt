package com.bird.StarrySkyTeaHouse.game

import android.content.ActivityNotFoundException
import java.io.IOException

interface GameEntryGateway {
    fun isGameInstalled(): Boolean
    fun openGame(): Boolean
    fun installBundledGame(): GameInstallResult
}

class AndroidGameEntryGateway(private val mGameEntryManager: GameEntryManager) : GameEntryGateway {
    override fun isGameInstalled(): Boolean = mGameEntryManager.isGameInstalled()
    override fun openGame(): Boolean = mGameEntryManager.openGame()
    override fun installBundledGame(): GameInstallResult {
        return try {
            mGameEntryManager.installBundledGame()
            GameInstallResult.Started
        } catch (exception: IOException) {
            GameInstallResult.AssetMissing
        } catch (exception: ActivityNotFoundException) {
            GameInstallResult.NoHandler
        } catch (exception: SecurityException) {
            GameInstallResult.PermissionFailed
        }
    }
}

enum class GameInstallResult {
    Started,
    AssetMissing,
    NoHandler,
    PermissionFailed
}
