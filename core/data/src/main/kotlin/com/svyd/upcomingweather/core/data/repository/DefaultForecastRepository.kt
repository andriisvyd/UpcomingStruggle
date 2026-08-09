package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.cloud.ForecastApi
import com.svyd.upcomingweather.core.data.localsource.ForecastLocalSource
import com.svyd.upcomingweather.core.data.localsource.dto.StoredForecast
import com.svyd.upcomingweather.core.data.localsource.mapper.toStored
import com.svyd.upcomingweather.core.data.localsource.mapper.toLabel
import com.svyd.upcomingweather.core.data.mapper.toForecast
import com.svyd.upcomingweather.core.data.mapper.translateFailures
import com.svyd.upcomingweather.core.domain.model.ForecastRead
import com.svyd.upcomingweather.core.domain.model.SelectedPlace
import com.svyd.upcomingweather.core.domain.repository.ForecastRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Answers from what was kept, then from the provider.
 *
 * There is no expiry: what was stored is worth drawing while the request is in flight, and the
 * provider is always asked. A stored forecast is a head start, not a substitute.
 */
internal class DefaultForecastRepository(
    private val api: ForecastApi,
    private val forecasts: ForecastLocalSource,
) : ForecastRepository {

    override fun forecast(at: SelectedPlace): Flow<ForecastRead> = flow {
        forecasts.forecast(at.coordinates)?.let { kept ->
            emit(
                value = ForecastRead.Cached(
                    forecast = kept
                        .response
                        .toForecast(label = kept.label.toLabel())
                )
            )
        }

        val response = translateFailures {
            api.forecast(
                latitude = at.coordinates.latitude,
                longitude = at.coordinates.longitude,
            )
        }

        forecasts.save(
            at = at.coordinates,
            forecast = StoredForecast(label = at.label.toStored(), response = response),
        )

        emit(value = ForecastRead.Fresh(forecast = response.toForecast(at.label)))
    }
}
