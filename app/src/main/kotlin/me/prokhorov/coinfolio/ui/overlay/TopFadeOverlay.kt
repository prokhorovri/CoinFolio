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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import kotlin.math.pow
import me.prokhorov.coinfolio.theme.TopFadeExtraHeight

/**
 * Controls how quickly the content becomes visible from the top.
 */
private const val TOP_FADE_EXPONENT = 1.5f

/** How many color stops draw the curve. More stops make it smoother. */
private const val TOP_FADE_STEPS = 8

@Composable
fun TopFadeOverlay(modifier: Modifier = Modifier) {
    val topInset = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val backgroundColor = MaterialTheme.colorScheme.background

    val colorStops = remember(backgroundColor) {
        Array(TOP_FADE_STEPS + 1) { step ->
            val fraction = step.toFloat() / TOP_FADE_STEPS
            fraction to backgroundColor.copy(alpha = 1f - fraction.pow(TOP_FADE_EXPONENT))
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(topInset + TopFadeExtraHeight)
            .background(brush = Brush.verticalGradient(colorStops = colorStops)),
    )
}
