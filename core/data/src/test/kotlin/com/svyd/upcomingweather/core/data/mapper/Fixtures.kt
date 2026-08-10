package com.svyd.upcomingweather.core.data.mapper

import kotlinx.serialization.json.Json

/** Payloads captured from Open-Meteo, so the mappers are read against what the API really sends. */
internal object Fixtures {

    val json = Json { ignoreUnknownKeys = true }

    fun read(name: String): String = checkNotNull(
        Fixtures::class.java.classLoader?.getResourceAsStream(name),
    ) { "missing fixture: $name" }.bufferedReader().use { it.readText() }
}
