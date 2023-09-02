package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable
import kotlin.test.Test

class IntegerTest {
    @Serializable
    data class M1(
        val i: Int
    )

    val m11 = M1(
        i = 10000
    )

    val s11 = """
        i = 10000
    """.trimIndent()

    @Test
    fun encodePositive() {
        testEncode(M1.serializer(), m11, s11)
    }

    @Test
    fun decodePositiveRegularly() {
        testDecode(M1.serializer(), s11, m11)
    }

    val s12 = """
        i = +10000
    """.trimIndent()

    @Test
    fun decodePositiveWithSign() {
        testDecode(M1.serializer(), s12, m11)
    }

    val s13 = """
        i = 10_000
    """.trimIndent()

    @Test
    fun decodeDecimalWithUnderscore() {
        testDecode(M1.serializer(), s13, m11)
    }

    val m12 = M1(
        i = 0
    )

    val s14 = """
        i = 0
    """.trimIndent()

    @Test
    fun encodeZero() {
        testEncode(M1.serializer(), m12, s14)
    }

    @Test
    fun decodeZeroRegularly() {
        testDecode(M1.serializer(), s14, m12)
    }

    val s15 = """
        i = +0
    """.trimIndent()

    @Test
    fun decodeZeroWithPositiveSign() {
        testDecode(M1.serializer(), s15, m12)
    }

    val s16 = """
        i = -0
    """.trimIndent()

    @Test
    fun decodeZeroWithNegativeSign() {
        testDecode(M1.serializer(), s16, m12)
    }

    val m13 = M1(
        i = -233
    )

    val s17 = """
        i = -233
    """.trimIndent()

    @Test
    fun encodeNegative() {
        testEncode(M1.serializer(), m13, s17)
    }

    @Test
    fun decodeNegative() {
        testDecode(M1.serializer(), s17, m13)
    }

    @Serializable
    data class M2(
        @TomlInteger(TomlInteger.Base.Bin)
        val i: Int
    )

    val m21 = M2(
        i = 0b10101010
    )

    val s21 = """
        i = 0b10101010
    """.trimIndent()

    @Test
    fun encodeBinary() {
        testEncode(M2.serializer(), m21, s21)
    }

    @Test
    fun decodeBinary() {
        testDecode(M2.serializer(), s21, m21)
    }

    val s22 = """
        i = 0b1010_1010
    """.trimIndent()

    @Test
    fun decodeBinaryWithUnderscore() {
        testDecode(M2.serializer(), s22, m21)
    }

    @Serializable
    data class M3(
        @TomlInteger(TomlInteger.Base.Oct)
        val i: Int
    )

    val m31 = M3(
        i = 2739128
    )

    val s31 = """
        i = 0o12345670
    """.trimIndent()

    @Test
    fun encodeOctal() {
        testEncode(M3.serializer(), m31, s31)
    }

    @Test
    fun decodeOctal() {
        testDecode(M3.serializer(), s31, m31)
    }

    val s32 = """
        i = 0o1234_5670
    """.trimIndent()

    @Test
    fun decodeOctalWithUnderscore() {
        testDecode(M3.serializer(), s32, m31)
    }

    @Serializable
    data class M4(
        @TomlInteger(TomlInteger.Base.Hex)
        val i: Int
    )

    val m41 = M4(
        i = 0xFFFFFF
    )

    val s41 = """
        i = 0xffffff
    """.trimIndent()

    @Test
    fun encodeHexadecimal() {
        testEncode(M4.serializer(), m41, s41)
    }

    @Test
    fun decodeHexadecimal() {
        testDecode(M4.serializer(), s41, m41)
    }

    val s42 = """
        i = 0xff_ff_ff
    """.trimIndent()

    @Test
    fun decodeHexadecimalWithUnderscore() {
        testDecode(M4.serializer(), s42, m41)
    }

    val s43 = """
        i = 0xFfFffF
    """.trimIndent()

    @Test
    fun decodeHexadecimalWithMixedCase() {
        testDecode(M4.serializer(), s43, m41)
    }

    val s44 = """
        i = 0xf_f_F_f_f_F
    """.trimIndent()

    @Test
    fun decodeWeirdHexadecimal() {
        testDecode(M4.serializer(), s44, m41)
    }

    @Serializable
    data class M5(
        val b: Byte
    )

    val m51 = M5(
        b = 127
    )

    val s51 = """
        b = 127
    """.trimIndent()

    @Test
    fun encodeByte() {
        testEncode(M5.serializer(), m51, s51)
    }

    @Test
    fun decodeByte() {
        testDecode(M5.serializer(), s51, m51)
    }

    @Serializable
    data class M6(
        @TomlInteger(TomlInteger.Base.Bin)
        val b: Byte
    )

    val m61 = M6(
        b = 0b1111111
    )

    val s61 = """
        b = 0b1111111
    """.trimIndent()

    @Test
    fun encodeByteAsBinary() {
        testEncode(M6.serializer(), m61, s61)
    }

    @Test
    fun decodeByteAsBinary() {
        testDecode(M6.serializer(), s61, m61)
    }

    @Serializable
    data class M7(
        val s: Short
    )

    val m71 = M7(
        s = -32768
    )

    val s71 = """
        s = -32768
    """.trimIndent()

    @Test
    fun encodeShort() {
        testEncode(M7.serializer(), m71, s71)
    }

    @Test
    fun decodeShort() {
        testDecode(M7.serializer(), s71, m71)
    }

    @Serializable
    data class M8(
        val l: Long
    )

    val m81 = M8(
        l = -1L
    )

    val s81 = """
        l = -1
    """.trimIndent()

    @Test
    fun encodeLong() {
        testEncode(M8.serializer(), m81, s81)
    }

    @Test
    fun decodeLong() {
        testDecode(M8.serializer(), s81, m81)
    }

    @Serializable
    data class M9(
        @TomlInteger(TomlInteger.Base.Hex)
        val l: Long
    )

    val m91 = M9(
        l = 0xFFFFFFFF
    )

    val s91 = """
        l = 0xffffffff
    """.trimIndent()


    @Test
    fun encodeLongAsHexadecimal() {
        testEncode(M9.serializer(), m91, s91)
    }

    @Test
    fun decodeLongAsHexadecimal() {
        testDecode(M9.serializer(), s91, m91)
    }
}
