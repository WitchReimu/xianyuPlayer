package com.example.xianyuplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.xianyuplayer.database.LocalFile
import com.example.xianyuplayer.database.PlayFile
import com.example.xianyuplayer.databinding.ActivityMainBinding
import com.example.xianyuplayer.fragment.HomeFragment
import com.example.xianyuplayer.fragment.LocalFileFragment
import com.example.xianyuplayer.fragment.PlayListBottomFragment
import com.example.xianyuplayer.vm.GlobalViewModel
import com.example.xianyuplayer.vm.MainViewModel
import com.example.xianyuplayer.vm.MainViewModelFactory

class MainActivity : AppCompatActivity(), MusicNativeMethod.PlayStateChangeListener {

    private val TAG = "MainActivity"
    private val permissionList by lazy { arrayListOf(Manifest.permission.INTERNET) }
    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding
    lateinit var globalViewModel: GlobalViewModel
    private val permissionRequestCode = 0
    private val localFileFragment by lazy { LocalFileFragment() }
    private val homeFragment by lazy { HomeFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val repository = PlayerApplication.getInstance().repository
        viewModel = ViewModelProvider(
            this,
            MainViewModelFactory(repository)
        )[TAG, MainViewModel::class.java]

        globalViewModel = FragmentInstanceManager.getGlobalViewModel(this, repository)

        globalViewModel.playListLiveData.observe(this) {
            globalViewModel.playList.clear()
            globalViewModel.playList.addAll(it)
        }

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
            val playListBottomFragment = PlayListBottomFragment()
            playListBottomFragment.show(supportFragmentManager, TAG)
        }

        binding.imgPlay.setOnClickListener {
            if (Constant.playStatus == Constant.playStatusStarting || Constant.playStatus == Constant.playStatusStarted) {
                pausePlay()
            } else {
                startPlay()
            }
        }

        binding.linearPlayerStatus.setOnClickListener {
            startActivity(Intent(this, AudioActivity::class.java))
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
        val playFile = PlayFile(
            file.filePath,
            file.fileName,
            0,
            true,
            file.singer,
            file.albumsName,
            file.songTitle
        )
        viewModel.insertPlayFile(playFile)
        globalViewModel.updateCurrentPlayFile(playFile)
    }

    private fun pausePlay() {
        MusicNativeMethod.getInstance().pausePlay()
    }

    private fun startPlay() {
        MusicNativeMethod.getInstance().startPlay()
    }

    override fun playStatusChangeCallback(status: Int): Unit {
        Constant.playStatus = status

        when (status) {
            Constant.playStatusStarting, Constant.playStatusStarted -> {
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
        MusicNativeMethod.getInstance().addPlayStateChangeListener(this)
    }

    override fun onResume() {
        super.onResume()
        MusicNativeMethod.getInstance().setMainActivity(this)
        Constant.displayHeightExcludeSystem = binding.root.bottom
        Constant.displayWidthExcludeSystem = binding.root.right
    }

    override fun onDestroy() {
        MusicNativeMethod.getInstance().destroyRes()
        super.onDestroy()
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