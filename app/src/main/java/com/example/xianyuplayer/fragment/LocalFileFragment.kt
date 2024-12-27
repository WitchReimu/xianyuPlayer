package com.example.xianyuplayer.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xianyuplayer.FragmentInstanceManager
import com.example.xianyuplayer.PlayerApplication
import com.example.xianyuplayer.R
import com.example.xianyuplayer.ScanCustomActivity
import com.example.xianyuplayer.adapter.LocalFileAdapter
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.databinding.FragmentLocalFileBinding
import com.example.xianyuplayer.vm.LocalFileViewModel
import com.example.xianyuplayer.vm.LocalFileViewModelFactory
import java.util.Arrays

class LocalFileFragment : Fragment(), View.OnClickListener {

    private val TAG = "LocalFileFragment"
    private val prefixPath = "/sdcard/"

    private val viewModel: LocalFileViewModel by lazy {
        val playerApplication = requireActivity().application as PlayerApplication
        ViewModelProvider(
            this@LocalFileFragment, LocalFileViewModelFactory(playerApplication.repository)
        )[TAG, LocalFileViewModel::class.java]
    }

    private lateinit var binding: FragmentLocalFileBinding
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val localFileList = ArrayList<LocalFile>(20)
    private val adapter by lazy { LocalFileAdapter(requireContext()) }
    private val supportFileType = arrayOf("mp3", "mp4", "flac")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        launcher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

                if (result.resultCode == Activity.RESULT_OK) {
                    val treeUri = result.data!!.data!!
                    requireContext().contentResolver.takePersistableUriPermission(
                        treeUri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    recursionScanFile(treeUri)
                    viewModel.insertScanPath(treeUri)
                }
            }
    }

    private fun recursionScanFile(file: DocumentFile) {

        if (file.isFile) {
            val name = file.name
            //  uri path字段的字符格式/tree/primary:xxx/document/primary:去掉了/sdcard/的文件路径
            val path = file.uri.path

            if (name != null) {
                val split = name.split(".")
                val type = split[split.size - 1]

                if (Arrays.stream(supportFileType).anyMatch { it == type }) {
                    var absolutePath = prefixPath + path!!.split(":")[2]
                    absolutePath = absolutePath.substring(
                        absolutePath.indexOf("/"),
                        absolutePath.lastIndexOf("/") + 1
                    )
                    val localFile = LocalFile(absolutePath, name)
                    FragmentInstanceManager.getMetadata(localFile)
                    viewModel.insertLocalFile(localFile)
                    Log.i(TAG, "recursionScanFile: --> ${localFile.filePath}")
                    localFileList.add(localFile)
                }
            }
        } else if (file.isDirectory) {
            val fileDirect = file.listFiles()
            for (documentFile in fileDirect) {
                recursionScanFile(documentFile)
            }
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocalFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.allLocalFile.observe(viewLifecycleOwner) { list ->

            if (list.isEmpty()) {
                binding.btnScanFile.isEnabled = true
                binding.btnScanFile.visibility = View.VISIBLE
            } else {
                binding.btnScanFile.isEnabled = false
                binding.btnScanFile.visibility = View.GONE
                adapter.setData(list)
            }
        }

        viewModel.scanPathUriList.observe(viewLifecycleOwner) { list ->
            for (localScanPath in list) {
                val uri = Uri.parse(localScanPath.uri)
                recursionScanFile(uri)
            }
        }

        binding.btnScanFile.setOnClickListener(this)
        binding.imgBtnList.setOnClickListener(this)

        binding.recycLocalFileContainer.adapter = adapter
        binding.recycLocalFileContainer.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }

    private fun recursionScanFile(uri: Uri) {
        val documentFile = DocumentFile.fromTreeUri(requireContext(), uri)

        if (documentFile != null) {
            recursionScanFile(documentFile)
        }
    }

    override fun onClick(v: View?) {
        if (v == null) {
            return
        }

        when (v.id) {
            binding.btnScanFile.id -> {
                if (::launcher.isInitialized) {
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                    launcher.launch(intent)
                }
            }

            binding.imgBtnList.id -> {
                val listPopupMenu = PopupMenu(requireContext(), binding.imgBtnList)
                listPopupMenu.menuInflater.inflate(R.menu.list_pop_menu, listPopupMenu.menu)
                listPopupMenu.show()
                listPopupMenu.setOnMenuItemClickListener { item ->
                    if (item == null) {
                        return@setOnMenuItemClickListener false
                    }

                    when (item.itemId) {

                        R.id.menu_custom_scan_path -> {
                            val intent = Intent(requireContext(), ScanCustomActivity::class.java)
                            startActivity(intent)
                        }
                    }

                    return@setOnMenuItemClickListener true
                }
            }

        }
    }

}