package me.prokhorov.coinfolio.ui.statusbar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import me.prokhorov.coinfolio.theme.StatusBarScrimExtraHeight

// Light-only for now: the app has no dark theme yet, so this scrim isn't theme-aware.
// We'll come back to this point when the actual dark and light themes are added.
@Composable
fun StatusBarScrim() {
    val statusBarHeight = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(statusBarHeight + StatusBarScrimExtraHeight)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 1f),
                        Color.White.copy(alpha = 0f),
                    )
                )
            )
    )
}
