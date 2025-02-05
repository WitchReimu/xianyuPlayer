package com.example.xianyuplayer.vm

import android.util.Log
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.xianyuplayer.Constant
import com.example.xianyuplayer.MusicNativeMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VideoViewModel : ViewModel(), MusicNativeMethod.VideoResolutionListener,
    MusicNativeMethod.PlayStateChangeListener {
    private val TAG = "VideoViewModel"

    var ensureResolution = false
    var videoStatus = Constant.playStatusUninitialized
    var videoStatusLiveData = MutableLiveData<Int>(Constant.playStatus)
    var renderResolutionLiveData = MutableLiveData<VideoRenderResolution>(
        VideoRenderResolution(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    )

    fun initCallback() {
        if (!ensureResolution){
            MusicNativeMethod.getInstance().addVideoResolutionListener(this)
            MusicNativeMethod.getInstance().addPlayStateChangeListener(this)
        }
    }

    fun reset() {
        renderResolutionLiveData.value = VideoRenderResolution(-1, -1)
    }

    override fun playStatusChangeCallback(status: Int) {
        viewModelScope.launch(Dispatchers.Main) {
            videoStatusLiveData.value = status
        }
    }

    override fun renderVideoResolution(width: Int, height: Int) {
        renderResolutionLiveData.value = VideoRenderResolution(width, height)
    }

    override fun onCleared() {
        MusicNativeMethod.getInstance().removeVideoResolutionListener(this)
        MusicNativeMethod.getInstance().removePlayStateChangeListener(this)
    }

    inner class VideoRenderResolution(val width: Int, val height: Int)
}