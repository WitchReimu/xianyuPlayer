package com.example.xianyuplayer

import android.app.Application
import com.example.xianyuplayer.database.PlayerRepository
import com.example.xianyuplayer.database.PlayerRoomDatabase

class PlayerApplication : Application() {
    private val database by lazy { PlayerRoomDatabase.getDataBase(this) }
    val repository by lazy { PlayerRepository(database.getLocalScanPathDao()) }
}