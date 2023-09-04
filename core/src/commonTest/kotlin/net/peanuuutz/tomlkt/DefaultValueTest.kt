package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable
import kotlin.test.Test

class DefaultValueTest {
    @Serializable
    data class M1(
        val b: Boolean = true
    )

    val m21 = M1()

    val s21 = ""

    @Test
    fun decode() {
        testDecode(M1.serializer(), s21, m21)
    }
}
