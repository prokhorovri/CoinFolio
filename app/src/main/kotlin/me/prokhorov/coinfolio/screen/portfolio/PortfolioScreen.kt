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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
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
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
import java.math.BigDecimal
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.component.CoinRowPlaceholder
import me.prokhorov.coinfolio.component.EdgeFadeBox
import me.prokhorov.coinfolio.component.FadingPullToRefreshBox
import me.prokhorov.coinfolio.component.PlaceholderLine
import me.prokhorov.coinfolio.domain.model.Coin
import me.prokhorov.coinfolio.domain.model.Holding
import me.prokhorov.coinfolio.domain.model.Portfolio
import me.prokhorov.coinfolio.domain.model.ValuedHolding
import me.prokhorov.coinfolio.layout.BottomInset
import me.prokhorov.coinfolio.layout.LocalBottomInset
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.theme.CoinIconSize
import me.prokhorov.coinfolio.theme.ListMaxWidth
import me.prokhorov.coinfolio.theme.PlaceholderTotalLabelWidth
import me.prokhorov.coinfolio.theme.PlaceholderTotalValueWidth
import me.prokhorov.coinfolio.theme.PortfolioTotalPaneWidth
import me.prokhorov.coinfolio.theme.Spacing12
import me.prokhorov.coinfolio.theme.Spacing16
import me.prokhorov.coinfolio.theme.Spacing24
import me.prokhorov.coinfolio.util.asMessage
import me.prokhorov.coinfolio.util.formatAsQuantity
import me.prokhorov.coinfolio.util.formatAsUsd
import me.prokhorov.coinfolio.util.isWideWindow

// The header key for the list
private const val TOTAL_HEADER_KEY = "total"

// The number of holdings is not known before they load, so the rows fill the screen.
// MB - refactor later somehow.
// A Column would compose all of them. A LazyColumn composes only the visible ones.
private const val PLACEHOLDER_ROW_COUNT = 50

@Composable
fun PortfolioScreen(
    onCoinClick: (String) -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel(),
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2(),
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
        bottomInset = LocalBottomInset.current,
        isWideWindow = windowAdaptiveInfo.isWideWindow,
        onRetry = { viewModel.onIntent(PortfolioIntent.LoadPortfolio) },
        onRefresh = { viewModel.onIntent(PortfolioIntent.RefreshPortfolio) },
        onCoinClick = onCoinClick,
    )
}

@Composable
private fun PortfolioContent(
    state: PortfolioUiState,
    snackbarHostState: SnackbarHostState,
    bottomInset: BottomInset,
    isWideWindow: Boolean,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onCoinClick: (String) -> Unit,
) {
    val rows = state.portfolio.valuedHoldings

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        // Add space for the bottom bar so the last item is visible when scrolled to the bottom.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
            .add(WindowInsets(bottom = bottomInset.padding)),
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            state.isLoading && rows.isEmpty() -> {
                LoadingState(
                    contentPadding = innerPadding,
                    isWideWindow = isWideWindow,
                    hasBottomFade = !bottomInset.hasBottomBar,
                )
            }

            rows.isEmpty() && state.error != null -> {
                ErrorState(
                    message = state.error.asMessage(),
                    onRetry = onRetry,
                    contentPadding = innerPadding,
                )
            }

            rows.isEmpty() -> EmptyState(contentPadding = innerPadding)

            else -> {
                PortfolioPanes(
                    portfolio = state.portfolio,
                    contentPadding = innerPadding,
                    isWideWindow = isWideWindow,
                    isRefreshing = state.isRefreshing,
                    // The bottom bar already covers the list, so it needs no fade there.
                    hasBottomFade = !bottomInset.hasBottomBar,
                    onRefresh = onRefresh,
                    onCoinClick = onCoinClick,
                )
            }
        }
    }
}

