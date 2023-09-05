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
    fun encodeCollectionLikeWithPrimitiveElement() {
        testEncode(M1.serializer(), m11, s11)
    }

    @Test
    fun decodeCollectionLikeWithPrimitiveElement() {
        testDecode(M1.serializer(), s11, m11)
    }

    val s12 = """
        fs = [ 0.0 ]
    """.trimIndent()

    @Test
    fun decodeCollectionLikeWithPrimitiveElementInline() {
        testDecode(M1.serializer(), s12, m11)
    }

    val m12 = M1(
        fs = listOf()
    )

    val s13 = """
        fs = [  ]
    """.trimIndent()

    @Test
    fun encodeEmptyCollectionLikeWithPrimitiveElement() {
        testEncode(M1.serializer(), m12, s13)
    }

    @Test
    fun decodeEmptyCollectionLikeWithPrimitiveElement() {
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
    fun encodeCollectionLikeWithCollectionLikeElement() {
        testEncode(M2.serializer(), m21, s21)
    }

    @Test
    fun decodeCollectionLikeWithCollectionLikeElement() {
        testDecode(M2.serializer(), s21, m21)
    }

    val s22 = """
        ls = [ [ "0" ] ]
    """.trimIndent()

    @Test
    fun decodeCollectionLikeWithCollectionLikeElementInline() {
        testDecode(M2.serializer(), s22, m21)
    }

    val m22 = M2(
        ls = listOf(
            emptyList()
        )
    )

    val s23 = """
        ls = [
            [  ]
        ]
    """.trimIndent()

    @Test
    fun encodeCollectionLikeWithEmptyCollectionLikeElement() {
        testEncode(M2.serializer(), m22, s23)
    }

    @Test
    fun decodeCollectionLikeWithEmptyCollectionLikeElement() {
        testDecode(M2.serializer(), s23, m22)
    }

    val s24 = """
        ls = [ [  ] ]
    """.trimIndent()

    @Test
    fun decodeCollectionLikeWithEmptyCollectionLikeElementInline() {
        testDecode(M2.serializer(), s24, m22)
    }

    val m23 = M2(
        ls = emptyList()
    )

    val s25 = """
        ls = [  ]
    """.trimIndent()

    @Test
    fun encodeEmptyCollectionLikeWithCollectionLikeElement() {
        testEncode(M2.serializer(), m23, s25)
    }

    @Test
    fun decodeEmptyCollectionLikeWithCollectionLikeElement() {
        testDecode(M2.serializer(), s25, m23)
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
    fun encodeCollectionLikeWithPrimitiveElementInline() {
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
    fun encodeCollectionLikeWithCollectionLikeElementInline() {
        testEncode(M4.serializer(), m41, s41)
    }
}
