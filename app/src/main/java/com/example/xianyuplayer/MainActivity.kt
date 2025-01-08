package com.example.xianyuplayer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.xianyuplayer.Constant.playStatus
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.databinding.ActivityMainBinding
import com.example.xianyuplayer.fragment.HomeFragment
import com.example.xianyuplayer.fragment.LocalFileFragment
import com.example.xianyuplayer.fragment.PlayListBottomFragment
import com.example.xianyuplayer.vm.MainViewModel
import com.example.xianyuplayer.vm.MainViewModelFactory

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val permissionList by lazy { arrayListOf(Manifest.permission.INTERNET) }
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    private val permissionRequestCode = 0
    private val localFileFragment by lazy { LocalFileFragment() }
    private val homeFragment by lazy { HomeFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(PlayerApplication.getInstance().repository)
        )[TAG, MainViewModel::class.java]

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    val homeFragmentTag = "home"
                    val findFragmentByTag =
                        supportFragmentManager.findFragmentByTag(homeFragmentTag)

                    if (findFragmentByTag == null) {
                        addFragment(homeFragmentTag, homeFragment)
                    }
                    FragmentInstanceManager.showSpecialFragment(this, homeFragment)
                }

                R.id.menu_mine -> {
                    val localFragmentTag = "localFile"
                    val findFragmentByTag =
                        supportFragmentManager.findFragmentByTag(localFragmentTag)

                    if (findFragmentByTag == null) {
                        addFragment(localFragmentTag, localFileFragment)
                    }
                    FragmentInstanceManager.showSpecialFragment(this, localFileFragment)
                }
            }
            true
        }

        binding.imgBtnPlayList.setOnClickListener {
            val playListBottomFragment = PlayListBottomFragment(viewModel)
            playListBottomFragment.show(supportFragmentManager, TAG)
        }

        binding.imgPlay.setOnClickListener {
            if (playStatus == 3 || playStatus == 4) {
                pausePlay()
            } else {
                startPlay()
            }
        }
    }

    fun starPlayCallBack(file: LocalFile) {
        val value = viewModel.playerListLiveData.value
        val absolutePath = file.filePath + file.fileName
        val albumCoverByte =
            MusicNativeMethod.getInstance().getAudioAlbum(absolutePath = absolutePath)

        if (albumCoverByte.isNotEmpty()) {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565
            val bitmap =
                BitmapFactory.decodeByteArray(albumCoverByte, 0, albumCoverByte.size, options)

            if (bitmap != null) {
                binding.imgAudioAlbum.setImageBitmap(bitmap)
            }
        }
        binding.txtAudioName.text = file.songTitle

        if (value!!.indexOf(file) != -1) {
            value.remove(file)
        }
        value.addFirst(file)
    }

    private fun pausePlay() {
        MusicNativeMethod.getInstance().pausePlay()
    }

    private fun startPlay() {
        MusicNativeMethod.getInstance().startPlay()
    }

    /**
     * native 调用该函数
     * 播放状态发生变化后，c++会调用该函数将播放状态的值赋值给变量形参status
     * @param status 播放状态发生改变后的值
     */
    fun playStatusChangeCallback(status: Int): Unit {
        Constant.playStatus = status

        when (status) {
            3, 4 -> {
                binding.imgPlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.pause_circle_24
                    )
                )
            }

            else -> {
                binding.imgPlay.setImageDrawable(
                    ContextCompat.getDrawable(
                        this,
                        R.drawable.start_circle_24
                    )
                )
            }
        }
    }

    override fun onStart() {
        super.onStart()
        requestPermissionList()
    }

    override fun onResume() {
        super.onResume()

    }

    private fun addFragment(tag: String, fragment: Fragment) {
        supportFragmentManager.beginTransaction().add(
            binding.frameMainContainer.id,
            fragment,
            tag
        ).commit()
    }

    private fun requestPermissionList() {
        if (Build.VERSION.SDK_INT <= 29) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else if (Build.VERSION.SDK_INT <= 32) {
            permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        requestPermissions(permissionList.toTypedArray(), permissionRequestCode)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        if (requestCode == permissionRequestCode) {

            for (grantedIndicate in grantResults.indices) {

                if (grantResults[grantedIndicate] != PackageManager.PERMISSION_GRANTED) {
                    Log.e(
                        TAG,
                        "onRequestPermissionsResult: 被拒绝的权限--> ${permissions[grantedIndicate]}"
                    )
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    external fun stringFromJNI(): String

    companion object {
        init {
            System.loadLibrary("xianyuplayer")
        }
    }
}