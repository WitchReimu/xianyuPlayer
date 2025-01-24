package com.example.xianyuplayer.database

import kotlinx.coroutines.flow.Flow

class PlayerRepository(
    private val localScanPathDao: LocalScanPathDao,
    private val localFileDao: LocalFileDao,
    private val fileSystemPathDao: FileSystemPathDao,
    private val playFileDao: PlayFileDao
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

    suspend fun updateScanLocalPaths(localScanPaths: List<LocalScanPath>): Int {
        return localScanPathDao.updatePaths(localScanPaths)
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

    suspend fun insertAbsolutePath(fileSystemPath: FileSystemPath): Long {
        return fileSystemPathDao.insertAbsolutePath(fileSystemPath)
    }

    suspend fun insertAbsolutePaths(fileSystemPath: List<FileSystemPath>): LongArray {
        return fileSystemPathDao.insertAbsolutePaths(fileSystemPath)
    }

    fun getAllPath(): Flow<List<FileSystemPath>> {
        return fileSystemPathDao.getAllPath()
    }

    suspend fun deletePath(fileSystemPath: FileSystemPath): Int {
        return fileSystemPathDao.deletePath(fileSystemPath)
    }

    suspend fun deletePaths(fileSystemPaths: List<FileSystemPath>): Int {
        return fileSystemPathDao.deletePaths(fileSystemPaths)
    }

    suspend fun insertPlayFile(playFile: PlayFile): Long {
        return playFileDao.insertPlayFile(playFile)
    }

    suspend fun insertPlayFiles(playFiles: List<PlayFile>): LongArray {
        return playFileDao.insertPlayFiles(playFiles)
    }

    suspend fun updatePlayFile(playFile: PlayFile): Int {
        return playFileDao.updatePlayFile(playFile)
    }

    suspend fun updatePlayFiles(playFiles: List<PlayFile>): Int {
        return playFileDao.updatePlayFiles(playFiles)
    }

    fun getPlayList(): Flow<List<PlayFile>> {
        return playFileDao.getPlayList()
    }

}