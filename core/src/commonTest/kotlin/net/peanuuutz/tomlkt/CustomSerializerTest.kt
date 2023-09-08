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
                is String -> encoder.encodeString(value)
                is Int -> encoder.encodeInt(value)
            }
        }

        override fun deserialize(decoder: Decoder): Any {
            val literal = decoder.asTomlDecoder().decodeTomlElement().asTomlLiteral()
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
    fun encodeClassWithSerializableProperty() {
        testEncode(M1.serializer(), m11, s11)
    }

    @Test
    fun decodeClassWithSerializableProperty() {
        testDecode(M1.serializer(), s11, m11)
    }

    val m12: Map<String, Any> = mapOf(
        "a" to "YES",
        "b" to 1
    )

    val s12 = """
        a = "YES"
        b = 1
    """.trimIndent()

    @Test
    fun encodeMapLikeWithSerializableProperty() {
        val serializer = MapSerializer(
            keySerializer = String.serializer(),
            valueSerializer = StringOrIntSerializer
        )

        testEncode(serializer, m12, s12)
    }

    @Test
    fun decodeMapLikeWithSerializableProperty() {
        val serializer = MapSerializer(
            keySerializer = String.serializer(),
            valueSerializer = StringOrIntSerializer
        )

        testDecode(serializer, s12, m12)
    }
}
