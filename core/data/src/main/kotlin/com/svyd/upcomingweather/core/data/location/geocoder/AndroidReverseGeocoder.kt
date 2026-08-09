package com.svyd.upcomingweather.core.data.location.geocoder

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.svyd.upcomingweather.core.domain.model.Coordinates
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume

/**
 * The platform's own geocoder.
 *
 * It needs a backend that some devices do not carry and that nothing reaches offline, so returning
 * nothing is an ordinary outcome rather than a failure.
 */
internal class AndroidReverseGeocoder(
    private val context: Context,
) : ReverseGeocoder {

    override suspend fun name(at: Coordinates): String? {
        if (!Geocoder.isPresent()) return null

        val address = try {
            lookUp(at)
        } catch (_: IOException) {
            null
        }

        return address?.locality ?: address?.subAdminArea ?: address?.adminArea
    }

    private suspend fun lookUp(at: Coordinates): Address? {
        val geocoder = Geocoder(context, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocation(at.latitude, at.longitude, 1) { addresses ->
                    continuation.resume(addresses.firstOrNull())
                }
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(at.latitude, at.longitude, 1)?.firstOrNull()
        }
    }
}
