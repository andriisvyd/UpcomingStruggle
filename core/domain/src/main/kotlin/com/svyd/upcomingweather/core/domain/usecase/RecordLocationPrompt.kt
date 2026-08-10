package com.svyd.upcomingweather.core.domain.usecase

import com.svyd.upcomingweather.core.domain.failure.catching
import com.svyd.upcomingweather.core.domain.repository.LocationPromptRepository

/**
 * Notes that the system prompt has been shown.
 *
 * Only the component that launches it knows this happened, and nothing else can find out
 * afterwards: the platform reports a refusal the same way whether or not it was ever asked.
 */
class RecordLocationPrompt(private val prompts: LocationPromptRepository) {

    suspend operator fun invoke(): Result<Unit> = catching { prompts.promptShown() }
}
