package com.svyd.upcomingweather.core.data.localsource

/**
 * Whether the location prompt has ever been shown.
 *
 * Half of what separates "not asked yet" from "refused for good"; the other half is the rationale
 * flag, which only a caller holding an Activity can read.
 */
internal interface LocationPromptLocalSource {

    suspend fun everAsked(): Boolean

    suspend fun recordAsked()
}
