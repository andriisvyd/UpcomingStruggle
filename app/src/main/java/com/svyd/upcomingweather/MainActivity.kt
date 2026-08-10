package com.svyd.upcomingweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme

/** One activity, no fragments, no arguments — everything past this point is Compose. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UpcomingWeatherTheme {
                UpcomingWeatherApp()
            }
        }
    }
}
