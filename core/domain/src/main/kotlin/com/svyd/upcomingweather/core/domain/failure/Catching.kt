package com.svyd.upcomingweather.core.domain.failure

import kotlin.coroutines.cancellation.CancellationException

/**
 * [runCatching] for suspending work.
 *
 * The standard one catches [CancellationException] along with everything else, which turns a
 * cancelled coroutine into a failed result and lets the caller carry on inside a scope that is no
 * longer alive. Cancellation is rethrown here so it stays cancellation.
 */
internal inline fun <T> catching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (failure: Throwable) {
    Result.failure(failure)
}
