package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.cloud.SearchApi
import com.svyd.upcomingweather.core.data.location.DeviceLocationSource
import com.svyd.upcomingweather.core.data.mapper.toPlaces
import com.svyd.upcomingweather.core.data.mapper.translateFailures
import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.PlaceRepository

/** Places by name from the provider's search, and by position from the device. */
internal class DefaultPlaceRepository(
    private val api: SearchApi,
    private val locationSource: DeviceLocationSource,
) : PlaceRepository {

    override suspend fun search(query: String): List<Place> =
        translateFailures { api.search(name = query) }.toPlaces()

    override suspend fun currentPlace(): SelectedPlace = locationSource.currentPlace()
}
