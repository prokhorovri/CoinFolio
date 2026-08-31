package me.prokhorov.coinfolio.data.local.converter

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import org.junit.Test

class BigDecimalConverterTest {

    private val converter = BigDecimalConverter()

    @Test
    fun `a string becomes a big decimal`() {
        assertThat(converter.fromString("0.5")).isEqualTo(BigDecimal("0.5"))
    }

    // Use toPlainString() so small numbers are written as normal decimals, not 1E-8,
    // which makes them easier for SQLite to compare and read back.
    @Test
    fun `a small number is stored without an exponent`() {
        assertThat(converter.toString(BigDecimal("0.00000001"))).isEqualTo("0.00000001")
    }

    @Test
    fun `an exponent written by hand is stored in plain form`() {
        assertThat(converter.toString(BigDecimal("1E-8"))).isEqualTo("0.00000001")
    }

    @Test
    fun `a round trip keeps the scale`() {
        val stored = converter.toString(BigDecimal("0.50"))

        assertThat(converter.fromString(stored)).isEqualTo(BigDecimal("0.50"))
    }

    @Test
    fun `null stays null in both directions`() {
        assertThat(converter.fromString(null)).isNull()
        assertThat(converter.toString(null)).isNull()
    }
}
