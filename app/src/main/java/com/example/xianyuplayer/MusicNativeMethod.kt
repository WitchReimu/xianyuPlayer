package com.example.xianyuplayer

import com.example.xianyuplayer.database.MusicMetadata

private const val TAG = "MusicNativeMethod"

class MusicNativeMethod {

    private var decodeStreamPtr: Long = 0
    private var playerPtr: Long = 0

    /**
     * @param filePath 文件路径应为绝对路径
     */
    external fun getMetadata(filePath: String): Array<MusicMetadata>
    private external fun startPlay(playerPtr: Long): Boolean
    private external fun initPlay(streamPtr: Long, activity: MainActivity, playerPtr: Long): Long
    private external fun openDecodeStream(path: String, streamPtr: Long, playerPtr: Long): Long
    private external fun startDecodeStream(streamPtr: Long)
    external fun getAudioAlbum(
        streamPtr: Long = decodeStreamPtr,
        absolutePath: String
    ): ByteArray

    external fun getPlayStatus(ptr: Long = playerPtr): Int
    external fun pausePlay(ptr: Long = playerPtr): Boolean
    external fun seekPosition(position: Long, streamPtr: Long = decodeStreamPtr): Boolean
    external fun getAudioDuration(streamPtr: Long = decodeStreamPtr): Long

    fun openDecodeStream(path: String) {
        decodeStreamPtr = openDecodeStream(path, decodeStreamPtr, playerPtr)
    }

    fun startDecodeStream() {
        startDecodeStream(decodeStreamPtr)
    }

    fun initPlay(mainActivity: MainActivity) {
        playerPtr = initPlay(decodeStreamPtr, mainActivity, playerPtr)
    }

    fun startPlay(): Boolean {
        return startPlay(playerPtr)
    }

    fun addDtsListener(listener: DtsListener) {
        dtsListeners.add(listener)
    }

    fun removeDtsListener(listener: DtsListener) {
        dtsListeners.remove(listener)
    }

    companion object {
        private var instance: MusicNativeMethod? = null
        private val dtsListeners = mutableListOf<DtsListener>()

        fun getInstance(): MusicNativeMethod {

            if (instance == null) {
                synchronized(this) {
                    if (instance == null) {
                        val instance = MusicNativeMethod()
                        this.instance = instance
                        return instance
                    }
                }
            }
            return instance!!
        }

        @JvmStatic
        fun notifyDtsChange(dts: Double) {
            for (dtsListener in dtsListeners) {
                dtsListener.dtsChange(dts)
            }
        }
    }

    interface DtsListener {
        fun dtsChange(dts: Double)
    }

}