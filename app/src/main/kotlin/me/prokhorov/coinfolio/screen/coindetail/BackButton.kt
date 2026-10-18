package me.prokhorov.coinfolio.screen.coindetail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.theme.BackButtonBackgroundSize
import me.prokhorov.coinfolio.theme.BackButtonShadowElevation

@Composable
internal fun BackButton(hasBackground: Boolean, onClick: () -> Unit) {
    val backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val backgroundAlpha by animateFloatAsState(targetValue = if (hasBackground) 1f else 0f)

    Box(contentAlignment = Alignment.Center) {
        // The layer alpha fades the color and the shadow together. The alpha of the color alone
        // does not fade the shadow, and a small dark shadow shows as a polygon.
        // The arrow is outside this layer, so it stays visible.
        Box(
            modifier = Modifier
                .size(BackButtonBackgroundSize)
                .graphicsLayer {
                    alpha = backgroundAlpha
                    shape = CircleShape
                    clip = true
                    shadowElevation = backgroundAlpha * BackButtonShadowElevation.toPx()
                }
                .background(backgroundColor),
        )
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(R.drawable.ic_arrow_back),
                contentDescription = stringResource(R.string.back_text),
            )
        }
    }
}
