package com.svyd.upcomingweather.core.designsystem.preview

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

/** Both grades of the theme, at the 360 dp reference width the spec is measured in. */
@Preview(name = "day", widthDp = 360, group = "noir")
@Preview(name = "night", widthDp = 360, group = "noir", uiMode = UI_MODE_NIGHT_YES)
annotation class NoirPreviews

/** The full 360 × 800 dp reference device, both grades. */
@Preview(name = "day", device = "spec:width=360dp,height=800dp", group = "noir")
@Preview(name = "night", device = "spec:width=360dp,height=800dp", group = "noir", uiMode = UI_MODE_NIGHT_YES)
annotation class NoirScreenPreviews

/**
 * The accessibility floor: the system's largest font setting, on the narrowest reference screen.
 * Layouts that reflow past a threshold should be checked here and at 130%.
 */
@Preview(name = "130%", device = "spec:width=360dp,height=800dp", group = "font scale", fontScale = 1.3f)
@Preview(name = "200%", device = "spec:width=360dp,height=800dp", group = "font scale", fontScale = 2f)
@Preview(
    name = "200% night",
    device = "spec:width=360dp,height=800dp",
    group = "font scale",
    fontScale = 2f,
    uiMode = UI_MODE_NIGHT_YES,
)
annotation class NoirFontScalePreviews
