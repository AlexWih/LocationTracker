package com.example.aleksei.locationtracker

import android.app.Service
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import java.text.SimpleDateFormat
import java.util.*

class LocationService : Service() {

    val TAG = "DTM463"

    private lateinit var googleApiClient: GoogleApiClient

    private lateinit var geocoder: Geocoder

    private lateinit var firebaseDatabaseReference: DatabaseReference

    private lateinit var varsdf: SimpleDateFormat

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        geocoder = Geocoder(this, Locale.getDefault())
        firebaseDatabaseReference = FirebaseDatabase.getInstance().reference
        varsdf = SimpleDateFormat("dd.MM HH:mm:ss", Locale.getDefault())

        Completable.create { e ->
            googleApiClient = GoogleApiClient.Builder(this)
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
        }.andThen(Observable.create<Location> { source ->
            val location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            location.print()

            val locationRequest: LocationRequest = LocationRequest()
            locationRequest.interval = 10000
            locationRequest.fastestInterval = 5000
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient,
                    locationRequest) {
                source.onNext(it)
            }
        })
                .observeOn(Schedulers.computation())
                .map {
                    var geoAddress: String? = null
                    var address: Address? = null
                    // todo get rid of smells
                    try {
                        address = geocoder.getFromLocation(it.latitude, it.longitude, 1)[0]
                        val stringBuilder = StringBuilder()
                        for (i in 0..address.maxAddressLineIndex - 1) {
                            stringBuilder.append(address.getAddressLine(i))
                            stringBuilder.append(" ")
                        }
                        geoAddress = stringBuilder.toString()
                    } catch (ex: Throwable) {
                        Log.e(TAG, "Swallow exception: ", ex)
                    }
                    LocationModel(
                            varsdf.format(Date()),
                            it.longitude,
                            it.latitude,
                            it.accuracy,
                            geoAddress,
                            address?.postalCode)
                }
                .subscribe({
                    // todo get rid of smells
                    try {
                        firebaseDatabaseReference.child("locations").push().setValue(
                                it,
                                DatabaseReference.CompletionListener({ p0: DatabaseError?, p1: DatabaseReference? ->
                                    Log.d(TAG, "err: " + p0)
                                })
                        )
                        Log.d(TAG, "Address: " + it)
                    } catch (e: Throwable) {
                        Log.e(TAG, "Failed to push location2: ", e)
                    } finally {

                    }
                }, {
                    Log.e(TAG, "Failed to push location: ", it)
                })
        googleApiClient.connect()
        return Service.START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun Location.print() {
        Log.d(TAG, "New location(" + this.longitude + "/" + this.latitude + "), accuracy: " + this.accuracy)
    }

}
