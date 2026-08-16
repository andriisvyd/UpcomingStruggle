package com.svyd.upcomingweather.feature.forecast.component

import com.svyd.upcomingweather.core.designsystem.primitive.NoirCondition
import com.svyd.upcomingweather.core.designsystem.primitive.NoirConditionGlyph
import com.svyd.upcomingweather.core.designsystem.primitive.ink

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.foundation.ScreenTravel
import com.svyd.upcomingweather.core.designsystem.foundation.largeFontScale
import com.svyd.upcomingweather.core.designsystem.foundation.travelsBetweenScreens
import com.svyd.upcomingweather.core.designsystem.preview.NoirPreviews
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTiltedStamp
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTypedText
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme
import com.svyd.upcomingweather.feature.forecast.R
import com.svyd.upcomingweather.feature.forecast.model.Freshness
import com.svyd.upcomingweather.feature.forecast.model.HeroUi
import kotlin.time.Duration.Companion.milliseconds

/**
 * The mark the splash comes to rest on, handed to the dashboard's hero.
 *
 * The only journey of its kind in the app: the day details screen draws a hero too, and is handed
 * no travel at all, so its mark simply arrives with the page it is on.
 */
internal val OpeningGlyphTravel = ScreenTravel(key = "hero-condition-glyph")

/**
 * The block at the top of both forecast screens: temperature, stamp, the voiced line, the meta
 * line, and the typed glyph — no card, straight onto the page.
 *
 * Only the stamp is read aloud; the line and the glyph are decoration and are silenced, because
 * "slash slash slash" is not a forecast.
 */
@Composable
fun HeroBlock(
    modifier: Modifier = Modifier,
    hero: HeroUi,
    glyphTravel: ScreenTravel? = null,
) {
    val ink = hero.condition.ink()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = HeroTopPadding, bottom = HeroBottomPadding),
    ) {
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)) {
                NoirTypedText(
                    text = hero.temperature,
                    style = NoirTheme.type.tempDisplay,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                NoirTiltedStamp(
                    text = hero.conditionLabel,
                    ink = ink,
                    modifier = Modifier.padding(top = StampTopGap, bottom = StampBottomGap),
                )
                NoirTypedText(
                    text = hero.line,
                    style = NoirTheme.type.heroLine,
                    color = MaterialTheme.colorScheme.onSurface,
                    startDelay = LineDelay,
                    modifier = Modifier
                        .padding(top = LineGap)
                        .clearAndSetSemantics { },
                )
                Text(
                    text = heroMeta(hero),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = MetaGap),
                )
            }
            // At very large type a 44 sp glyph and a 72 sp temperature cannot share a line, and
            // the temperature is the one carrying information — the glyph is decoration the
            // stamp already says in words, in the same ink.
            if (!largeFontScale()) {
                NoirConditionGlyph(
                    condition = hero.condition,
                    style = NoirTheme.type.glyphHero,
                    modifier = Modifier
                        .padding(start = GlyphStartGap, top = GlyphTopGap)
                        .travelsBetweenScreens(glyphTravel)
                        .clearAndSetSemantics { },
                )
            }
        }
        NoirTypedText(
            text = freshnessLine(hero.freshness),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            startDelay = FreshnessDelay,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = TimestampGap),
        )
    }
}

/**
 * Says how current the reading is: a plain line while it is, the time it was obtained once it is
 * not. Both come from resources, so the wording is not decided here.
 */
@Composable
private fun freshnessLine(freshness: Freshness) = when (freshness) {
    Freshness.Fresh -> AnnotatedString(stringResource(R.string.forecast_still_current))
    is Freshness.Stale -> boldValue(
        template = stringResource(R.string.forecast_updated_at),
        value = freshness.refreshedAt,
    )
}

/** "Felt like 29°  ·  **H 29°  L 19°**" — feels-like is dropped on the day-details variant. */
@Composable
private fun heroMeta(hero: HeroUi) = buildAnnotatedString {
    if (hero.feelsLike != null) {
        append(stringResource(R.string.forecast_feels_like, hero.feelsLike))
        append(stringResource(R.string.forecast_meta_separator))
    }
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
        append(stringResource(R.string.forecast_high_low, hero.high, hero.low))
    }
}

/**
 * Fills a one-placeholder template, emboldening the value.
 *
 * Formatting rather than concatenating keeps the word order the translator's business — the value
 * does not have to sit at the end of the sentence.
 */
private fun boldValue(template: String, value: String) = buildAnnotatedString {
    val at = template.indexOf(PLACEHOLDER)
    if (at < 0) {
        append(template)
        return@buildAnnotatedString
    }
    append(template.substring(0, at))
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(value) }
    append(template.substring(at + PLACEHOLDER.length))
}

@NoirPreviews
@Composable
private fun HeroBlockPreview() {
    UpcomingWeatherTheme {
        NoirBackground(drawGrain = false) {
            HeroBlock(
                modifier = Modifier.padding(horizontal = NoirSpacing.gutter),
                hero = HeroUi(
                    temperature = "27°",
                    condition = NoirCondition.Partly,
                    conditionLabel = "Partly cloudy",
                    line = "Clouds circle like old regrets.",
                    feelsLike = "29°",
                    high = "29°",
                    low = "19°",
                    freshness = Freshness.Stale(refreshedAt = "10:12"),
                ),
            )
        }
    }
}

private const val PLACEHOLDER = $$"%1$s"

// The block types top to bottom: the temperature goes down first, then the line under it, then the
// timestamp. The temperature holds the default and starts at once.
private val LineDelay = 220.milliseconds
private val FreshnessDelay = 540.milliseconds

private val HeroTopPadding = 12.dp
private val HeroBottomPadding = 4.dp
private val StampTopGap = NoirSpacing.s
private val StampBottomGap = NoirSpacing.xs
private val LineGap = 4.dp
private val MetaGap = 8.dp
private val GlyphStartGap = NoirSpacing.s
private val GlyphTopGap = 8.dp
private val TimestampGap = 8.dp
