package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable
import kotlin.test.Test

class ArrayTest {
    @Serializable
    data class M1(
        val fs: List<Float>
    )

    val m11 = M1(
        fs = listOf(
            0.0f
        )
    )

    val s11 = """
        fs = [
            0.0
        ]
    """.trimIndent()

    @Test
    fun encodePrimitiveElement() {
        testEncode(M1.serializer(), m11, s11)
    }

    @Test
    fun decodePrimitiveElement() {
        testDecode(M1.serializer(), s11, m11)
    }

    val s12 = """
        fs = [ 0.0 ]
    """.trimIndent()

    @Test
    fun decodePrimitiveElementInline() {
        testDecode(M1.serializer(), s12, m11)
    }

    val m12 = M1(
        fs = listOf()
    )

    val s13 = """
        fs = [  ]
    """.trimIndent()

    @Test
    fun encodeEmptyPrimitiveElement() {
        testEncode(M1.serializer(), m12, s13)
    }

    @Test
    fun decodeEmptyPrimitiveElement() {
        testDecode(M1.serializer(), s13, m12)
    }

    @Serializable
    data class M2(
        val ls: List<List<String>>
    )

    val m21 = M2(
        ls = listOf(
            listOf(
                "0"
            )
        )
    )

    val s21 = """
        ls = [
            [ "0" ]
        ]
    """.trimIndent()

    @Test
    fun encodeCollectionLikeElement() {
        testEncode(M2.serializer(), m21, s21)
    }

    @Test
    fun decodeCollectionLikeElement() {
        testDecode(M2.serializer(), s21, m21)
    }

    val s22 = """
        ls = [ [ "0" ] ]
    """.trimIndent()

    @Test
    fun decodeCollectionLikeElementInline() {
        testDecode(M2.serializer(), s22, m21)
    }

    val m22 = M2(
        ls = listOf()
    )

    val s23 = """
        ls = [  ]
    """.trimIndent()

    @Test
    fun encodeEmptyCollectionLikeElement() {
        testEncode(M2.serializer(), m22, s23)
    }

    @Test
    fun decodeEmptyCollectionLikeElement() {
        testDecode(M2.serializer(), s23, m22)
    }

    @Serializable
    class M3(
        @TomlInline
        val fs: FloatArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is M3) return false
            if (!fs.contentEquals(other.fs)) return false
            return true
        }

        override fun hashCode(): Int {
            return fs.contentHashCode()
        }

        override fun toString(): String {
            return "M3(fs=${fs.contentToString()})"
        }
    }

    val m31 = M3(
        fs = floatArrayOf(0.0f)
    )

    val s31 = """
        fs = [ 0.0 ]
    """.trimIndent()

    @Test
    fun encodePrimitiveElementInline() {
        testEncode(M3.serializer(), m31, s31)
    }

    @Serializable
    data class M4(
        @TomlInline
        val ls: List<List<String>>
    )

    val m41 = M4(
        ls = listOf(
            listOf(
                "Hi"
            )
        )
    )

    val s41 = """
        ls = [ [ "Hi" ] ]
    """.trimIndent()

    @Test
    fun encodeCollectionLikeElementInline() {
        testEncode(M4.serializer(), m41, s41)
    }
}
