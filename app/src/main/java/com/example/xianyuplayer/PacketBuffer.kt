package com.example.xianyuplayer

class PacketBuffer {

    private var bytes: ByteArray
    var byteSize: Int = 0

    constructor(byteArray: ByteArray) {
        bytes = byteArray
        byteSize = byteArray.size
    }

    fun getBuffer(): ByteArray {
        return bytes
    }
}