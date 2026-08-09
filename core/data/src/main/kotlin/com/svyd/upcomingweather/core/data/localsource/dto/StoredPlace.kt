package com.svyd.upcomingweather.core.data.localsource.dto

import kotlinx.serialization.Serializable

/** A searched place, written down. Mirrors the domain's `Place` without serializing it. */
@Serializable
internal data class StoredPlace(
    val id: String,
    val name: String,
    val region: String? = null,
    val country: String,
    val latitude: Double,
    val longitude: Double,
)

/** The selected place: a label and somewhere to ask about. */
@Serializable
internal data class StoredSelection(
    val label: StoredLabel,
    val latitude: Double,
    val longitude: Double,
)
