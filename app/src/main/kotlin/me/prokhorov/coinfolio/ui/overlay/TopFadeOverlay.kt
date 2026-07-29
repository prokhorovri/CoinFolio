package me.prokhorov.coinfolio.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import me.prokhorov.coinfolio.theme.TopFadeExtraHeight

@Composable
fun TopFadeOverlay() {
    val topInset = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(topInset + TopFadeExtraHeight)
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        .0f to backgroundColor,
                        .4f to backgroundColor,
                        1f to backgroundColor.copy(alpha = 0f),
                    )
                )
            )
    )
}
