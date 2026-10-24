package me.prokhorov.coinfolio.component

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape

// Time for the light stripe to cross the box.
private const val SHIMMER_DURATION_MILLIS = 1200

// Light stripe width as a part of the box width.
private const val SHIMMER_BAND_WIDTH_FRACTION = 0.5f

/**
 * A gray box with a moving light stripe, shown while content is loading.
 * If system animations are off, the stripe stays outside the box.
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.small,
) {
    val baseColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val highlightColor = MaterialTheme.colorScheme.surfaceContainerLow

    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_DURATION_MILLIS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer progress",
    )

    Box(
        modifier = modifier
            .clip(shape)
            // Reads progress during drawing, so frames don't trigger recomposition.
            .drawBehind {
                val bandWidth = size.width * SHIMMER_BAND_WIDTH_FRACTION
                val bandStartX = shimmerBandStartX(progress, size.width, bandWidth)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(baseColor, highlightColor, baseColor),
                        start = Offset(bandStartX, 0f),
                        end = Offset(bandStartX + bandWidth, 0f),
                    ),
                )
            },
    )
}

/**
 * Returns the stripe's start position for progress from 0 to 1.
 * At 0 the stripe is left of the box. At 1 it's right of the box.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun shimmerBandStartX(progress: Float, boxWidth: Float, bandWidth: Float): Float {
    //   E.G.: boxwidth = 200px; bandwidth = 100px; one char = 10px
    //   progress  startX
    //   0         -100    [========]|....................|
    //   0.5       50                |.....[========].....|
    //   1         200               |....................|[========]

    return -bandWidth + progress * (boxWidth + bandWidth)
}
