package com.example.xianyuplayer.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.xianyuplayer.database.PlayFile
import com.example.xianyuplayer.database.PlayerRepository

class GlobalViewModel(private val repository: PlayerRepository) : ViewModel() {
    var currentPlatFile: PlayFile? = null
}

class GlobalViewModelFactory(private val repository: PlayerRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocalFileViewModel::class.java)) {
            return GlobalViewModel(repository) as T
        }
        throw IllegalArgumentException("unknown class argument")
    }
}
