package com.example.xianyuplayer.vm

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.xianyuplayer.database.LocalScanPath
import com.example.xianyuplayer.database.PlayerRepository
import kotlinx.coroutines.launch

class CustomScanViewModel(private val repository: PlayerRepository) : ViewModel() {

    private val TAG = "CustomScanViewModel"

    fun updateScanPathList(list: List<LocalScanPath>) {
        viewModelScope.launch {
            repository.updateScanLocalPaths(list)
        }
    }
}

class CustomScanViewModelFactory(private val repository: PlayerRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(CustomScanViewModel::class.java)) {
            return CustomScanViewModel(repository) as T
        }
        throw IllegalArgumentException("unknown class argument")
    }
}
