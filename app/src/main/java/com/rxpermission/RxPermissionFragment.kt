package com.rxpermission

import android.annotation.TargetApi
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.Fragment
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor

class RxPermissionFragment : Fragment() {

    private val PERMISSIONS_REQUEST_CODE = 0x42

    private val mSubjects = HashMap<String, PublishProcessor<Permission>>()
    var mLogging = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun requestPermissions(permissions: Array<String>) {
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE)
    }

    @TargetApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode != PERMISSIONS_REQUEST_CODE) return

        val shouldShowRequestPermissionRationale = BooleanArray(permissions.size)

        for (index in 0..permissions.size) {
            shouldShowRequestPermissionRationale[index] = shouldShowRequestPermissionRationale(permissions[index])
        }

        onRequestPermissionsResult(permissions, grantResults, shouldShowRequestPermissionRationale)
    }

    fun onRequestPermissionsResult(permissions: Array<out String>, grantResults: IntArray, shouldShowRequestPermissionRationale: BooleanArray) {
        for (index in 0..permissions.size) {
            val subject = mSubjects[permissions[index]] ?: return
            mSubjects.remove(permissions[index])
            val granted = grantResults[index] == PackageManager.PERMISSION_GRANTED
            subject.onNext(Permission(permissions[index], granted, shouldShowRequestPermissionRationale[index]))
            subject.onComplete()
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun isGranted(permission: String): Boolean {
        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun isRevoked(permission: String): Boolean {
        return activity.packageManager.isPermissionRevokedByPolicy(permission, activity.packageName)
    }

    fun getProcessorByPermission(permission: String): FlowableProcessor<Permission>? {
        return mSubjects[permission]
    }

    fun containsByPermission(permission: String): Boolean {
        return mSubjects.containsKey(permission)
    }

    fun setProcessorByPermission(permission: String, processor: PublishProcessor<Permission>): PublishProcessor<Permission>? {
        return mSubjects.put(permission, processor)
    }
}