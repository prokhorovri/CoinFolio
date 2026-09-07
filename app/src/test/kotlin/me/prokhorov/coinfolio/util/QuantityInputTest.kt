package me.prokhorov.coinfolio.util

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import java.util.Locale
import org.junit.Rule
import org.junit.Test

/** The quantity field rules in a locale where the dot is the decimal separator. */
class QuantityInputTest {

    @get:Rule
    val localeRule = DefaultLocaleRule(Locale.US)

    @Test
    fun `digits alone are valid`() {
        val result = QuantityInput.normalize("123")

        assertThat(result).isEqualTo(QuantityInputResult.Valid("123"))
    }

    @Test
    fun `empty text is valid`() {
        val result = QuantityInput.normalize("")

        assertThat(result).isEqualTo(QuantityInputResult.Valid(""))
    }

    @Test
    fun `the dot is kept as the decimal separator`() {
        val result = QuantityInput.normalize("1.5")

        assertThat(result).isEqualTo(QuantityInputResult.Valid("1.5"))
    }

    @Test
    fun `text may start with the decimal separator`() {
        val result = QuantityInput.normalize(".5")

        assertThat(result).isEqualTo(QuantityInputResult.Valid(".5"))
    }

    @Test
    fun `the grouping separator is dropped`() {
        val result = QuantityInput.normalize("1,000,000")

        assertThat(result).isEqualTo(QuantityInputResult.Valid("1000000"))
    }

    @Test
    fun `a space is dropped`() {
        val result = QuantityInput.normalize("1 000")

        assertThat(result).isEqualTo(QuantityInputResult.Valid("1000"))
    }

    // Russian and French put a no break space between groups of digits. Kotlin's isWhitespace
    // covers it, Java's does not.
    @Test
    fun `a no break space is dropped`() {
        val result = QuantityInput.normalize("1\u00A0000")

        assertThat(result).isEqualTo(QuantityInputResult.Valid("1000"))
    }

    @Test
    fun `a second decimal separator is refused`() {
        val result = QuantityInput.normalize("1.5.5")

        assertThat(result).isEqualTo(QuantityInputResult.InvalidCharacter)
    }

    @Test
    fun `a letter is refused`() {
        val result = QuantityInput.normalize("1a")

        assertThat(result).isEqualTo(QuantityInputResult.InvalidCharacter)
    }

    @Test
    fun `a minus sign is refused`() {
        val result = QuantityInput.normalize("-1")

        assertThat(result).isEqualTo(QuantityInputResult.InvalidCharacter)
    }

    @Test
    fun `the decimal separator of another locale is refused`() {
        val result = QuantityInput.normalize("1;5")

        assertThat(result).isEqualTo(QuantityInputResult.InvalidCharacter)
    }

    @Test
    fun `the integer digit limit itself is valid`() {
        val text = "1".repeat(QuantityInput.MAX_INTEGER_DIGITS)

        val result = QuantityInput.normalize(text)

        assertThat(result).isEqualTo(QuantityInputResult.Valid(text))
    }

    @Test
    fun `one integer digit over the limit is too long`() {
        val text = "1".repeat(QuantityInput.MAX_INTEGER_DIGITS + 1)

        val result = QuantityInput.normalize(text)

        val expected = QuantityInputResult.TooLong(text, LengthProblem.INTEGER_DIGITS)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `the fraction digit limit itself is valid`() {
        val text = "0." + "1".repeat(QuantityInput.MAX_FRACTION_DIGITS)

        val result = QuantityInput.normalize(text)

        assertThat(result).isEqualTo(QuantityInputResult.Valid(text))
    }

    @Test
    fun `one fraction digit over the limit is too long`() {
        val text = "0." + "1".repeat(QuantityInput.MAX_FRACTION_DIGITS + 1)

        val result = QuantityInput.normalize(text)

        val expected = QuantityInputResult.TooLong(text, LengthProblem.FRACTION_DIGITS)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `text that is too long is still cleaned`() {
        val result = QuantityInput.normalize("1,111,111,111,111,111")

        val expected = QuantityInputResult.TooLong(
            "1111111111111111",
            LengthProblem.INTEGER_DIGITS,
        )
        assertThat(result).isEqualTo(expected)
    }

    @Test
    fun `grouping separators do not count towards the digit limit`() {
        val result = QuantityInput.normalize("111,111,111,111,111")

        assertThat(result).isEqualTo(QuantityInputResult.Valid("111111111111111"))
    }

    @Test
    fun `toCanonical keeps the dot`() {
        val result = QuantityInput.toCanonical("1.5")

        assertThat(result).isEqualTo("1.5")
    }

    @Test
    fun `format drops the trailing zeros`() {
        val result = QuantityInput.format(BigDecimal("1.500"))

        assertThat(result).isEqualTo("1.5")
    }

    @Test
    fun `format writes a small number in plain digits`() {
        val result = QuantityInput.format(BigDecimal("0.00000001"))

        assertThat(result).isEqualTo("0.00000001")
    }
}
