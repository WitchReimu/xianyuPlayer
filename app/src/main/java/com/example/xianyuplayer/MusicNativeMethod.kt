package com.example.xianyuplayer

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.xianyuplayer.database.MusicMetadata

private const val TAG = "MusicNativeMethod"

class MusicNativeMethod {

    private var decodeStreamPtr: Long = 0
    private var playerPtr: Long = 0
    private val handler = Handler(Looper.getMainLooper())

    /**
     * @param filePath 文件路径应为绝对路径
     */
    external fun getMetadata(filePath: String): Array<MusicMetadata>
    private external fun startPlay(playerPtr: Long): Boolean
    private external fun initPlay(streamPtr: Long, playerPtr: Long): Long
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

    fun initPlay() {
        playerPtr = initPlay(decodeStreamPtr, playerPtr)
    }

    fun startPlay(): Boolean {
        return startPlay(playerPtr)
    }

    fun startPlay(absolutePath: String): Boolean {
        openDecodeStream(absolutePath)
        startDecodeStream()
        initPlay()
        return startPlay()
    }

    fun addDtsListener(listener: DtsListener) {
        dtsListeners.add(listener)
    }

    fun removeDtsListener(listener: DtsListener) {
        dtsListeners.remove(listener)
    }

    fun addPlayStateChangeListener(listener: PlayStateChangeListener) {
        playStateChangeListeners.add(listener)
    }

    fun removePlayStateChangeListener(listener: PlayStateChangeListener) {
        playStateChangeListeners.remove(listener)
    }

    fun setMainActivity(mainActivity: MainActivity) {
        mainActivityInstance = mainActivity
    }

    fun destroyRes() {
        mainActivityInstance = null
    }

    companion object {
        private var instance: MusicNativeMethod? = null
        private var mainActivityInstance: MainActivity? = null
        private val dtsListeners = mutableListOf<DtsListener>()
        private val playStateChangeListeners = mutableListOf<PlayStateChangeListener>()

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

        /**
         * native 调用该函数
         * 播放状态发生变化后，c++会调用该函数将播放状态的值赋值给变量形参status
         * @param status 播放状态发生改变后的值
         */
        @JvmStatic
        fun notifyPlayStatusChangeCallback(status: Int) {
            for (playStateChangeListener in playStateChangeListeners) {
                playStateChangeListener.playStatusChangeCallback(status)
            }
        }

        /**
         * c语言调用该函数，执行下一首歌词的功能
         */
        @JvmStatic
        fun nextAudio() {
            if (mainActivityInstance != null) {
                mainActivityInstance!!.runOnUiThread {
                    val globalViewModel = mainActivityInstance!!.globalViewModel

                    if (globalViewModel.playList.isEmpty()) {
                        return@runOnUiThread
                    }
                    var position = globalViewModel.getCurrentPlayFilePosition()

                    if (position == -1) {
                        Log.w(TAG, "nextAudio: playlist is null ")
                        return@runOnUiThread
                    }
                    position = (position + 1) % globalViewModel.playList.size
                    val nextFile = globalViewModel.playList[position]
                    val startPlay =
                        getInstance().startPlay(nextFile.filePath + nextFile.fileName)

                    if (startPlay) {
                        globalViewModel.updateCurrentPlayFile(nextFile)
                    }
                }
            } else {
                Log.w(TAG, "nextAudio: --> main activity is null can't do next audio")
            }
        }

        @JvmStatic
        fun previousAudio() {
            if (mainActivityInstance != null) {
                mainActivityInstance!!.runOnUiThread {
                    val globalViewModel = mainActivityInstance!!.globalViewModel

                    if (globalViewModel.playList.isEmpty()) {
                        return@runOnUiThread
                    }
                    var position = globalViewModel.getCurrentPlayFilePosition()

                    if (position == -1) {
                        Log.w(TAG, "nextAudio: playlist is null ")
                        return@runOnUiThread
                    }
                    val length = globalViewModel.playList.size
                    position = (position - 1 + length) % length
                    val previousFile = globalViewModel.playList[position]
                    val startPlay =
                        getInstance().startPlay(previousFile.filePath + previousFile.fileName)

                    if (startPlay) {
                        globalViewModel.updateCurrentPlayFile(previousFile)
                    }
                }
            } else {
                Log.w(TAG, "previousAudio: --> main activity is null can't do next audio")
            }
        }
    }

    interface DtsListener {
        /**
         * @param 返回解码时间戳dts 或者返回显示时间戳pts
         */
        fun dtsChange(dts: Double)
    }

    interface PlayStateChangeListener {
        fun playStatusChangeCallback(status: Int): Unit
    }

}