package com.example.xianyuplayer.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.example.xianyuplayer.database.PlayerRepository

class ScanSettingViewModel(private val repository: PlayerRepository) : ViewModel() {

    private val TAG = "ScanSettingViewModel"
    val scanPathsLiveData = repository.getScanLocalPath().asLiveData()
}

class ScanSettingViewModelFactory(val repository: PlayerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ScanSettingViewModel::class.java)) {
            return ScanSettingViewModel(repository) as T
        }
        throw IllegalArgumentException("unknown class argument")
    }
}
