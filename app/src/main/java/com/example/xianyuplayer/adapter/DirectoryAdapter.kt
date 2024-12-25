package com.example.xianyuplayer.adapter

import android.content.Context
import android.text.TextUtils
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
    private var currentDirectory: File,
    private var checkedBoxCallback: ((view: View) -> Unit)? = null,
    private var directoryItemCallback: ((view: View, itemFile: File, position: Int) -> Unit)? = null
) : RecyclerView.Adapter<DirectoryAdapter.ViewHolder>() {

    private val TAG = "DirectoryAdapter"
    private val selectedFileList = ArrayList<File>()
    private lateinit var directoryUpdateCallback: (item: File) -> Unit

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

        holder.binding.checkSelect.isChecked =
            selectedFileList.isNotEmpty() && selectedFileList.contains(file)

        holder.binding.checkSelect.setOnClickListener { view ->
            updateSelectedFileList(holder, file)

            if (checkedBoxCallback != null) {
                checkedBoxCallback!!(view)
            }
        }

        holder.binding.linearDirectoryItemRoot.setOnClickListener {
            Files.newDirectoryStream(Paths.get(file.path)).use { stream ->
                for (path in stream) {

                    if (Files.isDirectory(path)) {
                        updateCurrentFile(file)
                        return@use
                    }
                }
                holder.binding.checkSelect.isChecked = !holder.binding.checkSelect.isChecked
                updateSelectedFileList(holder, file)
            }

            if (directoryItemCallback != null) {
                directoryItemCallback!!(it, file, position)
            }
        }
    }

    private fun getListFile(): ArrayList<Path> {
        val list = Files.newDirectoryStream(Paths.get(currentDirectory.path)).use {
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

    private fun updateSelectedFileList(holder: ViewHolder, file: File) {
        if (holder.binding.checkSelect.isChecked) {
            selectedFileList.add(file)
        } else {
            selectedFileList.remove(file)
        }
    }

    fun setCheckBoxSelectCallback(callback: (view: View) -> Unit) {
        checkedBoxCallback = callback
    }

    fun setDirectorySelectCallback(callback: (view: View, itemFile: File, position: Int) -> Unit) {
        directoryItemCallback = callback
    }

    fun setOnDirectoryUpdateCallback(callback: (itemFile: File) -> Unit) {
        directoryUpdateCallback = callback
    }

    fun getSelectedFile(): ArrayList<File> {
        val arrayList = ArrayList<File>()
        arrayList.addAll(selectedFileList)
        return arrayList
    }

    fun backParentDirectory() {
        val parent = currentDirectory.parent

        if (TextUtils.isEmpty(parent)) {
            return
        }
        parent as String
        updateCurrentFile(File(parent))
    }

    private fun updateCurrentFile(file: File) {

        if (!file.exists()) {
            Log.w(TAG, "updateCurrentFile: --> 目录不存在不进行更新")
            return
        }

        if (!file.canRead() || !file.canExecute()) {
            Log.w(TAG, "updateCurrentFile: --> 目录权限不足，不进行更新")
            return
        }
        currentDirectory = file
        listFiles.clear()
        listFiles.addAll(getListFile())
        directoryUpdateCallback(file)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemDirectoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }

}