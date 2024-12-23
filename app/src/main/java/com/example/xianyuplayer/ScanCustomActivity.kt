package com.example.xianyuplayer

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.xianyuplayer.databinding.ActivityScanCustomBinding
import com.example.xianyuplayer.fragment.ScanDirectorySelectFragment

class ScanCustomActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityScanCustomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanCustomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgBtnBack.setOnClickListener(this)
        binding.btnCustomScan.setOnClickListener(this)
        binding.btnScanSetting.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v == null) {
            return
        }

        when (v.id) {

            binding.imgBtnBack.id -> {
                onBackPressed()
            }

            binding.btnCustomScan.id -> {
                binding.frameCustomContainer.visibility = View.VISIBLE
                val transaction = supportFragmentManager.beginTransaction()
                transaction.add(ScanDirectorySelectFragment(), "DirectorySelect")
                transaction.commit()
            }

            binding.btnScanSetting.id -> {

            }
        }
    }
}