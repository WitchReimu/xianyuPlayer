package com.example.xianyuplayer

object Constant {

    const val pathSuffix: String = "/"
    const val prefixRootPath = "/sdcard/"
    const val defaultMetadataInfo = "群星"
    const val globalViewModelKey = "global_viewModel"
    const val singleCircle = "单曲循环"
    const val listCircle = "列表循环"
    const val playStatusUnknown = -1
    const val playStatusUninitialized = 0
    const val playStatusOpen = 2
    const val playStatusStarting = 3
    const val playStatusStarted = 4
    const val playStatusPausing = 5;
    val playListCircle = arrayListOf<String>(listCircle, singleCircle)
    var displayHeightExcludeSystem = 0
    var displayWidthExcludeSystem = 0
    var playStatus = 0
}