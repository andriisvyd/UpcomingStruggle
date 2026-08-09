package com.svyd.upcomingweather.core.data.location

import com.svyd.upcomingweather.core.domain.model.SelectedPlace

/** Where the device is, and what that place is called. */
internal interface DeviceLocationSource {

    /**
     * @throws com.svyd.upcomingweather.core.domain.failure.WeatherFailure.LocationPermissionMissing
     * @throws com.svyd.upcomingweather.core.domain.failure.WeatherFailure.LocationUnavailable
     */
    suspend fun currentPlace(): SelectedPlace
}
