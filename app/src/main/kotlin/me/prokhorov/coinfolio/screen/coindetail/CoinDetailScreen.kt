package me.prokhorov.coinfolio.screen.coindetail

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
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
import kotlinx.coroutines.flow.drop
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.theme.CoinDetailIconSize
import me.prokhorov.coinfolio.theme.CoinFolioTheme
import me.prokhorov.coinfolio.theme.Spacing16
import me.prokhorov.coinfolio.theme.Spacing24
import me.prokhorov.coinfolio.theme.Spacing4
import me.prokhorov.coinfolio.theme.Spacing8
import me.prokhorov.coinfolio.theme.extendedColors
import me.prokhorov.coinfolio.ui.overlay.EdgeFadeBox
import me.prokhorov.coinfolio.util.QuantityInput
import me.prokhorov.coinfolio.util.asMessage
import me.prokhorov.coinfolio.util.formatAsCompact
import me.prokhorov.coinfolio.util.formatAsCompactUsd
import me.prokhorov.coinfolio.util.formatAsPercent
import me.prokhorov.coinfolio.util.formatAsUsd
import me.prokhorov.coinfolio.util.isGain

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal const val TOP_BAR_TAG = "top_bar"

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal const val COIN_SUMMARY_TAG = "coin_summary"

@Composable
fun CoinDetailScreen(
    onBackClick: () -> Unit,
    viewModel: CoinDetailViewModel = hiltViewModel(),
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
    onBackClick: () -> Unit,
    onQuantityChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        // Keeps the snackbar above the keyboard.
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.union(WindowInsets.ime),
        topBar = {
            TopAppBar(
                title = { Text(text = state.coin?.name ?: state.coinId) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.back_text),
                        )
                    }
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

            state.coin == null -> LoadingState(contentPadding = innerPadding)

            else -> {
                CoinDetailBody(
                    coin = state.coin,
                    quantityInput = state.quantityInput,
                    quantityError = state.quantityError,
                    savedQuantity = state.savedQuantity,
                    holdingError = state.holdingError,
                    canSave = state.canSave,
                    contentPadding = innerPadding,
                    topBarHeight = topBarHeight,
                    onQuantityChange = onQuantityChange,
                    onSaveClick = onSaveClick,
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
private fun CoinDetailBody(
    coin: CoinDetail,
    quantityInput: String,
    quantityError: QuantityError?,
    savedQuantity: BigDecimal?,
    holdingError: AppError?,
    canSave: Boolean,
    contentPadding: PaddingValues,
    topBarHeight: Dp,
    onQuantityChange: (String) -> Unit,
    onSaveClick: () -> Unit,
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
        modifier = Modifier
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
    ) { fadePadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(fadePadding)
                .padding(horizontal = Spacing16, vertical = Spacing24),
            verticalArrangement = Arrangement.spacedBy(Spacing16),
        ) {
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
                onQuantityChange = onQuantityChange,
                onSaveClick = onSaveClick,
            )
        }
    }
}

@Composable
private fun CoinSummary(coin: CoinDetail) {
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
private fun MarketDataSection(coin: CoinDetail) {
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

@Composable
private fun HoldingSection(
    symbol: String,
    quantityInput: String,
    quantityError: QuantityError?,
    savedQuantity: BigDecimal?,
    holdingError: AppError?,
    canSave: Boolean,
    onQuantityChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    val errorMessage = holdingError?.asMessage()
    val focusManager = LocalFocusManager.current
    val quantityState = rememberTextFieldState()

    val quantityErrorMessage = quantityError?.asMessage()
    val currentInput by rememberUpdatedState(quantityInput)
    val currentOnQuantityChange by rememberUpdatedState(onQuantityChange)

    // The field keeps its own text, so the cursor stays in place. This effect sends the text up.
    // It runs once, because quantityState never changes. The two current* values above are
    // the ones that stay fresh.
    // Text the ViewModel already knows is skipped: it would mark the field as edited.
    LaunchedEffect(quantityState) {
        snapshotFlow { quantityState.text.toString() }
            // The text the field starts with is not an edit. Sending it up would erase the
            // quantity that came from the database.
            .drop(1)
            .collect { text ->
                if (text != currentInput) {
                    currentOnQuantityChange(text)
                }
            }
    }

    // The other direction: text from the ViewModel goes into the field, like the saved holding
    // when the screen opens.
    // TODO: drop this once the effects come from a Channel, not a SharedFlow. Then a new
    //  CoinDetailEffect.SetQuantity(text) can set the text where the effects are collected:
    //  is CoinDetailEffect.SetQuantity -> quantityState.setTextAndPlaceCursorAtEnd(effect.text)
    LaunchedEffect(quantityInput) {
        if (quantityInput != quantityState.text.toString()) {
            quantityState.setTextAndPlaceCursorAtEnd(quantityInput)
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing8)) {
        Text(
            text = stringResource(R.string.coin_detail_screen_holding_title_text),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = errorMessage ?: when (savedQuantity) {
                null -> stringResource(R.string.coin_detail_screen_not_held_text)
                else -> "${QuantityInput.format(savedQuantity)} ${symbol.uppercase()}"
            },
            color = when (errorMessage) {
                null -> Color.Unspecified
                else -> MaterialTheme.colorScheme.error
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            state = quantityState,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = stringResource(R.string.coin_detail_screen_quantity_label_text))
            },
            supportingText = quantityErrorMessage?.let { { Text(text = it) } },
            isError = quantityErrorMessage != null,
            inputTransformation = QuantityInputTransformation,
            // Without an explicit action the Done key only hides the keyboard. The field then
            // keeps the cursor.
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            onKeyboardAction = { focusManager.clearFocus() },
            lineLimits = TextFieldLineLimits.SingleLine,
        )
        Button(onClick = onSaveClick, enabled = canSave) {
            Text(text = stringResource(R.string.coin_detail_screen_save_text))
        }
    }
}

/**
 * Same idea as [AppError.asMessage]. The state says which rule the input broke, and
 * presentation turns that into text.
 */
@Composable
@ReadOnlyComposable
private fun QuantityError.asMessage(): String {
    return when (this) {
        QuantityError.INVALID_CHARACTER -> {
            stringResource(R.string.coin_detail_screen_quantity_invalid_text)
        }

        QuantityError.TOO_MANY_INTEGER_DIGITS -> {
            stringResource(R.string.coin_detail_screen_quantity_too_large_text)
        }

        QuantityError.TOO_MANY_FRACTION_DIGITS -> {
            stringResource(
                R.string.coin_detail_screen_quantity_decimals_text,
                QuantityInput.MAX_FRACTION_DIGITS,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoinDetailContentPreview() {
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
            onBackClick = {},
            onQuantityChange = {},
            onSaveClick = {},
            onRetry = {},
        )
    }
}
