package net.peanuuutz.tomlkt

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.NothingSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.test.Test

class TomlNullTest {
    @Serializable
    data class M1(
        val n: TomlNull
    )

    val m11 = M1(
        n = TomlNull
    )

    val s11 = """
        n = null
    """.trimIndent()

    @Test
    fun encodeRegularly() {
        testEncode(M1.serializer(), m11, s11)
    }

    @Test
    fun decodeRegularly() {
        testDecode(M1.serializer(), s11, m11)
    }

    @Serializable
    data class M2(
        val e: TomlElement
    )

    val m21 = M2(
        e = TomlNull
    )

    val s21 = """
        e = null
    """.trimIndent()

    @Test
    fun encodeAsElement() {
        testEncode(M2.serializer(), m21, s21)
    }

    @Test
    fun decodeAsElement() {
        testDecode(M2.serializer(), s21, m21)
    }

    val m31 = null

    val e31 = TomlNull

    // The official doesn't provide a serializer for null only value...
    object NullSerializer : KSerializer<Nothing?> {
        override val descriptor: SerialDescriptor
            get() = NothingSerializer().descriptor

        override fun serialize(encoder: Encoder, value: Nothing?) {
            encoder.encodeNull()
        }

        override fun deserialize(decoder: Decoder): Nothing? {
            return decoder.decodeNull()
        }
    }

    @Test
    fun encodeToTomlNull() {
        testEncodeTomlElement(NullSerializer, m31, e31)
    }

    @Test
    fun decodeFromTomlNull() {
        testDecodeTomlElement(NullSerializer, e31, m31)
    }
}
