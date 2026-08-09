package com.svyd.upcomingweather.core.data.localsource

import com.svyd.upcomingweather.core.data.localsource.dto.StoredSelection
import kotlinx.coroutines.flow.Flow

/**
 * The place being reported on.
 *
 * A stream, because the forecast follows the selection rather than being told about each change.
 */
internal interface SelectionLocalSource {

    /** Null until something has been selected for the first time. */
    val selection: Flow<StoredSelection?>

    suspend fun save(selection: StoredSelection)
}
