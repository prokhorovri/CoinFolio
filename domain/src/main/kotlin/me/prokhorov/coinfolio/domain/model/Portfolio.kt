package me.prokhorov.coinfolio.domain.model

import java.math.BigDecimal

data class Portfolio(
    val holdings: List<Holding>,
    val totalValue: BigDecimal,
)
