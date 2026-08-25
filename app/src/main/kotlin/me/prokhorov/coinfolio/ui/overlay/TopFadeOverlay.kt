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

/** The gradient stays solid down to this part of the height, then it fades out. */
private const val TOP_FADE_SOLID_STOP = .4f

@Composable
fun TopFadeOverlay(modifier: Modifier = Modifier) {
    val topInset = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val backgroundColor = MaterialTheme.colorScheme.background

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(topInset + TopFadeExtraHeight)
            .background(
                brush = Brush.verticalGradient(
                    colorStops = arrayOf(
                        .0f to backgroundColor,
                        TOP_FADE_SOLID_STOP to backgroundColor,
                        1f to backgroundColor.copy(alpha = 0f),
                    ),
                ),
            ),
    )
}
