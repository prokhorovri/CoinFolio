package me.prokhorov.coinfolio.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import me.prokhorov.coinfolio.theme.RefreshIndicatorSize
import me.prokhorov.coinfolio.theme.RefreshIndicatorSlideDistance
import me.prokhorov.coinfolio.theme.RefreshIndicatorSpinnerSize
import me.prokhorov.coinfolio.theme.RefreshIndicatorStrokeWidth

/**
 * Pull to refresh indicator that fades in while it slides a short way down. The Material one
 * clips itself at its own top edge, so it looks cut when it does not sit at the screen edge.
 */
@Composable
fun FadingPullToRefreshIndicator(
    state: PullToRefreshState,
    isRefreshing: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(RefreshIndicatorSize)
            .graphicsLayer {
                // Read the pull distance in the draw phase. A pull then redraws this disc
                // and does not recompose the screen.
                val fraction = state.distanceFraction.coerceIn(0f, 1f)

                alpha = fraction
                translationY = fraction * RefreshIndicatorSlideDistance.toPx()
                shape = CircleShape
                clip = true

                shadowElevation = fraction * PullToRefreshDefaults.Elevation.toPx()
            }
            .background(PullToRefreshDefaults.indicatorContainerColor),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(targetState = isRefreshing) { refreshing ->
            if (refreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(RefreshIndicatorSpinnerSize),
                    color = PullToRefreshDefaults.indicatorColor,
                    strokeWidth = RefreshIndicatorStrokeWidth,
                )
            } else {
                // The ring fills up while the user pulls. It is full at the refresh threshold.
                CircularProgressIndicator(
                    progress = { state.distanceFraction },
                    modifier = Modifier.size(RefreshIndicatorSpinnerSize),
                    color = PullToRefreshDefaults.indicatorColor,
                    strokeWidth = RefreshIndicatorStrokeWidth,
                    trackColor = Color.Transparent,
                )
            }
        }
    }
}
