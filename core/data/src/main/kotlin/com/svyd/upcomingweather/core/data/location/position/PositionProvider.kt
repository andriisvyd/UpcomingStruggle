package com.svyd.upcomingweather.core.data.location.position

import com.svyd.upcomingweather.core.domain.model.Coordinates

/** Where the device is, as the platform reports it. */
internal interface PositionProvider {

    /** Null when there is no fix to be had. */
    suspend fun currentPosition(): Coordinates?
}
