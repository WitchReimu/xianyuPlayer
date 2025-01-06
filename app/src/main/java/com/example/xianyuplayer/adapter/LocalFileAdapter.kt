package com.example.xianyuplayer.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.xianyuplayer.MainActivity
import com.example.xianyuplayer.MusicNativeMethod
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.databinding.ItemLocalFileBinding

class LocalFileAdapter(private val context: Context) :
    RecyclerView.Adapter<LocalFileAdapter.ViewHolder>() {

    private val dataList = ArrayList<LocalFile>(20)
    private var onStart: ((localFile: LocalFile) -> Unit)? = null
    private var currentAbsolutePath = ""
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
        val songTitle = localFile.songTitle

        if (TextUtils.isEmpty(songTitle) || songTitle == "群星") {
            holder.binding.txtFileName.text = fileName.substring(0, fileName.lastIndexOf("."))
        } else {
            holder.binding.txtFileName.text = songTitle
        }
        holder.binding.txtNumber.text = String.format("%s", position + 1)
        holder.binding.txtFileInfo.text = localFile.singer

        holder.binding.constraintItemContainer.setOnClickListener {
            val absolutePath = localFile.filePath + localFile.fileName

            if (currentAbsolutePath != absolutePath) {

                if (onStart != null) {
                    onStart!!(localFile)
                }
                MusicNativeMethod.getInstance().openDecodeStream(absolutePath)
                MusicNativeMethod.getInstance().startDecodeStream()
                MusicNativeMethod.getInstance().initPlay(context as MainActivity)
                val startResult = MusicNativeMethod.getInstance().startPlay()

                if (startResult)
                    currentAbsolutePath = absolutePath
            }
        }
    }

    fun setOnStartPlay(callback: (localFile: LocalFile) -> Unit) {
        onStart = callback
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