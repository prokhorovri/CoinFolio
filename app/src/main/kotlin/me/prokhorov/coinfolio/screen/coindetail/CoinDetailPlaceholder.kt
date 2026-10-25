package me.prokhorov.coinfolio.screen.coindetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import me.prokhorov.coinfolio.component.PlaceholderLine
import me.prokhorov.coinfolio.component.ShimmerBox
import me.prokhorov.coinfolio.theme.CoinDetailIconSize
import me.prokhorov.coinfolio.theme.PlaceholderButtonWidth
import me.prokhorov.coinfolio.theme.PlaceholderChangeWidth
import me.prokhorov.coinfolio.theme.PlaceholderDetailPriceWidth
import me.prokhorov.coinfolio.theme.PlaceholderHoldingQuantityWidth
import me.prokhorov.coinfolio.theme.PlaceholderRankWidth
import me.prokhorov.coinfolio.theme.PlaceholderSectionTitleWidth
import me.prokhorov.coinfolio.theme.PlaceholderStatLabelWidth
import me.prokhorov.coinfolio.theme.PlaceholderStatValueWidth
import me.prokhorov.coinfolio.theme.PlaceholderSymbolWidth
import me.prokhorov.coinfolio.theme.Spacing16
import me.prokhorov.coinfolio.theme.Spacing4
import me.prokhorov.coinfolio.theme.Spacing8

// The same number of rows as MarketDataSection shows for a coin with all values.
private const val PLACEHOLDER_STAT_ROW_COUNT = 6

// The same layout as CoinSummary.
@Composable
internal fun CoinSummaryPlaceholder() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShimmerBox(modifier = Modifier.size(CoinDetailIconSize), shape = CircleShape)
        Column(verticalArrangement = Arrangement.spacedBy(Spacing4)) {
            PlaceholderLine(
                style = MaterialTheme.typography.bodyMedium,
                width = PlaceholderSymbolWidth,
            )
            PlaceholderLine(
                style = MaterialTheme.typography.headlineSmall,
                width = PlaceholderDetailPriceWidth,
            )
            PlaceholderLine(
                style = MaterialTheme.typography.bodyMedium,
                width = PlaceholderChangeWidth,
            )
            PlaceholderLine(
                style = MaterialTheme.typography.bodySmall,
                width = PlaceholderRankWidth,
            )
        }
    }
}

// The same layout as MarketDataSection.
@Composable
internal fun MarketDataPlaceholder() {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing8)) {
        PlaceholderLine(
            style = MaterialTheme.typography.titleMedium,
            width = PlaceholderSectionTitleWidth,
        )
        repeat(PLACEHOLDER_STAT_ROW_COUNT) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                PlaceholderLine(
                    style = MaterialTheme.typography.bodyMedium,
                    width = PlaceholderStatLabelWidth,
                )
                PlaceholderLine(
                    style = MaterialTheme.typography.bodyMedium,
                    width = PlaceholderStatValueWidth,
                )
            }
        }
    }
}

// The same layout as HoldingSection: the title, the quantity line, the field and the button.
@Composable
internal fun HoldingSectionPlaceholder() {
    // OutlinedTextField adds half of the floating label above the outline.
    val labelHalfHeight = with(LocalDensity.current) {
        MaterialTheme.typography.bodySmall.lineHeight.toDp() / 2
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing8)) {
        PlaceholderLine(
            style = MaterialTheme.typography.titleMedium,
            width = PlaceholderSectionTitleWidth,
        )
        PlaceholderLine(
            style = MaterialTheme.typography.bodyMedium,
            width = PlaceholderHoldingQuantityWidth,
        )
        ShimmerBox(
            modifier = Modifier
                .padding(top = labelHalfHeight)
                .fillMaxWidth()
                .height(OutlinedTextFieldDefaults.MinHeight),
            shape = OutlinedTextFieldDefaults.shape,
        )
        // The Button has a larger touch area, so we use minimumInteractiveComponentSize()
        Box(modifier = Modifier.minimumInteractiveComponentSize()) {
            ShimmerBox(
                modifier = Modifier.size(
                    width = PlaceholderButtonWidth,
                    height = ButtonDefaults.MinHeight,
                ),
                shape = ButtonDefaults.shape,
            )
        }
    }
}
