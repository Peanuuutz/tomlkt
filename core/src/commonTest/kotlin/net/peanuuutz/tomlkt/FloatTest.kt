package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FloatTest {
    @Serializable
    data class M1(
        val f: Float
    )

    val m11 = M1(
        f = 12345.0f
    )

    val s11 = """
        f = 12345.0
    """.trimIndent()

    val s11a = """
        f = 12345
    """.trimIndent()

    @Test
    fun encodePositive() {
        val r = Toml.encodeToString(M1.serializer(), m11)
        try {
            assertEquals(s11, r)
        } catch (e: AssertionError) {
            // For JS.
            assertEquals(s11a, r)
        }
    }

    @Test
    fun decodePositive() {
        testDecode(M1.serializer(), s11, m11)
    }

    val s12 = """
        f = +12345.0
    """.trimIndent()

    @Test
    fun decodePositiveWithSign() {
        testDecode(M1.serializer(), s12, m11)
    }

    val m12 = M1(
        f = 0.0f
    )

    val s13 = """
        f = 0.0
    """.trimIndent()

    val s13a = """
        f = 0
    """.trimIndent()

    @Test
    fun encodeZero() {
        val r = Toml.encodeToString(M1.serializer(), m12)
        try {
            assertEquals(s13, r)
        } catch (e: AssertionError) {
            // For JS.
            assertEquals(s13a, r)
        }
    }

    @Test
    fun decodeZero() {
        testDecode(M1.serializer(), s13, m12)
    }

    val s14 = """
        f = +0.0
    """.trimIndent()

    @Test
    fun decodeZeroWithPositiveSign() {
        testDecode(M1.serializer(), s14, m12)
    }

    val m13 = M1(
        f = -0.0f
    )

    val s15 = """
        f = -0.0
    """.trimIndent()

    @Test
    fun decodeZeroWithNegativeSign() {
        val r = Toml.decodeFromString(M1.serializer(), s15)
        assertEquals(m13.f, r.f, 0.0f)
    }

    val m14 = M1(
        f = 10000.0f
    )

    val s16 = """
        f = 1e4
    """.trimIndent()
    
    @Test
    fun decodeExponent() {
        testDecode(M1.serializer(), s16, m14)
    }

    val s17 = """
        f = 1.0e4
    """.trimIndent()
    
    @Test
    fun decodeExponentWithFraction() {
        testDecode(M1.serializer(), s17, m14)
    }
    
    val s18 = """
        f = 1e+4
    """.trimIndent()
    
    @Test
    fun decodeExponentWithPositiveSign() {
        testDecode(M1.serializer(), s18, m14)
    }
    
    val s19 = """
        f = 100000e-1
    """.trimIndent()
    
    @Test
    fun decodeExponentWithNegativeSign() {
        testDecode(M1.serializer(), s19, m14)
    }

    val s110 = """
        f = 100000.0e-1
    """.trimIndent()

    @Test
    fun decodeExponentWithBothFractionAndSign() {
        testDecode(M1.serializer(), s110, m14)
    }

    val s111 = """
        f = 1E4
    """.trimIndent()

    @Test
    fun decodeExponentWithUppercase() {
        testDecode(M1.serializer(), s111, m14)
    }

    val s112 = """
        f = 10_000.000_000
    """.trimIndent()

    @Test
    fun decodeWithUnderscore() {
        testDecode(M1.serializer(), s112, m14)
    }

    val m15 = M1(
        f = Float.NaN
    )

    val s113 = """
        f = nan
    """.trimIndent()

    @Test
    fun encodeNaN() {
        testEncode(M1.serializer(), m15, s113)
    }

    @Test
    fun decodeNaN() {
        val r = Toml.decodeFromString(M1.serializer(), s113)
        assertTrue { r.f.isNaN() }
    }

    val s114 = """
        f = +nan
    """.trimIndent()

    @Test
    fun decodeNaNWithPositiveSign() {
        val r = Toml.decodeFromString(M1.serializer(), s114)
        assertTrue { r.f.isNaN() }
    }

    val s115 = """
        f = -nan
    """.trimIndent()

    @Test
    fun decodeNaNWithNegativeSign() {
        val r = Toml.decodeFromString(M1.serializer(), s115)
        assertTrue { r.f.isNaN() }
    }

    val m16 = M1(
        f = Float.POSITIVE_INFINITY
    )

    val s116 = """
        f = inf
    """.trimIndent()

    @Test
    fun encodePositiveInfinity() {
        testEncode(M1.serializer(), m16, s116)
    }

    @Test
    fun decodePositiveInfinity() {
        testDecode(M1.serializer(), s116, m16)
    }

    val s117 = """
        f = +inf
    """.trimIndent()

    @Test
    fun decodePositiveInfinityWithSign() {
        testDecode(M1.serializer(), s117, m16)
    }

    val m17 = M1(
        f = Float.NEGATIVE_INFINITY
    )

    val s118 = """
        f = -inf
    """.trimIndent()

    @Test
    fun encodeNegativeInfinity() {
        testEncode(M1.serializer(), m17, s118)
    }

    @Test
    fun decodeNegativeInfinity() {
        testDecode(M1.serializer(), s118, m17)
    }
}
