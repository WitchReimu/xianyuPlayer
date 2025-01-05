package com.example.xianyuplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xianyuplayer.FragmentInstanceManager
import com.example.xianyuplayer.PlayerApplication
import com.example.xianyuplayer.ScanCustomActivity
import com.example.xianyuplayer.adapter.ScanSettingAdapter
import com.example.xianyuplayer.databinding.FragmentScanSettingBinding
import com.example.xianyuplayer.vm.ScanSettingViewModel
import com.example.xianyuplayer.vm.ScanSettingViewModelFactory

class ScanSettingFragment() : Fragment(), View.OnClickListener {

    private val viewModel by lazy {
        val application = requireActivity().application as PlayerApplication
        val factory = ScanSettingViewModelFactory(application.repository)
        ViewModelProvider(viewModelStore, factory)[TAG, ScanSettingViewModel::class.java]
    }
    private lateinit var binding: FragmentScanSettingBinding
    private lateinit var scanSettingAdapter: ScanSettingAdapter
    private val TAG = "ScanSettingFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        scanSettingAdapter = ScanSettingAdapter(requireContext())
        binding.linearFilter.setOnClickListener(this)
        binding.switchFilter.setOnClickListener(this)
        binding.recycleScanPathList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recycleScanPathList.adapter = scanSettingAdapter

        viewModel.scanPathsLiveData.observe(viewLifecycleOwner) {
            scanSettingAdapter.addPath(it)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val list = scanSettingAdapter.getSelectedList()
                    val fragment =
                        requireActivity().supportFragmentManager.findFragmentByTag(
                            ScanCustomActivity.customScanTag
                        )

                    if (list.isNotEmpty()) {

                        if (fragment != null) {
                            val customScanFragment = fragment as CustomScanFragment
                            customScanFragment.updateScanPathList(list)
                        }
                    }

                    FragmentInstanceManager.showAndRemoveSpecialFragment(
                        requireActivity(),
                        fragment!!,
                        this@ScanSettingFragment,
                        false
                    )
                }
            })
    }

    override fun onClick(v: View?) {

        if (v == null) {
            return
        }

        when (v.id) {
            binding.linearFilter.id -> {

            }

            binding.switchFilter.id -> {

            }
        }
    }


}