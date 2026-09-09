package me.prokhorov.coinfolio.screen.coindetail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import me.prokhorov.coinfolio.util.QuantityInput
import me.prokhorov.coinfolio.util.QuantityInputResult

/**
 * Applies [QuantityInput] rules while the user types or pastes. A grouping separator or a
 * space is removed, so `1,000.25` becomes `1000.25`. A character that cannot be in a quantity
 * is refused. A too long number is kept, because [CoinDetailUiState.quantityError] can explain
 * it. Cleaning rewrites the whole text, so the cursor is put back where the user left it.
 */
object QuantityInputTransformation : InputTransformation {
    @OptIn(ExperimentalFoundationApi::class)
    override fun TextFieldBuffer.transformInput() {
        val typed = asCharSequence().toString()
        val cleaned = QuantityInput.normalize(typed).cleanedText

        if (cleaned == null) {
            // Moving the cursor calls this too, and changeCount is 0 for it. `changes` only
            // holds text edits. Text that is already wrong must stay editable, for example
            // after the user changed the language. Reverting a cursor move would block every
            // edit, and the user could not fix the text.
            if (changes.changeCount > 0) {
                revertAllChanges()
            }
            return
        }

        if (cleaned == typed) return

        // selection.end is a place in typed. Cleaning that part gives the place in the new text.
        val typedBeforeCursor = typed.take(selection.end)
        val cleanedBeforeCursor = QuantityInput.normalize(typedBeforeCursor).cleanedText
        val newCursor = cleanedBeforeCursor?.length ?: cleaned.length

        replace(0, length, cleaned)

        placeCursorBeforeCharAt(newCursor)
    }
}

/**
 * The text to put in the field, or null when the input must be refused. Only this file needs
 * `Valid` and `TooLong` together. So the shared type keeps no nullable member.
 */
private val QuantityInputResult.cleanedText: String?
    get() = when (this) {
        is QuantityInputResult.Valid -> text
        is QuantityInputResult.TooLong -> text
        QuantityInputResult.InvalidCharacter -> null
    }
