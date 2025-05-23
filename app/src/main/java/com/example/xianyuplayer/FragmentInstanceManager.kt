package com.example.xianyuplayer

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.database.PlayerRepository
import com.example.xianyuplayer.vm.GlobalViewModel
import com.example.xianyuplayer.vm.GlobalViewModelFactory
import java.io.File
import kotlin.io.path.absolutePathString

object FragmentInstanceManager {

    val key_album = "album"
    val key_artist = "artist"
    val key_title = "title"

    fun showSpecialFragment(activity: FragmentActivity, fragment: Fragment) {
        val manager = activity.supportFragmentManager
        val transaction = manager.beginTransaction()

        for (fragment in manager.fragments) {
            if (!fragment.isHidden) {
                transaction.hide(fragment)
            }
        }
        transaction.show(fragment)
        transaction.commit()
    }

    fun showSpecialFragment(
        transaction: FragmentTransaction,
        manager: FragmentManager,
        showFragment: Fragment
    ) {

        for (fragment in manager.fragments) {
            if (!fragment.isHidden) {
                transaction.hide(fragment)
            }
        }
        transaction.show(showFragment)
        transaction.commit()
    }

    fun showAndRemoveSpecialFragment(
        activity: FragmentActivity,
        showFragment: Fragment,
        removeFragment: Fragment?,
        allFragment: Boolean = false
    ) {
        val manager = activity.supportFragmentManager
        val transaction = manager.beginTransaction()

        for (fragment in manager.fragments) {

            if (allFragment) {

                if (showFragment != fragment) {
                    transaction.remove(fragment)
                }
            } else {

                if (removeFragment != null && fragment == removeFragment) {
                    transaction.remove(removeFragment)
                }
            }
        }
        transaction.show(showFragment)
        transaction.commit()
    }

    //不知道放到哪里 先放到这个类里
    /**
     * @param localFile 将音乐文件中的元数据赋值给变量的属性
     */
    fun getMetadata(localFile: LocalFile) {
        val metadataArray = MusicNativeMethod.getInstance()
            .getMetadata(localFile.filePath + File.separator + localFile.fileName)

        for (musicMetadata in metadataArray) {

            when (musicMetadata.key) {

                key_artist -> {
                    localFile.singer = musicMetadata.value
                }

                key_album -> {
                    localFile.albumsName = musicMetadata.value
                }

                key_title -> {
                    localFile.songTitle = musicMetadata.value
                }
            }
        }
    }

    fun getGlobalViewModel(
        storeOwner: ViewModelStoreOwner,
        repository: PlayerRepository
    ): GlobalViewModel {
        return ViewModelProvider(
            storeOwner,
            GlobalViewModelFactory(repository)
        )[Constant.globalViewModelKey, GlobalViewModel::class.java]
    }
}
