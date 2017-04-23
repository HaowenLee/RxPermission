package com.rxpermission

import android.Manifest
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RxPermission(this)
                .request(Manifest.permission.RECORD_AUDIO)
                .subscribe { granted ->
                    if (granted) {
                        Log.e("Rx","granted")
                    }
                }
    }
}