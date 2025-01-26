package com.example.xianyuplayer.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.xianyuplayer.database.PlayFile
import com.example.xianyuplayer.database.PlayerRepository
import kotlinx.coroutines.launch
import java.util.LinkedList

class GlobalViewModel(private val repository: PlayerRepository) : ViewModel() {
    private val TAG = "GlobalViewModel"
    var currentPlayFile: PlayFile? = null
    val playList = LinkedList<PlayFile>()
    val playListLiveData = repository.getPlayList().asLiveData()

    fun updateCurrentPlayFile(newPlayFile: PlayFile) {

        viewModelScope.launch {
            newPlayFile.isContinuePlay = true
            newPlayFile.lastPlayPosition = 0
            val updatePlayList = if (currentPlayFile != null) {
                currentPlayFile!!.isContinuePlay = false
                currentPlayFile!!.lastPlayPosition = 0
                arrayListOf<PlayFile>(currentPlayFile!!, newPlayFile)
            } else {
                arrayListOf<PlayFile>(newPlayFile)
            }
            repository.updatePlayFiles(updatePlayList)
            currentPlayFile = newPlayFile
        }
    }

    fun getCurrentPlayFilePosition(): Int {
        currentPlayFile?.apply {
            return isCurrentPlayFile(this)
        }

        for (i in 0 until playList.size) {
            val playFile = playList[i]
            if (playFile.isContinuePlay) {
                return isCurrentPlayFile(playFile)
            }
        }
        return -1
    }

    private fun isCurrentPlayFile(playFile: PlayFile): Int {
        val index = playList.indexOf(playFile)
        val playFile1 = playList[0]
        if (playFile1 == playFile) {
            Log.i(TAG, "isCurrentPlayFile: --> equal")
        }
        return index
    }
}

class GlobalViewModelFactory(private val repository: PlayerRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GlobalViewModel::class.java)) {
            return GlobalViewModel(repository) as T
        }
        throw IllegalArgumentException("unknown class argument")
    }
}
