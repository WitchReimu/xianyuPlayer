package com.example.xianyuplayer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xianyuplayer.adapter.LocalFileAdapter
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.databinding.FragmentLocalFileBinding
import java.util.Arrays

class LocalFileFragment : Fragment() {

    private val TAG = "LocalFileFragment"
    private val prefixPath = "/sdcard/"
    private val key_album = "album"
    private val key_artist = "artist"

    private val viewModel: LocalFileViewModel by lazy {
        val playerApplication = requireActivity().application as PlayerApplication
        ViewModelProvider(
            this@LocalFileFragment, LocalFileViewModelFactory(playerApplication.repository)
        )[TAG, LocalFileViewModel::class.java]
    }

    private lateinit var binding: FragmentLocalFileBinding
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val localFileList = ArrayList<LocalFile>(20)
    private val adapter by lazy { LocalFileAdapter() }
    private val supportFileType = arrayOf("mp3", "mp4")

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
                    val documentFile = DocumentFile.fromTreeUri(requireContext(), treeUri)

                    if (documentFile != null) {
                        viewModel.insertScanPath(treeUri)
                        recursionScanFile(documentFile)
                    }
                }
            }
    }

    private fun recursionScanFile(file: DocumentFile) {

        if (file.isFile) {
            val name = file.name
            val path = file.uri.path

            if (name != null) {
                val split = name.split(".")
                val type = split[split.size - 1]

                if (Arrays.stream(supportFileType).anyMatch { it == type }) {
                    var absolutePath = prefixPath + path!!.split(":")[2]
                    absolutePath = absolutePath.substring(
                        absolutePath.indexOf("/"), absolutePath.lastIndexOf("/")
                    )

                    val metadataArray =
                        MusicNativeMethod.getInstance().getMetadata("$absolutePath/$name")
                    val localFile = LocalFile(absolutePath, name)

                    for (musicMetadata in metadataArray) {
                        when (musicMetadata.key) {
                            key_artist -> {
                                localFile.singer = musicMetadata.value
                            }

                            key_album -> {
                                localFile.albumsName = musicMetadata.value
                            }
                        }
                    }
                    viewModel.insertLocalFile(localFile)
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
        binding.btnScanFile.setOnClickListener {

            if (::launcher.isInitialized) {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                launcher.launch(intent)
            }
        }
        binding.recycLocalFileContainer.adapter = adapter
        binding.recycLocalFileContainer.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
    }
}