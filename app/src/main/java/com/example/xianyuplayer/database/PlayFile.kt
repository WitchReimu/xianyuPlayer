package com.example.xianyuplayer.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.example.xianyuplayer.Constant

@Entity(tableName = "play_file", primaryKeys = ["file_path", "file_name"])
data class PlayFile(
    @ColumnInfo(name = "file_path") var filePath: String,
    @ColumnInfo(name = "file_name") var fileName: String,
    @ColumnInfo(name = "last_play_position") var lastPlayPosition: Long,
    @ColumnInfo(name = "last_play_status") var isContinuePlay: Boolean,
    @ColumnInfo(name = "singer") var singer: String = Constant.defaultMetadataInfo,
    @ColumnInfo(name = "albums_name") var albumsName: String = Constant.defaultMetadataInfo,
    @ColumnInfo(name = "song_title") var songTitle: String = Constant.defaultMetadataInfo
) {
}