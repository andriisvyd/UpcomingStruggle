package com.svyd.upcomingweather.core.domain.repository

import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.model.SelectedPlace

/** Where places come from: named by the user, or read off the device. */
interface PlaceRepository {

    /**
     * Places matching [query], best match first. Empty when nothing matches.
     *
     * @throws com.svyd.upcomingweather.core.domain.failure.WeatherFailure.NoConnection
     * @throws com.svyd.upcomingweather.core.domain.failure.WeatherFailure.ServiceUnavailable
     */
    suspend fun search(query: String): List<Place>

    /**
     * Where the device is. Comes back unnamed when nothing could put a town to the position — the
     * forecast works either way, so failing to name a position is not a failure.
     *
     * @throws com.svyd.upcomingweather.core.domain.failure.WeatherFailure.LocationPermissionMissing
     * @throws com.svyd.upcomingweather.core.domain.failure.WeatherFailure.LocationUnavailable
     */
    suspend fun currentPlace(): SelectedPlace
}
