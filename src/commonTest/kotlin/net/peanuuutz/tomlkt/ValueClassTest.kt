package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable
import kotlin.test.Test

class ValueClassTest {
    @Serializable
    data class M1(
        val v: V1
    )

    @Serializable
    @JvmInline
    value class V1(val i: Int)

    val m11 = M1(
        v = V1(
            i = 0
        )
    )

    val s11 = """
        v = 0
    """.trimIndent()

    @Test
    fun encodePrimitiveBacked() {
        testEncode(M1.serializer(), m11, s11)
    }

    @Test
    fun decodePrimitiveBacked() {
        testDecode(M1.serializer(), s11, m11)
    }

    @Serializable
    data class M2(
        val v: V2
    )

    @Serializable
    @JvmInline
    value class V2(val c: C)

    @Serializable
    data class C(val i: Int)

    val m21 = M2(
        v = V2(
            c = C(
                i = 0
            )
        )
    )

    val s21 = """
        v = { i = 0 }
    """.trimIndent()

    @Test
    fun encodeClassBacked() {
        testEncode(M2.serializer(), m21, s21)
    }

    @Test
    fun decodeClassBacked() {
        testDecode(M2.serializer(), s21, m21)
    }

    @Serializable
    data class M3(
        val u: UByte
    )

    val m31 = M3(
        u = 1.toUByte()
    )

    val s31 = """
        u = 1
    """.trimIndent()

    @Test
    fun encodeUnsigned() {
        testEncode(M3.serializer(), m31, s31)
    }

    @Test
    fun decodeUnsigned() {
        testDecode(M3.serializer(), s31, m31)
    }
}
