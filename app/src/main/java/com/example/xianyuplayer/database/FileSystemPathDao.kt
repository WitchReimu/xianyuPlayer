package com.example.xianyuplayer.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FileSystemPathDao {

    @Insert(FileSystemPath::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbsolutePath(fileSystemPath: FileSystemPath): Long

    @Insert(FileSystemPath::class, onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbsolutePaths(fileSystemPath: List<FileSystemPath>): LongArray

    @Query("select * from file_system_path")
    fun getAllPath(): Flow<List<FileSystemPath>>

    @Delete
    suspend fun deletePath(fileSystemPath: FileSystemPath): Int

    @Delete
    suspend fun deletePaths(fileSystemPaths: List<FileSystemPath>): Int
}