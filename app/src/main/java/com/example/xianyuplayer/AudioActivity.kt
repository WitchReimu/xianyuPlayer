package com.example.xianyuplayer

import android.os.Bundle
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.xianyuplayer.databinding.ActivityAudioBinding
import com.example.xianyuplayer.fragment.LrcFragment
import com.example.xianyuplayer.vm.AudioViewModel
import com.example.xianyuplayer.vm.AudioViewModelFactory

class AudioActivity : AppCompatActivity(), MusicNativeMethod.DtsListener {

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

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

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

}