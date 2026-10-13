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
import me.prokhorov.coinfolio.theme.FadeBandHeight

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal const val TOP_FADE_TAG = "top_fade"

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal const val BOTTOM_FADE_TAG = "bottom_fade"

// The alpha where the inset ends and the band starts. Over the inset the fade is almost solid.
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal const val BAR_ALPHA = 0.9f

// Controls how quickly the content becomes visible in the band.
private const val FADE_EXPONENT = 1.5f

// How many color stops draw the curve in the band. More stops make it smoother.
private const val FADE_STEPS = 8

private enum class FadeEdge { TOP, BOTTOM }

/**
 * Fades the [content] at the top and/or bottom edge.
 * Adds padding, so the first and last items can scroll fully out of the fades.
 * [topBarHeight] is a bar inside the top padding. The content starts below the bar,
 * but the top fade keeps its normal height.
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
                top = if (hasTopFade) topInset + max(topBarHeight, FadeBandHeight) else topPadding,
                end = contentPadding.calculateEndPadding(layoutDirection),
                bottom = if (hasBottomFade) bottomInset + FadeBandHeight else bottomInset,
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

    val bandStartFraction = systemBarsHeight / (systemBarsHeight + FadeBandHeight)

    // Both edges use the same stops. Only the brush direction is different.
    val colorStops = remember(backgroundColor, bandStartFraction) {
        fadeAlphaStops(bandStartFraction)
            .map { (position, alpha) -> position to backgroundColor.copy(alpha = alpha) }
            .toTypedArray()
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
            .height(systemBarsHeight + FadeBandHeight)
            .background(brush = brush),
    )
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun fadeAlphaStops(bandStartFraction: Float): List<Pair<Float, Float>> {
    // Example: a 48dp system bar and the 24dp band, so bandStartFraction = 48 / 72 = 0.67.
    // Every second band stop is shown.
    //
    //   position  alpha
    //   0.00      1.00   screen edge
    //                    | system bar: an even gradient, almost solid
    //   0.67      0.90   band start
    //   0.75      0.79   |
    //   0.83      0.58   | band: a curve, from dark to clear
    //   0.92      0.32   |
    //   1.00      0.00   inner end, the content is fully visible

    val stops = mutableListOf(0f to 1f)

    val bandHeightFraction = 1f - bandStartFraction

    for (step in 0..FADE_STEPS) {
        val stepInBandFraction = step.toFloat() / FADE_STEPS
        val stopPositionFraction = bandStartFraction + stepInBandFraction * bandHeightFraction
        val stopAlpha = BAR_ALPHA * (1f - stepInBandFraction.pow(FADE_EXPONENT))
        stops += stopPositionFraction to stopAlpha
    }

    return stops
}
