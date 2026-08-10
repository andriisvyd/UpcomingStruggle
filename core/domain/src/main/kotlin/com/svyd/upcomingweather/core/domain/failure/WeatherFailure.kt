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
     * Location was not granted.
     *
     * Whether the platform will still offer the prompt is not knowable here — it needs an Activity
     * — so the caller reads it and decides between asking again and a trip to settings.
     */
    class LocationPermissionMissing : WeatherFailure("location permission not granted")

    /** Permission is held, but there is no position: services off, or nothing reported. */
    class LocationUnavailable(cause: Throwable? = null) :
        WeatherFailure("no position available", cause)
}
