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
        // Before the content, and before super: the system holds the launcher's mark until the
        // first Compose frame is ready, and that frame is the app's own splash drawing the same
        // mark. Without it the two would be separated by a window with neither on it.
        val splash = installSplashScreen()

        // Struck rather than faded. The stage is holding the same mark, in the same ink, in the
        // same place as the frame waiting behind it, so there is nothing for an exit to show: a
        // fade would only be that mark thinning and coming back solid.
        splash.setOnExitAnimationListener { stage -> stage.remove() }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UpcomingWeatherTheme {
                UpcomingWeatherApp()
            }
        }
    }
}
