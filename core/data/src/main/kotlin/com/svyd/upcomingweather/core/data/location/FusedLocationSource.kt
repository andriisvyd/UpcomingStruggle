package com.svyd.upcomingweather.core.data.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.svyd.upcomingweather.core.data.localsource.LocationPromptLocalSource
import com.svyd.upcomingweather.core.data.location.geocoder.ReverseGeocoder
import com.svyd.upcomingweather.core.domain.failure.WeatherFailure
import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Google's location client, and whatever can name what it returns.
 *
 * Whether the permission is held is knowable here; whether the user has refused it for good is not,
 * because that needs an Activity. So the failure carries whether the prompt has ever been shown,
 * and the caller — which has the Activity — decides between prompting and sending them to settings.
 */
internal class FusedLocationSource(
    private val context: Context,
    private val locations: FusedLocationProviderClient,
    private val geocoder: ReverseGeocoder,
    private val prompts: LocationPromptLocalSource,
) : DeviceLocationSource {

    override suspend fun currentPlace(): SelectedPlace {
        if (!hasPermission()) {
            throw WeatherFailure.LocationPermissionMissing(askedBefore = prompts.everAsked())
        }

        val fix = currentLocation() ?: throw WeatherFailure.LocationUnavailable()
        val reading = Coordinates(fix.latitude, fix.longitude)

        // Named from the reading itself, then rounded: a kilometre of slack is enough to pick the
        // wrong side of a district boundary, and the accurate figure is right here.
        return SelectedPlace(label = labelFor(reading), coordinates = reading.rounded())
    }

    /**
     * A name for the position, or none.
     *
     * A position nothing can name still has a correct forecast, so this is not a failure — the
     * screen says "current location" instead of a town.
     */
    private suspend fun labelFor(reading: Coordinates): PlaceLabel =
        geocoder.name(reading)?.let(PlaceLabel::Named) ?: PlaceLabel.NamelessCurrentLocation

    /**
     * Coarse only. A forecast is drawn for a grid cell and named after a town, so a precise fix
     * would be asking for more than anything here can use.
     */
    private fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED

    @Suppress("MissingPermission")
    private suspend fun currentLocation(): Location? = suspendCancellableCoroutine { continuation ->
        val cancellation = CancellationTokenSource()

        locations.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancellation.token)
            .addOnSuccessListener { continuation.resume(it) }
            .addOnFailureListener {
                continuation.resumeWithException(WeatherFailure.LocationUnavailable(it))
            }

        continuation.invokeOnCancellation { cancellation.cancel() }
    }

    /**
     * The reading, cut down to the precision it actually has.
     *
     * A coarse fix is good to about a kilometre, so reporting five decimal places claims an accuracy
     * the reading does not have. Rounding also makes two fixes from the same area equal, which is
     * what stops standing still from looking like moving.
     */
    private fun Coordinates.rounded(): Coordinates = Coordinates(
        latitude = latitude.roundTo(COORDINATE_PLACES),
        longitude = longitude.roundTo(COORDINATE_PLACES),
    )

    private fun Double.roundTo(places: Int): Double {
        val factor = 10.0.pow(places)
        return (this * factor).roundToLong() / factor
    }

    private companion object {
        /** Two places, roughly a kilometre — the accuracy a coarse fix actually carries. */
        const val COORDINATE_PLACES = 2
    }
}