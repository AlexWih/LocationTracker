package com.example.aleksei.locationtracker

/**
 * Created by aleksei on 02/02/2017.
 */
data class LocationModel(
        val time: String,
        val longitude: Double,
        val latitude: Double,
        val accuracy: Float,
        val address: String?,
        val postalCode: String?
)