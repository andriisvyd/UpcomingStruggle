package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.localsource.SelectionLocalSource
import com.svyd.upcomingweather.core.data.localsource.mapper.toStored
import com.svyd.upcomingweather.core.data.localsource.mapper.toSelectedPlace
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.SelectedPlaceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn

/**
 * The selected place, kept across launches.
 *
 * Reading from storage rather than memory is what makes the app reopen where it was left, and it
 * costs nothing here — the store already emits.
 *
 * Held alive in [scope] with its last value, because every screen needs this before it can ask for
 * anything else: a forecast is looked up by place, so a page that has to read the selection off
 * disk first has not started reading its own data yet. One small object, changing rarely, gating
 * everything — the best thing in the app to keep.
 */
internal class DefaultSelectedPlaceRepository(
    private val selections: SelectionLocalSource,
    scope: CoroutineScope,
) : SelectedPlaceRepository {

    override val selected: Flow<SelectedPlace?> = selections.selection
        .map { it?.toSelectedPlace() }
        .distinctUntilChanged()
        .shareIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(
                stopTimeoutMillis = STOP_TIMEOUT,
                replayExpirationMillis = Long.MAX_VALUE,
            ),
            replay = 1,
        )

    override suspend fun select(place: SelectedPlace) {
        selections.save(place.toStored())
    }

    private companion object {
        /** Long enough to cover a screen change, short enough not to outlive an app in the background. */
        const val STOP_TIMEOUT = 5_000L
    }
}
