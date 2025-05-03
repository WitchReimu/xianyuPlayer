package com.example.xianyuplayer

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image.Plane
import android.media.ImageReader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.xianyuplayer.databinding.ActivityLiveStreamBinding
import com.example.xianyuplayer.notify_service.LiveStreamForegroundService
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.nio.ByteBuffer


private const val TAG = "LiveStreamActivity"
private const val rtspPushTestUrl = "rtsp://192.168.1.9:8554/test_video"

//只考虑消费端速度大于或等于生产端的情况
class LiveStreamActivity : AppCompatActivity(), SurfaceHolder.Callback,
    ImageReader.OnImageAvailableListener, View.OnClickListener {

    //1MB  以bit为单位
    private val MB = 8388608
    private lateinit var binding: ActivityLiveStreamBinding
    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var imageReader: ImageReader
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaProjectionState = 0
    private val screenCaptureRequestCode = 1
    private val enablePreview = false
    private val liveStreamIntent by lazy { Intent(this, LiveStreamForegroundService::class.java) }
    private var videoHwEncode: MediaCodec? = null
    private var videoEncodeInputSurface: Surface? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLiveStreamBinding.inflate(layoutInflater)
        setContentView(binding.root)
        startForegroundService(liveStreamIntent)
        binding.surfaceLiveStream.holder.addCallback(this)
        mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onStart() {
        super.onStart()

        if (mediaProjectionState <= 0) {
            val createScreenCaptureIntent = mediaProjectionManager.createScreenCaptureIntent()
            startActivityForResult(createScreenCaptureIntent, screenCaptureRequestCode)
            val displayMetrics = resources.displayMetrics
            val widthPx = displayMetrics.widthPixels
            val heightPx = displayMetrics.heightPixels
            val videoMediaFormat = MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC,
                widthPx,
                heightPx
            )
            videoMediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, MB * 1)
            videoMediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30)
            videoMediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 4)
            videoMediaFormat.setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
            )
            videoHwEncode = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
            videoHwEncode!!.configure(
                videoMediaFormat,
                null,
                null,
                MediaCodec.CONFIGURE_FLAG_ENCODE
            )
            videoEncodeInputSurface = videoHwEncode!!.createInputSurface()

            MusicNativeMethod.getInstance()
                .initRtspPushLiveStream(rtspPushTestUrl, widthPx, heightPx)
        }
    }

    override fun onResume() {
        super.onResume()

        if (mediaProjectionState <= 0) {
            mediaProjection?.apply {
                val displayMetrics = resources.displayMetrics
                virtualDisplay = createVirtualDisplay(
                    "screenVirtualDisplay",
                    binding.surfaceLiveStream.width,
                    binding.surfaceLiveStream.height,
                    displayMetrics.densityDpi,
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY,
                    videoEncodeInputSurface!!,
                    null,
                    null
                )
                mediaProjectionState = 1

                videoHwEncode!!.setCallback(object : MediaCodec.Callback() {
                    override fun onInputBufferAvailable(codec: MediaCodec, index: Int) {

                    }

                    override fun onOutputBufferAvailable(
                        codec: MediaCodec,
                        index: Int,
                        info: MediaCodec.BufferInfo
                    ) {
                        val outputBuffer = codec.getOutputBuffer(index)
                        outputBuffer?.apply {

                            if (info.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                                MusicNativeMethod.getInstance()
                                    .setRtspExtraData(outputBuffer, outputBuffer.remaining())
                            } else {
                                if (info.flags and MediaCodec.BUFFER_FLAG_KEY_FRAME != 0) {
                                    MusicNativeMethod.getInstance().pushRtspData(
                                        outputBuffer,
                                        outputBuffer.remaining(),
                                        info.presentationTimeUs,
                                        true
                                    )
                                } else {
                                    MusicNativeMethod.getInstance().pushRtspData(
                                        outputBuffer,
                                        outputBuffer.remaining(),
                                        info.presentationTimeUs,
                                        false
                                    )
                                }
                            }
                        }
                        codec.releaseOutputBuffer(index, false)
                    }

                    override fun onError(codec: MediaCodec, e: MediaCodec.CodecException) {
                        Log.e(TAG, "onError: mediacodec onError called ", e)
                    }

                    override fun onOutputFormatChanged(codec: MediaCodec, format: MediaFormat) {

                    }
                })
                videoHwEncode!!.start()
            }
        }
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        videoHwEncode?.stop()
        videoHwEncode?.release()
        virtualDisplay?.release()
        mediaProjection?.stop()
        imageReader.close()
        stopService(liveStreamIntent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == screenCaptureRequestCode) {

            if (resultCode == RESULT_OK && data != null) {
                mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data)
            }
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {

    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        imageReader = ImageReader.newInstance(
            width,
            height,
            PixelFormat.RGBA_8888,
            3
        )

        imageReader.setOnImageAvailableListener(this, null)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {

    }

    override fun onImageAvailable(reader: ImageReader?) {
        val image = imageReader.acquireLatestImage()
        image?.let { internalImage ->
            val plane = internalImage.planes[0]

            if (enablePreview) {
                val bitmap = Bitmap.createBitmap(
                    plane.rowStride / plane.pixelStride,
                    internalImage.height,
                    Bitmap.Config.ARGB_8888
                )
                bitmap.copyPixelsFromBuffer(plane.buffer)
                val lockCanvas = binding.surfaceLiveStream.holder.lockCanvas()

                if (lockCanvas != null) {
                    lockCanvas.drawBitmap(bitmap, 0f, 0f, null)
                    binding.surfaceLiveStream.holder.unlockCanvasAndPost(lockCanvas)
                }
            }
            internalImage.close()
        }
    }

    override fun onClick(v: View?) {
        if (v == null)
            return


    }

}