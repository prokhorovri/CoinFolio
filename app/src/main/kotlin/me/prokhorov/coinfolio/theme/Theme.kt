package me.prokhorov.coinfolio.theme

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal val LightColorScheme = lightColorScheme(
    primary = Teal40,
    onPrimary = Color.White,
    primaryContainer = Teal90,
    onPrimaryContainer = Teal30,
    inversePrimary = Teal80,
    secondary = TealGrey40,
    onSecondary = Color.White,
    secondaryContainer = TealGrey90,
    onSecondaryContainer = TealGrey30,
    tertiary = Gold40,
    onTertiary = Color.White,
    tertiaryContainer = Gold90,
    onTertiaryContainer = Gold30,
    background = Cloud98,
    onBackground = Charcoal10,
    surface = Cloud98,
    onSurface = Charcoal10,
    surfaceVariant = Mist90,
    onSurfaceVariant = Mist30,
    surfaceDim = Cloud87,
    surfaceBright = Cloud98,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Cloud96,
    surfaceContainer = Cloud94,
    surfaceContainerHigh = Cloud92,
    surfaceContainerHighest = Cloud90,
    inverseSurface = Charcoal20,
    inverseOnSurface = Cloud95,
    outline = Mist50,
    outlineVariant = Mist80,
    scrim = Color.Black,
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red30,
)

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    onPrimary = Teal20,
    primaryContainer = Teal30,
    onPrimaryContainer = Teal90,
    inversePrimary = Teal40,
    secondary = TealGrey80,
    onSecondary = TealGrey20,
    secondaryContainer = TealGrey30,
    onSecondaryContainer = TealGrey90,
    tertiary = Gold80,
    onTertiary = Gold20,
    tertiaryContainer = Gold30,
    onTertiaryContainer = Gold90,
    background = Charcoal6,
    onBackground = Cloud90,
    surface = Charcoal6,
    onSurface = Cloud90,
    surfaceVariant = Mist30,
    onSurfaceVariant = Mist80,
    surfaceDim = Charcoal6,
    surfaceBright = Charcoal24,
    surfaceContainerLowest = Charcoal4,
    surfaceContainerLow = Charcoal10,
    surfaceContainer = Charcoal12,
    surfaceContainerHigh = Charcoal17,
    surfaceContainerHighest = Charcoal22,
    inverseSurface = Cloud90,
    inverseOnSurface = Charcoal20,
    outline = Mist60,
    outlineVariant = Mist30,
    scrim = Color.Black,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
)

@Composable
fun CoinFolioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = CoinFolioTypography,
            shapes = CoinFolioShapes,
            content = content,
        )
    }
}
