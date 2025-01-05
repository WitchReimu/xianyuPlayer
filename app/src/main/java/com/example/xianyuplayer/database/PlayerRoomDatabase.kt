package com.example.xianyuplayer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlin.concurrent.Volatile

@Database(entities = [LocalScanPath::class, LocalFile::class, FileSystemPath::class], version = 1)
abstract class PlayerRoomDatabase : RoomDatabase() {

    abstract fun getLocalScanPathDao(): LocalScanPathDao
    abstract fun getLocalFileDao(): LocalFileDao
    abstract fun getFileSystemPathDao(): FileSystemPathDao

    companion object {
        @Volatile
        private var INSTANCE: PlayerRoomDatabase? = null

        fun getDataBase(context: Context): PlayerRoomDatabase {

            if (INSTANCE == null) {
                synchronized(this) {
                    val instance = Room.databaseBuilder(
                        context,
                        PlayerRoomDatabase::class.java,
                        "player_database"
                    ).build()
                    INSTANCE = instance
                    return instance
                }
            }
            return INSTANCE!!
        }
    }

}