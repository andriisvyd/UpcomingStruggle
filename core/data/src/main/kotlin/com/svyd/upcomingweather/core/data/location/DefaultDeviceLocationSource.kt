package com.svyd.upcomingweather.core.data.location

import com.svyd.upcomingweather.core.data.location.geocoder.ReverseGeocoder
import com.svyd.upcomingweather.core.data.location.permission.LocationPermission
import com.svyd.upcomingweather.core.data.location.position.PositionProvider
import com.svyd.upcomingweather.core.domain.failure.WeatherFailure
import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.PlaceLabel
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Where the device is, and what that place is called.
 *
 * Whether the permission is held is knowable here; whether the user has refused it for good is not,
 * because that needs an Activity. So the refusal travels up plain, and the caller — which has the
 * Activity — decides between prompting again and sending them to settings.
 */
internal class DefaultDeviceLocationSource(
    private val permission: LocationPermission,
    private val positions: PositionProvider,
    private val geocoder: ReverseGeocoder,
) : DeviceLocationSource {

    override suspend fun currentPlace(): SelectedPlace {
        if (!permission.granted()) {
            throw WeatherFailure.LocationPermissionMissing()
        }

        val reading = positions.currentPosition() ?: throw WeatherFailure.LocationUnavailable()

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
     * The reading, cut down to the precision it actually has.
     *
     * A coarse reading is good to about a kilometre, so reporting five decimal places claims an
     * accuracy it does not have. Rounding also makes two readings from the same area equal, which is
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
        /** Two places, roughly a kilometre — the accuracy a coarse reading actually carries. */
        const val COORDINATE_PLACES = 2
    }
}
