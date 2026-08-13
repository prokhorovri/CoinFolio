package me.prokhorov.coinfolio.screen.coindetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import coil3.compose.AsyncImage
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
import me.prokhorov.coinfolio.util.asMessage
import me.prokhorov.coinfolio.util.formatAsCompact
import me.prokhorov.coinfolio.util.formatAsCompactUsd
import me.prokhorov.coinfolio.util.formatAsPercent
import me.prokhorov.coinfolio.util.formatAsQuantity
import me.prokhorov.coinfolio.util.formatAsUsd
import me.prokhorov.coinfolio.util.isGain
import java.math.BigDecimal

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
        stringResource(R.string.coin_detail_screen_saved_text)
    )

    LaunchedEffect(key1 = lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.effect.collect { effect ->
                when (effect) {
                    is CoinDetailEffect.ShowError -> {
                        snackbarHostState.showSnackbar(effect.error.asMessage(context))
                    }

                    // The edit is over, so the keyboard goes away. It has to happen before the
                    // snackbar call, which waits until the snackbar is gone.
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
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
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
                    savedQuantity = state.savedQuantity,
                    holdingError = state.holdingError,
                    canSave = state.canSave,
                    contentPadding = innerPadding,
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
    savedQuantity: BigDecimal?,
    holdingError: AppError?,
    canSave: Boolean,
    contentPadding: PaddingValues,
    onQuantityChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
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
            savedQuantity = savedQuantity,
            holdingError = holdingError,
            canSave = canSave,
            onQuantityChange = onQuantityChange,
            onSaveClick = onSaveClick,
        )
    }
}

@Composable
private fun CoinSummary(coin: CoinDetail) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                text = coin.currentPrice.formatAsUsd(),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = coin.priceChangePercent24h.formatAsPercent(),
                color = if (coin.priceChangePercent24h.isGain()) {
                    MaterialTheme.extendedColors.positive
                } else {
                    MaterialTheme.extendedColors.negative
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
    savedQuantity: BigDecimal?,
    holdingError: AppError?,
    canSave: Boolean,
    onQuantityChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    val errorMessage = holdingError?.asMessage()
    val focusManager = LocalFocusManager.current

    Column(verticalArrangement = Arrangement.spacedBy(Spacing8)) {
        Text(
            text = stringResource(R.string.coin_detail_screen_holding_title_text),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = errorMessage ?: when (savedQuantity) {
                null -> stringResource(R.string.coin_detail_screen_not_held_text)
                else -> "${savedQuantity.formatAsQuantity()} ${symbol.uppercase()}"
            },
            color = when (errorMessage) {
                null -> Color.Unspecified
                else -> MaterialTheme.colorScheme.error
            },
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = quantityInput,
            onValueChange = onQuantityChange,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(text = stringResource(R.string.coin_detail_screen_quantity_label_text))
            },
            singleLine = true,
            // Without an explicit action the Done key only hides the keyboard, and the field
            // keeps the cursor.
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        )
        Button(onClick = onSaveClick, enabled = canSave) {
            Text(text = stringResource(R.string.coin_detail_screen_save_text))
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
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onBackClick = {},
            onQuantityChange = {},
            onSaveClick = {},
            onRetry = {},
        )
    }
}
