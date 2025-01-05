package com.example.xianyuplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


/**
 * @param uri 参数uri不一定是Treeuri
 */
@Entity(tableName = "local_scan_path")
data class LocalScanPath(
    @PrimaryKey @ColumnInfo(name = "absolute_path") val absolutePath: String,
    @ColumnInfo(name = "uri") val uri: String,
    @ColumnInfo(name = "mask") var mask: Boolean = false
) {
}