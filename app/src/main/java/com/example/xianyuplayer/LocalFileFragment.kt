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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xianyuplayer.adapter.LocalFileAdapter
import com.example.xianyuplayer.databinding.FragmentLocalFileBinding

class LocalFileFragment : Fragment() {

    private val TAG = "LocalFileFragment"

    private lateinit var binding: FragmentLocalFileBinding
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private val localFileList = ArrayList<String>(20)
    private val adapter by lazy { LocalFileAdapter() }
    private val supportFileType = arrayOf("zip")

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
                        recursionScanFile(documentFile)
                    }
                    adapter.setData(localFileList)
                }
            }
    }

    private fun recursionScanFile(file: DocumentFile) {

        if (file.isFile) {
            val name = file.name

            if (name != null) {
                val split = name.split(".")
                val type = split[split.size - 1]
                if (type == supportFileType[0]) {
                    localFileList.add(name)
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLocalFileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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