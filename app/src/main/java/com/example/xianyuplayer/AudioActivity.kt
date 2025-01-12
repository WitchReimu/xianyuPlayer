package com.example.xianyuplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.xianyuplayer.databinding.ActivityAudioBinding
import com.example.xianyuplayer.fragment.LrcFragment

class AudioActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAudioBinding
    private val TAG = "AudioActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAudioBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(binding.frameAudioLrc.id, LrcFragment())
        transaction.commit()
    }
}