package me.prokhorov.coinfolio.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.theme.CoinIconSize
import me.prokhorov.coinfolio.theme.Spacing12
import me.prokhorov.coinfolio.theme.Spacing16

/**
 * A gray shape shown while content is loading. Use [modifier] to set its size.
 * Put it inside a [ShimmerFrame] to add the moving stripe.
 */
@Composable
fun ShapePlaceholder(modifier: Modifier = Modifier, shape: Shape = MaterialTheme.shapes.small) {
    Box(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = shape,
        ),
    )
}

/**
 * A gray bar shown in place of a line of text while loading.
 * The bar uses the font size, and the space around it matches the text line height.
 */
@Composable
fun TextPlaceholder(style: TextStyle, width: Dp, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val lineHeight = with(density) { style.lineHeight.toDp() }
    val barHeight = with(density) { style.fontSize.toDp() }

    Box(modifier = modifier.height(lineHeight), contentAlignment = Alignment.Center) {
        ShapePlaceholder(modifier = Modifier.size(width = width, height = barHeight))
    }
}

@Preview
@Composable
private fun PlaceholderPreview() {
    PlaceholderPreviewBody(darkTheme = false)
}

@Preview
@Composable
private fun PlaceholderDarkPreview() {
    PlaceholderPreviewBody(darkTheme = true)
}

@Composable
private fun PlaceholderPreviewBody(darkTheme: Boolean) {
    CoinFolioTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            ShimmerFrame(modifier = Modifier.padding(Spacing16)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing12),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ShapePlaceholder(modifier = Modifier.size(CoinIconSize), shape = CircleShape)
                    Column {
                        TextPlaceholder(
                            style = MaterialTheme.typography.bodyLarge,
                            width = 96.dp,
                        )
                        TextPlaceholder(
                            style = MaterialTheme.typography.bodySmall,
                            width = 40.dp,
                        )
                    }
                }
            }
        }
    }
}
