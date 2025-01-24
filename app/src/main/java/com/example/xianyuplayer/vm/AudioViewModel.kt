package com.example.xianyuplayer.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.xianyuplayer.MusicNativeMethod
import com.example.xianyuplayer.database.PlayFile
import com.example.xianyuplayer.database.PlayerRepository
import kotlinx.coroutines.launch
import java.util.LinkedList

class AudioViewModel(private val repository: PlayerRepository) : ViewModel() {
    val durationLiveData = MutableLiveData<Long>(0)
    val playList = LinkedList<PlayFile>()
    val playListLiveData = repository.getPlayList().asLiveData()
    var playPosition = 0

    fun getAudioDuration(): Long {
        val duration = MusicNativeMethod.getInstance().getAudioDuration()
        durationLiveData.value = duration
        return duration
    }

    fun updatePlayFiles(playFiles: List<PlayFile>) {
        viewModelScope.launch {
            repository.updatePlayFiles(playFiles)
        }
    }
}

class AudioViewModelFactory(private val repository: PlayerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AudioViewModel::class.java)) {
            return AudioViewModel(repository) as T
        }
        throw IllegalArgumentException("unknown class argument")
    }
}
