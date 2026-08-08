package com.svyd.upcomingweather.core.designsystem.foundation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable

/** Defaults for the containers that scroll beneath the system bars. */
object NoirInsetDefaults {

    /**
     * What a scroll container owes the system bars: room at the bottom, plus any display cutout
     * at the sides.
     *
     * Hand it to `LazyColumn(contentPadding = …)`, or to `Modifier.padding(…)` applied *after*
     * `verticalScroll`, and the content passes under the navigation bar while still coming to
     * rest clear of it. There is no top component — the top bar clears the status bar itself.
     */
    val scrollableContentPadding: PaddingValues
        @Composable
        get() = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom)
            .asPaddingValues()
}
