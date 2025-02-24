package com.example.xianyuplayer

import android.content.res.AssetManager
import android.util.Log
import android.view.Surface
import com.example.xianyuplayer.database.MusicMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private const val TAG = "MusicNativeMethod"

class MusicNativeMethod {

    //音频解码指针
    private var decodeStreamPtr: Long = 0

    //音频播放器指针
    private var playerPtr: Long = 0

    //视频播放器与解码指针
    private var nativeWindowPtr: Long = 0
    private var hwPlayerPtr = 0L

    /**
     * @param filePath 文件路径应为绝对路径
     */
    external fun getMetadata(filePath: String): Array<MusicMetadata>
    private external fun startPlay(playerPtr: Long): Boolean
    private external fun initPlay(streamPtr: Long, playerPtr: Long): Long
    private external fun openDecodeStream(path: String, streamPtr: Long, playerPtr: Long): Long
    private external fun startDecodeStream(streamPtr: Long)
    private external fun initVideo(
        absolutePath: String,
        surface: Surface,
        streamPtr: Long = decodeStreamPtr,
        ptr: Long = playerPtr
    ): LongArray

    private external fun hwVideoStreamInit(locationPath: String, surface: Surface): Long

    external fun getAudioAlbum(
        streamPtr: Long = decodeStreamPtr,
        absolutePath: String
    ): ByteArray

    external fun getPlayStatus(ptr: Long = playerPtr): Int
    external fun pausePlay(ptr: Long = playerPtr): Boolean
    external fun seekPosition(position: Long, streamPtr: Long = decodeStreamPtr): Boolean
    external fun getAudioDuration(streamPtr: Long = decodeStreamPtr): Long
    external fun setPlayCircleType(playType: String, ptr: Long = playerPtr)
    external fun playVideo(
        windowPtr: Long = nativeWindowPtr,
        streamPtr: Long = decodeStreamPtr,
        ptr: Long = playerPtr
    )

    external fun setVideoState(state: Int, windowPtr: Long = nativeWindowPtr)
    external fun screenOrientationChange(surface: Surface, windowPtr: Long = nativeWindowPtr)
    external fun hwVideoStartPlay(playerPtr: Long = hwPlayerPtr)
    external fun hwVideoStartPlayTest(
        surface: Surface,
        locationPath: String
    )

    fun initHwVideoStream(locationPath: String, surface: Surface) {
        hwPlayerPtr = hwVideoStreamInit(locationPath, surface)
    }

    fun openDecodeStream(path: String) {
        decodeStreamPtr = openDecodeStream(path, decodeStreamPtr, playerPtr)
    }

    fun startDecodeStream() {
        startDecodeStream(decodeStreamPtr)
    }

    fun initPlay() {
        playerPtr = initPlay(decodeStreamPtr, playerPtr)
    }

    fun initNativeWindow(path: String, surface: Surface) {
        val resultArray = initVideo(path, surface)
        nativeWindowPtr = resultArray[0]
        decodeStreamPtr = resultArray[1]
        playerPtr = resultArray[2]
    }

    /**
     * 播放当前缓冲区内的音乐文件
     */
    fun startPlay(): Boolean {
        return startPlay(playerPtr)
    }

    /**
     * 播放指定路径的音乐文件
     */
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

    /**
     * native 回调函数
     * @param width 视频渲染宽度 单位像素
     * @param height 视频渲染高度 单位像素
     */
    fun notifyVideoResolution(width: Int, height: Int) {
        for (videoResolutionListener in videoResolutionListeners) {
            videoResolutionListener.renderVideoResolution(width, height)
        }
    }

    fun addVideoResolutionListener(listener: VideoResolutionListener) {
        videoResolutionListeners.add(listener)
    }

    fun removeVideoResolutionListener(listener: VideoResolutionListener) {
        videoResolutionListeners.remove(listener)
    }

    /**
     * c++语言调用该此方法，在音乐播放结束后请求重复播放当前音乐文件
     */
    fun requestRestartAudioFile() {
        if (mainActivityInstance != null) {
            mainActivityInstance!!.runOnUiThread {
                val currentPlayFile = mainActivityInstance!!.globalViewModel.currentPlayFile
                if (currentPlayFile != null) {
                    startPlay(currentPlayFile.filePath + currentPlayFile.fileName)
                } else {
                    Log.w(TAG, "requestRestartAudioFile: --> current play file is null ")
                }
            }
        }
    }

    companion object {
        private var instance: MusicNativeMethod? = null
        private var mainActivityInstance: MainActivity? = null
        private val dtsListeners = mutableListOf<DtsListener>()
        private val playStateChangeListeners = mutableListOf<PlayStateChangeListener>()
        private val videoResolutionListeners = mutableListOf<VideoResolutionListener>()
        private val mainScope = CoroutineScope(Dispatchers.Main)

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

        /**
         * @param dts
         * 返回解码时间戳dts 或者返回显示时间戳pts
         */
        @JvmStatic
        fun notifyDtsChange(dts: Double) {
            for (dtsListener in dtsListeners) {
                dtsListener.dtsChange(dts)
            }
        }

        /**
         * @param status 播放状态发生改变后的值
         * native 调用该函数
         * 播放状态发生变化后，c++会调用该函数将播放状态的值赋值给变量形参status
         */
        @JvmStatic
        fun notifyPlayStatusChangeCallback(status: Int) {
            mainScope.launch {
                for (playStateChangeListener in playStateChangeListeners) {
                    playStateChangeListener.playStatusChangeCallback(status)
                }
            }
        }

        /**
         * 通知当前播放的音乐文件发生改变
         */
        fun notifyPlayFileChange() {
            // TODO: 通知当前播放的音乐文件发生改变
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
        fun dtsChange(dts: Double)
    }

    interface PlayStateChangeListener {
        fun playStatusChangeCallback(status: Int): Unit
    }

    interface VideoResolutionListener {
        fun renderVideoResolution(width: Int, height: Int)
    }

}