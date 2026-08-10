package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.localsource.SelectionLocalSource
import com.svyd.upcomingweather.core.data.localsource.mapper.toStored
import com.svyd.upcomingweather.core.data.localsource.mapper.toSelectedPlace
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/**
 * The selected place, kept across launches.
 *
 * Reading from storage rather than memory is what makes the app reopen where it was left, and it
 * costs nothing here — the store already emits.
 */
internal class DefaultSelectedPlaceRepository(
    private val selections: SelectionLocalSource,
) : SelectedPlaceRepository {

    override val selected: Flow<SelectedPlace?> = selections.selection
        .map { it?.toSelectedPlace() }
        .distinctUntilChanged()

    override suspend fun select(place: SelectedPlace) {
        selections.save(place.toStored())
    }
}
