package com.example.xianyuplayer.vm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.database.LocalScanPath
import com.example.xianyuplayer.database.PlayerRepository
import kotlinx.coroutines.launch

class ScanDirectorySelectViewModel(private val repository: PlayerRepository) : ViewModel() {

    val insertNumber = MutableLiveData<LongArray>()

    fun insertLocalFile(localFile: LocalFile) {
        viewModelScope.launch {
            repository.insertLocalFile(localFile)
        }
    }

    fun insertLocalFile(localFile: List<LocalFile>) {
        viewModelScope.launch {
            insertNumber.value = repository.insertLocalFile(localFile)
        }
    }

    fun insertScanPath(uri: String) {
        viewModelScope.launch {
            val localScanPath = LocalScanPath(uri)
            repository.insertScanLocalPath(localScanPath)
        }
    }

    fun insertScanPath(uriList: List<LocalScanPath>) {
        viewModelScope.launch {

            repository.insertScanLocalPaths(uriList)
        }
    }

    fun insertScanPathsAndLocalFiles(uriList: List<LocalScanPath>, localFile: List<LocalFile>) {
        viewModelScope.launch {
            var resultArray = repository.insertScanLocalPaths(uriList)
            resultArray += repository.insertLocalFile(localFile)
            insertNumber.value = resultArray
        }
    }

}

class ScanDirectorySelectViewModelFactory(private val repository: PlayerRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ScanDirectorySelectViewModel::class.java)) {
            return ScanDirectorySelectViewModel(repository) as T
        }
        throw IllegalArgumentException("unknown class argument")
    }
}

