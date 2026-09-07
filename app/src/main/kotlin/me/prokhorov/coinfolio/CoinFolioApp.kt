package me.prokhorov.coinfolio

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.prokhorov.coinfolio.navigation.AppNavGraph
import me.prokhorov.coinfolio.ui.overlay.TopFadeOverlay

// This is the app root. MainActivity calls it once with the whole window.
// So a modifier parameter would never be used.
@Suppress("ModifierMissing")
@Composable
fun CoinFolioApp() {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppNavGraph()
            TopFadeOverlay()
        }
    }
}
