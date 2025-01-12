package com.example.xianyuplayer

import android.app.Application
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.instance
import com.example.xianyuplayer.database.PlayerRepository
import com.example.xianyuplayer.database.PlayerRoomDatabase

class PlayerApplication : Application() {
    private val database by lazy { PlayerRoomDatabase.getDataBase(this) }
    val repository by lazy {
        PlayerRepository(
            database.getLocalScanPathDao(),
            database.getLocalFileDao(),
            database.getFileSystemPathDao(),
            database.getPlayListDao()
        )
    }

    override fun onCreate() {
        super.onCreate()
        applicationInstance = this
    }

    companion object {
        lateinit var applicationInstance: PlayerApplication
        fun getInstance(): PlayerApplication {
            return applicationInstance
        }
    }
}