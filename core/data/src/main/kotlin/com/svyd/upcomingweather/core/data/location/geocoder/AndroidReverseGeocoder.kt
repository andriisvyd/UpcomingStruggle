package com.svyd.upcomingweather.core.data.location.geocoder

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import com.svyd.upcomingweather.core.domain.model.Coordinates
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds

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

        val address = withTimeoutOrNull(TIMEOUT_MILLIS.milliseconds) {
            try {
                lookUp(at)
            } catch (_: IOException) {
                null
            }
        }

        return address?.locality ?: address?.subAdminArea ?: address?.adminArea
    }

    private suspend fun lookUp(at: Coordinates): Address? {
        val geocoder = Geocoder(context, Locale.getDefault())

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { waiting ->
                // Both halves of the listener must be answered. Supplying only the success half —
                // which a lambda does, since onError carries a default — leaves the caller waiting
                // forever on a device whose Geocoder has nothing to say.
                geocoder.getFromLocation(
                    at.latitude,
                    at.longitude,
                    1,
                    object : Geocoder.GeocodeListener {
                        override fun onGeocode(addresses: MutableList<Address>) {
                            waiting.resume(addresses.firstOrNull())
                        }

                        override fun onError(errorMessage: String?) {
                            waiting.resume(null)
                        }
                    },
                )
            }
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocation(at.latitude, at.longitude, 1)?.firstOrNull()
        }
    }

    private companion object {
        /** A name is a nicety; a forecast that waits on one is not. */
        const val TIMEOUT_MILLIS = 5_000L
    }
}
