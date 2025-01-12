package com.example.xianyuplayer.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.xianyuplayer.database.LrcBean
import com.example.xianyuplayer.databinding.ItemLrcBinding

class LrcAdapter() : RecyclerView.Adapter<LrcAdapter.ViewHolder>() {

    private val list = ArrayList<LrcBean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LrcAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemLrcBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LrcAdapter.ViewHolder, position: Int) {
        val lrcBean = list[position]
        holder.binding.txtLrc.text = lrcBean.text
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setLrcData(lrcList: ArrayList<LrcBean>) {
        list.clear()
        list.addAll(lrcList)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemLrcBinding) : RecyclerView.ViewHolder(binding.root) {

    }
}