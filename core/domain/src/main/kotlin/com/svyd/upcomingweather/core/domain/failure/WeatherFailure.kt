package com.svyd.upcomingweather.core.domain.failure

/**
 * The ways this app is expected to fail.
 *
 * Anything the data layer can see coming is translated into one of these, so nothing above it has
 * to know what an `UnknownHostException` or a 503 is. A failure that is *not* one of these is a bug
 * rather than a forecast that did not arrive, and the difference is worth being able to tell.
 *
 * The messages are for logs. What the reader is told lives with the strings.
 */
sealed class WeatherFailure(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** No usable network: nothing resolved, nothing connected, or nothing answered in time. */
    class NoConnection(cause: Throwable? = null) :
        WeatherFailure("no route to the provider", cause)

    /** The provider was reached and was no help: refused, broken, or answered unreadably. */
    class ServiceUnavailable(cause: Throwable? = null) :
        WeatherFailure("provider did not answer usefully", cause)

    /**
     * Location was never granted.
     *
     * [askedBefore] is the half of the picture the data layer owns. Combined with the rationale
     * flag, which only a UI holding an Activity can read, it separates "has not been asked yet"
     * from "said no for good" — and those two want a prompt and a trip to settings respectively.
     */
    class LocationPermissionMissing(val askedBefore: Boolean) :
        WeatherFailure("location permission not granted, askedBefore=$askedBefore")

    /** Permission is held, but there is no position: services off, or no fix. */
    class LocationUnavailable(cause: Throwable? = null) :
        WeatherFailure("no position available", cause)
}
