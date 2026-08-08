package com.svyd.upcomingweather.feature.search.mock

import com.svyd.upcomingweather.feature.search.model.CityUi
import com.svyd.upcomingweather.feature.search.model.SearchResultsUi
import com.svyd.upcomingweather.feature.search.model.SearchUiState

/** What geocoding would hand the screen. Previews and the app both read from here. */
object MockSearch {

    val budapest = CityUi(id = "budapest", name = "Budapest", subtitle = "Hungary")

    private val catalogue = listOf(
        budapest,
        CityUi("san-francisco", "San Francisco", "California, United States"),
        CityUi("san-salvador", "San Salvador", "El Salvador"),
        CityUi("san-sebastian", "San Sebastián", "Basque Country, Spain"),
        CityUi("sanaa", "Sanaa", "Yemen"),
        CityUi("sandnes", "Sandnes", "Rogaland, Norway"),
        CityUi("seattle", "Seattle", "Washington, United States"),
        CityUi("lisbon", "Lisbon", "Portugal"),
        CityUi("reykjavik", "Reykjavík", "Iceland"),
        CityUi("tokyo", "Tokyo", "Japan"),
        CityUi("vienna", "Vienna", "Austria"),
    )

    val recents = listOf(budapest, catalogue[6], catalogue[7])

    /** Frame C — "san" typed, five leads. */
    val typing = SearchUiState(
        query = "san",
        results = SearchResultsUi.Cities(search("san")),
    )

    /** Empty query: the cold cases. */
    val idle = SearchUiState(results = SearchResultsUi.Recents(recents))

    val noResults = SearchUiState(
        query = "qqq",
        results = SearchResultsUi.NoResults(query = "qqq"),
    )

    val failed = SearchUiState(query = "san", results = SearchResultsUi.Error)

    /**
     * Stands in for `/geo/1.0/direct`: the two-character minimum and the limit of ten are the
     * real thing's rules, so the mock plays by them too.
     */
    fun byId(id: String): CityUi? = catalogue.firstOrNull { it.id == id }

    fun search(query: String): List<CityUi> {
        if (query.length < 2) return emptyList()
        return catalogue
            .filter { it.name.startsWith(query, ignoreCase = true) }
            .take(10)
    }
}
