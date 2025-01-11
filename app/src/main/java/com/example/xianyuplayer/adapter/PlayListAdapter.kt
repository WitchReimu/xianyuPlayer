package com.example.xianyuplayer.adapter

import android.graphics.Color
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.xianyuplayer.Constant
import com.example.xianyuplayer.ItemTouchMoveListener
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.databinding.ItemPlayListBinding
import java.util.Collections

class PlayListAdapter : RecyclerView.Adapter<PlayListAdapter.ViewHolder>(), ItemTouchMoveListener {

    private val list = ArrayList<LocalFile>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayListAdapter.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemPlayListBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlayListAdapter.ViewHolder, position: Int) {
        val localFile = list[position]
        var songTitle = localFile.songTitle

        if (songTitle == Constant.defaultMetadataInfo) {
            songTitle = localFile.fileName
        }
        val spannableString = SpannableString(songTitle + " · " + localFile.singer)
        val foregroundColorSpan = ForegroundColorSpan(Color.parseColor("#D3D3D3"))
        spannableString.setSpan(
            foregroundColorSpan,
            songTitle.length,
            spannableString.length,
            SpannableString.SPAN_EXCLUSIVE_INCLUSIVE
        )
        spannableString.setSpan(
            RelativeSizeSpan(0.8f),
            songTitle.length,
            spannableString.length,
            SpannableString.SPAN_EXCLUSIVE_INCLUSIVE
        )
        holder.binding.txtAudioInfo.text = spannableString
    }

    override fun getItemCount(): Int {
        return list.size
//        return 20
    }

    fun addListData(data: List<LocalFile>) {
        list.clear()
        list.addAll(data)
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: ItemPlayListBinding) :
        RecyclerView.ViewHolder(binding.root) {
    }

    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        Collections.swap(list, fromPosition, toPosition)
        notifyItemMoved(fromPosition, toPosition)
        return true
    }

    override fun onItemRemove(position: Int): Boolean {
        list.removeAt(position)
        notifyItemRemoved(position)
        return true
    }
}