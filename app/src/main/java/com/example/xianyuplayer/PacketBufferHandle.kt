package com.example.xianyuplayer

import java.util.LinkedList

class PacketBufferHandle {

    private val MB = 1048576

    private var allPacketBufferCapacity = 0L
    private val packetBufferList = LinkedList<PacketBuffer>()
    private val packetBufferLimit = MB * 10L
    fun addPacketBuffer(packetBuffer: PacketBuffer) {
        packetBufferList.add(packetBuffer)
        allPacketBufferCapacity += packetBuffer.byteSize
    }

    fun getPacketBuffer(): PacketBuffer {
        val packetBuffer = packetBufferList.get(packetBufferList.size - 1)
        allPacketBufferCapacity -= packetBuffer.byteSize
        return packetBuffer
    }

    fun getPacketBuffer(index: Int): PacketBuffer {
        val packetBuffer = packetBufferList.get(index)
        packetBufferList.removeAt(index)
        allPacketBufferCapacity -= packetBuffer.byteSize
        return packetBuffer
    }
}