package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.cloud.ForecastApi
import com.svyd.upcomingweather.core.data.localsource.ForecastLocalSource
import com.svyd.upcomingweather.core.data.localsource.dto.StoredForecast
import com.svyd.upcomingweather.core.data.localsource.mapper.toLabel
import com.svyd.upcomingweather.core.data.localsource.mapper.toStored
import com.svyd.upcomingweather.core.data.mapper.toForecast
import com.svyd.upcomingweather.core.data.mapper.translateFailures
import com.svyd.upcomingweather.core.domain.model.ForecastRead
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Clock
import java.time.Duration
import java.time.Instant

/**
 * Answers from what was kept, then from the provider.
 *
 * A stored forecast younger than the age asked for is the whole answer and stops there. An older one
 * is still worth drawing while the request that replaces it is in flight.
 */
internal class DefaultForecastRepository(
    private val api: ForecastApi,
    private val forecasts: ForecastLocalSource,
    private val clock: Clock,
) : ForecastRepository {

    override fun forecast(
        at: SelectedPlace,
        maxAge: Duration,
        force: Boolean,
    ): Flow<ForecastRead> = flow {
        val kept = forecasts.forecast(at.coordinates)

        if (kept != null) {
            val keptAt = Instant.ofEpochMilli(kept.savedAt)
            val forecast = kept.response.toForecast(kept.label.toLabel(), retrievedAt = keptAt)

            if (!force && Duration.between(keptAt, clock.instant()) < maxAge) {
                emit(ForecastRead.Cached(forecast))
                return@flow
            }

            emit(ForecastRead.Stale(forecast))
        }

        val response = translateFailures {
            api.forecast(
                latitude = at.coordinates.latitude,
                longitude = at.coordinates.longitude,
            )
        }
        val retrievedAt = clock.instant()

        forecasts.save(
            at = at.coordinates,
            forecast = StoredForecast(
                label = at.label.toStored(),
                response = response,
                savedAt = retrievedAt.toEpochMilli(),
            ),
        )

        emit(ForecastRead.Fresh(response.toForecast(at.label, retrievedAt)))
    }
}
