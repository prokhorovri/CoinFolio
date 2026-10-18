package me.prokhorov.coinfolio.screen.coindetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import java.math.BigDecimal
import kotlinx.coroutines.flow.drop
import me.prokhorov.coinfolio.R
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.theme.Spacing8
import me.prokhorov.coinfolio.util.QuantityInput
import me.prokhorov.coinfolio.util.asMessage

@Composable
internal fun HoldingSection(
    symbol: String,
    quantityInput: String,
    quantityError: QuantityError?,
    savedQuantity: BigDecimal?,
    holdingError: AppError?,
    canSave: Boolean,
    quantityState: TextFieldState,
    onQuantityChange: (String) -> Unit,
    onSaveClick: () -> Unit,
) {
    val errorMessage = holdingError?.asMessage()
    val focusManager = LocalFocusManager.current

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
