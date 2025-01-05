package com.example.xianyuplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.xianyuplayer.database.LocalScanPath
import com.example.xianyuplayer.databinding.ItemScanSettingBinding

class ScanSettingAdapter(private val context: Context) :
    RecyclerView.Adapter<ScanSettingAdapter.ViewHolder>() {

    private val pathList = ArrayList<LocalScanPath>()
    private val selectedMap = HashMap<Int, LocalScanPath>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ScanSettingAdapter.ViewHolder {
        val inflater = LayoutInflater.from(context)
        val binding = ItemScanSettingBinding.inflate(inflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ScanSettingAdapter.ViewHolder, position: Int) {
        val scanPath = pathList[position]
        holder.binding.txtPath.text = scanPath.absolutePath

        if (selectedMap.contains(position)) {
            val selectScanPath = selectedMap[position]
            holder.binding.checkPathSelect.isChecked = selectScanPath!!.mask
        } else {
            holder.binding.checkPathSelect.isChecked = scanPath.mask
        }

        holder.binding.checkPathSelect.setOnClickListener {
            doSelectedItem(holder.binding.checkPathSelect.isChecked, scanPath, position)
        }

        holder.binding.root.setOnClickListener {
            holder.binding.checkPathSelect.isChecked = !holder.binding.checkPathSelect.isChecked
            doSelectedItem(holder.binding.checkPathSelect.isChecked, scanPath, position)
        }
    }

    override fun getItemCount(): Int {
        return pathList.size
    }

    private fun doSelectedItem(
        select: Boolean,
        scanPath: LocalScanPath,
        position: Int
    ) {
        if (selectedMap.contains(position)) {
            scanPath.mask = select
            selectedMap.replace(position, scanPath)
        } else {
            scanPath.mask = select
            selectedMap.put(position, scanPath)
        }
    }

    fun getSelectedList(): ArrayList<LocalScanPath> {
        val list = ArrayList<LocalScanPath>()
        list.addAll(selectedMap.values.toList())
        return list
    }

    fun addPath(scanPaths: List<LocalScanPath>) {
        pathList.clear()
        pathList.addAll(scanPaths)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemScanSettingBinding) :
        RecyclerView.ViewHolder(binding.root) {

    }
}