package com.example.xianyuplayer.adapter.helper

interface ItemTouchMoveListener {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    fun onItemRemove(position: Int): Boolean
}