package com.example.xianyuplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "local_file", primaryKeys = ["file_path", "file_name"])
data class LocalFile(
    @ColumnInfo(name = "file_path")
    val filePath: String,
    @ColumnInfo(name = "file_name")
    val fileName: String,
    @ColumnInfo(name = "singer", defaultValue = "群星")
    val singer: String,
    @ColumnInfo(name = "albums_name", defaultValue = "群星")
    val albumsName: String
) {
}