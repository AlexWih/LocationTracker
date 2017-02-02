package com.example.aleksei.locationtracker

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.tbruyelle.rxpermissions2.RxPermissions


class MainActivity : AppCompatActivity() {

    val TAG = "DTM463"

    private lateinit var rxPermissions: RxPermissions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rxPermissions = RxPermissions(this)
        rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION)
                .subscribe {
                    Log.d(TAG, "Starting service...")
                    val intent = Intent(this, LocationService::class.java)
                    startService(intent)
                }
    }

}
