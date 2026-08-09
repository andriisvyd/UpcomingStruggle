package com.svyd.upcomingweather.core.data.location.permission

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * Coarse only.
 *
 * A forecast is drawn for a grid cell and named after a town, so a precise location would be asking for
 * more than anything in this app can use.
 */
internal class CoarseLocationPermission(
    private val context: Context,
) : LocationPermission {

    override fun granted(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
}
