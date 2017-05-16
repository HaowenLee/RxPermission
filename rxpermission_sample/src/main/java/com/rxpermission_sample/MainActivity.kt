package com.rxpermission_sample

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.rxpermission.RxPermission
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnRequestPermission.setOnClickListener {
            requestPermission()
        }
    }

    private fun requestPermission() {
        RxPermission(this)
                .request(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe { granted ->
                    if (granted) {
                        Log.e("Rx", "granted")
                    } else {
                        Log.e("Rx", "not granted")
                    }
                }
    }
}