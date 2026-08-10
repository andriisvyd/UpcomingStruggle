package com.svyd.upcomingweather.core.domain.repository

import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import kotlinx.coroutines.flow.Flow

/**
 * The place the app is currently reporting on.
 *
 * A stream rather than a getter: the forecast follows whatever is selected instead of being told
 * about each change by whoever made it.
 */
interface SelectedPlaceRepository {

    /** Null until a place has been chosen for the first time. */
    val selected: Flow<SelectedPlace?>

    suspend fun select(place: SelectedPlace)
}
