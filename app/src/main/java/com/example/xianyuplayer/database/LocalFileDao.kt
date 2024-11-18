package com.example.xianyuplayer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface LocalFileDao {

    @Insert(LocalFile::class, OnConflictStrategy.REPLACE)
    suspend fun insertLocalFile(localFile: LocalFile)
}