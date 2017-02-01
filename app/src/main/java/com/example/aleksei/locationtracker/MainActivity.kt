package com.example.aleksei.locationtracker

import android.Manifest
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Completable
import android.location.Geocoder
import java.util.*


class MainActivity : AppCompatActivity() {

    val TAG = "DTM463"

    lateinit var googleApiClient: GoogleApiClient

    lateinit var rxPermissions: RxPermissions

    lateinit var googleConnectionCompletable: Completable

    lateinit var geocoder: Geocoder

    val locationListener = LocationListener { loc ->
        loc?.print()
        Log.d(TAG, "Address: " + geocoder.getFromLocation(loc.latitude, loc.longitude, 1))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rxPermissions = RxPermissions(this)
        geocoder = Geocoder(this, Locale.getDefault())

        googleConnectionCompletable = Completable.create { e ->
            googleApiClient = GoogleApiClient.Builder(this@MainActivity)
                    .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                        override fun onConnected(bundle: Bundle?) {
                            Log.d(TAG, "Google API connected")
                            e.onComplete()
                        }

                        override fun onConnectionSuspended(var1: Int) {
                            Log.d(TAG, "Google API connection suspended")
                        }

                    })
                    .addOnConnectionFailedListener { Log.d(TAG, "Google API Connection Failed") }
                    .addApi(LocationServices.API)
                    .build()
        }

        googleConnectionCompletable.andThen(rxPermissions.request(Manifest.permission.ACCESS_FINE_LOCATION))
                .subscribe({ t ->
                    Log.d(TAG, "Google API and Permissions were received: " + t)
                    if (t!!) {
                        val location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
                        location.print()

                        var locationRequest: LocationRequest = LocationRequest()
                        locationRequest.interval = 10000
                        locationRequest.fastestInterval = 5000
                        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        Log.d("", "")
                        LocationServices.FusedLocationApi.requestLocationUpdates(
                                googleApiClient, locationRequest, locationListener)
                    }
                },
                        { t -> Log.e(TAG, "Failed to connect Google API and get Permissions, thr:", t) }
                )
    }

    override fun onStart() {
        super.onStart()
        googleApiClient.connect()
    }

    override fun onPause() {
        super.onPause()
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener)
    }

    override fun onStop() {
        super.onStop()
        googleApiClient.disconnect()
    }

    fun Location.print() {
        Log.d(TAG, "New location(" + this.longitude + "/" + this.latitude + "), accuracy: " + this.accuracy)
    }

}
