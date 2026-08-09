package com.svyd.upcomingweather.core.data.location.position

import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.svyd.upcomingweather.core.domain.failure.WeatherFailure
import com.svyd.upcomingweather.core.domain.model.Coordinates
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Google's location client.
 *
 * Balanced accuracy is only a battery hint: with coarse permission the platform throttles the
 * answer regardless of what is asked for.
 */
internal class FusedPositionProvider(
    private val locations: FusedLocationProviderClient,
) : PositionProvider {

    @Suppress("MissingPermission")
    override suspend fun currentPosition(): Coordinates? = suspendCancellableCoroutine { waiting ->
        val cancellation = CancellationTokenSource()

        locations.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellation.token)
            .addOnSuccessListener { fix ->
                waiting.resume(fix?.let { Coordinates(it.latitude, it.longitude) })
            }
            .addOnFailureListener { waiting.resumeWithException(WeatherFailure.LocationUnavailable(it)) }

        waiting.invokeOnCancellation { cancellation.cancel() }
    }
}
