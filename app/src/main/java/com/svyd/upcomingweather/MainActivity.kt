package com.svyd.upcomingweather

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.svyd.upcomingweather.core.designsystem.theme.UpcomingWeatherTheme

/** One activity, no fragments, no arguments — everything past this point is Compose. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Before the content, and before super: the system holds its blank stage until the first
        // Compose frame is ready, which is the frame the app's own splash draws. Without it the
        // window shows a launcher icon first and the launch reads as two screens.
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UpcomingWeatherTheme {
                UpcomingWeatherApp()
            }
        }
    }
}
