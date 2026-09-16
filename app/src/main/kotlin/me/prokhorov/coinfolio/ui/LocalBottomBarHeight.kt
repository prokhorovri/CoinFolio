package me.prokhorov.coinfolio.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * The height of the bottom bar. Used as bottom padding so content can scroll above the bar.
 * Not a parameter on purpose. The nav graph is only about routes, not about layout.
 * Zero by default.
 */
val LocalBottomBarHeight = compositionLocalOf { 0.dp }
