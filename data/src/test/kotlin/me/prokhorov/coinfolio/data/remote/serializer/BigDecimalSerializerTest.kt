package me.prokhorov.coinfolio.data.remote.serializer

import com.google.common.truth.Truth.assertThat
import java.math.BigDecimal
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Test

/** A serializer always runs on a field, so the tests need a class to hold one. */
@Serializable
data class PriceHolder(
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal,
)

/** The same, but the price may be missing, which is how the DTOs declare it. */
@Serializable
data class NullablePriceHolder(
    @Serializable(with = BigDecimalSerializer::class)
    val price: BigDecimal?,
)

class BigDecimalSerializerTest {

    private val json = Json

    private fun decode(price: String): BigDecimal {
        return json.decodeFromString<PriceHolder>("""{"price":$price}""").price
    }

    // This is the whole reason the serializer exists. A Double would round the last digits away.
    @Test
    fun `a long number keeps every digit`() {
        assertThat(decode("123456789.123456789123456789"))
            .isEqualTo(BigDecimal("123456789.123456789123456789"))
    }

    @Test
    fun `a small number keeps every digit`() {
        assertThat(decode("0.000000000000001234")).isEqualTo(BigDecimal("0.000000000000001234"))
    }

    // Real values from /coins/markets on 2026-09-01. CoinGecko writes a small price in exponent
    // form with a lowercase e, so reading that form is not an edge case, it is the normal path
    // for cheap coins.
    @Test
    fun `a price in exponent form is read`() {
        assertThat(decode("5.08e-06")).isEqualTo(BigDecimal("0.00000508"))
    }

    @Test
    fun `a negative number in exponent form is read`() {
        assertThat(decode("-9.3997717648e-08")).isEqualTo(BigDecimal("-0.000000093997717648"))
    }

    @Test
    fun `a very small number in exponent form keeps every digit`() {
        assertThat(decode("5.6366e-11")).isEqualTo(BigDecimal("0.000000000056366"))
    }

    // isEqualTo, not isEqualByComparingTo: the trailing zero must survive, because it says how
    // exact the price is.
    @Test
    fun `reading keeps the scale`() {
        assertThat(decode("1.50")).isEqualTo(BigDecimal("1.50"))
    }

    // The serializer reads the text of the JSON value, so quotes around a number do not break it.
    // CoinGecko sends numbers without quotes, but a quoted one still works.
    @Test
    fun `a quoted number is read as a number`() {
        assertThat(decode(""""1.5"""")).isEqualTo(BigDecimal("1.5"))
    }

    @Test(expected = SerializationException::class)
    fun `text that is not a number is rejected`() {
        decode(""""n/a"""")
    }

    @Test(expected = SerializationException::class)
    fun `an object in place of a number is rejected`() {
        decode("""{"usd":1.5}""")
    }

    @Test
    fun `a number is written as a number, not as text`() {
        val encoded = json.encodeToString(PriceHolder(BigDecimal("1.5")))

        assertThat(encoded).isEqualTo("""{"price":1.5}""")
    }

    @Test
    fun `writing keeps every digit`() {
        val price = BigDecimal("123456789.123456789123456789")

        val encoded = json.encodeToString(PriceHolder(price))

        assertThat(encoded).isEqualTo("""{"price":123456789.123456789123456789}""")
    }

    @Test
    fun `writing keeps a trailing zero`() {
        val encoded = json.encodeToString(PriceHolder(BigDecimal("1.50")))

        assertThat(encoded).isEqualTo("""{"price":1.50}""")
    }

    // toString writes an exponent below about 1e-8. That is valid JSON, and CoinGecko itself
    // sends prices in this form, so it is safe to write.
    @Test
    fun `a very small price is written with an exponent`() {
        val encoded = json.encodeToString(PriceHolder(BigDecimal("0.00000012")))

        assertThat(encoded).isEqualTo("""{"price":1.2E-7}""")
    }

    @Test
    fun `a small price survives a round trip`() {
        val price = BigDecimal("0.00000012")

        val decoded = json.decodeFromString<PriceHolder>(json.encodeToString(PriceHolder(price)))

        assertThat(decoded.price).isEqualTo(price)
    }

    // toPlainString would write 1000 here.
    @Test
    fun `writing keeps an exponent that the value already had`() {
        val encoded = json.encodeToString(PriceHolder(BigDecimal("1E+3")))

        assertThat(encoded).isEqualTo("""{"price":1E+3}""")
    }

    // JsonUnquotedLiteral cannot handle "null", but serialize() is only called for a real value.
    // If the price is null, kotlinx handles it before calling this method.
    @Test
    fun `a null price is written as JSON null`() {
        val encoded = json.encodeToString(NullablePriceHolder(null))

        assertThat(encoded).isEqualTo("""{"price":null}""")
    }

    @Test
    fun `a null price survives a round trip`() {
        val encoded = json.encodeToString(NullablePriceHolder(null))

        val decoded = json.decodeFromString<NullablePriceHolder>(encoded)

        assertThat(decoded.price).isNull()
    }

    @Test
    fun `a round trip returns an equal value`() {
        val price = BigDecimal("123456789.123456789123456789")

        val decoded = json.decodeFromString<PriceHolder>(json.encodeToString(PriceHolder(price)))

        assertThat(decoded.price).isEqualTo(price)
    }
}
