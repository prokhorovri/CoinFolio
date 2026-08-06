package me.prokhorov.coinfolio.screen.coindetail

import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.util.formatAsQuantity
import java.math.BigDecimal

data class CoinDetailUiState(
    val coinId: String,
    /** Tracks the in-flight load. Nothing reads it yet: the screen keys off [coin] being null. */
    val isLoading: Boolean = false,
    val coin: CoinDetail? = null,
    val error: AppError? = null,
    /**
     * What the user typed, or `null` while they haven't touched the field yet. Null is what makes
     * [quantityInput] fall back to the stored holding, so the field prefills on open and stops
     * following the database from the first keystroke on, including a keystroke that lands before
     * the initial holding arrives.
     */
    val editedQuantity: String? = null,
    val savedQuantity: BigDecimal? = null,
    /**
     * Why the stored holding could not be read. Scoped to the holding section: the coin itself
     * loaded fine, so this must not take over the screen the way [error] does. Terminal once set,
     * because the query flow stops emitting after its first failure.
     */
    val holdingError: AppError? = null,
) {
    val quantityInput: String = editedQuantity ?: savedQuantity?.formatAsQuantity().orEmpty()

    /** Blocks saving on empty or malformed input. `0` is allowed: it clears the holding. */
    val parsedQuantity: BigDecimal? = quantityInput.trim()
        .takeIf(String::isNotEmpty)
        ?.toBigDecimalOrNull()
        ?.takeIf { it.signum() >= 0 }

    val canSave: Boolean = coin != null && parsedQuantity != null
}
