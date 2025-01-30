package com.example.xianyuplayer

import android.os.Build
import android.os.Bundle
import android.view.SurfaceHolder
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.example.xianyuplayer.databinding.ActivityVideoBinding

class VideoActivity : AppCompatActivity(), SurfaceHolder.Callback {

    private lateinit var binding: ActivityVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        hideSystemUi()
        setContentView(binding.root)
        binding.surfaceVideo.holder.addCallback(this)
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
        MusicNativeMethod.getInstance().initNativeWindow("/sdcard/Download/Tifa_Morning_Cowgirl_4K.mp4", holder.surface)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        MusicNativeMethod.getInstance().playVideo()
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

}