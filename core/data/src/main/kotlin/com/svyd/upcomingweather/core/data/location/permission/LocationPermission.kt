package com.svyd.upcomingweather.core.data.location.permission

/** Whether the app may ask the platform where it is. */
internal fun interface LocationPermission {

    fun granted(): Boolean
}
