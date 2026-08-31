package me.prokhorov.coinfolio.data.mapper

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import me.prokhorov.coinfolio.data.local.entity.HoldingEntity
import me.prokhorov.coinfolio.domain.model.Holding
import org.junit.Test

class HoldingMapperTest {

    @Test
    fun `an entity becomes a holding`() {
        val entity = HoldingEntity(coinId = "bitcoin", quantity = BigDecimal("0.5"))

        val holding = entity.toDomain()

        assertThat(holding).isEqualTo(Holding(coinId = "bitcoin", quantity = BigDecimal("0.5")))
    }

    @Test
    fun `a holding becomes an entity`() {
        val holding = Holding(coinId = "bitcoin", quantity = BigDecimal("0.5"))

        val entity = holding.toEntity()

        assertThat(entity).isEqualTo(
            HoldingEntity(coinId = "bitcoin", quantity = BigDecimal("0.5")),
        )
    }

    // The user can type 0.50, and the database must give back the same text later.
    @Test
    fun `a round trip keeps the scale`() {
        val holding = Holding(coinId = "bitcoin", quantity = BigDecimal("0.50"))

        val result = holding.toEntity().toDomain()

        assertThat(result.quantity).isEqualTo(BigDecimal("0.50"))
    }
}
