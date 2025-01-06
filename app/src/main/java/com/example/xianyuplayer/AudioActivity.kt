package com.example.xianyuplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.xianyuplayer.databinding.ActivityAudioBinding

class AudioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }
    }
}