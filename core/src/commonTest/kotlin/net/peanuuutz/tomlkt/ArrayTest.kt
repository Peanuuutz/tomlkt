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
        val d: Double,
        val ls: List<List<String>>
    )

    val m21 = M2(
        d = 0.0,
        ls = listOf(
            listOf(
                "0"
            )
        )
    )

    val s21 = """
        d = 0.0
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
        d = 0.0
        ls = [ [ "0" ] ]
    """.trimIndent()

    @Test
    fun decodeCollectionLikeElementInline() {
        testDecode(M2.serializer(), s22, m21)
    }

    val m22 = M2(
        d = -3.14,
        ls = listOf()
    )

    val s23 = """
        d = -3.14
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
}
