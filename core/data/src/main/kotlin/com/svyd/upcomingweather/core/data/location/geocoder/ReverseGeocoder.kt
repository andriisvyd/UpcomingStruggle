package com.svyd.upcomingweather.core.data.location.geocoder

import com.svyd.upcomingweather.core.domain.model.Coordinates

/** Puts a name to a position, or admits it cannot. */
internal interface ReverseGeocoder {

    /** Null when nothing could name the position: no service on the device, or no answer. */
    suspend fun name(at: Coordinates): String?
}
