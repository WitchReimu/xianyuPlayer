package com.example.xianyuplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.xianyuplayer.databinding.ActivityMainBinding
import com.example.xianyuplayer.fragment.HomeFragment
import com.example.xianyuplayer.fragment.LocalFileFragment

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private val permissionList by lazy { arrayListOf(Manifest.permission.INTERNET) }
    private lateinit var binding: ActivityMainBinding
    private val permissionRequestCode = 0
    private val localFileFragment by lazy { LocalFileFragment() }
    private val homeFragment by lazy { HomeFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_home -> {
                    val homeFragmentTag = "home"
                    val findFragmentByTag =
                        supportFragmentManager.findFragmentByTag(homeFragmentTag)

                    if (findFragmentByTag == null) {
                        addFragment(homeFragmentTag, homeFragment)
                    }
                    showSpecialFragment(homeFragment)
                }

                R.id.menu_mine -> {
                    val localFragmentTag = "localFile"
                    val findFragmentByTag =
                        supportFragmentManager.findFragmentByTag(localFragmentTag)

                    if (findFragmentByTag == null) {
                        addFragment(localFragmentTag, localFileFragment)
                    }
                    showSpecialFragment(localFileFragment)

                }
            }

            true
        }
        binding.sampleText.text = stringFromJNI()
    }

    override fun onStart() {
        super.onStart()
        requestPermissionList()
    }

    private fun addFragment(tag: String, fragment: Fragment) {
        supportFragmentManager.beginTransaction().add(
            binding.frameLocalContainer.id,
            fragment,
            tag
        ).commit()
    }

    private fun showSpecialFragment(showFragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()

        for (fragment in supportFragmentManager.fragments) {

            if (fragment != showFragment && fragment.isVisible) {
                transaction.hide(fragment);
            } else {
                transaction.show(showFragment)
            }
        }
        transaction.commit()
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