package com.example.xianyuplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.xianyuplayer.MusicNativeMethod
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.databinding.ItemLocalFileBinding

class LocalFileAdapter() : RecyclerView.Adapter<LocalFileAdapter.ViewHolder>() {

    private val dataList = ArrayList<LocalFile>(20)
    private val TAG = "LocalFileAdapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemLocalFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val localFile = dataList[position]
        val fileName = localFile.fileName
        holder.binding.txtFileName.text = fileName.substring(0, fileName.lastIndexOf("."))
        holder.binding.txtNumber.text = String.format("%s", position + 1)
        holder.binding.txtFileInfo.text = localFile.singer

        holder.binding.constraintItemContainer.setOnClickListener {
            MusicNativeMethod.getInstance().openDecodeStream(localFile.filePath + localFile.fileName)
            MusicNativeMethod.getInstance().startDecodeStream()
            MusicNativeMethod.getInstance().initPlay()
            MusicNativeMethod.getInstance().startPlay()
        }
    }

    fun setData(localFileList: List<LocalFile>) {
        dataList.clear()
        dataList.addAll(localFileList)
        notifyDataSetChanged()
    }

    class ViewHolder(public val binding: ItemLocalFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}