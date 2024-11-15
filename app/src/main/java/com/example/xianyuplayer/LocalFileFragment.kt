package com.example.xianyuplayer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import com.example.xianyuplayer.databinding.FragmentLocalFileBinding

class LocalFileFragment : Fragment() {

    private val TAG = "LocalFileFragment"

    private lateinit var binding: FragmentLocalFileBinding
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
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.setDataAndType(null, "*/*")
            registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ActivityResultCallback<ActivityResult> { result ->

                if (result.resultCode == Activity.RESULT_OK) {
                    val treeUri = result.data!!.data!!
                    requireContext().contentResolver.takePersistableUriPermission(treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    val documentFile = DocumentFile.fromTreeUri(requireContext(), treeUri)
                    val listFiles = documentFile!!.listFiles()
                    Log.i(TAG, "onViewCreated: -->${listFiles.toString()}")
                }
            })
        }
    }
}