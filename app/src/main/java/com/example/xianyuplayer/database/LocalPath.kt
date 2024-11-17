package com.example.xianyuplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_path")
data class LocalPath(@PrimaryKey @ColumnInfo(name = "uri") public val uri: String) {
}