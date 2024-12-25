package com.example.xianyuplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "local_file", primaryKeys = ["file_path", "file_name"])
data class LocalFile(
    @ColumnInfo(name = "file_path")
    var filePath: String,
    @ColumnInfo(name = "file_name")
    var fileName: String,
    @ColumnInfo(name = "singer")
    var singer: String = "群星",
    @ColumnInfo(name = "albums_name")
    var albumsName: String = "群星",
    @ColumnInfo(name = "song_title")
    var songTitle: String = "群星"
) {
}