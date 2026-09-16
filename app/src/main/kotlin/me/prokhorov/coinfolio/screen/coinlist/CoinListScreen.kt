package me.prokhorov.coinfolio.screen.coinlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
import java.math.BigDecimal
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.theme.CoinIconSize
import me.prokhorov.coinfolio.theme.Spacing12
import me.prokhorov.coinfolio.theme.Spacing16
import me.prokhorov.coinfolio.theme.TopFadeExtraHeight
import me.prokhorov.coinfolio.theme.extendedColors
import me.prokhorov.coinfolio.ui.LocalBottomBarHeight
import me.prokhorov.coinfolio.util.asMessage
import me.prokhorov.coinfolio.util.formatAsPercent
import me.prokhorov.coinfolio.util.formatAsUsd
import me.prokhorov.coinfolio.util.isGain

@Composable
fun CoinListScreen(
    onCoinClick: (String) -> Unit,
    viewModel: CoinListViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    LaunchedEffect(key1 = lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is CoinListEffect.ShowError -> {
                        snackbarHostState.showSnackbar(effect.error.asMessage(context))
                    }
                }
            }
        }
    }

    CoinListContent(
        state = state,
        snackbarHostState = snackbarHostState,
        bottomBarHeight = LocalBottomBarHeight.current,
        onRetry = { viewModel.onIntent(CoinListIntent.LoadCoins) },
        onCoinClick = onCoinClick,
    )
}

@Composable
private fun CoinListContent(
    state: CoinListUiState,
    snackbarHostState: SnackbarHostState,
    bottomBarHeight: Dp,
    onRetry: () -> Unit,
    onCoinClick: (String) -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // Add space for the bottom bar so the last item is visible when scrolled to the bottom.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
            .add(WindowInsets(bottom = bottomBarHeight)),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            state.isLoading && state.coins.isEmpty() -> LoadingState(contentPadding = innerPadding)

            state.coins.isEmpty() && state.error != null -> {
                ErrorState(
                    message = state.error.asMessage(),
                    onRetry = onRetry,
                    contentPadding = innerPadding,
                )
            }

            state.coins.isEmpty() -> EmptyState(contentPadding = innerPadding)

            else -> {
                CoinList(
                    coins = state.coins,
                    contentPadding = innerPadding,
                    onCoinClick = onCoinClick,
                )
            }
        }
    }
}

@Composable
private fun LoadingState(contentPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, contentPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = message)
            Button(onClick = onRetry) {
                Text(text = stringResource(R.string.retry_text))
            }
        }
    }
}

@Composable
private fun EmptyState(contentPadding: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(R.string.coin_list_screen_empty_text))
    }
}

@Composable
private fun CoinList(
    coins: List<Coin>,
    contentPadding: PaddingValues,
    onCoinClick: (String) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection),
            end = contentPadding.calculateEndPadding(layoutDirection),
            top = contentPadding.calculateTopPadding() + TopFadeExtraHeight,
            bottom = contentPadding.calculateBottomPadding() + Spacing16,
        ),
    ) {
        items(coins, key = Coin::id) { coin ->
            CoinRow(coin = coin, onClick = { onCoinClick(coin.id) })
        }
    }
}

@Composable
private fun CoinRow(coin: Coin, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing16, vertical = Spacing12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = coin.imageUrl,
            contentDescription = coin.name,
            modifier = Modifier.size(CoinIconSize),
        )
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing12)
                .weight(1f),
        ) {
            Text(text = coin.name, style = MaterialTheme.typography.bodyLarge)
            Text(text = coin.symbol.uppercase(), style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = coin.currentPrice?.formatAsUsd() ?: stringResource(R.string.no_value_text),
                style = MaterialTheme.typography.bodyLarge,
            )

            val percent = coin.priceChangePercent24h
            Text(
                text = percent?.formatAsPercent() ?: stringResource(R.string.no_value_text),
                color = when {
                    percent == null -> Color.Unspecified
                    percent.isGain() -> MaterialTheme.extendedColors.positive
                    else -> MaterialTheme.extendedColors.negative
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoinListContentPreview() {
    CoinFolioTheme {
        CoinListContent(
            state = CoinListUiState(
                coins = listOf(
                    Coin(
                        id = "bitcoin",
                        symbol = "btc",
                        name = "Bitcoin",
                        imageUrl = "",
                        currentPrice = BigDecimal("65000.12"),
                        priceChangePercent24h = BigDecimal("2.34"),
                    ),
                    Coin(
                        id = "ethereum",
                        symbol = "eth",
                        name = "Ethereum",
                        imageUrl = "",
                        currentPrice = BigDecimal("3200.50"),
                        priceChangePercent24h = BigDecimal("-1.12"),
                    ),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            bottomBarHeight = 0.dp,
            onRetry = {},
            onCoinClick = {},
        )
    }
}
