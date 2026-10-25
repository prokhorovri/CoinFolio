package me.prokhorov.coinfolio.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.theme.CoinIconSize
import me.prokhorov.coinfolio.theme.ListMaxWidth
import me.prokhorov.coinfolio.theme.PlaceholderChangeWidth
import me.prokhorov.coinfolio.theme.PlaceholderNameWidth
import me.prokhorov.coinfolio.theme.PlaceholderPriceWidth
import me.prokhorov.coinfolio.theme.PlaceholderSymbolWidth
import me.prokhorov.coinfolio.theme.Spacing12
import me.prokhorov.coinfolio.theme.Spacing16

// A loading row with the same size as a coin row, so the list doesn't jump when data loads.
@Composable
fun CoinRowPlaceholder(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .widthIn(max = ListMaxWidth)
            .fillMaxWidth()
            .padding(horizontal = Spacing16, vertical = Spacing12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShimmerBox(modifier = Modifier.size(CoinIconSize), shape = CircleShape)
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing12)
                .weight(1f),
        ) {
            PlaceholderLine(
                style = MaterialTheme.typography.bodyLarge,
                width = PlaceholderNameWidth,
            )
            PlaceholderLine(
                style = MaterialTheme.typography.bodySmall,
                width = PlaceholderSymbolWidth,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            PlaceholderLine(
                style = MaterialTheme.typography.bodyLarge,
                width = PlaceholderPriceWidth,
            )
            PlaceholderLine(
                style = MaterialTheme.typography.bodySmall,
                width = PlaceholderChangeWidth,
            )
        }
    }
}

private const val PREVIEW_ROW_COUNT = 3

@Preview
@Composable
private fun CoinRowPlaceholderPreview() {
    CoinRowPlaceholderPreviewBody(darkTheme = false)
}

@Preview
@Composable
private fun CoinRowPlaceholderDarkPreview() {
    CoinRowPlaceholderPreviewBody(darkTheme = true)
}

@Composable
private fun CoinRowPlaceholderPreviewBody(darkTheme: Boolean) {
    CoinFolioTheme(darkTheme = darkTheme) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column {
                repeat(PREVIEW_ROW_COUNT) { CoinRowPlaceholder() }
            }
        }
    }
}
