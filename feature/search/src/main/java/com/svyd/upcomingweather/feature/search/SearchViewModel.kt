package com.svyd.upcomingweather.feature.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.svyd.upcomingweather.core.domain.failure.WeatherFailure
import com.svyd.upcomingweather.core.domain.model.Place
import com.svyd.upcomingweather.core.domain.model.SearchOutcome
import com.svyd.upcomingweather.core.domain.usecase.ObserveRecentPlaces
import com.svyd.upcomingweather.core.domain.usecase.SearchPlaces
import com.svyd.upcomingweather.core.domain.usecase.SelectCurrentPlace
import com.svyd.upcomingweather.core.domain.usecase.SelectPlace
import com.svyd.upcomingweather.feature.search.mapper.toCityUi
import com.svyd.upcomingweather.feature.search.model.CityUi
import com.svyd.upcomingweather.feature.search.model.LocationNoticeUi
import com.svyd.upcomingweather.feature.search.model.SearchResultsUi
import com.svyd.upcomingweather.feature.search.model.SearchUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Naming a city.
 *
 * How long to wait before asking is decided here, because it is about typing rather than about
 * searching; what counts as a search at all is decided by [SearchPlaces].
 */
internal class SearchViewModel(
    private val searchPlaces: SearchPlaces,
    private val selectPlace: SelectPlace,
    private val selectCurrentPlace: SelectCurrentPlace,
    private val recentPlaces: ObserveRecentPlaces,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val retries = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val location = MutableStateFlow<LocationNoticeUi?>(null)

    /**
     * Whatever is currently listed, so a tapped row can be turned back into the place it names.
     *
     * Rows come from two sources — a search and the places looked at before — and both are tappable,
     * so both have to be remembered.
     */
    private var shown: List<Place> = emptyList()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val state: StateFlow<SearchUiState> =
        combine(query, searches(), location) { typed, results, locating ->
            SearchUiState(query = typed, results = results, location = locating)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_GRACE),
            initialValue = SearchUiState(),
        )

    fun query(text: String) {
        query.value = text
    }

    fun clearQuery() {
        query.value = ""
    }

    fun retry() {
        retries.tryEmit(Unit)
    }

    /** Settles on a row. The forecast finds out by observing, so nothing is handed back. */
    fun select(city: CityUi, then: () -> Unit) {
        val place = shown.firstOrNull { it.id == city.id } ?: return
        viewModelScope.launch {
            selectPlace(place)
            then()
        }
    }

    /**
     * [canAskAgain] is the half of the permission picture only a caller holding an Activity can
     * read, so it is passed in rather than looked up here.
     */
    fun useCurrentLocation(canAskAgain: Boolean, then: () -> Unit) {
        viewModelScope.launch {
            selectCurrentPlace()
                .onSuccess {
                    location.value = null
                    then()
                }
                .onFailure { cause -> location.value = cause.asNotice(canAskAgain) }
        }
    }

    /** Anything other than these two is a defect rather than something to explain to a reader. */
    private fun Throwable.asNotice(canAskAgain: Boolean): LocationNoticeUi? = when (this) {
        is WeatherFailure.LocationPermissionMissing -> LocationNoticeUi.Refused(canAskAgain)
        is WeatherFailure.LocationUnavailable -> LocationNoticeUi.Unavailable
        else -> null
    }

    /**
     * A new query restarts the outer level after the typing settles; a retry restarts only the inner
     * one, so asking again does not wait out another pause.
     *
     * An empty field is not something anyone is in the middle of typing: it is the state the screen
     * opens in and the state clearing it returns to, and the answer to it is already in hand. So it
     * waits out no pause, and the recents are on screen with the screen.
     */
    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    private fun searches(): Flow<SearchResultsUi> =
        query.debounce { text -> if (text.isBlank()) 0L else TYPING_PAUSE }.flatMapLatest { text ->
            if (text.isBlank()) {
                recents()
            } else {
                retries.onStart { emit(Unit) }.flatMapLatest { results(text) }
            }
        }

    private fun results(text: String) = flow {
        searchPlaces(text)
            .onSuccess { outcome ->
                when (outcome) {
                    // Too little typed to search on, so the list falls back to what was looked at
                    // before — including everything filed since, which is why it is followed rather
                    // than read.
                    SearchOutcome.TooShort -> emitAll(recents())

                    SearchOutcome.NoMatch -> emit(SearchResultsUi.NoResults(text))

                    is SearchOutcome.Found -> emit(SearchResultsUi.Cities(outcome.places.listed()))
                }
            }
            .onFailure { emit(SearchResultsUi.Error) }
    }

    private fun recents(): Flow<SearchResultsUi> =
        recentPlaces().map { places -> SearchResultsUi.Recents(places.listed()) }

    private fun List<Place>.listed(): List<CityUi> {
        shown = this
        return map(Place::toCityUi)
    }

    private companion object {
        const val TYPING_PAUSE = 300L
        const val SUBSCRIPTION_GRACE = 5_000L
    }
}
