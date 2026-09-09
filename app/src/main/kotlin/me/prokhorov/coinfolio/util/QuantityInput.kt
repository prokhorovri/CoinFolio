package me.prokhorov.coinfolio.util

import java.math.BigDecimal

/**
 * The rules of the holding quantity field, in one place. The screen uses them to filter what
 * the user types. The state uses them to check what it holds. The field shows the separator of
 * the user's locale. Everything below the UI uses a dot: [BigDecimal], the domain models and
 * Room.
 */
object QuantityInput {
    const val MAX_INTEGER_DIGITS = 15
    const val MAX_FRACTION_DIGITS = 18

    /**
     * Removes the grouping separator of the locale and every space. Keeps digits with at most
     * one decimal separator. Only the separator of the user's locale counts as a decimal
     * separator. Any other separator character is refused.
     */
    fun normalize(text: CharSequence): QuantityInputResult {
        val symbols = localeSymbols()
        val decimalSeparator = symbols.decimalSeparator
        val groupingSeparator = symbols.groupingSeparator

        var integerDigits = 0
        var fractionDigits = 0
        var hasDecimalSeparator = false

        val cleaned = buildString(text.length) {
            for (character in text) {
                when {
                    character.isDigit() -> {
                        if (hasDecimalSeparator) fractionDigits++ else integerDigits++
                        append(character)
                    }

                    character == decimalSeparator -> {
                        if (hasDecimalSeparator) {
                            return QuantityInputResult.InvalidCharacter
                        }
                        hasDecimalSeparator = true
                        append(character)
                    }

                    character == groupingSeparator -> Unit

                    // Kotlin's isWhitespace also covers the no break spaces. Russian and
                    // French use them between groups of digits.
                    character.isWhitespace() -> Unit

                    else -> {
                        return QuantityInputResult.InvalidCharacter
                    }
                }
            }
        }

        return when {
            integerDigits > MAX_INTEGER_DIGITS -> {
                QuantityInputResult.TooLong(cleaned, LengthProblem.INTEGER_DIGITS)
            }

            fractionDigits > MAX_FRACTION_DIGITS -> {
                QuantityInputResult.TooLong(cleaned, LengthProblem.FRACTION_DIGITS)
            }

            else -> QuantityInputResult.Valid(cleaned)
        }
    }

    /** Turns the text of the field into the form that [BigDecimal] and Room understand. */
    fun toCanonical(text: String): String {
        return text.replace(localeSymbols().decimalSeparator, '.')
    }

    /** Turns a stored number into the text of the field. */
    fun format(value: BigDecimal): String {
        return value.formatAsQuantity().localizeDecimalSeparator()
    }
}

/** What [QuantityInput.normalize] found in the text. */
sealed interface QuantityInputResult {
    /** The text is a quantity. [text] has grouping separators and spaces removed. */
    data class Valid(val text: String) : QuantityInputResult

    /**
     * The text is a number with too many digits. [text] is still cleaned. The field can show
     * it, and the state can explain the problem.
     */
    data class TooLong(val text: String, val problem: LengthProblem) : QuantityInputResult

    /**
     * The text holds a character that can never be part of a quantity: a letter, a second decimal
     * separator, or the decimal separator of another locale.
     */
    data object InvalidCharacter : QuantityInputResult
}

enum class LengthProblem { INTEGER_DIGITS, FRACTION_DIGITS }
