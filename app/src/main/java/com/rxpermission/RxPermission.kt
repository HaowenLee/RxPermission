package com.rxpermission

import android.annotation.TargetApi
import android.app.Activity
import android.os.Build
import io.reactivex.Flowable
import io.reactivex.FlowableTransformer
import io.reactivex.processors.PublishProcessor

class RxPermission(activity: Activity) {

    val TAG = "RxPermission"

    val mRxPermissionFragment: RxPermissionFragment

    init {
        mRxPermissionFragment = getRxPermissionFragment(activity)
    }

    private fun getRxPermissionFragment(activity: Activity): RxPermissionFragment {
        var rxPermissionFragment = findRxPermissionFragment(activity)
        if (rxPermissionFragment == null) {
            rxPermissionFragment = RxPermissionFragment()
            val fragmentManager = activity.fragmentManager
            fragmentManager.beginTransaction()
                    .add(rxPermissionFragment, TAG)
                    .commitAllowingStateLoss()
            fragmentManager.executePendingTransactions()
        }
        return rxPermissionFragment
    }

    private fun findRxPermissionFragment(activity: Activity): RxPermissionFragment? {
        return activity.fragmentManager.findFragmentByTag(TAG) as RxPermissionFragment?
    }

    @TargetApi(Build.VERSION_CODES.M)
    fun ensure(permissions: Array<out String>): FlowableTransformer<in Nothing?, out Boolean>? {
        return FlowableTransformer {
            trigger ->
            request(trigger, permissions)
                    .buffer(permissions.size)
                    .flatMap {
                        permissions: List<Permission> ->
                        if (permissions.isEmpty()) {
                            Flowable.empty<Boolean>()
                        }
                        // Return true if all permissions are granted
                        for ((_, granted) in permissions) {
                            if (!granted) {
                                Flowable.just(false)
                            }
                        }
                        Flowable.just(true)
                    }
        }
    }

    fun ensureEach(permissions: Array<out String>): FlowableTransformer<Nothing?, Permission> {
        return FlowableTransformer { trigger -> request(trigger, permissions) }
    }

    @SuppressWarnings("WeakerAccess", "unused")
    @TargetApi(Build.VERSION_CODES.M)
    fun request(vararg permissions: String): Flowable<Boolean> {
        return Flowable.just(null).compose(ensure(permissions))
    }

    @SuppressWarnings("WeakerAccess", "unused")
    @TargetApi(Build.VERSION_CODES.M)
    fun requestEach(vararg permissions: String): Flowable<Permission> {
        return Flowable.just(null).compose(ensureEach(permissions))
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun request(trigger: Flowable<Nothing?>?, permissions: Array<out String>): Flowable<Permission> {
        if (permissions.isEmpty()) {
            throw IllegalArgumentException("RxPermission.request/requestEach requires at least one input permission")
        }
        return oneOf(trigger, pending(permissions))
                .flatMap {
                    requestImplementation(permissions)
                }
    }

    private fun oneOf(trigger: Flowable<Nothing?>?, pending: Flowable<Nothing?>): Flowable<Permission> {
        if (trigger == null) {
            return Flowable.just(null)
        }
        return Flowable.merge(trigger, pending)
    }

    private fun pending(permissions: Array<out String>): Flowable<Nothing?> {
        return if (permissions.none { mRxPermissionFragment.containsByPermission(it) }) {
            Flowable.empty()
        } else {
            Flowable.just(null)
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestImplementation(permissions: Array<out String>): Flowable<Permission> {

        val list = ArrayList<Flowable<Permission>>(permissions.size)
        val unRequestedPermissions = ArrayList<String>()

        for (permission in permissions) {
            if (isGranted(permission)) {
                list.add(Flowable.just(Permission(permission, true, false)))
                continue
            }

            if (isRevoked(permission)) {
                list.add(Flowable.just(Permission(permission, false, false)))
                continue
            }

            var processor = mRxPermissionFragment.getProcessorByPermission(permission)
            if (processor == null) {
                unRequestedPermissions.add(permission)
                processor = PublishProcessor.create()
                mRxPermissionFragment.setProcessorForPermission(permission, processor)
            }

            list.add(processor!!)

            if (unRequestedPermissions.isNotEmpty()) {
                val unRequestedPermissionsArray = unRequestedPermissions.toTypedArray()
                requestPermissionsFromFragment(unRequestedPermissionsArray)
            }
        }
        return Flowable.concat { Flowable.fromArray(list) }
    }

    fun shouldShowRequestPermissionRationale(activity: Activity, vararg permissions: String): Flowable<Boolean> {
        if (!isMarshmallow()) {
            return Flowable.just(false)
        }
        return Flowable.just(shouldShowRequestPermissionRationaleImplementation(activity, permissions))
    }

    private fun shouldShowRequestPermissionRationaleImplementation(activity: Activity, permissions: Array<out String>): Boolean {
        return permissions.none { !isGranted(it) && !activity.shouldShowRequestPermissionRationale(it) }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private fun requestPermissionsFromFragment(permissions: Array<String>) {
        mRxPermissionFragment.requestPermissions(permissions)
    }

    /**
     * Returns true if the permission is already granted.
     * Always true if SDK < 23.
     */
    @SuppressWarnings("WeakerAccess")
    fun isGranted(permission: String): Boolean {
        return isMarshmallow() && mRxPermissionFragment.isGranted(permission)
    }

    /**
     * Returns true if the permission has been revoked by a policy.
     * Always false if SDK < 23.
     */
    @SuppressWarnings("WeakerAccess")
    fun isRevoked(permission: String): Boolean {
        return isMarshmallow() && mRxPermissionFragment.isRevoked(permission)
    }

    fun isMarshmallow(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }
}