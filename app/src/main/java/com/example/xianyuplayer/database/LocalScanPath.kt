package com.example.xianyuplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "local_scan_path")
data class LocalScanPath(@PrimaryKey @ColumnInfo(name = "uri") public val uri: String) {
}