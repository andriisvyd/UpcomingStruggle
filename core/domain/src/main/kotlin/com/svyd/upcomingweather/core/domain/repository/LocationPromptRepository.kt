package com.svyd.upcomingweather.core.domain.repository

/**
 * Whether the system location prompt has ever been put in front of the reader.
 *
 * Half of what separates a first refusal from a permanent one; the other half is the rationale flag,
 * which only a caller holding an Activity can read.
 */
interface LocationPromptRepository {

    suspend fun promptShown()
}
