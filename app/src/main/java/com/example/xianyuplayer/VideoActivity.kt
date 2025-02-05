package com.example.xianyuplayer

import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.xianyuplayer.databinding.ActivityVideoBinding
import com.example.xianyuplayer.vm.VideoViewModel

class VideoActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private val TAG = "VideoActivity"
    private lateinit var binding: ActivityVideoBinding
    private lateinit var viewModel: VideoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        hideSystemUi()
        setContentView(binding.root)
        viewModel = ViewModelProvider.create(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        )[TAG, VideoViewModel::class]
        binding.surfaceVideo.holder.addCallback(this)
        viewModel.initCallback()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR
    }

    override fun onStart() {
        super.onStart()
        viewModel.videoStatusLiveData.observe(this) {
            Log.i(TAG, "onStart: --> Status ")
            playStatusChangeCallback(it)
        }

        viewModel.renderResolutionLiveData.observe(this) {
            Log.i(TAG, "onStart: --> Resolution ")
            if (it.width < 0 || it.height < 0)
                return@observe
            renderVideoResolution(it.width, it.height)
        }
    }

    override fun onStop() {
        super.onStop()
        viewModel.reset()
        MusicNativeMethod.getInstance().setVideoState(Constant.playStatusPausing)
    }

    private fun hideSystemUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val insetsController = window.decorView.windowInsetsController
            insetsController!!.hide(WindowInsets.Type.systemBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (viewModel.ensureResolution) {

        } else {
            MusicNativeMethod.getInstance()
                .initNativeWindow("/sdcard/Download/Tifa_Morning_Cowgirl_4K.mp4", holder.surface)
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        if (viewModel.ensureResolution && viewModel.videoStatus == Constant.playStatusPausing) {
            MusicNativeMethod.getInstance()
                .screenOrientationChange(holder.surface)
            viewModel.videoStatus = 0
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    private fun renderVideoResolution(width: Int, height: Int) {
        val layoutParams = binding.surfaceVideo.layoutParams
        layoutParams.width = width
        layoutParams.height = height
        binding.surfaceVideo.layoutParams = layoutParams
        viewModel.ensureResolution = true
    }

    private fun playStatusChangeCallback(status: Int) {
        viewModel.videoStatus = status

        if (status == Constant.playStatusOpen) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            MusicNativeMethod.getInstance().playVideo()
        }
    }

}