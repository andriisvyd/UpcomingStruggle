package com.svyd.upcomingweather.core.designsystem.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.svyd.upcomingweather.core.designsystem.foundation.NoirBackground
import com.svyd.upcomingweather.core.designsystem.icon.NoirIcons
import com.svyd.upcomingweather.core.designsystem.primitive.NoirBlinkingCursor
import com.svyd.upcomingweather.core.designsystem.primitive.NoirDotLeaderRow
import com.svyd.upcomingweather.core.designsystem.primitive.NoirPanel
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTextField
import com.svyd.upcomingweather.core.designsystem.primitive.NoirHairlineDivider
import com.svyd.upcomingweather.core.designsystem.primitive.NoirIconButton
import com.svyd.upcomingweather.core.designsystem.primitive.NoirListRow
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTopBar
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTopBarTitle
import com.svyd.upcomingweather.core.designsystem.primitive.NoirPipeDivider
import com.svyd.upcomingweather.core.designsystem.primitive.NoirPrimaryAction
import com.svyd.upcomingweather.core.designsystem.primitive.NoirSecondaryAction
import com.svyd.upcomingweather.core.designsystem.primitive.NoirSectionStamp
import com.svyd.upcomingweather.core.designsystem.primitive.NoirEmptyStateMessage
import com.svyd.upcomingweather.core.designsystem.primitive.NoirMarkerBar
import com.svyd.upcomingweather.core.designsystem.primitive.NoirPlaceholder
import com.svyd.upcomingweather.core.designsystem.primitive.NoirRangeBar
import com.svyd.upcomingweather.core.designsystem.primitive.NoirTiltedStamp
import com.svyd.upcomingweather.core.designsystem.theme.NoirSpacing
import com.svyd.upcomingweather.core.designsystem.theme.NoirTheme
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme

/** Every primitive on one page, in both grades. Nothing here knows what a forecast is. */
@NoirPreviews
@Composable
private fun PrimitiveGallery() {
    UpcomingWeatherTheme {
        NoirBackground {
            Column(
                modifier = Modifier.padding(bottom = NoirSpacing.section),
                verticalArrangement = Arrangement.spacedBy(NoirSpacing.m),
            ) {
                NoirTopBar(
                    navigation = {
                        NoirIconButton(NoirIcons.MyLocation, "Use current location", onClick = {})
                    },
                    actions = {
                        NoirIconButton(NoirIcons.Search, "Search for a city", onClick = {})
                    },
                ) {
                    NoirTopBarTitle("Budapest · 27°")
                }

                Column(Modifier.padding(horizontal = NoirSpacing.gutter)) {
                    NoirSectionStamp("Section stamp")

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(NoirSpacing.s),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = NoirSpacing.s),
                    ) {
                        NoirTiltedStamp("Rain", NoirTheme.inks.steel)
                        NoirTiltedStamp("Clear", NoirTheme.inks.sodium)
                        NoirTiltedStamp("Snow", NoirTheme.inks.tealIce)
                    }

                    NoirDotLeaderRow(
                        label = "Humidity",
                        value = "46%",
                        detail = "clouds packing 40%",
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = NoirSpacing.m),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NoirSpacing.s),
                    ) {
                        Text("range", style = MaterialTheme.typography.labelSmall)
                        NoirRangeBar(startFraction = 0.2f, endFraction = 0.8f)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NoirSpacing.s),
                    ) {
                        Text("mark", style = MaterialTheme.typography.labelSmall)
                        NoirMarkerBar(fraction = 0.45f)
                    }

                    NoirHairlineDivider(Modifier.padding(vertical = NoirSpacing.m))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(NoirSpacing.m),
                    ) {
                        NoirBlinkingCursor()
                        NoirPipeDivider(Modifier.height(92.dp).width(16.dp))
                        NoirPlaceholder("HUMIDITY ............ --")
                    }

                    NoirTextField(
                        value = "san",
                        onValueChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = NoirSpacing.m),
                        leading = {
                            Icon(
                                imageVector = NoirIcons.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        placeholder = "Name the city.",
                    )

                    NoirPanel(Modifier.padding(top = NoirSpacing.m)) {
                        Text("Offline — cold trail from 09:12", style = MaterialTheme.typography.bodyMedium)
                    }

                    NoirListRow(
                        headline = "San Francisco",
                        supporting = "California, United States",
                        onClick = {},
                    )
                    NoirHairlineDivider()

                    NoirEmptyStateMessage(
                        glyph = "(?)",
                        title = "No city. No case.",
                        body = "Every case starts with a city. Name one, or let me trace your steps.",
                    ) {
                        NoirPrimaryAction("Trace my steps", onClick = {})
                        NoirSecondaryAction("name a city instead", onClick = {})
                    }
                }
            }
        }
    }
}
