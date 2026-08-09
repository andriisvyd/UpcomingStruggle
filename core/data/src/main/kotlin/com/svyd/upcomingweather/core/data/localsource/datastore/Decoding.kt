package com.svyd.upcomingweather.core.data.localsource.datastore

import kotlinx.serialization.json.Json

/**
 * Reads a stored value, reporting anything unreadable as absent.
 *
 * A store written by an older build is not a reason to fail to start: dropping the value and
 * fetching again is the whole recovery, and the alternative is an app that cannot open.
 */
internal inline fun <reified T> Json.decodeOrNull(raw: String): T? = try {
    decodeFromString<T>(raw)
} catch (_: IllegalArgumentException) {
    null
}
