package me.prokhorov.coinfolio.domain.model

import java.math.BigDecimal

/**
 * One row of the portfolio: a stored [Holding] with the market data of its coin. [coin] is null
 * when the market data has no such coin.
 */
data class ValuedHolding(
    val holding: Holding,
    val coin: Coin?,
) {
    /** Null when the price is unknown. Then the row shows a dash and the total skips it. */
    val value: BigDecimal? get() = coin?.currentPrice?.multiply(holding.quantity)
}
