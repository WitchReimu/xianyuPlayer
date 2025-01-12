package com.example.xianyuplayer.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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

    @Query("select * from play_file")
    fun getPlayList(): Flow<List<PlayFile>>

    @Query("select * from local_file inner join play_file on local_file.file_path=play_file.file_path and local_file.file_name=play_file.file_name")
    fun getLocalFileJoinPlayList(): Flow<List<LocalFile>>

}
