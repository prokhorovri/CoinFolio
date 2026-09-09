package me.prokhorov.coinfolio.domain.model

import java.math.BigDecimal

/** What the user owns, with the price of each coin at one moment. A use case builds it. */
data class Portfolio(
    val holdings: List<ValuedHolding>,
    /** Sum of the rows with a known price. A row with no price is skipped. */
    val totalValue: BigDecimal,
)
