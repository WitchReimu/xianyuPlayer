package com.example.xianyuplayer.vm

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.database.LocalScanPath
import com.example.xianyuplayer.database.PlayerRepository
import kotlinx.coroutines.launch

class LocalFileViewModel(private val repository: PlayerRepository) : ViewModel() {

    val scanPathUriList: LiveData<List<LocalScanPath>> = repository.getScanLocalPath().asLiveData()
    val allLocalFile = repository.getAllLocalFiles().asLiveData()

    fun insertScanPath(uri: Uri, absolutePath: String) {
        viewModelScope.launch {
            val localScanPath = LocalScanPath(absolutePath, uri.toString())
            repository.insertScanLocalPath(localScanPath)
        }
    }

    fun deleteScanPath(uri: Uri) {
        viewModelScope.launch {
            repository.deleteScanPathLocalPath(uri.toString())
        }
    }

    fun insertLocalFile(localFile: LocalFile) {
        viewModelScope.launch {
            repository.insertLocalFile(localFile)
        }
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