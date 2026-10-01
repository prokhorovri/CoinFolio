package me.prokhorov.coinfolio.ui.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import kotlin.math.pow
import me.prokhorov.coinfolio.theme.FadeExtraHeight

// Controls how quickly the content becomes visible from the edge.
private const val FADE_EXPONENT = 1.5f

// How many color stops draw the curve. More stops make it smoother.
private const val FADE_STEPS = 8

/** The screen edge the fade sits on. */
enum class FadeEdge { TOP, BOTTOM }

@Composable
fun EdgeFadeOverlay(edge: FadeEdge, modifier: Modifier = Modifier) {
    val inset = when (edge) {
        FadeEdge.TOP -> WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()

        // safeDrawing also includes the keyboard. The bottom fade is only for the navigation bar.
        FadeEdge.BOTTOM -> WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    }
    val backgroundColor = MaterialTheme.colorScheme.background

    // The first stop is solid and sits on the screen edge. The last one is fully clear.
    // Both edges use the same stops. Only the brush direction is different.
    val colorStops = remember(backgroundColor) {
        Array(FADE_STEPS + 1) { step ->
            val fraction = step.toFloat() / FADE_STEPS
            fraction to backgroundColor.copy(alpha = 1f - fraction.pow(FADE_EXPONENT))
        }
    }
    val brush = when (edge) {
        // By default the brush goes from the top of the box (startY = 0f) to the bottom
        // (endY = Float.POSITIVE_INFINITY). So the first stop is on the top edge.
        FadeEdge.TOP -> Brush.verticalGradient(colorStops = colorStops)

        // Start and end are swapped, so the brush goes from the bottom up. The first stop
        // is on the bottom edge.
        FadeEdge.BOTTOM -> {
            Brush.verticalGradient(
                colorStops = colorStops,
                startY = Float.POSITIVE_INFINITY,
                endY = 0f,
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(inset + FadeExtraHeight)
            .background(brush = brush),
    )
}
