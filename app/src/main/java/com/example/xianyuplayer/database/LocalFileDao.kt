package com.example.xianyuplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalFileDao {

    @Insert(LocalFile::class, OnConflictStrategy.REPLACE)
    suspend fun insertLocalFile(localFile: LocalFile)

    @Query("select * from local_file")
    fun getAllLocalFiles():Flow<List<LocalFile>>

    @Query("delete from local_file where file_name=:fileName and file_path=:filePath")
    suspend fun deleteSpecialLocalFile(fileName:String,filePath:String)
}