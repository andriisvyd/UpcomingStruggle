package com.svyd.upcomingweather.core.domain.repository

import com.svyd.upcomingweather.core.domain.model.Place

/**
 * Storage for the places already looked at.
 *
 * It keeps whatever list it is handed, in the order it is handed it. Which places belong on that
 * list, and how many, is decided by [com.svyd.upcomingweather.core.domain.usecase.RememberPlace].
 */
interface RecentPlacesRepository {

    suspend fun recentPlaces(): List<Place>

    suspend fun save(places: List<Place>)
}
