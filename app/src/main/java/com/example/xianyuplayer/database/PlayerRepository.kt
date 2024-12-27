package com.example.xianyuplayer.database

import kotlinx.coroutines.flow.Flow

class PlayerRepository(
    private val localScanPathDao: LocalScanPathDao,
    private val localFileDao: LocalFileDao
) {

    fun getScanLocalPath(): Flow<List<LocalScanPath>> {
        return localScanPathDao.getAllPath()
    }

    suspend fun insertScanLocalPath(localScanPath: LocalScanPath) {
        localScanPathDao.insertPath(localScanPath)
    }

    suspend fun insertScanLocalPaths(localScanPaths: List<LocalScanPath>): LongArray {
        return localScanPathDao.insertPath(localScanPaths)
    }

    suspend fun deleteScanPathLocalPath(uri: String) {
        localScanPathDao.deletePath(uri)
    }

    suspend fun insertLocalFile(localFile: LocalFile): Long {
        return localFileDao.insertLocalFile(localFile)
    }

    suspend fun insertLocalFile(localFile: List<LocalFile>): LongArray {
        return localFileDao.insertLocalFile(localFile)
    }

    fun getAllLocalFiles(): Flow<List<LocalFile>> {
        return localFileDao.getAllLocalFiles()
    }

    suspend fun deleteTargetLocalFile(localFile: LocalFile) {
        localFileDao.deleteSpecialLocalFile(localFile.fileName, localFile.filePath)
    }

    suspend fun deleteTargetLocalFile(fileName: String, filePath: String) {
        localFileDao.deleteSpecialLocalFile(fileName, filePath)
    }
}