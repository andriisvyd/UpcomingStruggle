package com.svyd.upcomingweather

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.svyd.upcomingweather.feature.forecast.mock.MockForecast
import com.svyd.upcomingweather.feature.forecast.model.ForecastUiState
import com.svyd.upcomingweather.feature.search.mock.MockSearch
import com.svyd.upcomingweather.feature.search.model.CityUi
import com.svyd.upcomingweather.feature.search.model.SearchResultsUi
import com.svyd.upcomingweather.feature.search.model.SearchUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val FAKE_FETCH_MS = 1400L

/**
 * Where the app's data would come from once there is a data layer — for now, mocks.
 *
 * This is the only object in the app that holds state. Both screens are handed a finished state
 * and report gestures back here; when ViewModels arrive they replace this class and nothing in
 * either feature module has to change.
 */
@Stable
class MockAppState(
    private val scope: CoroutineScope,
    initialCity: CityUi? = null,
    private val onCityChanged: (CityUi) -> Unit = {},
) {

    /** Null until the user picks one — that is the forecast screen's empty state. */
    var selectedCity by mutableStateOf<CityUi?>(null)
        private set

    var forecast by mutableStateOf<ForecastUiState>(ForecastUiState.Empty)
        private set

    var search by mutableStateOf(SearchUiState(results = SearchResultsUi.Recents(MockSearch.recents)))
        private set

    private var recents = MockSearch.recents
    private var fetch: Job? = null

    init {
        // Survives the activity being recreated — a theme flip or a rotation should not lose the
        // case the user opened.
        initialCity?.let(::selectCity)
    }

    // ---- forecast ---------------------------------------------------------------------------

    fun selectCity(city: CityUi) {
        selectedCity = city
        recents = (listOf(city) + recents.filterNot { it.id == city.id }).take(5)
        onCityChanged(city)
        clearQuery()
        load(city)
    }

    /** The GPS button. The real one asks for coarse location and reverse-geocodes the fix. */
    fun traceMySteps() {
        selectCity(MockSearch.budapest)
    }

    /** Pull-to-refresh: content stays up, the indicator spins, the timestamp would swap. */
    fun refresh() {
        val content = forecast as? ForecastUiState.Content ?: return
        fetch?.cancel()
        fetch = scope.launch {
            forecast = content.copy(isRefreshing = true)
            delay(FAKE_FETCH_MS)
            forecast = content.copy(isRefreshing = false, offline = null)
        }
    }

    /** Retry from the full-screen error, or from the offline banner. */
    fun retry() {
        selectedCity?.let(::load)
    }

    private fun load(city: CityUi) {
        fetch?.cancel()
        fetch = scope.launch {
            forecast = ForecastUiState.Loading(city.name)
            delay(FAKE_FETCH_MS)
            forecast = MockForecast.content.copy(city = city.name)
        }
    }

    // ---- search -----------------------------------------------------------------------------

    fun query(text: String) {
        search = SearchUiState(query = text, results = resultsFor(text))
    }

    fun clearQuery() {
        query("")
    }

    fun retrySearch() {
        query(search.query)
    }

    private fun resultsFor(text: String): SearchResultsUi = when {
        text.isEmpty() -> SearchResultsUi.Recents(recents)
        text.length < 2 -> SearchResultsUi.Recents(recents)
        else -> MockSearch.search(text).let { cities ->
            if (cities.isEmpty()) SearchResultsUi.NoResults(text) else SearchResultsUi.Cities(cities)
        }
    }
}

@Composable
fun rememberMockAppState(): MockAppState {
    val scope = rememberCoroutineScope()
    var savedCityId by rememberSaveable { mutableStateOf<String?>(null) }
    return remember(scope) {
        MockAppState(
            scope = scope,
            initialCity = savedCityId?.let(MockSearch::byId),
            onCityChanged = { savedCityId = it.id },
        )
    }
}
