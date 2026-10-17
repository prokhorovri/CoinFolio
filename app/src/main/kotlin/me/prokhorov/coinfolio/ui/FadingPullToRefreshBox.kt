package me.prokhorov.coinfolio.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalLayoutDirection
import me.prokhorov.coinfolio.theme.RefreshIndicatorSize
import me.prokhorov.coinfolio.theme.RefreshIndicatorSlideDistance
import me.prokhorov.coinfolio.theme.RefreshIndicatorSpinnerSize
import me.prokhorov.coinfolio.theme.RefreshIndicatorStrokeWidth

/**
 * A [PullToRefreshBox] with [FadingPullToRefreshIndicator]. The indicator appears below the top
 * of [contentPadding] and is centered between its sides.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FadingPullToRefreshBox(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            FadingPullToRefreshIndicator(
                state = state,
                isRefreshing = isRefreshing,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(
                        start = contentPadding.calculateStartPadding(layoutDirection),
                        top = contentPadding.calculateTopPadding(),
                        end = contentPadding.calculateEndPadding(layoutDirection),
                    ),
            )
        },
        content = content,
    )
}

/**
 * Pull to refresh indicator that fades in while it slides a short way down. The Material one
 * clips itself at its own top edge, so it looks cut when it does not sit at the screen edge.
 */
@Composable
private fun FadingPullToRefreshIndicator(
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
