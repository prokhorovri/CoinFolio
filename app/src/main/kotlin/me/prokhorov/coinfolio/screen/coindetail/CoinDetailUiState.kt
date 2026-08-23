package me.prokhorov.coinfolio.screen.coindetail

import java.math.BigDecimal
import me.prokhorov.coinfolio.domain.model.CoinDetail
import me.prokhorov.coinfolio.domain.result.AppError

data class CoinDetailUiState(
    val coinId: String,
    val coin: CoinDetail? = null,
    val error: AppError? = null,
    val quantityInput: String = "",
    val quantityError: QuantityError? = null,
    val savedQuantity: BigDecimal? = null,
    /**
     * Why the stored holding could not be read. It is shown inside the holding section only, not
     * on the whole screen like [error]. While it is set, nothing is observing the database.
     */
    val holdingError: AppError? = null,
    val isSaving: Boolean = false,
    val canSave: Boolean = false,
)

/** What is wrong with the text in the quantity field. */
enum class QuantityError { INVALID_CHARACTER, TOO_MANY_INTEGER_DIGITS, TOO_MANY_FRACTION_DIGITS }