// Same layout as PortfolioPanes, so nothing moves when the holdings arrive.
@Composable
private fun LoadingState(
    contentPadding: PaddingValues,
    isWideWindow: Boolean,
    hasBottomFade: Boolean,
) {
    val layoutDirection = LocalLayoutDirection.current
    val loadingText = stringResource(R.string.loading_text)

    Row(
        modifier = Modifier
            .fillMaxSize()
            .clearAndSetSemantics { contentDescription = loadingText },
    ) {
        if (isWideWindow) {
            TotalValuePlaceholder(
                modifier = Modifier
                    .padding(
                        start = contentPadding.calculateStartPadding(layoutDirection),
                        top = contentPadding.calculateTopPadding(),
                    )
                    .width(PortfolioTotalPaneWidth),
            )
        }
        // The same fade as HoldingList. It also adds the same top padding.
        EdgeFadeBox(
            contentPadding = if (isWideWindow) {
                PaddingValues(
                    top = contentPadding.calculateTopPadding(),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPadding.calculateBottomPadding(),
                )
            } else {
                contentPadding
            },
            modifier = Modifier.weight(1f),
            hasTopFade = true,
            hasBottomFade = hasBottomFade,
        ) { fadePadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = fadePadding,
                horizontalAlignment = Alignment.CenterHorizontally,
                userScrollEnabled = false,
            ) {
                if (!isWideWindow) {
                    item(key = TOTAL_HEADER_KEY) {
                        TotalValuePlaceholder(modifier = Modifier.fillMaxWidth())
                    }
                }
                items(count = PLACEHOLDER_ROW_COUNT, key = { index -> index }) {
                    CoinRowPlaceholder()
                }
            }
        }
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

// a narrow window shows one pane: the list with the total on top.
// a wide window shows two panes: the total on the left, the list on the right.
@Composable
private fun PortfolioPanes(
    portfolio: Portfolio,
    contentPadding: PaddingValues,
    isWideWindow: Boolean,
    isRefreshing: Boolean,
    hasBottomFade: Boolean,
    onRefresh: () -> Unit,
    onCoinClick: (String) -> Unit,
) {
    // both layouts use this one state, so a rotation keeps the scroll position.
    // the total is a list item only in the narrow layout. So the list can move by one row.
    val listState = rememberLazyListState()

    if (isWideWindow) {
        TotalAndHoldingPanes(
            portfolio = portfolio,
            listState = listState,
            contentPadding = contentPadding,
            isRefreshing = isRefreshing,
            hasBottomFade = hasBottomFade,
            onRefresh = onRefresh,
            onCoinClick = onCoinClick,
        )
    } else {
        HoldingList(
            portfolio = portfolio,
            listState = listState,
            contentPadding = contentPadding,
            isRefreshing = isRefreshing,
            hasTotalHeader = true,
            hasBottomFade = hasBottomFade,
            onRefresh = onRefresh,
            onCoinClick = onCoinClick,
        )
    }
}

@Composable
private fun TotalAndHoldingPanes(
    portfolio: Portfolio,
    listState: LazyListState,
    contentPadding: PaddingValues,
    isRefreshing: Boolean,
    hasBottomFade: Boolean,
    onRefresh: () -> Unit,
    onCoinClick: (String) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current

    Row(modifier = Modifier.fillMaxSize()) {
        TotalValueHeader(
            total = portfolio.totalValue,
            modifier = Modifier
                .padding(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    top = contentPadding.calculateTopPadding(),
                )
                .width(PortfolioTotalPaneWidth),
        )
        HoldingList(
            portfolio = portfolio,
            listState = listState,
            // The total pane is on the start side, so the list needs no start padding.
            contentPadding = PaddingValues(
                top = contentPadding.calculateTopPadding(),
                end = contentPadding.calculateEndPadding(layoutDirection),
                bottom = contentPadding.calculateBottomPadding(),
            ),
            isRefreshing = isRefreshing,
            hasTotalHeader = false,
            hasBottomFade = hasBottomFade,
            onRefresh = onRefresh,
            onCoinClick = onCoinClick,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HoldingList(
    portfolio: Portfolio,
    listState: LazyListState,
    contentPadding: PaddingValues,
    isRefreshing: Boolean,
    hasTotalHeader: Boolean,
    hasBottomFade: Boolean,
    onRefresh: () -> Unit,
    onCoinClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layoutDirection = LocalLayoutDirection.current

    FadingPullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        contentPadding = contentPadding,
        modifier = modifier,
    ) {
        EdgeFadeBox(
            contentPadding = contentPadding,
            hasTopFade = true,
            hasBottomFade = hasBottomFade,
        ) { fadePadding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(
                    start = fadePadding.calculateStartPadding(layoutDirection),
                    end = fadePadding.calculateEndPadding(layoutDirection),
                    top = fadePadding.calculateTopPadding(),
                    bottom = fadePadding.calculateBottomPadding() + Spacing16,
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (hasTotalHeader) {
                    item(key = TOTAL_HEADER_KEY) {
                        TotalValueHeader(
                            total = portfolio.totalValue,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                items(portfolio.valuedHoldings, key = { it.holding.coinId }) { valuedHolding ->
                    ValuedHoldingRow(
                        valuedHolding = valuedHolding,
                        onClick = { onCoinClick(valuedHolding.holding.coinId) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TotalValueHeader(total: BigDecimal, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = Spacing16, vertical = Spacing24),
    ) {
        Text(
            text = stringResource(R.string.portfolio_screen_total_label_text),
            style = MaterialTheme.typography.labelMedium,
        )
        // A long total gets a smaller font instead of a second line.
        // If it still does not fit, it ends with dots.
        // TODO: show big totals in a short format like $12.3B if we need it.
        Text(
            text = total.formatAsUsd(),
            style = MaterialTheme.typography.headlineMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            autoSize = TextAutoSize.StepBased(
                minFontSize = MaterialTheme.typography.titleLarge.fontSize,
                maxFontSize = MaterialTheme.typography.headlineMedium.fontSize,
            ),
        )
    }
}

// A loading total with the same size as TotalValueHeader.
@Composable
private fun TotalValuePlaceholder(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = Spacing16, vertical = Spacing24),
    ) {
        PlaceholderLine(
            style = MaterialTheme.typography.labelMedium,
            width = PlaceholderTotalLabelWidth,
        )
        PlaceholderLine(
            style = MaterialTheme.typography.headlineMedium,
            width = PlaceholderTotalValueWidth,
        )
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
            .widthIn(max = ListMaxWidth)
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
    PortfolioContentPreviewBody(isWideWindow = false)
}

@Preview(name = "Wide", showBackground = true, widthDp = 891, heightDp = 411)
@Composable
private fun PortfolioContentWidePreview() {
    PortfolioContentPreviewBody(isWideWindow = true)
}

@Composable
private fun PortfolioContentPreviewBody(isWideWindow: Boolean) {
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
            // A wide window has the rail, so no bottom bar.
            bottomInset = BottomInset(padding = 0.dp, hasBottomBar = !isWideWindow),
            isWideWindow = isWideWindow,
            onRetry = {},
            onRefresh = {},
            onCoinClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PortfolioContentLoadingPreview() {
    PortfolioContentLoadingPreviewBody(isWideWindow = false)
}

@Preview(name = "Wide loading", showBackground = true, widthDp = 891, heightDp = 411)
@Composable
private fun PortfolioContentWideLoadingPreview() {
    PortfolioContentLoadingPreviewBody(isWideWindow = true)
}

@Composable
private fun PortfolioContentLoadingPreviewBody(isWideWindow: Boolean) {
    CoinFolioTheme {
        PortfolioContent(
            state = PortfolioUiState(isLoading = true),
            snackbarHostState = remember { SnackbarHostState() },
            bottomInset = BottomInset(padding = 0.dp, hasBottomBar = !isWideWindow),
            isWideWindow = isWideWindow,
            onRetry = {},
            onRefresh = {},
            onCoinClick = {},
        )
    }
}
