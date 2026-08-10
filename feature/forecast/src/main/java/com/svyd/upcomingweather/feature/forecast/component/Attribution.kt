package com.svyd.upcomingweather.feature.forecast.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.preview.NoirPreviews
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.forecast.R

/**
 * The credit at the foot of both forecast screens.
 *
 * The provider's data is CC BY 4.0, which asks for the source by name and a link to it. Only the
 * name carries the link; the rest of the line is not part of what the licence asks for.
 */
@Composable
fun Attribution(modifier: Modifier = Modifier) {
    val template = stringResource(R.string.forecast_attribution)
    val source = stringResource(R.string.forecast_attribution_source)
    val linked = SpanStyle(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textDecoration = TextDecoration.Underline,
    )

    Text(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = TopGap, bottom = NoirSpacing.section),
        text = buildAnnotatedString {
            val at = template.indexOf(PLACEHOLDER)
            if (at < 0) {
                append(template)
                return@buildAnnotatedString
            }
            append(template.substring(0, at))
            withLink(
                LinkAnnotation.Url(url = SOURCE_URL, styles = TextLinkStyles(style = linked)),
            ) {
                append(source)
            }
            append(template.substring(at + PLACEHOLDER.length))
        },
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
    )
}

@NoirPreviews
@Composable
private fun AttributionPreview() {
    UpcomingWeatherTheme {
        NoirBackground(drawGrain = false) {
            Attribution(Modifier.padding(horizontal = NoirSpacing.gutter))
        }
    }
}

private const val SOURCE_URL = "https://open-meteo.com/"
private const val PLACEHOLDER = $$"%1$s"

private val TopGap = 16.dp
