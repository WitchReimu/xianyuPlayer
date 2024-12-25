package com.example.xianyuplayer.fragment

import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xianyuplayer.MusicNativeMethod
import com.example.xianyuplayer.PlayerApplication
import com.example.xianyuplayer.vm.ScanDirectorySelectViewModel
import com.example.xianyuplayer.vm.ScanDirectorySelectViewModelFactory
import com.example.xianyuplayer.adapter.DirectoryAdapter
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.databinding.FragmentScanDirectorySelectBinding
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import kotlin.io.path.absolutePathString
import kotlin.io.path.name
import kotlin.io.path.pathString

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
    private val directoryAdapter by lazy { DirectoryAdapter(requireContext(), rootFile) }

    private val TAG = "ScanDirectorySelectFrag"
    private lateinit var binding: FragmentScanDirectorySelectBinding
    private val key_album = "album"
    private val key_artist = "artist"
    private val key_title = "title"
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
                val localFileList = ArrayList<Path>()
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
                    localFileList.addAll(result)
                }

                //获取符合条件的音乐文件的metadata
                for (path in localFileList) {
                    val localFile = LocalFile(path.parent.toString() + File.separator, path.name)

                    val metadataArray =
                        MusicNativeMethod.getInstance().getMetadata(path.absolutePathString())

                    for (musicMetadata in metadataArray) {

                        when (musicMetadata.key) {

                            key_artist -> {
                                localFile.singer = musicMetadata.value
                            }

                            key_album -> {
                                localFile.albumsName = musicMetadata.value
                            }

                            key_title -> {
                                localFile.songTitle = musicMetadata.value
                            }
                        }
                    }
                    viewModel.insertLocalFile(localFile)
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