package com.example.xianyuplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.xianyuplayer.FragmentInstanceManager
import com.example.xianyuplayer.PlayerApplication
import com.example.xianyuplayer.R
import com.example.xianyuplayer.ScanCustomActivity
import com.example.xianyuplayer.database.LocalScanPath
import com.example.xianyuplayer.databinding.FragmentCustomScanBinding
import com.example.xianyuplayer.vm.CustomScanViewModel
import com.example.xianyuplayer.vm.CustomScanViewModelFactory

class CustomScanFragment : Fragment(), View.OnClickListener {

    private val TAG = "CustomScanFragment"

    private lateinit var binding: FragmentCustomScanBinding
    private val viewModel by lazy {
        val application = requireActivity().application as PlayerApplication
        val factory = CustomScanViewModelFactory(application.repository)
        ViewModelProvider(viewModelStore, factory)[TAG, CustomScanViewModel::class.java]
    }

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
                val manager = requireActivity().supportFragmentManager
                val transaction = manager.beginTransaction()
                val scanSettingFragment = ScanSettingFragment()
                transaction.add(
                    R.id.frame_custom_container,
                    scanSettingFragment,
                    ScanCustomActivity.scanSettingTag
                )
                FragmentInstanceManager.showSpecialFragment(
                    transaction,
                    manager,
                    scanSettingFragment
                )
            }
        }
    }

    fun updateScanPathList(scanPathList: List<LocalScanPath>) {
        viewModel.updateScanPathList(scanPathList)
    }
}