package com.example.xianyuplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.xianyuplayer.FragmentInstanceManager
import com.example.xianyuplayer.R
import com.example.xianyuplayer.ScanCustomActivity
import com.example.xianyuplayer.databinding.FragmentCustomScanBinding

class CustomScanFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentCustomScanBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCustomScanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
                requireActivity().onBackPressed()
            }

            binding.btnCustomScan.id -> {
                val manager = requireActivity().supportFragmentManager
                val transaction = manager.beginTransaction()
                val directoryFragment = ScanDirectorySelectFragment()
                transaction.add(
                    R.id.frame_custom_container,
                    directoryFragment,
                    ScanCustomActivity.directorySelectTag
                )
                transaction.addToBackStack(ScanCustomActivity.directorySelectTag)
                FragmentInstanceManager.showSpecialFragment(transaction, manager, directoryFragment)
            }

            binding.btnScanSetting.id -> {

            }
        }
    }
}