package com.example.xianyuplayer.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.database.PlayFile
import com.example.xianyuplayer.database.PlayerRepository
import kotlinx.coroutines.flow.Flow

class PlayListBottomViewModel(private val repository: PlayerRepository) : ViewModel() {

    val playList = repository.getPlayList().asLiveData()

}

class PlayListBottomVMFactory(private val repository: PlayerRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayListBottomViewModel::class.java)) {
            return PlayListBottomViewModel(repository) as T
        }
        throw IllegalArgumentException("unknown class argument")
    }
}
