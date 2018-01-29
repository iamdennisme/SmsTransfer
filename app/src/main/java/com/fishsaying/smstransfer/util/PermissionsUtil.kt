package com.fishsaying.smstransfer.util

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat

import java.util.ArrayList

/**
 * Created by ${dennis} on 5/31/16.
 */
object PermissionsUtil {
    /*
     * check permissions
     * Returns a List of unauthorized
     * */
    fun checkPermissions(activity: Activity, permissions: List<String>): List<String> {
        val unGranted = permissions.filterNot { checkPermission(activity, it) }
        return unGranted
    }

    /*
     * check single permission
     * */
    fun checkPermission(activity: Activity, permissions: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return activity.checkSelfPermission(permissions) == PackageManager.PERMISSION_GRANTED
        } else {
            //if sdk <23,always return true,Do not carry out dynamic authorization
            return true
        }
    }

    fun requestPermissions(activity: Activity, permissions: List<String>, requestCode: Int) {

        val permissionsArr = permissions.toTypedArray()
        request(activity, permissionsArr, requestCode)
    }

    /*
    * request permission
    * */
    fun requsetPermission(activity: Activity, permission: String, requestCode: Int) {
        val permissionsArr = arrayOf(permission)
        request(activity, permissionsArr, requestCode)
    }

    private fun request(activity: Activity, permissions: Array<String>, requestCode: Int) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode)
    }

    /*
    * check permissions
    * Returns a List of unauthorized
    * */
    fun checkPermissionsRequest(permissions: Array<String>, grantResult: IntArray): List<String> {
        val unGranted = ArrayList<String>()
        for (i in permissions.indices) {
            if (grantResult[i] != PackageManager.PERMISSION_GRANTED) {
                unGranted.add(permissions[i])
            }
        }
        return unGranted
    }

    fun checkPermissionRequest(permissions: Array<String>, grantResult: IntArray): Boolean {
        return grantResult[0] == PackageManager.PERMISSION_GRANTED
    }

}
