package com.svyd.upcomingweather.core.domain.repository

import com.svyd.upcomingweather.core.domain.model.Forecast
import com.svyd.upcomingweather.core.domain.model.ForecastRead
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import kotlinx.coroutines.flow.Flow
import java.time.Duration

/**
 * Where forecasts come from.
 *
 * Storage is the one copy, and both of these read it as it changes rather than once: a fetch does
 * not answer the caller that asked for it, it writes, and everyone reading that place is told. Two
 * screens over the same forecast therefore agree without either knowing about the other.
 *
 * Takes the whole place rather than its coordinates: providers report on a grid cell and cannot say
 * what it is called, so a name already in hand is the best one there is.
 */
interface ForecastRepository {

    /**
     * What is stored for [at], and a fetch behind it when what is stored is older than [maxAge] —
     * or whatever its age, when [force] says so, which is what a pull-to-refresh does.
     *
     * How long a forecast stays good for is the caller's rule, not this layer's.
     *
     * Never throws for a failed fetch; that arrives as [ForecastRead.Failed] and the stream goes on.
     */
    fun forecast(at: SelectedPlace, maxAge: Duration, force: Boolean): Flow<ForecastRead>

    /**
     * What is stored for [at] as it changes, and nothing else — never a fetch.
     *
     * For a reader that wants only what is already in hand: a day opened from the list is looked up
     * in the forecast the list was built from, and asking the provider again on its behalf would be
     * a second fetch for a page that has nothing new to say.
     */
    fun stored(at: SelectedPlace): Flow<Forecast?>
}
