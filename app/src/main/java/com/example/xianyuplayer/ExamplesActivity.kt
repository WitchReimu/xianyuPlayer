package com.example.xianyuplayer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.xianyuplayer.databinding.ActivityExamplesBinding
import com.example.xianyuplayer.notify_service.ExamplesService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.LinkedList

class ExamplesActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = "ExamplesActivity"

    //1MB  以bit为单位
    private val MB = 8388608
    private val coroutine = CoroutineScope(Dispatchers.Main)
    private val mediaProjectionIntent = 1
    private val keepCaptureScreenData = true
    private lateinit var binding: ActivityExamplesBinding
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var videoEncodeMediaCodec: MediaCodec
    private val exampleForegroundServiceIntent by lazy { Intent(this, ExamplesService::class.java) }
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var mediacodecInputSurface: Surface? = null

    private val imageReader by lazy {
        val displayMetrics = resources.displayMetrics
        ImageReader.newInstance(
            displayMetrics.widthPixels,
            displayMetrics.heightPixels,
            PixelFormat.RGBA_8888,
            3
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExamplesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnEncodeTest.setOnClickListener(this)
        binding.btnRequestMediaProject.setOnClickListener(this)
        binding.btnStartRecord.setOnClickListener(this)
        binding.btnStartCapture.setOnClickListener(this)
        binding.btnMediacodecEncode.setOnClickListener(this)
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onResume() {
        super.onResume()
        startForegroundService(exampleForegroundServiceIntent)
    }

    override fun onStop() {
        super.onStop()
        mediaRecorder?.apply {
            release()
        }
        virtualDisplay?.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoEncodeMediaCodec.stop()
        videoEncodeMediaCodec.release()
        stopService(exampleForegroundServiceIntent)
        MusicNativeMethod.getInstance().testEncodeStop()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == mediaProjectionIntent && resultCode == Activity.RESULT_OK) {
            data?.apply {
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, this)
            }
        }
    }

    private fun setImageReaderListener() {
        imageReader.setOnImageAvailableListener({
            val acquireLatestImage = it.acquireLatestImage()
            acquireLatestImage?.apply {
                val plane = acquireLatestImage.planes[0]
                val buffer = plane.buffer
                val createBitmap = Bitmap.createBitmap(
                    acquireLatestImage.width,
                    acquireLatestImage.height,
                    Bitmap.Config.ARGB_8888
                )
                createBitmap.copyPixelsFromBuffer(buffer)
                val lockCanvas = binding.surfaceView.holder.lockCanvas()

                if (lockCanvas != null) {
                    lockCanvas.drawBitmap(createBitmap, 0f, 0f, null)
                    binding.surfaceView.holder.unlockCanvasAndPost(lockCanvas)
                }

                if (!keepCaptureScreenData) {
                    mediaRecorder?.release()
                    mediaProjection?.stop()
                }
                this.close()
            }

            if (!keepCaptureScreenData) {
                it.close()
            }
        }, null)
    }

    private fun createVirtualDisplay(surface: Surface) {
        val displayMetrics = resources.displayMetrics

        virtualDisplay = mediaProjection!!.createVirtualDisplay(
            TAG,
            displayMetrics.widthPixels,
            displayMetrics.heightPixels,
            displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            surface,
            null,
            null
        )
        //开始录屏
//        mediaRecorder?.start()
    }

    private fun createVideoMediaCodec() {

        if (!::videoEncodeMediaCodec.isInitialized) {
            val displayMetrics = resources.displayMetrics
            val videoFormat = MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC,
                displayMetrics.widthPixels,
                displayMetrics.heightPixels
            )
            videoFormat.setInteger(MediaFormat.KEY_BIT_RATE, MB * 1)
            videoFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            videoFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3)
            videoFormat.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            videoEncodeMediaCodec =
                MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            videoEncodeMediaCodec.configure(
                videoFormat,
                null,
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )

            videoEncodeMediaCodec.setCallback(object : MediaCodec.Callback() {

                override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {
                    Log.i(TAG, "onInputBufferAvailable: --> ")
                }

                override fun onOutputBufferAvailable(
                    codec: MediaCodec,
                    index: Int,
                    info: MediaCodec.BufferInfo
                ) {

                    codec.releaseOutputBuffer(index, false)
                }

                override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {

                }

                override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {

                }

            })
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            // 将音频流与数据流编码为文件 示例按钮
            binding.btnEncodeTest.id -> {
                coroutine.launch(Dispatchers.IO) {
                    MusicNativeMethod.getInstance().testEncode()
                }
            }

            binding.btnRequestMediaProject.id -> {
                val createScreenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent()
                startActivityForResult(createScreenCaptureIntent, mediaProjectionIntent)

                createVideoMediaCodec()
            }

            binding.btnStartRecord.id -> {
                mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    MediaRecorder(this)
                } else {
                    MediaRecorder()
                }

                if (mediaProjection != null) {
                    mediaRecorder!!.apply {
                        setImageReaderListener()
                        //设置录屏参数需要按照特定顺序执行
                        setAudioSource(MediaRecorder.AudioSource.DEFAULT)
                        setVideoSource(MediaRecorder.VideoSource.SURFACE)
                        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                        setAudioSamplingRate(48000)
                        setAudioChannels(2)
                        setVideoSize(binding.root.width, binding.root.height)
                        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                        setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                        setVideoFrameRate(60)
                        setVideoEncodingBitRate(2 * 1024 * 1024)
                        setOutputFile(externalCacheDir!!.path + "/dev/null")
                        prepare()
                    }
                    createVirtualDisplay(imageReader.surface)
                }
            }

            binding.btnStartCapture.id -> {
                mediaProjection?.apply {
                    setImageReaderListener()
                    createVirtualDisplay(imageReader.surface)
                }
            }

            binding.btnMediacodecEncode.id -> {
                videoEncodeMediaCodec.apply {
                    mediacodecInputSurface = videoEncodeMediaCodec.createInputSurface()
                    createVirtualDisplay(mediacodecInputSurface!!)
                    videoEncodeMediaCodec.start()
                }
            }
        }
    }
}