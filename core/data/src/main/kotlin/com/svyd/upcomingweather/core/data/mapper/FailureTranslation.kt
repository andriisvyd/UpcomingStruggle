package com.svyd.upcomingweather.core.data.mapper

import com.svyd.upcomingweather.core.domain.failure.WeatherFailure
import retrofit2.HttpException
import java.io.IOException

/**
 * Turns what a network call throws into what the domain expects.
 *
 * Only two things can go wrong out here that the app has anything to say about: the network is not
 * there, or the provider is no help. Anything else — a decoding failure above all — is this app's
 * own defect and travels untouched, so it surfaces as the bug it is rather than as a retry button.
 */
internal inline fun <T> translateFailures(block: () -> T): T = try {
    block()
} catch (failure: IOException) {
    throw WeatherFailure.NoConnection(failure)
} catch (failure: HttpException) {
    throw WeatherFailure.ServiceUnavailable(failure)
}
