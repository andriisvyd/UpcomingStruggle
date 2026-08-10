package com.svyd.upcomingweather.core.data.location.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Either grade of location will do.
 *
 * A forecast covers a grid cell, so an approximate position is enough to answer with. Precise is
 * accepted as well because a device whose only source is GPS — an emulator, or anywhere out of reach
 * of wifi and cell — cannot produce even an approximate position without it.
 */
internal class AndroidLocationPermission(
    private val context: Context,
) : LocationPermission {

    override fun granted(): Boolean = GRADES.any { permission ->
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    private companion object {
        val GRADES = listOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
        )
    }
}
