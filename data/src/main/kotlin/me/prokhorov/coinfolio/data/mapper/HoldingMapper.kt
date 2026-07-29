package me.prokhorov.coinfolio.data.mapper

import me.prokhorov.coinfolio.data.local.entity.HoldingEntity
import me.prokhorov.coinfolio.domain.model.Holding

fun HoldingEntity.toDomain(): Holding {
    return Holding(
        coinId = coinId,
        quantity = quantity,
    )
}

fun Holding.toEntity(): HoldingEntity {
    return HoldingEntity(
        coinId = coinId,
        quantity = quantity,
    )
}
