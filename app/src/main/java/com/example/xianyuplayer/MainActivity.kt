package com.example.xianyuplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.xianyuplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val permissionList by lazy { arrayListOf(Manifest.permission.INTERNET) }
    private lateinit var binding: ActivityMainBinding
    private val permissionRequestCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEntryLocalFile.setOnClickListener {
            val fragmentTag = "localFile"
            val findFragmentByTag = supportFragmentManager.findFragmentByTag(fragmentTag)
            if (findFragmentByTag == null) {
                supportFragmentManager.beginTransaction().add(binding.frameLocalContainer.id, LocalFileFragment(), fragmentTag).commit()
            }
        }

        binding.sampleText.text = stringFromJNI()
    }

    override fun onStart() {
        super.onStart()
        requestPermissionList()
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == permissionRequestCode) {

            for (grantedIndicate in grantResults.indices) {

                if (grantResults[grantedIndicate] != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "onRequestPermissionsResult: 被拒绝的权限--> ${permissions[grantedIndicate]}")
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