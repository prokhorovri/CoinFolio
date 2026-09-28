package me.prokhorov.coinfolio

import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import me.prokhorov.coinfolio.theme.CoinFolioTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Must run before super.onCreate. It swaps the splash theme for Theme.CoinFolio.
        // see https://developer.android.com/develop/ui/views/launch/splash-screen/migrate
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // The dark or light choice is read once, so a theme change must recreate the activity.
        // Do not add uiMode to android:configChanges. The bar icons would keep the old color.
        enableEdgeToEdge(navigationBarStyle = transparentNavigationBarStyle())
        setContent {
            CoinFolioTheme {
                CoinFolioApp()
            }
        }
    }

    // Keeps the system navigation bar transparent.
    // With SystemBarStyle.auto() the platform draws a scrim over the button navigation bar.
    private fun transparentNavigationBarStyle(): SystemBarStyle {
        val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        val darkTheme = nightMode == Configuration.UI_MODE_NIGHT_YES

        return if (darkTheme) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
    }
}
