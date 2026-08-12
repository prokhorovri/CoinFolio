package me.prokhorov.coinfolio.screen.coindetail

import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.domain.result.AppError
import me.prokhorov.coinfolio.util.formatAsQuantity
import java.math.BigDecimal

data class CoinDetailUiState(
    val coinId: String,
    /** Tracks the running load. Nothing reads it yet: the screen checks if [coin] is null. */
    val isLoading: Boolean = false,
    val coin: CoinDetail? = null,
    val error: AppError? = null,
    /**
     * What the user typed, or `null` until they touch the field. While it is null,
     * [quantityInput] shows the stored holding, so the field is filled when the screen opens.
     * The first keystroke sets it and the field stops following the database, even if that
     * keystroke happens before the holding arrives.
     */
    val editedQuantity: String? = null,
    val savedQuantity: BigDecimal? = null,
    /**
     * Why the stored holding could not be read. It is shown inside the holding section only, not
     * on the whole screen like [error]. While it is set, nothing is observing the database.
     */
    val holdingError: AppError? = null,
    /** True while a save is running. It blocks a second save through [canSave]. */
    val isSaving: Boolean = false,
) {
    val quantityInput: String = editedQuantity ?: savedQuantity?.formatAsQuantity().orEmpty()

    /** Blocks saving if input is empty or not a number. `0` is allowed: it clears the holding. */
    val parsedQuantity: BigDecimal? = quantityInput.trim()
        .takeIf(String::isNotEmpty)
        ?.toBigDecimalOrNull()
        ?.takeIf { it.signum() >= 0 }

    val canSave: Boolean = coin != null && parsedQuantity != null && !isSaving
}
