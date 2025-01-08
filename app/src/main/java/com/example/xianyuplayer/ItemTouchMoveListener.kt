package com.example.xianyuplayer

interface ItemTouchMoveListener {
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    fun onItemRemove(position: Int): Boolean
}