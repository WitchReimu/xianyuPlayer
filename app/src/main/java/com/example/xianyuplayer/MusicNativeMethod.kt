package com.example.xianyuplayer

import com.example.xianyuplayer.database.MusicMetadata


class MusicNativeMethod {
    external fun getMetadata(filePath: String): Array<MusicMetadata>
    external fun startPlay()
    external fun initStream(path: String)

    companion object {
        private var instance: MusicNativeMethod? = null

        fun getInstance(): MusicNativeMethod {

            if (instance == null) {
                synchronized(this) {

                    if (instance == null) {
                        val instance = MusicNativeMethod()
                        this.instance = instance
                        return instance
                    }
                }
            }
            return instance!!
        }
    }

}