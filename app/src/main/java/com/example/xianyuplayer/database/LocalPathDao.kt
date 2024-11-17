package com.example.xianyuplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalPathDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPath(localPath: LocalPath)

    @Query("select * from local_path")
    fun getAllPath(): Flow<List<LocalPath>>

    @Query("delete from local_path where uri=:uri")
    fun deletePath(uri: String)
}