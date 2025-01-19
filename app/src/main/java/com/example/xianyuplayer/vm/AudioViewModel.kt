package com.example.xianyuplayer.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.xianyuplayer.MusicNativeMethod
import com.example.xianyuplayer.database.PlayerRepository

class AudioViewModel(private val repository: PlayerRepository) : ViewModel() {
    val durationLiveData = MutableLiveData<Long>(0)

    fun getAudioDuration(): Long {
        val duration = MusicNativeMethod.getInstance().getAudioDuration()
        durationLiveData.value = duration
        return duration
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
