package com.example.xianyuplayer

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.xianyuplayer.database.PlayFile
import com.example.xianyuplayer.databinding.ActivityAudioBinding
import com.example.xianyuplayer.fragment.LrcFragment
import com.example.xianyuplayer.vm.AudioViewModel
import com.example.xianyuplayer.vm.AudioViewModelFactory
import com.example.xianyuplayer.vm.GlobalViewModel

class AudioActivity : AppCompatActivity(), MusicNativeMethod.DtsListener, View.OnClickListener,
    MusicNativeMethod.PlayStateChangeListener {

    private val TAG = "AudioActivity"
    private lateinit var binding: ActivityAudioBinding
    private lateinit var viewModel: AudioViewModel
    private lateinit var globalViewModel: GlobalViewModel
    private var isTouch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = PlayerApplication.getInstance().repository
        viewModel = ViewModelProvider.create(
            this,
            AudioViewModelFactory(repository)
        )[TAG, AudioViewModel::class]

        globalViewModel = FragmentInstanceManager.getGlobalViewModel(this, repository)

        binding.imgBack.setOnClickListener(this)
        binding.txtLoopType.setOnClickListener(this)
        binding.imgPrevious.setOnClickListener(this)
        binding.imgPlay.setOnClickListener(this)
        binding.imgNext.setOnClickListener(this)
        binding.imgBtnPlayList.setOnClickListener(this)

        binding.seekAudioPosition.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isTouch = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                MusicNativeMethod.getInstance()
                    .seekPosition(binding.seekAudioPosition.progress * 1000L)
                isTouch = false
            }
        })

        viewModel.durationLiveData.observe(this) {

        }

        viewModel.playListLiveData.observe(this) {
            viewModel.playList.clear()
            viewModel.playList.addAll(it)
        }

        MusicNativeMethod.getInstance().addDtsListener(this)
        MusicNativeMethod.getInstance().addPlayStateChangeListener(this)

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(binding.frameAudioLrc.id, LrcFragment())
        transaction.commit()
    }

    override fun onStart() {
        super.onStart()
        val duration = viewModel.getAudioDuration()
        val secondDuration = duration / 1000000
        binding.seekAudioPosition.max = secondDuration.toInt()
    }

    override fun onDestroy() {
        MusicNativeMethod.getInstance().removeDtsListener(this)
        MusicNativeMethod.getInstance().removePlayStateChangeListener(this)
        super.onDestroy()
    }

    override fun dtsChange(dts: Double) {
        runOnUiThread {

            if (!isTouch)
                binding.seekAudioPosition.progress = dts.toInt()
        }
    }

    override fun onClick(v: View?) {
        if (v == null)
            return

        when (v.id) {
            binding.imgBack.id -> {
                onBackPressed()
            }

            binding.txtLoopType.id -> {

                for (i in 0 until Constant.playListCircle.size) {
                    val circleType = Constant.playListCircle[i]

                    if (binding.txtLoopType.text.equals(circleType)) {
                        val index = (i + 1) % Constant.playListCircle.size
                        val nextType = Constant.playListCircle[index]
                        binding.txtLoopType.text = nextType
                        MusicNativeMethod.getInstance().setPlayCircleType(nextType)
                        break
                    }
                }
            }

            binding.imgPrevious.id -> {
                MusicNativeMethod.previousAudio()
            }

            binding.imgPlay.id -> {
                when (MusicNativeMethod.getInstance().getPlayStatus()) {
                    Constant.playStatusStarting, Constant.playStatusStarted -> {
                        MusicNativeMethod.getInstance().pausePlay()
                    }

                    else -> {
                        MusicNativeMethod.getInstance().startPlay()
                    }
                }
            }

            binding.imgNext.id -> {
                MusicNativeMethod.nextAudio()
            }

            binding.imgBtnPlayList.id -> {

            }
        }
    }

    private fun currentPlayAudioPosition(): Int {

        globalViewModel.currentPlayFile?.apply {
            return isCurrentPlayFile(this)
        }

        for (i in 0 until viewModel.playList.size) {
            val playFile = viewModel.playList[i]
            if (playFile.isContinuePlay) {
                return isCurrentPlayFile(playFile)
            }
        }
        return 0
    }

    private fun isCurrentPlayFile(playFile: PlayFile): Int {
        val length = viewModel.playList.size
        var index = viewModel.playList.indexOf(playFile)
        index = (index % length + length) % length
        return index
    }

    override fun playStatusChangeCallback(status: Int) {
        Constant.playStatus = status

        when (status) {
            Constant.playStatusStarting, Constant.playStatusStarted -> {
                binding.imgPlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.pause_circle_24
                    )
                )
            }

            else -> {
                binding.imgPlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.start_circle_24
                    )
                )
            }
        }
    }

}