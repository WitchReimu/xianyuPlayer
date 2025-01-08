package com.example.xianyuplayer.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.database.PlayerRepository
import java.util.LinkedList

class MainViewModel(private val repository: PlayerRepository) : ViewModel() {
    val playerListLiveData = MutableLiveData<LinkedList<LocalFile>>(LinkedList())
}

class MainViewModelFactory(private val repository: PlayerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("unknown class argument")
    }
}
