package me.prokhorov.coinfolio.domain.model

import java.math.BigDecimal

data class Holding(
    val coinId: String,
    val quantity: BigDecimal,
)
