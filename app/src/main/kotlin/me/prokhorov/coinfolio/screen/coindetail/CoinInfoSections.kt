package me.prokhorov.coinfolio.screen.coindetail

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import coil3.compose.AsyncImage
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.theme.CoinDetailIconSize
import me.prokhorov.coinfolio.theme.Spacing16
import me.prokhorov.coinfolio.theme.Spacing4
import me.prokhorov.coinfolio.theme.Spacing8
import me.prokhorov.coinfolio.theme.extendedColors
import me.prokhorov.coinfolio.util.formatAsCompact
import me.prokhorov.coinfolio.util.formatAsCompactUsd
import me.prokhorov.coinfolio.util.formatAsPercent
import me.prokhorov.coinfolio.util.formatAsUsd
import me.prokhorov.coinfolio.util.isGain

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal const val COIN_SUMMARY_TAG = "coin_summary"

@Composable
internal fun CoinSummary(coin: CoinDetail) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(COIN_SUMMARY_TAG),
        horizontalArrangement = Arrangement.spacedBy(Spacing16),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = coin.imageUrl,
            contentDescription = coin.name,
            modifier = Modifier.size(CoinDetailIconSize),
        )
        Column(verticalArrangement = Arrangement.spacedBy(Spacing4)) {
            Text(text = coin.symbol.uppercase(), style = MaterialTheme.typography.bodyMedium)
            Text(
                text = coin.currentPrice?.formatAsUsd() ?: stringResource(R.string.no_value_text),
                style = MaterialTheme.typography.headlineSmall,
            )

            val percent = coin.priceChangePercent24h
            Text(
                text = percent?.formatAsPercent() ?: stringResource(R.string.no_value_text),
                color = when {
                    percent == null -> Color.Unspecified
                    percent.isGain() -> MaterialTheme.extendedColors.positive
                    else -> MaterialTheme.extendedColors.negative
                },
                style = MaterialTheme.typography.bodyMedium,
            )
            coin.marketCapRank?.let { rank ->
                Text(
                    text = stringResource(R.string.coin_detail_screen_rank_text, rank),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
internal fun MarketDataSection(coin: CoinDetail) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing8)) {
        Text(
            text = stringResource(R.string.coin_detail_screen_market_data_title_text),
            style = MaterialTheme.typography.titleMedium,
        )
        coin.high24h?.let { value ->
            CoinStatRow(
                label = stringResource(R.string.coin_detail_screen_high_24h_label_text),
                value = value.formatAsUsd(),
            )
        }
        coin.low24h?.let { value ->
            CoinStatRow(
                label = stringResource(R.string.coin_detail_screen_low_24h_label_text),
                value = value.formatAsUsd(),
            )
        }
        coin.marketCap?.let { value ->
            CoinStatRow(
                label = stringResource(R.string.coin_detail_screen_market_cap_label_text),
                value = value.formatAsCompactUsd(),
            )
        }
        coin.totalVolume?.let { value ->
            CoinStatRow(
                label = stringResource(R.string.coin_detail_screen_volume_label_text),
                value = value.formatAsCompactUsd(),
            )
        }
        coin.allTimeHigh?.let { value ->
            CoinStatRow(
                label = stringResource(R.string.coin_detail_screen_ath_label_text),
                value = value.formatAsUsd(),
            )
        }
        coin.circulatingSupply?.let { value ->
            CoinStatRow(
                label = stringResource(R.string.coin_detail_screen_supply_label_text),
                value = "${value.formatAsCompact()} ${coin.symbol.uppercase()}",
            )
        }
    }
}

@Composable
private fun CoinStatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
