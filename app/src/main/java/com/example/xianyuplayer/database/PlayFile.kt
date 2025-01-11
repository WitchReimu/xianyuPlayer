package com.example.xianyuplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity(tableName = "play_file", primaryKeys = ["file_path", "file_name"])
data class PlayFile(
    @ColumnInfo(name = "file_path") var filePath: String,
    @ColumnInfo(name = "file_name") var fileName: String
) {
}