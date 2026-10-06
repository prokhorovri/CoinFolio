package me.prokhorov.coinfolio.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * The height of the bottom bar. With the rail there is no bottom bar. Then it is the height
 * of the system navigation bar. Used as bottom padding so content can scroll above the bars.
 * Not a parameter on purpose. The nav graph is only about routes, not about layout.
 * Zero by default.
 */
val LocalBottomBarHeight = compositionLocalOf { 0.dp }
