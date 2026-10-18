package me.prokhorov.coinfolio.layout

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** What a screen needs to know about its bottom edge. */
@Immutable
data class BottomInset(
    // Used as bottom padding so content can scroll above the bars.
    val padding: Dp,

    // The screen needs no bottom fade under the bottom bar.
    val hasBottomBar: Boolean,
)

/**
 * Not a parameter on purpose. The nav graph is only about routes, not about layout.
 * Zero padding and no bottom bar by default.
 */
val LocalBottomInset = compositionLocalOf { BottomInset(padding = 0.dp, hasBottomBar = false) }
