package com.example.xianyuplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalScanPathDao {

    @Insert(LocalScanPath::class, OnConflictStrategy.IGNORE)
    suspend fun insertPath(localScanPath: LocalScanPath)

    @Insert(LocalScanPath::class, onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPath(localScanPath: List<LocalScanPath>): LongArray

    @Update
    suspend fun updatePath(localScanPath: LocalScanPath): Int

    @Update
    suspend fun updatePaths(scanPaths: List<LocalScanPath>): Int

    @Query("select * from local_scan_path")
    fun getAllPath(): Flow<List<LocalScanPath>>

    @Query("delete from local_scan_path where uri=:uri")
    suspend fun deletePath(uri: String)
}