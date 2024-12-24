package com.example.xianyuplayer.fragment

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xianyuplayer.adapter.DirectoryAdapter
import com.example.xianyuplayer.databinding.FragmentScanDirectorySelectBinding
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class ScanDirectorySelectFragment : Fragment(), View.OnClickListener {

    private val rootFile by lazy {
        val path = Environment.getExternalStorageDirectory().path
        File(path)
    }
    private val directoryAdapter by lazy { DirectoryAdapter(requireContext(), rootFile) }
    private lateinit var binding: FragmentScanDirectorySelectBinding
    private val TAG = "ScanDirectorySelectFrag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentScanDirectorySelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!rootFile.exists()) {
            Log.e(TAG, "onViewCreated: --> 根目录不存在")
            return
        }

        binding.linearTopContainer.setOnClickListener(this)
        binding.linearManageSearch.setOnClickListener(this)
        directoryAdapter.setCheckBoxSelectCallback(::checkBoxSelectCallback)
        directoryAdapter.setDirectorySelectCallback(::directorySelectCallback)
        binding.recycleDirectoryList.adapter = directoryAdapter
        binding.recycleDirectoryList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

    }

    override fun onClick(v: View?) {

        if (v == null) {
            return
        }

        when (v.id) {

            binding.linearTopContainer.id -> {

            }

            binding.linearManageSearch.id -> {

            }
        }
    }

    private fun checkBoxSelectCallback(button: CompoundButton, isChecked: Boolean) {
        Log.i(TAG, "checkBoxSelectCallback: --> isChecked $isChecked")
    }

    private fun directorySelectCallback(view: View, itemFile: File, position: Int) {
        Log.i(TAG, "directorySelectCallback: --> view $view")
    }
}