package me.prokhorov.coinfolio.screen.coindetail

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import java.math.BigDecimal
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.component.EdgeFadeBox
import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.theme.Spacing16
import me.prokhorov.coinfolio.theme.Spacing24
import me.prokhorov.coinfolio.util.asMessage
import me.prokhorov.coinfolio.util.isWideWindow

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal const val TOP_BAR_TAG = "top_bar"

@Composable
fun CoinDetailScreen(
    onBackClick: () -> Unit,
    viewModel: CoinDetailViewModel = hiltViewModel(),
    windowAdaptiveInfo: WindowAdaptiveInfo = currentWindowAdaptiveInfoV2(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val savedMessage by rememberUpdatedState(
        stringResource(R.string.coin_detail_screen_saved_text),
    )

    LaunchedEffect(key1 = lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is CoinDetailEffect.ShowError -> {
                        snackbarHostState.showSnackbar(effect.error.asMessage(context))
                    }

                    // The edit is over, so the keyboard goes away. It has to happen before the
                    // snackbar call. That call waits until the snackbar is gone.
                    is CoinDetailEffect.HoldingSaved -> {
                        focusManager.clearFocus()
                        snackbarHostState.showSnackbar(savedMessage)
                    }
                }
            }
        }
    }

    CoinDetailContent(
        state = state,
        snackbarHostState = snackbarHostState,
        isWideWindow = windowAdaptiveInfo.isWideWindow,
        onBackClick = onBackClick,
        onQuantityChange = { viewModel.onIntent(CoinDetailIntent.QuantityChanged(it)) },
        onSaveClick = { viewModel.onIntent(CoinDetailIntent.SaveHolding) },
        onRetry = { viewModel.onIntent(CoinDetailIntent.LoadCoin) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CoinDetailContent(
    state: CoinDetailUiState,
    snackbarHostState: SnackbarHostState,
    isWideWindow: Boolean,
    onBackClick: () -> Unit,
    onQuantityChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onRetry: () -> Unit,
) {
    // Both layouts use this one state, so a rotation keeps the scroll position.
    val coinScrollState = rememberScrollState()
    // The holding pane of the wide layout.
    val holdingScrollState = rememberScrollState()
    // Kept here, so a rotation keeps the text. HoldingSection sits at a different place in
    // each layout.
    val quantityState = rememberTextFieldState()

    val isScrolled = coinScrollState.canScrollBackward ||
        (isWideWindow && holdingScrollState.canScrollBackward)

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        // Keeps the snackbar above the keyboard.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime),
        topBar = {
            TopAppBar(
                title = {
                    // The title is shown only while nothing is scrolled under it.
                    AnimatedVisibility(visible = !isScrolled, enter = fadeIn(), exit = fadeOut()) {
                        Text(text = state.coin?.name ?: state.coinId)
                    }
                },
                navigationIcon = {
                    BackButton(hasBackground = isScrolled, onClick = onBackClick)
                },
                modifier = Modifier.testTag(TOP_BAR_TAG),
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        // The top padding is the status bar plus the bar. This is the bar alone.
        val topBarHeight = innerPadding.calculateTopPadding() -
            TopAppBarDefaults.windowInsets.asPaddingValues().calculateTopPadding()

        @Suppress("IntroduceWhenSubject")
        when {
            state.coin == null && state.error != null -> {
                ErrorState(
                    message = state.error.asMessage(),
                    onRetry = onRetry,
                    contentPadding = innerPadding,
                )
            }

            state.coin == null -> LoadingState(isWideWindow, innerPadding, topBarHeight)

            else -> {
                CoinDetailBody(
                    coin = state.coin,
                    quantityInput = state.quantityInput,
                    quantityError = state.quantityError,
                    savedQuantity = state.savedQuantity,
                    holdingError = state.holdingError,
                    canSave = state.canSave,
                    isWideWindow = isWideWindow,
                    coinScrollState = coinScrollState,
                    holdingScrollState = holdingScrollState,
                    quantityState = quantityState,
                    contentPadding = innerPadding,
                    topBarHeight = topBarHeight,
                    onQuantityChange = onQuantityChange,
                    onSaveClick = onSaveClick,
                )
            }
        }
    }
}

// The same panes as CoinDetailPanes, so nothing moves when the coin arrives.
@Composable
private fun LoadingState(isWideWindow: Boolean, contentPadding: PaddingValues, topBarHeight: Dp) {
    val loadingText = stringResource(R.string.loading_text)
    // DetailPane needs a scroll state. These never move, because scrolling is off.
    val coinScrollState = rememberScrollState()
    val holdingScrollState = rememberScrollState()

    CoinDetailFrame(
        contentPadding = contentPadding,
        topBarHeight = topBarHeight,
        modifier = Modifier.clearAndSetSemantics { contentDescription = loadingText },
    ) { fadePadding ->
        if (isWideWindow) {
            Row(modifier = Modifier.fillMaxSize()) {
                DetailPane(
                    scrollState = coinScrollState,
                    contentPadding = fadePadding,
                    modifier = Modifier.weight(1f),
                    isScrollEnabled = false,
                ) {
                    CoinSummaryPlaceholder()
                    HorizontalDivider()
                    MarketDataPlaceholder()
                }
                DetailPane(
                    scrollState = holdingScrollState,
                    contentPadding = fadePadding,
                    modifier = Modifier.weight(1f),
                    isScrollEnabled = false,
                ) {
                    HoldingSectionPlaceholder()
                }
            }
        } else {
            DetailPane(
                scrollState = coinScrollState,
                contentPadding = fadePadding,
                isScrollEnabled = false,
            ) {
                CoinSummaryPlaceholder()
                HorizontalDivider()
                MarketDataPlaceholder()
                HorizontalDivider()
                HoldingSectionPlaceholder()
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
private fun CoinDetailBody(
    coin: CoinDetail,
    quantityInput: String,
    quantityError: QuantityError?,
    savedQuantity: BigDecimal?,
    holdingError: AppError?,
    canSave: Boolean,
    isWideWindow: Boolean,
    coinScrollState: ScrollState,
    holdingScrollState: ScrollState,
    quantityState: TextFieldState,
    contentPadding: PaddingValues,
    topBarHeight: Dp,
    onQuantityChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    CoinDetailFrame(contentPadding = contentPadding, topBarHeight = topBarHeight) { fadePadding ->
        CoinDetailPanes(
            coin = coin,
            quantityInput = quantityInput,
            quantityError = quantityError,
            savedQuantity = savedQuantity,
            holdingError = holdingError,
            canSave = canSave,
            isWideWindow = isWideWindow,
            coinScrollState = coinScrollState,
            holdingScrollState = holdingScrollState,
            quantityState = quantityState,
            contentPadding = fadePadding,
            onQuantityChange = onQuantityChange,
            onSaveClick = onSaveClick,
        )
    }
}

// The scroll area with the edge fades. The coin and the loading placeholder both use it.
@Composable
private fun CoinDetailFrame(
    contentPadding: PaddingValues,
    topBarHeight: Dp,
    modifier: Modifier = Modifier,
    content: @Composable (fadePadding: PaddingValues) -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    // Checks the keyboard height, not its visibility. A floating keyboard covers no edge,
    // so the fade stays.
    val isKeyboardDocked = WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp

    // The part of the navigation bar that the keyboard does not cover. Zero when the keyboard
    // is docked.
    val navigationBarHeight = WindowInsets.navigationBars
        .exclude(WindowInsets.ime)
        .asPaddingValues()
        .calculateBottomPadding()

    EdgeFadeBox(
        // The scroll area reaches the top and the bottom of the screen.
        // The content scrolls under the top bar and the navigation bar.
        modifier = modifier
            .padding(
                start = contentPadding.calculateStartPadding(layoutDirection),
                end = contentPadding.calculateEndPadding(layoutDirection),
            )
            // Stop the scroll area at the keyboard, so the text field stays visible.
            .imePadding(),
        // Add space for the top bar and the navigation bar, so the first and last items can
        // scroll out from under them.
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = navigationBarHeight,
        ),
        topBarHeight = topBarHeight,
        hasTopFade = true,
        // No fade over the keyboard. The scroll area ends at its top.
        hasBottomFade = !isKeyboardDocked,
        content = content,
    )
}

// narrow: one pane.
// wide: two panes - the coin on the left, the holding on the right.
@Composable
private fun CoinDetailPanes(
    coin: CoinDetail,
    quantityInput: String,
    quantityError: QuantityError?,
    savedQuantity: BigDecimal?,
    holdingError: AppError?,
    canSave: Boolean,
    isWideWindow: Boolean,
    coinScrollState: ScrollState,
    holdingScrollState: ScrollState,
    quantityState: TextFieldState,
    contentPadding: PaddingValues,
    onQuantityChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    if (isWideWindow) {
        Row(modifier = Modifier.fillMaxSize()) {
            DetailPane(
                scrollState = coinScrollState,
                contentPadding = contentPadding,
                modifier = Modifier.weight(1f),
            ) {
                CoinSummary(coin = coin)
                HorizontalDivider()
                MarketDataSection(coin = coin)
            }
            DetailPane(
                scrollState = holdingScrollState,
                contentPadding = contentPadding,
                modifier = Modifier.weight(1f),
            ) {
                HoldingSection(
                    symbol = coin.symbol,
                    quantityInput = quantityInput,
                    quantityError = quantityError,
                    savedQuantity = savedQuantity,
                    holdingError = holdingError,
                    canSave = canSave,
                    quantityState = quantityState,
                    onQuantityChange = onQuantityChange,
                    onSaveClick = onSaveClick,
                )
            }
        }
    } else {
        DetailPane(scrollState = coinScrollState, contentPadding = contentPadding) {
            CoinSummary(coin = coin)
            HorizontalDivider()
            MarketDataSection(coin = coin)
            HorizontalDivider()
            HoldingSection(
                symbol = coin.symbol,
                quantityInput = quantityInput,
                quantityError = quantityError,
                savedQuantity = savedQuantity,
                holdingError = holdingError,
                canSave = canSave,
                quantityState = quantityState,
                onQuantityChange = onQuantityChange,
                onSaveClick = onSaveClick,
            )
        }
    }
}

@Composable
private fun DetailPane(
    scrollState: ScrollState,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    isScrollEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState, enabled = isScrollEnabled)
            .padding(contentPadding)
            .padding(horizontal = Spacing16, vertical = Spacing24),
        verticalArrangement = Arrangement.spacedBy(Spacing16),
        content = content,
    )
}

@Preview(showBackground = true)
@Composable
private fun CoinDetailContentPreview() {
    CoinDetailContentPreviewBody(isWideWindow = false)
}

@Preview(name = "Wide", showBackground = true, widthDp = 891, heightDp = 411)
@Composable
private fun CoinDetailContentWidePreview() {
    CoinDetailContentPreviewBody(isWideWindow = true)
}

@Composable
private fun CoinDetailContentPreviewBody(isWideWindow: Boolean) {
    CoinFolioTheme {
        CoinDetailContent(
            state = CoinDetailUiState(
                coinId = "bitcoin",
                coin = CoinDetail(
                    id = "bitcoin",
                    symbol = "btc",
                    name = "Bitcoin",
                    imageUrl = "",
                    currentPrice = BigDecimal("65000.12"),
                    priceChangePercent24h = BigDecimal("2.34"),
                    marketCapRank = 1,
                    high24h = BigDecimal("66120.00"),
                    low24h = BigDecimal("64010.55"),
                    marketCap = BigDecimal("1283000000000"),
                    totalVolume = BigDecimal("41200000000"),
                    allTimeHigh = BigDecimal("73750.07"),
                    circulatingSupply = BigDecimal("19700000"),
                ),
                savedQuantity = BigDecimal("0.5"),
                quantityInput = "0.5",
                canSave = true,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            isWideWindow = isWideWindow,
            onBackClick = {},
            onQuantityChange = {},
            onSaveClick = {},
            onRetry = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CoinDetailContentLoadingPreview() {
    CoinDetailContentLoadingPreviewBody(isWideWindow = false)
}

@Preview(name = "Wide loading", showBackground = true, widthDp = 891, heightDp = 411)
@Composable
private fun CoinDetailContentWideLoadingPreview() {
    CoinDetailContentLoadingPreviewBody(isWideWindow = true)
}

@Composable
private fun CoinDetailContentLoadingPreviewBody(isWideWindow: Boolean) {
    CoinFolioTheme {
        CoinDetailContent(
            state = CoinDetailUiState(coinId = "bitcoin"),
            snackbarHostState = remember { SnackbarHostState() },
            isWideWindow = isWideWindow,
            onBackClick = {},
            onQuantityChange = {},
            onSaveClick = {},
            onRetry = {},
        )
    }
}
