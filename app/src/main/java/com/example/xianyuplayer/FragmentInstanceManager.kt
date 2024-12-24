package com.example.xianyuplayer

import android.app.Activity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

object FragmentInstanceManager {
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

    fun showSpecialFragmentAndRemoveSpecialFragment(
        activity: FragmentActivity,
        showFragment: Fragment,
        removeFragment: Fragment
    ) {
        val manager = activity.supportFragmentManager
        val transaction = manager.beginTransaction()

        for (fragment in manager.fragments) {
            if (fragment == removeFragment) {
                transaction.remove(removeFragment)
            }
        }
        transaction.show(showFragment)
        transaction.commit()
    }
}