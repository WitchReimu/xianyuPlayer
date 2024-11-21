package com.example.xianyuplayer.database

import kotlinx.coroutines.flow.Flow

class PlayerRepository(private val localScanPathDao: LocalScanPathDao,private val localFileDao: LocalFileDao) {

    fun getScanLocalPath(): Flow<List<LocalScanPath>> {
        return localScanPathDao.getAllPath()
    }

    suspend fun insertScanLocalPath(localScanPath: LocalScanPath) {
        localScanPathDao.insertPath(localScanPath)
    }

    fun deleteScanPathLocalPath(uri: String) {
        localScanPathDao.deletePath(uri)
    }
}