package com.example.xianyuplayer.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.xianyuplayer.R
import com.example.xianyuplayer.databinding.FragmentScanDirectorySelectBinding

class ScanDirectorySelectFragment : Fragment(), View.OnClickListener {

    private lateinit var binding: FragmentScanDirectorySelectBinding
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
        binding.linearTopContainer.setOnClickListener(this)
        binding.linearManageSearch.setOnClickListener(this)
    }

    override fun onClick(v: View?) {

        if (v == null) {
            return
        }

        when (v.id) {
            binding.linearManageSearch.id -> {

            }

            binding.linearTopContainer.id -> {

            }
        }
    }
}