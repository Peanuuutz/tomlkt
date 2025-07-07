@file:OptIn(ExperimentalTime::class)

package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.time.ExperimentalTime

class DateTimeTest {
    @Serializable
    data class M1(
        val ldt: TomlLocalDateTime
    )

    val m11 = M1(
        ldt = TomlLocalDateTime("2024-01-01T01:02:03")
    )

    val s11 = """
        ldt = 2024-01-01T01:02:03
    """.trimIndent()

    @Test
    fun encodeLocalDateTimeRegularly() {
        testEncode(M1.serializer(), m11, s11)
    }

    @Test
    fun decodeLocalDateTimeRegularly() {
        testDecode(M1.serializer(), s11, m11)
    }

    val s12 = """
        ldt = 2024-01-01 01:02:03
    """.trimIndent()

    @Test
    fun decodeLocalDateTimeWithSpace() {
        testDecode(M1.serializer(), s12, m11)
    }

    val m12 = M1(
        ldt = TomlLocalDateTime("2024-01-01T01:02:03.500")
    )

    // The field for nano is encoded either `.xxx` or `.xxxxxx`.
    val s13 = """
        ldt = 2024-01-01T01:02:03.500
    """.trimIndent()

    @Test
    fun encodeLocalDateTimeWithNano() {
        testEncode(M1.serializer(), m12, s13)
    }

    @Test
    fun decodeLocalDateTimeWithNano1() {
        testDecode(M1.serializer(), s13, m12)
    }

    val s14 = """
        ldt = 2024-01-01T01:02:03.500000
    """.trimIndent()

    @Test
    fun decodeLocalDateTimeWithNano2() {
        testDecode(M1.serializer(), s14, m12)
    }

    @Serializable
    data class M2(
        val odt: TomlOffsetDateTime
    )

    val m21 = M2(
        odt = TomlOffsetDateTime("2024-01-01T01:02:03Z")
    )

    val s21 = """
        odt = 2024-01-01T01:02:03Z
    """.trimIndent()

    @Test
    fun encodeOffsetDateTimeRegularly() {
        testEncode(M2.serializer(), m21, s21)
    }

    @Test
    fun decodeOffsetDateTimeRegularly() {
        testDecode(M2.serializer(), s21, m21)
    }

    val s22 = """
        odt = 2024-01-01T01:02:03+00:00
    """.trimIndent()

    @Test
    fun decodeOffsetDateTimeWithPositiveZeroOffset() {
        testDecode(M2.serializer(), s22, m21)
    }

    val s23 = """
        odt = 2024-01-01T01:02:03-00:00
    """.trimIndent()

    @Test
    fun decodeOffsetDateTimeWithNegativeZeroOffset() {
        testDecode(M2.serializer(), s23, m21)
    }

    val s24 = """
        odt = 2024-01-01 01:02:03Z
    """.trimIndent()

    @Test
    fun decodeOffsetDateTimeWithSpace() {
        testDecode(M2.serializer(), s24, m21)
    }

    val m22 = M2(
        odt = TomlOffsetDateTime("2024-01-01T01:02:03+02:00")
    )

    val s25 = """
        odt = 2024-01-01T01:02:03+02:00
    """.trimIndent()

    @Test
    fun decodeOffsetDateTimeWithPositiveOffset() {
        testDecode(M2.serializer(), s25, m22)
    }

    val m23 = M2(
        odt = TomlOffsetDateTime("2024-01-01T01:02:03-02:00")
    )

    val s26 = """
        odt = 2024-01-01T01:02:03-02:00
    """.trimIndent()

    @Test
    fun decodeOffsetDateTimeWithNegativeOffset() {
        testDecode(M2.serializer(), s26, m23)
    }

    @Serializable
    data class M3(
        val ld: TomlLocalDate
    )

    val m31 = M3(
        ld = TomlLocalDate("2024-01-01")
    )

    val s31 = """
        ld = 2024-01-01
    """.trimIndent()

    @Test
    fun encodeLocalDateRegularly() {
        testEncode(M3.serializer(), m31, s31)
    }

    @Test
    fun decodeLocalDateRegularly() {
        testDecode(M3.serializer(), s31, m31)
    }

    @Serializable
    data class M4(
        val lt: TomlLocalTime
    )

    val m41 = M4(
        lt = TomlLocalTime("01:02:03")
    )

    val s41 = """
        lt = 01:02:03
    """.trimIndent()

    @Test
    fun encodeLocalTime() {
        testEncode(M4.serializer(), m41, s41)
    }

    @Test
    fun decodeLocalTime() {
        testDecode(M4.serializer(), s41, m41)
    }

    val m42 = M4(
        lt = TomlLocalTime("01:02:03.456789")
    )

    val s42 = """
        lt = 01:02:03.456789
    """.trimIndent()

    @Test
    fun decodeLocalTimeWithNano() {
        testDecode(M4.serializer(), s42, m42)
    }
}
