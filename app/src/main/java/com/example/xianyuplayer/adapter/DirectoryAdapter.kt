package com.example.xianyuplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.example.xianyuplayer.databinding.ItemDirectoryBinding
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class DirectoryAdapter(
    private val context: Context,
    val rootFile: File,
    private var checkedBoxCallback: ((button: CompoundButton, isChecked: Boolean) -> Unit)? = null,
    private var directoryItemCallback: ((view: View) -> Unit)? = null
) : RecyclerView.Adapter<DirectoryAdapter.ViewHolder>() {

    private val TAG = "DirectoryAdapter"
    private val listFiles by lazy {
        Files.newDirectoryStream(Paths.get(rootFile.path)).use {
            val pathList = ArrayList<Path>(10)
            for (path in it) {
                if (Files.isDirectory(path)) {
                    pathList.add(path)
                }
            }
            pathList
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = ItemDirectoryBinding.inflate(layoutInflater, parent, false)

        if (checkedBoxCallback != null) {
            binding.checkSelect.setOnCheckedChangeListener(checkedBoxCallback)
        }

        if (directoryItemCallback != null) {
            binding.linearDirectoryItemRoot.setOnClickListener(directoryItemCallback)
        }

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listFiles.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = listFiles[position].toFile()
        holder.binding.txtFileName.text = file.name
    }

    fun setCheckBoxSelectCallback(callback: (button: CompoundButton, isChecked: Boolean) -> Unit) {
        checkedBoxCallback = callback
    }

    fun setDirectorySelectCallback(callback: (view: View) -> Unit) {
        directoryItemCallback = callback
    }

    inner class ViewHolder(val binding: ItemDirectoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}