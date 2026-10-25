package me.prokhorov.coinfolio.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

// The bar uses the font size, inside a line with the given line height.
@Composable
fun PlaceholderLine(style: TextStyle, width: Dp, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val lineHeight = with(density) { style.lineHeight.toDp() }
    val barHeight = with(density) { style.fontSize.toDp() }

    Box(modifier = modifier.height(lineHeight), contentAlignment = Alignment.Center) {
        ShimmerBox(modifier = Modifier.size(width = width, height = barHeight))
    }
}
