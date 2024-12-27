package com.example.xianyuplayer.fragment

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xianyuplayer.FragmentInstanceManager
import com.example.xianyuplayer.PlayerApplication
import com.example.xianyuplayer.adapter.DirectoryAdapter
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.databinding.FragmentScanDirectorySelectBinding
import com.example.xianyuplayer.vm.ScanDirectorySelectViewModel
import com.example.xianyuplayer.vm.ScanDirectorySelectViewModelFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.io.path.name

class ScanDirectorySelectFragment : Fragment(), View.OnClickListener {

    private val rootFile by lazy {
        val path = Environment.getExternalStorageDirectory().path
        File(path)
    }

    private val viewModel by lazy {
        val playerApplication = requireActivity().application as PlayerApplication
        ViewModelProvider(
            this@ScanDirectorySelectFragment,
            ScanDirectorySelectViewModelFactory(playerApplication.repository)
        )[TAG, ScanDirectorySelectViewModel::class]
    }

    private val searchAnimation by lazy {
        val path = android.graphics.Path()
        path.addCircle(
            -(binding.imgSearch.width / 2).toFloat(),
            0f,
            40f,
            android.graphics.Path.Direction.CCW
        )

        ObjectAnimator.ofFloat(
            binding.imgSearch,
            "translationX",
            "translationY",
            path
        )
    }
    private val directoryAdapter by lazy { DirectoryAdapter(requireContext(), rootFile) }

    private val TAG = "ScanDirectorySelectFrag"
    private lateinit var binding: FragmentScanDirectorySelectBinding
    private val supportFileType = arrayOf("mp3", "mp4", "flac")

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

        binding.txtCurrentDirectory.text = rootFile.absolutePath
        binding.linearTopContainer.setOnClickListener(this)
        binding.linearManageSearch.setOnClickListener(this)
        directoryAdapter.setCheckBoxSelectCallback(::checkBoxSelectCallback)
        directoryAdapter.setDirectorySelectCallback(::directorySelectCallback)
        directoryAdapter.setOnDirectoryUpdateCallback(::directoryUpdateCallback)
        binding.recycleDirectoryList.adapter = directoryAdapter
        binding.recycleDirectoryList.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        viewModel.insertNumber.observe(viewLifecycleOwner) {
            if (it == null)
                return@observe
            val valueArray = it as LongArray

            for (value in valueArray) {
                if (value == -1L) {
                    Log.w(TAG, "onViewCreated: --> 数据插入失败")
                }
            }
            searchAnimation.cancel()
            requireActivity().finish()
        }
    }

    override fun onClick(v: View?) {

        if (v == null) {
            return
        }

        when (v.id) {

            binding.linearTopContainer.id -> {
                directoryAdapter.backParentDirectory()
            }

            binding.linearManageSearch.id -> {
                val selectedFile = directoryAdapter.getSelectedFile()
                val pathList = ArrayList<Path>()
//                val localScanList = ArrayList<LocalScanPath>(selectedFile.size)
                val localFileList = ArrayList<LocalFile>()

                //开启扫描本地文件动画
                if (selectedFile.isNotEmpty()) {
                    searchAnimation.repeatCount = ValueAnimator.INFINITE
                    searchAnimation.repeatMode = ValueAnimator.RESTART
                    searchAnimation.duration = 1000
                    searchAnimation.start()
                }
                //遍历扫描选择的文件目录
                for (file in selectedFile) {
                    val walkStream = Files.walk(Paths.get(file.path))
                    val result = walkStream.use { stream ->

                        //将目录内的符合条件的文件筛选出来
                        stream
                            .filter { path -> !path.equals(Paths.get(file.path)) }
                            .filter { path ->
                                val name = path.name
                                val split = name.split(".")

                                if (split.size < 2) {
                                    return@filter false
                                }
                                val suffix = split[split.size - 1]

                                for (type in supportFileType) {

                                    if (type == suffix) {
                                        return@filter true
                                    }
                                }
                                false
                            }
                            .collect(Collectors.toList())
                    }
//                    val localScanPath = LocalScanPath(file.absolutePath)
//                    localScanList.add(localScanPath)
                    pathList.addAll(result)
                }

                if (pathList.isEmpty()) {
                    searchAnimation.cancel()
                } else {
                    //获取符合条件的音乐文件的metadata
                    for (path in pathList) {
                        val localFile =
                            LocalFile(path.parent.toString() + File.separator, path.name)
                        FragmentInstanceManager.getMetadata(localFile)
                        localFileList.add(localFile)
                    }
                    viewModel.insertLocalFile(localFileList)
                }
            }
        }
    }

    private fun checkBoxSelectCallback(view: View) {
        Log.i(TAG, "checkBoxSelectCallback: --> isChecked")
    }

    private fun directorySelectCallback(view: View, itemFile: File, position: Int) {
        Log.i(TAG, "directorySelectCallback: --> position")
    }

    private fun directoryUpdateCallback(file: File) {
        binding.txtCurrentDirectory.text = file.absolutePath
    }
}