package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.cloud.ForecastApi
import com.svyd.upcomingweather.core.data.localsource.ForecastLocalSource
import com.svyd.upcomingweather.core.data.localsource.dto.StoredForecast
import com.svyd.upcomingweather.core.data.localsource.mapper.toLabel
import com.svyd.upcomingweather.core.data.localsource.mapper.toStored
import com.svyd.upcomingweather.core.data.mapper.toForecast
import com.svyd.upcomingweather.core.data.mapper.translateFailures
import com.svyd.upcomingweather.core.domain.model.Coordinates
import com.svyd.upcomingweather.core.domain.model.Forecast
import com.svyd.upcomingweather.core.domain.model.ForecastRead
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.time.Clock
import java.time.Duration
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

/**
 * Storage answers, and a fetch writes to storage.
 *
 * The two are not steps in one sequence. Reading is a stream of whatever is kept; fetching is
 * something done alongside it, and its result arrives the same way everything else does — as a save
 * that storage reports. That is what lets a second reader of the same place see the answer to a
 * request it never made.
 *
 * One reading per place, kept alive in [scope] and holding its last value. Storage being the shared
 * copy is what makes two screens agree; it is this that makes the second one quick. Without it each
 * new reader would open the store, decode everything kept and map it again for itself, which is a
 * disk read and a parse in the time it takes a screen to draw its first frame.
 */
internal class DefaultForecastRepository(
    private val api: ForecastApi,
    private val forecasts: ForecastLocalSource,
    private val clock: Clock,
    private val scope: CoroutineScope,
) : ForecastRepository {

    private val readings = ConcurrentHashMap<String, Flow<Forecast?>>()

    /**
     * The last reading is kept for good, while the work that produces it is not: the upstream stops
     * once nothing is watching, and what it last said stays behind for whoever asks next. A screen
     * opened a minute later still gets its answer without touching the disk.
     */
    override fun stored(at: SelectedPlace): Flow<Forecast?> =
        readings.computeIfAbsent(at.coordinates.key()) {
            forecasts.forecast(at.coordinates)
                .map { kept -> kept?.asForecast() }
                .shareIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(
                        stopTimeoutMillis = STOP_TIMEOUT,
                        replayExpirationMillis = Long.MAX_VALUE,
                    ),
                    replay = 1,
                )
        }

    override fun forecast(
        at: SelectedPlace,
        maxAge: Duration,
        force: Boolean,
    ): Flow<ForecastRead> = callbackFlow {
        // Decided once, on the first thing storage says, rather than on every change: the save this
        // fetch makes is itself a change, and asking again on the strength of it would never stop.
        var asked = false

        val reading = launch {
            stored(at).collect { forecast ->
                if (forecast != null) {
                    val old = force || Duration.between(forecast.retrievedAt, clock.instant()) >= maxAge
                    send(if (old && !asked) ForecastRead.Stale(forecast) else ForecastRead.Cached(forecast))
                }

                if (!asked && (forecast == null || force ||
                        Duration.between(forecast.retrievedAt, clock.instant()) >= maxAge)
                ) {
                    asked = true
                    launch { fetch(at) }
                }
            }
        }

        awaitClose { reading.cancel() }
    }

    /** Writes what it gets, and reports what it does not. Either way the stream stays open. */
    private suspend fun ProducerScope<ForecastRead>.fetch(at: SelectedPlace) {
        try {
            val response = translateFailures {
                api.forecast(
                    latitude = at.coordinates.latitude,
                    longitude = at.coordinates.longitude,
                )
            }

            forecasts.save(
                at = at.coordinates,
                forecast = StoredForecast(
                    label = at.label.toStored(),
                    response = response,
                    savedAt = clock.instant().toEpochMilli(),
                ),
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Throwable) {
            send(ForecastRead.Failed(failure))
        }
    }

    private fun StoredForecast.asForecast(): Forecast =
        response.toForecast(label.toLabel(), retrievedAt = Instant.ofEpochMilli(savedAt))

    private fun Coordinates.key(): String = "$latitude,$longitude"

    private companion object {
        /** Long enough to cover a screen change, short enough not to outlive an app in the background. */
        const val STOP_TIMEOUT = 5_000L
    }
}
