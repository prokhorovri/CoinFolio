package me.prokhorov.coinfolio.screen.portfolio

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.model.Portfolio
import me.prokhorov.coinfolio.domain.model.ValuedHolding
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.theme.CoinIconSize
import me.prokhorov.coinfolio.theme.FadeExtraHeight
import me.prokhorov.coinfolio.theme.Spacing12
import me.prokhorov.coinfolio.theme.Spacing16
import me.prokhorov.coinfolio.theme.Spacing24
import me.prokhorov.coinfolio.ui.FadingPullToRefreshIndicator
import me.prokhorov.coinfolio.ui.LocalBottomBarHeight
import me.prokhorov.coinfolio.ui.overlay.EdgeFadeOverlay
import me.prokhorov.coinfolio.ui.overlay.FadeEdge
import me.prokhorov.coinfolio.util.asMessage
import me.prokhorov.coinfolio.util.formatAsQuantity
import me.prokhorov.coinfolio.util.formatAsUsd

// The header key for the list
private const val TOTAL_HEADER_KEY = "total"

@Composable
fun PortfolioScreen(
    onCoinClick: (String) -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    LaunchedEffect(key1 = lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is PortfolioEffect.ShowError -> {
                        snackbarHostState.showSnackbar(effect.error.asMessage(context))
                    }
                }
            }
        }
    }

    PortfolioContent(
        state = state,
        snackbarHostState = snackbarHostState,
        bottomBarHeight = LocalBottomBarHeight.current,
        onRetry = { viewModel.onIntent(PortfolioIntent.LoadPortfolio) },
        onRefresh = { viewModel.onIntent(PortfolioIntent.RefreshPortfolio) },
        onCoinClick = onCoinClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortfolioContent(
    state: PortfolioUiState,
    snackbarHostState: SnackbarHostState,
    bottomBarHeight: Dp,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onCoinClick: (String) -> Unit,
) {
    val rows = state.portfolio.valuedHoldings
    val pullToRefreshState = rememberPullToRefreshState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        // Add space for the bottom bar so the last item is visible when scrolled to the bottom.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
            .add(WindowInsets(bottom = bottomBarHeight)),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize(),
            state = pullToRefreshState,
            indicator = {
                // The list scrolls under the status bar, so the indicator starts below it.
                FadingPullToRefreshIndicator(
                    state = pullToRefreshState,
                    isRefreshing = state.isRefreshing,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = innerPadding.calculateTopPadding()),
                )
            },
        ) {
            when {
                state.isLoading && rows.isEmpty() -> LoadingState(contentPadding = innerPadding)

                rows.isEmpty() && state.error != null -> {
                    ErrorState(
                        message = state.error.asMessage(),
                        onRetry = onRetry,
                        contentPadding = innerPadding,
                    )
                }

                rows.isEmpty() -> EmptyState(contentPadding = innerPadding)

                else -> {
                    HoldingList(
                        portfolio = state.portfolio,
                        contentPadding = innerPadding,
                        onCoinClick = onCoinClick,
                    )
                }
            }

            EdgeFadeOverlay(edge = FadeEdge.TOP)
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
        Text(text = stringResource(R.string.portfolio_screen_empty_text))
    }
}

@Composable
private fun HoldingList(
    portfolio: Portfolio,
    contentPadding: PaddingValues,
    onCoinClick: (String) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection),
            end = contentPadding.calculateEndPadding(layoutDirection),
            top = contentPadding.calculateTopPadding() + FadeExtraHeight,
            bottom = contentPadding.calculateBottomPadding() + Spacing16,
        ),
    ) {
        item(key = TOTAL_HEADER_KEY) {
            TotalValueHeader(total = portfolio.totalValue)
        }
        items(portfolio.valuedHoldings, key = { it.holding.coinId }) { valuedHolding ->
            ValuedHoldingRow(
                valuedHolding = valuedHolding,
                onClick = { onCoinClick(valuedHolding.holding.coinId) },
            )
        }
    }
}

@Composable
private fun TotalValueHeader(total: BigDecimal) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing16, vertical = Spacing24),
    ) {
        Text(
            text = stringResource(R.string.portfolio_screen_total_label_text),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(text = total.formatAsUsd(), style = MaterialTheme.typography.headlineMedium)
    }
}

/*
 * A row with no Coin shows the coin id and dashes. The market call can miss a coin, and the
 * quantity is still worth showing.
 */
@Composable
private fun ValuedHoldingRow(valuedHolding: ValuedHolding, onClick: () -> Unit) {
    val coin = valuedHolding.coin
    val quantity = valuedHolding.holding.quantity.formatAsQuantity()
    val noValue = stringResource(R.string.no_value_text)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing16, vertical = Spacing12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (coin == null) {
            Spacer(modifier = Modifier.size(CoinIconSize))
        } else {
            AsyncImage(
                model = coin.imageUrl,
                contentDescription = coin.name,
                modifier = Modifier.size(CoinIconSize),
            )
        }
        Column(
            modifier = Modifier
                .padding(horizontal = Spacing12)
                .weight(1f),
        ) {
            Text(
                text = coin?.name ?: valuedHolding.holding.coinId,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = if (coin == null) {
                    quantity
                } else {
                    stringResource(
                        R.string.portfolio_screen_quantity_text,
                        quantity,
                        coin.symbol.uppercase(),
                    )
                },
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = valuedHolding.value?.formatAsUsd() ?: noValue,
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = coin?.currentPrice?.formatAsUsd() ?: noValue,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PortfolioContentPreview() {
    CoinFolioTheme {
        PortfolioContent(
            state = PortfolioUiState(
                portfolio = Portfolio(
                    valuedHoldings = listOf(
                        ValuedHolding(
                            holding = Holding(coinId = "bitcoin", quantity = BigDecimal("0.5")),
                            coin = Coin(
                                id = "bitcoin",
                                symbol = "btc",
                                name = "Bitcoin",
                                imageUrl = "",
                                currentPrice = BigDecimal("65000.12"),
                                priceChangePercent24h = BigDecimal("2.34"),
                            ),
                        ),
                        ValuedHolding(
                            holding = Holding(coinId = "unknown-coin", quantity = BigDecimal("12")),
                            coin = null,
                        ),
                    ),
                ),
            ),
            snackbarHostState = remember { SnackbarHostState() },
            bottomBarHeight = 0.dp,
            onRetry = {},
            onRefresh = {},
            onCoinClick = {},
        )
    }
}
