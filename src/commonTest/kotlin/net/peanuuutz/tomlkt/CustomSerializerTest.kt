package net.peanuuutz.tomlkt

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.test.Test

class CustomSerializerTest {
    @Serializable
    data class M1(
        @Serializable(StringOrIntSerializer::class)
        val any: Any
    )

    object StringOrIntSerializer : KSerializer<Any> {
        override val descriptor: SerialDescriptor = buildSerialDescriptor(
            serialName = "StringOrInt",
            kind = SerialKind.CONTEXTUAL
        )

        override fun serialize(encoder: Encoder, value: Any) {
            when (value) {
                is String -> String.serializer().serialize(encoder, value)
                is Int -> Int.serializer().serialize(encoder, value)
            }
        }

        override fun deserialize(decoder: Decoder): Any {
            val literal = decoder.asTomlDecoder().decodeTomlElement().toTomlLiteral()
            return literal.toIntOrNull() ?: literal.content
        }
    }

    val m11 = M1(
        any = 1
    )

    val s11 = """
        any = 1
    """.trimIndent()

    @Test
    fun encodeAnyInClass() {
        testEncode(M1.serializer(), m11, s11)
    }

    val m12: Map<String, Any> = mapOf(
        "1" to "YES"
    )

    val s12 = """
        1 = "YES"
    """.trimIndent()

    @Test
    fun encodeAnyInMap() {
        val serializer = MapSerializer(
            keySerializer = String.serializer(),
            valueSerializer = StringOrIntSerializer
        )

        testEncode(serializer, m12, s12)
    }
}
