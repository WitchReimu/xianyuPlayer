package com.example.xianyuplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.xianyuplayer.databinding.ActivityScanCustomBinding
import com.example.xianyuplayer.fragment.CustomScanFragment
import com.example.xianyuplayer.fragment.ScanDirectorySelectFragment

class ScanCustomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScanCustomBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScanCustomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(binding.frameCustomContainer.id, CustomScanFragment(), customScanTag)
        transaction.commit()
    }

    companion object {
        const val directorySelectTag = "directory_select"
        const val customScanTag = "custom_scan"
    }
}