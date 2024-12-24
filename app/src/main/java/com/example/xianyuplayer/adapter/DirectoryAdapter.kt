package com.example.xianyuplayer.adapter

import android.content.Context
import android.util.Log
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
    private var currentFile: File,
    private var checkedBoxCallback: ((button: CompoundButton, isChecked: Boolean) -> Unit)? = null,
    private var directoryItemCallback: ((view: View, itemFile: File, position: Int) -> Unit)? = null
) : RecyclerView.Adapter<DirectoryAdapter.ViewHolder>() {

    private val TAG = "DirectoryAdapter"

    private val listFiles by lazy {
        getListFile()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = ItemDirectoryBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return listFiles.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = listFiles[position].toFile()
        holder.binding.txtFileName.text = file.name

        if (checkedBoxCallback != null) {
            holder.binding.checkSelect.setOnCheckedChangeListener(checkedBoxCallback)
        }

        if (directoryItemCallback != null) {
            holder.binding.linearDirectoryItemRoot.setOnClickListener {
                Files.newDirectoryStream(Paths.get(file.path)).use { stream ->

                    for (path in stream) {

                        if (Files.isDirectory(path)) {
                            updateCurrentFile(path.toFile())
                            break
                        }

                        holder.binding.checkSelect.isChecked = true
                    }
                }
                directoryItemCallback!!(it, file, position)
            }
        }
    }

    private fun getListFile(): ArrayList<Path> {
        val list = Files.newDirectoryStream(Paths.get(currentFile.path)).use {
            val pathList = ArrayList<Path>(10)
            for (path in it) {
                if (Files.isDirectory(path)) {
                    pathList.add(path)
                }
            }
            pathList
        }

        return list
    }

    fun setCheckBoxSelectCallback(callback: (button: CompoundButton, isChecked: Boolean) -> Unit) {
        checkedBoxCallback = callback
    }

    fun setDirectorySelectCallback(callback: (view: View, itemFile: File, position: Int) -> Unit) {
        directoryItemCallback = callback
    }

    private fun updateCurrentFile(file: File) {
        currentFile = file
        listFiles.clear()
        listFiles.addAll(getListFile())
    }

    inner class ViewHolder(val binding: ItemDirectoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}