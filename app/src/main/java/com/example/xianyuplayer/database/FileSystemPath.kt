package com.example.xianyuplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "file_system_path")
data class FileSystemPath(@PrimaryKey @ColumnInfo(name = "absolute_path") val absolutePath: String) {

}