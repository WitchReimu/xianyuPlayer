package com.example.xianyuplayer

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.xianyuplayer.database.LocalScanPath
import com.example.xianyuplayer.database.PlayerRepository
import kotlinx.coroutines.launch

class LocalFileViewModel(private val repository: PlayerRepository) : ViewModel() {

    val scanPathUriList: LiveData<List<LocalScanPath>> = repository.getScanLocalPath().asLiveData()

    fun insertScanPath(uri: Uri) {
        viewModelScope.launch {
            val localScanPath = LocalScanPath(uri.toString())
            repository.insertScanLocalPath(localScanPath)
        }
    }

    fun deleteScanPath(uri: Uri) {
        repository.deleteScanPathLocalPath(uri.toString())
    }

}

class LocalFileViewModelFactory(private val repository: PlayerRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocalFileViewModel::class.java)) {
            return LocalFileViewModel(repository) as T
        }
        throw IllegalArgumentException("unknown class argument")
    }
}