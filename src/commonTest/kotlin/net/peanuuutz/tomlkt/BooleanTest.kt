package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable
import kotlin.test.Test

class BooleanTest {
    @Serializable
    data class M(
        val b: Boolean
    )

    val m1 = M(
        b = true
    )

    val s1 = """
        b = true
    """.trimIndent()

    @Test
    fun encodeTrue() {
        testEncode(M.serializer(), m1, s1)
    }

    @Test
    fun decodeTrue() {
        testDecode(M.serializer(), s1, m1)
    }

    val m2 = M(
        b = false
    )

    val s2 = """
        b = false
    """.trimIndent()

    @Test
    fun encodeFalse() {
        testEncode(M.serializer(), m2, s2)
    }

    @Test
    fun decodeFalse() {
        testDecode(M.serializer(), s2, m2)
    }
}
