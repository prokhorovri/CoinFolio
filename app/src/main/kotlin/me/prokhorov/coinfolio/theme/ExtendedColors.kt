package me.prokhorov.coinfolio.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class ExtendedColors(
    val positive: Color,
    val negative: Color,
)

internal val LightExtendedColors = ExtendedColors(
    positive = Green40,
    negative = Rose40,
)

internal val DarkExtendedColors = ExtendedColors(
    positive = Green80,
    negative = Rose80,
)

internal val LocalExtendedColors = staticCompositionLocalOf<ExtendedColors> {
    error("No extended colors provided")
}

@Suppress("UnusedReceiverParameter")
val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    get() = LocalExtendedColors.current
