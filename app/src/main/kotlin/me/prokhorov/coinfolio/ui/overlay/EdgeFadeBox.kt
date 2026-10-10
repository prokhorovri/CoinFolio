package me.prokhorov.coinfolio.ui.overlay

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import kotlin.math.pow
import me.prokhorov.coinfolio.theme.FadeExtraHeight

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal const val TOP_FADE_TAG = "top_fade"

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal const val BOTTOM_FADE_TAG = "bottom_fade"

// Controls how quickly the content becomes visible from the edge.
private const val FADE_EXPONENT = 1.5f

// How many color stops draw the curve. More stops make it smoother.
private const val FADE_STEPS = 8

/**
 * Fades the [content] at the top and/or bottom edge.
 * Adds padding, so the first and last items can scroll fully out of the fades.
 * [topBarHeight] is a solid bar inside the top padding.
 * The top fade ignores it because the bar covers the content.
 */
@Composable
fun EdgeFadeBox(
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    topBarHeight: Dp = 0.dp,
    hasTopFade: Boolean = false,
    hasBottomFade: Boolean = false,
    content: @Composable (contentPadding: PaddingValues) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val topPadding = contentPadding.calculateTopPadding()
    val bottomInset = contentPadding.calculateBottomPadding()

    // The system bars and the cutout at the top, without the bar.
    val topInset = (topPadding - topBarHeight).coerceAtLeast(0.dp)

    Box(modifier = modifier) {
        content(
            PaddingValues(
                start = contentPadding.calculateStartPadding(layoutDirection),
                // The content starts below the bar or fade, whichever is lower.
                top = if (hasTopFade) topInset + max(topBarHeight, FadeExtraHeight) else topPadding,
                end = contentPadding.calculateEndPadding(layoutDirection),
                bottom = if (hasBottomFade) bottomInset + FadeExtraHeight else bottomInset,
            ),
        )

        if (hasTopFade) {
            EdgeFadeOverlay(
                edge = FadeEdge.TOP,
                systemBarsHeight = topInset,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .testTag(TOP_FADE_TAG),
            )
        }
        if (hasBottomFade) {
            EdgeFadeOverlay(
                edge = FadeEdge.BOTTOM,
                systemBarsHeight = bottomInset,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .testTag(BOTTOM_FADE_TAG),
            )
        }
    }
}

@Composable
private fun EdgeFadeOverlay(
    edge: FadeEdge,
    systemBarsHeight: Dp,
    modifier: Modifier = Modifier,
) {
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
            .height(systemBarsHeight + FadeExtraHeight)
            .background(brush = brush),
    )
}
