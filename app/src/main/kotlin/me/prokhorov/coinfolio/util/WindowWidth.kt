package me.prokhorov.coinfolio.util

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.window.core.layout.WindowSizeClass

// 600dp and wider: the app shows the rail and two panes.
val WindowAdaptiveInfo.isWideWindow: Boolean
    get() = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
