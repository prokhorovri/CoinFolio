package me.prokhorov.coinfolio.component

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.theme.CoinIconSize
import me.prokhorov.coinfolio.theme.Spacing16

// Time for the light stripe to cross the frame.
private const val SHIMMER_DURATION_MILLIS = 1200

// Light stripe width as a part of the frame width.
private const val SHIMMER_BAND_WIDTH_FRACTION = 0.5f

/**
 * Draws a moving light stripe over the [content]. Empty space stays empty, so use gray shapes
 * or filled components. The stripe keeps the content's alpha. If system animations are off,
 * the stripe stays outside the frame.
 */
@Composable
fun ShimmerFrame(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
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
            // SrcAtop needs the content in its own layer, or it would also see the background.
            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            // Reads progress during drawing, so frames don't trigger recomposition.
            .drawWithContent {
                drawContent()
                val bandWidth = size.width * SHIMMER_BAND_WIDTH_FRACTION
                val bandStartX = shimmerBandStartX(progress, size.width, bandWidth)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(baseColor, highlightColor, baseColor),
                        start = Offset(bandStartX, 0f),
                        end = Offset(bandStartX + bandWidth, 0f),
                    ),
                    blendMode = BlendMode.SrcAtop,
                )
            },
    ) {
        content()
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun shimmerBandStartX(progress: Float, frameWidth: Float, bandWidth: Float): Float {
    //   E.G.: frameWidth = 200px; bandWidth = 100px; one char = 10px
    //   progress  startX
    //   0         -100    [========]|....................|
    //   0.5       50                |.....[========].....|
    //   1         200               |....................|[========]

    return -bandWidth + progress * (frameWidth + bandWidth)
}

@Preview
@Composable
private fun ShimmerFramePreview() {
    ShimmerFramePreviewBody(darkTheme = false)
}

@Preview
@Composable
private fun ShimmerFrameDarkPreview() {
    ShimmerFramePreviewBody(darkTheme = true)
}

@Composable
private fun ShimmerFramePreviewBody(darkTheme: Boolean) {
    CoinFolioTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ShimmerFrame(modifier = Modifier.padding(Spacing16)) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing16)) {
                    Box(
                        modifier = Modifier
                            .size(CoinIconSize)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                                shape = CircleShape,
                            ),
                    )
                    Button(onClick = {}) {
                        Text(text = stringResource(R.string.retry_text))
                    }
                }
            }
        }
    }
}
