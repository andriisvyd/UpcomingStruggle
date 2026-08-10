package com.svyd.upcomingweather.core.data.location.position

import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.Task
import com.svyd.upcomingweather.core.domain.failure.WeatherFailure
import com.svyd.upcomingweather.core.domain.model.Coordinates
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Google's location client.
 *
 * High accuracy is asked for because it decides which sensors may answer, not how precise the answer
 * is: with only coarse permission granted the platform rounds the result to roughly a kilometre
 * whatever is requested. Balanced accuracy leans on network-derived position, which a device without
 * one never resolves — it returns nothing and leaves the caller with wherever it used to be.
 */
internal class FusedPositionProvider(
    private val locations: FusedLocationProviderClient,
) : PositionProvider {

    private companion object {
        /** Long enough for a device that knows where it is, short enough not to look broken. */
        const val READING_TIMEOUT_MILLIS = 5_000L
    }

    /**
     * A new reading if one can be taken quickly, otherwise the last one known.
     *
     * Neither order works alone: asking only for a new reading can hang for half a minute before
     * the platform gives up, while taking the last known one first hands back wherever the device
     * used to be and never notices it has moved. So the new reading is asked for, briefly.
     */
    override suspend fun currentPosition(): Coordinates? =
        withTimeoutOrNull(READING_TIMEOUT_MILLIS) { currentReading() } ?: lastKnown()

    @Suppress("MissingPermission")
    private suspend fun currentReading(): Coordinates? = suspendCancellableCoroutine { waiting ->
        val cancellation = CancellationTokenSource()

        locations.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellation.token)
            .resumeWith(waiting)

        waiting.invokeOnCancellation { cancellation.cancel() }
    }

    @Suppress("MissingPermission")
    private suspend fun lastKnown(): Coordinates? = suspendCancellableCoroutine { waiting ->
        locations.lastLocation.resumeWith(waiting)
    }

    private fun Task<Location>.resumeWith(
        waiting: CancellableContinuation<Coordinates?>,
    ) {
        addOnSuccessListener { location ->
            waiting.resume(location?.let { Coordinates(it.latitude, it.longitude) })
        }
        addOnFailureListener { failure ->
            waiting.resumeWithException(WeatherFailure.LocationUnavailable(failure))
        }
    }
}
