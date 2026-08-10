package com.svyd.upcomingweather.core.data.repository

import com.svyd.upcomingweather.core.data.localsource.LocationPromptLocalSource
import com.svyd.upcomingweather.core.domain.repository.LocationPromptRepository

internal class DefaultLocationPromptRepository(
    private val prompts: LocationPromptLocalSource,
) : LocationPromptRepository {

    override suspend fun promptShown() {
        prompts.recordAsked()
    }
}
