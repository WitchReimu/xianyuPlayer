package com.example.xianyuplayer

import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.xianyuplayer.databinding.ActivityAudioBinding
import com.example.xianyuplayer.fragment.LrcFragment
import com.example.xianyuplayer.vm.AudioViewModel
import com.example.xianyuplayer.vm.AudioViewModelFactory

// TODO: 完成播放列表的附加功能，1.播放列表的循环方式 2.上一首 3.下一首
class AudioActivity : AppCompatActivity(), MusicNativeMethod.DtsListener, View.OnClickListener {

    private val TAG = "AudioActivity"
    private lateinit var binding: ActivityAudioBinding
    private lateinit var viewModel: AudioViewModel
    private var isTouch = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val playerApplication = application as PlayerApplication
        viewModel = ViewModelProvider.create(
            this,
            AudioViewModelFactory(playerApplication.repository)
        )[TAG, AudioViewModel::class]

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
                isTouch = false;
            }
        })

        viewModel.durationLiveData.observe(this) {

        }

        viewModel.playListLiveData.observe(this) {
            viewModel.playList.clear()
            viewModel.playList.addAll(it)
        }

        MusicNativeMethod.getInstance().addDtsListener(this)

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

            }

            binding.imgPrevious.id -> {
                if (viewModel.playList.isEmpty()) {
                    return
                }

                MusicNativeMethod.getInstance().openDecodeStream(absolutePath)
                MusicNativeMethod.getInstance().startDecodeStream()
                MusicNativeMethod.getInstance().initPlay(context as MainActivity)
                val startResult = MusicNativeMethod.getInstance().startPlay()
            }

            binding.imgPlay.id -> {

            }

            binding.imgNext.id -> {

            }

            binding.imgBtnPlayList.id -> {

            }
        }
    }

}