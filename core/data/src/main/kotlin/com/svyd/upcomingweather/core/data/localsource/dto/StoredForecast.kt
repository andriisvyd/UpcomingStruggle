package com.svyd.upcomingweather.core.data.localsource.dto

import com.svyd.upcomingweather.core.data.cloud.dto.ForecastResponse
import kotlinx.serialization.Serializable

/** The response as it arrived, so reading it back runs the same mapper the live call does. */
@Serializable
internal data class StoredForecast(
    val label: StoredLabel,
    val response: ForecastResponse,
    /** When it was fetched, as epoch milliseconds. Read back to decide whether it is still good. */
    val savedAt: Long,
)

/** The domain's label in a form that can be written down. */
@Serializable
internal data class StoredLabel(
    /** Null for a position nothing could name. */
    val name: String? = null,
)
