package com.example.xianyuplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalScanPathDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPath(localScanPath: LocalScanPath)

    @Query("select * from local_scan_path")
    fun getAllPath(): Flow<List<LocalScanPath>>

    @Query("delete from local_scan_path where uri=:uri")
    fun deletePath(uri: String)
}