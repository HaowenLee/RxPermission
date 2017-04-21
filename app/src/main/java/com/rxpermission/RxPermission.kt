package com.rxpermission

class RxPermission {

//    private fun request(trigger: Observable<Nothing?>?, vararg permissions: String?): Observable<Permission> {
//        if (permissions.isEmpty()) {
//            throw IllegalArgumentException("RxPermission.request/requestEach requires at least one input permission")
//        }
//        return oneOf(trigger, pending(permissions))
//                .flatMap { requestImplementation(permissions) }
//    }
//
//    private fun pending(permissions: Array<out String?>): Observable<Nothing?> {
//        for (p in permissions) {
//
//        }
//        return Observable.just(null)
//    }
//
//    private fun oneOf(trigger: Observable<Nothing?>?, pending: Observable<Nothing?>): Observable<Nothing?> {
//        if (trigger == null) {
//            return Observable.just(null)
//        }
//        return Observable.merge(trigger, pending)
//    }
//
//    @TargetApi(Build.VERSION_CODES.M)
//    private fun requestImplementation(permissions: Array<out String>): Observable<Permission> {
//        val list = ArrayList<Observable<Permission>>(permissions.size)
//        val unRequestedPermissions = ArrayList<String>()
//
//        for (permission in permissions) {
//            if (isGranted(permission)) {
//                list.add(Observable.just(Permission(permission, true, false)))
//                continue
//            }
//
//            if (isRevoked(permission)) {
//                list.add(Observable.just(Permission(permission, false, false)))
//                continue
//            }
//        }
//    }
//
//    fun isGranted(permission: String): Boolean {
//
//    }
//
//    fun isRevoked(permission: String): Boolean {
//
//    }
}