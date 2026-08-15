package me.prokhorov.coinfolio.data.remote.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonPrimitive
import java.math.BigDecimal

/**
 * Reads and writes a price as a JSON number and keeps every digit. A `Double` would lose digits,
 * and money must stay exact. A value that is not a number is rejected with
 * [SerializationException].
 */
object BigDecimalSerializer : KSerializer<BigDecimal> {
    /**
     * Tells the format what this type looks like. JSON does not use it here, because we read and
     * write the JSON element ourselves. There is no kind for a big number, so this one is only a
     * name.
     */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BigDecimal", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): BigDecimal {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("BigDecimal is only decodable from JSON")
        val element = jsonDecoder.decodeJsonElement()
        val primitive = element as? JsonPrimitive
            ?: throw SerializationException("Expected a JSON primitive, was $element")
        return primitive.content.toBigDecimalOrNull()
            ?: throw SerializationException("Expected a number, was \"${primitive.content}\"")
    }

    /** Writes a JSON number, not text, so a value comes back in the same shape when it is read. */
    override fun serialize(
        encoder: Encoder,
        value: BigDecimal,
    ) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: throw SerializationException("BigDecimal is only encodable to JSON")
        jsonEncoder.encodeJsonElement(JsonPrimitive(value))
    }
}
