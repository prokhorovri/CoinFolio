package me.prokhorov.coinfolio.util

import com.google.common.truth.Truth.assertThat
import java.util.Locale
import org.junit.Rule
import org.junit.Test

class LocaleFormattingTest {

    @get:Rule
    val localeRule = DefaultLocaleRule(Locale.GERMANY)

    @Test
    fun `localeSymbols follows the default format locale`() {
        val symbols = localeSymbols()

        assertThat(symbols.decimalSeparator).isEqualTo(',')
        assertThat(symbols.groupingSeparator).isEqualTo('.')
    }

    @Test
    fun `localizeDecimalSeparator swaps the dot for the separator of the locale`() {
        val result = "1.5".localizeDecimalSeparator()

        assertThat(result).isEqualTo("1,5")
    }

    @Test
    fun `localizeDecimalSeparator leaves text without a dot alone`() {
        val result = "1500".localizeDecimalSeparator()

        assertThat(result).isEqualTo("1500")
    }
}
