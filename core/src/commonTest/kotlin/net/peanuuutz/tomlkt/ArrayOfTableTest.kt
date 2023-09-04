package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable
import kotlin.test.Test

class ArrayOfTableTest {
    @Serializable
    data class M1(
        val b: Boolean,
        val cs: List<C1>
    )

    @Serializable
    data class C1(
        val s: String
    )

    val m11 = M1(
        b = true,
        cs = listOf(
            C1(
                s = "😋"
            )
        )
    )

    val s11 = """
        b = true
        
        [[cs]]
        s = "😋"
    """.trimIndent()

    @Test
    fun encodeClassElement() {
        testEncode(M1.serializer(), m11, s11)
    }

    @Test
    fun decodeClassElement() {
        testDecode(M1.serializer(), s11, m11)
    }

    @Serializable
    data class M2(
        val cs: List<C1>,
        val b: Boolean
    )

    val m21 = M2(
        cs = listOf(
            C1(
                s = "😋"
            )
        ),
        b = true
    )

    val s21 = """
        cs = [
            { s = "😋" }
        ]
        b = true
    """.trimIndent()

    @Test
    fun encodeClassElementUnstructured() {
        testEncode(M2.serializer(), m21, s21)
    }

    @Test
    fun decodeClassElementUnstructured() {
        testDecode(M2.serializer(), s21, m21)
    }

    val s22 = """
        cs = [ { s = "😋" } ]
        b = true
    """.trimIndent()

    @Test
    fun decodeClassElementInline() {
        testDecode(M2.serializer(), s22, m21)
    }

    @Serializable
    data class M3(
        val i: Int,
        val ms: List<Map<String, String>>
    )

    val m31 = M3(
        i = 0,
        ms = listOf(
            mapOf(
                "0" to "0"
            )
        )
    )

    val s31 = """
        i = 0
        
        [[ms]]
        0 = "0"
    """.trimIndent()

    @Test
    fun encodeMapLikeElement() {
        testEncode(M3.serializer(), m31, s31)
    }

    @Test
    fun decodeMapLikeElement() {
        testDecode(M3.serializer(), s31, m31)
    }

    @Serializable
    data class M4(
        val ms: List<Map<String, String>>,
        val i: Int
    )

    val m41 = M4(
        ms = listOf(
            mapOf(
                "0" to "0"
            )
        ),
        i = 0
    )

    val s41 = """
        ms = [
            { 0 = "0" }
        ]
        i = 0
    """.trimIndent()

    @Test
    fun encodeMapLikeElementUnstructured() {
        testEncode(M4.serializer(), m41, s41)
    }

    @Test
    fun decodeMapLikeElementUnstructured() {
        testDecode(M4.serializer(), s41, m41)
    }

    val s42 = """
        ms = [ { 0 = "0" } ]
        i = 0
    """.trimIndent()

    @Test
    fun decodeMapLikeElementInline() {
        testDecode(M4.serializer(), s42, m41)
    }
}
