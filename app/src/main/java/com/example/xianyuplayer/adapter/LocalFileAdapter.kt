package com.example.xianyuplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.xianyuplayer.databinding.ItemLocalFileBinding

class LocalFileAdapter() : RecyclerView.Adapter<LocalFileAdapter.ViewHolder>() {

    private val dataList = ArrayList<String>(20)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ItemLocalFileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.txtFileName.text = dataList[position]
    }

    fun setData(localFileList: ArrayList<String>) {
        dataList.addAll(localFileList)
        notifyDataSetChanged()
    }

    class ViewHolder(public val binding: ItemLocalFileBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}