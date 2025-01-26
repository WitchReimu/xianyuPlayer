package com.example.xianyuplayer.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayFileDao {

    @Insert(PlayFile::class, OnConflictStrategy.IGNORE)
    suspend fun insertPlayFile(playFile: PlayFile): Long

    @Insert(PlayFile::class, OnConflictStrategy.IGNORE)
    suspend fun insertPlayFiles(playFiles: List<PlayFile>): LongArray

    @Delete(PlayFile::class)
    suspend fun deletePlayFile(playFile: PlayFile): Int

    @Delete(PlayFile::class)
    suspend fun deletePlayFiles(playFile: List<PlayFile>): Int

    @Update(PlayFile::class)
    suspend fun updatePlayFile(playFile: PlayFile): Int
    @Update
    suspend fun updatePlayFiles(playFiles: List<PlayFile>): Int

    @Query("select * from play_file")
    fun getPlayList(): Flow<List<PlayFile>>

}
