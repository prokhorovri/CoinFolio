package me.prokhorov.coinfolio.component

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.GlobalPositionAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo

// Time for the light stripe to cross the window.
private const val SHIMMER_DURATION_MILLIS = 1200

// Light stripe width as a part of the window width.
private const val SHIMMER_BAND_WIDTH_FRACTION = 0.5f

// The one animation of a screen, with the theme values that a modifier node cannot read
@Stable
private class ShimmerWave(
    val progress: State<Float>,
    val baseColor: Color,
    val highlightColor: Color,
    val shape: Shape,
)

private val LocalShimmerWave = staticCompositionLocalOf<ShimmerWave?> { null }

/**
 * Runs one shimmer animation for [content]. Every [shimmer] inside uses it, so one wave
 * crosses the whole window. Put it only around loading content: the animation runs while
 * the host is shown. If system animations are off, the stripe stays outside the window.
 */
@Composable
fun ShimmerHost(content: @Composable () -> Unit) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = SHIMMER_DURATION_MILLIS, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer progress",
    )

    val baseColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val highlightColor = MaterialTheme.colorScheme.surfaceContainerLow
    val shape = MaterialTheme.shapes.small
    val wave = remember(progress, baseColor, highlightColor, shape) {
        ShimmerWave(progress, baseColor, highlightColor, shape)
    }

    CompositionLocalProvider(LocalShimmerWave provides wave, content = content)
}

/**
 * While [isVisible] is true, hides the content and draws a gray [shape] with the wave in its
 * place. The size comes from the content. A null [shape] means the theme's small shape.
 * Needs a [ShimmerHost] above it.
 */
fun Modifier.shimmer(isVisible: Boolean, shape: Shape? = null): Modifier {
    return this then ShimmerElement(isVisible, shape)
}

private data class ShimmerElement(
    val isVisible: Boolean,
    val shape: Shape?,
) : ModifierNodeElement<ShimmerNode>() {

    override fun create(): ShimmerNode = ShimmerNode(isVisible, shape)

    override fun update(node: ShimmerNode) {
        node.isVisible = isVisible
        node.shape = shape
        node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "shimmer"
        properties["isVisible"] = isVisible
        properties["shape"] = shape
    }
}

private class ShimmerNode(
    var isVisible: Boolean,
    var shape: Shape?,
) : Modifier.Node(),
    DrawModifierNode,
    GlobalPositionAwareModifierNode,
    CompositionLocalConsumerModifierNode {

    private var leftInWindow = 0f
    private var windowWidth = 0f

    // TODO: check in landscape with the rail. The band crosses the rail first, so the light
    // reaches the content a bit later. If it looks wrong, use the ShimmerHost bounds instead
    // of the window.
    override fun onGloballyPositioned(coordinates: LayoutCoordinates) {
        val newLeftInWindow = coordinates.positionInWindow().x
        val newWindowWidth = coordinates.findRootCoordinates().size.width.toFloat()
        if (newLeftInWindow != leftInWindow || newWindowWidth != windowWidth) {
            leftInWindow = newLeftInWindow
            windowWidth = newWindowWidth
            invalidateDraw()
        }
    }

    override fun ContentDrawScope.draw() {
        if (!isVisible) {
            drawContent()
            return
        }

        val wave = checkNotNull(currentValueOf(LocalShimmerWave)) {
            "Modifier.shimmer needs a ShimmerHost above it"
        }
        val bandWidth = windowWidth * SHIMMER_BAND_WIDTH_FRACTION
        // Reads the progress during drawing, so frames don't trigger recomposition.
        val bandStartXInWindow = shimmerBandStartX(wave.progress.value, windowWidth, bandWidth)
        // The gradient is set in window positions. This item draws only its own part of it.
        val bandStartX = bandStartXInWindow - leftInWindow

        drawOutline(
            outline = (shape ?: wave.shape).createOutline(size, layoutDirection, this),
            brush = Brush.linearGradient(
                colors = listOf(wave.baseColor, wave.highlightColor, wave.baseColor),
                start = Offset(bandStartX, 0f),
                end = Offset(bandStartX + bandWidth, 0f),
            ),
        )
    }
}

/**
 * Returns the stripe's start position in the window for progress from 0 to 1.
 * At 0 the stripe is left of the window. At 1 it's right of the window.
 */
@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal fun shimmerBandStartX(progress: Float, windowWidth: Float, bandWidth: Float): Float {
    //   E.G.: windowWidth = 200px; bandWidth = 100px; one char = 10px
    //   progress  startX
    //   0         -100    [========]|....................|
    //   0.5       50                |.....[========].....|
    //   1         200               |....................|[========]

    return -bandWidth + progress * (windowWidth + bandWidth)
}
