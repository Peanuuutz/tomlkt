package net.peanuuutz.tomlkt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TomlLiteralTest {
    val m01: Boolean = true

    val l01 = TomlLiteral(true)

    val m02: Boolean = false

    val l02 = TomlLiteral(false)

    @Test
    fun convertBoolean() {
        assertEquals(l01.toBoolean(), m01)
        assertEquals(l02.toBoolean(), m02)
    }

    val m03: Byte = Byte.MIN_VALUE

    val l03 = TomlLiteral(Byte.MIN_VALUE)

    val m04: Byte = -0

    val l04 = TomlLiteral(-0)

    val m05: Byte = 0

    val l05 = TomlLiteral(0)

    val m06: Byte = +0

    val l06 = TomlLiteral(+0)

    val m07: Byte = Byte.MAX_VALUE

    val l07 = TomlLiteral(Byte.MAX_VALUE)

    @Test
    fun convertByte() {
        assertEquals(l03.toByte(), m03)
        assertEquals(l04.toByte(), m04)
        assertEquals(l05.toByte(), m05)
        assertEquals(l06.toByte(), m06)
        assertEquals(l07.toByte(), m07)
    }

    val m08: Short = Short.MIN_VALUE

    val l08 = TomlLiteral(Short.MIN_VALUE)

    val m09: Short = -0

    val l09 = TomlLiteral(-0)

    val m010: Short = 0

    val l010 = TomlLiteral(0)

    val m011: Short = +0

    val l011 = TomlLiteral(+0)

    val m012: Short = Short.MAX_VALUE

    val l012 = TomlLiteral(Short.MAX_VALUE)

    @Test
    fun convertShort() {
        assertEquals(l08.toShort(), m08)
        assertEquals(l09.toShort(), m09)
        assertEquals(l010.toShort(), m010)
        assertEquals(l011.toShort(), m011)
        assertEquals(l012.toShort(), m012)
    }

    val m013: Int = Int.MIN_VALUE

    val l013 = TomlLiteral(Int.MIN_VALUE)

    val m014: Int = -0

    val l014 = TomlLiteral(-0)

    val m015: Int = 0

    val l015 = TomlLiteral(0)

    val m016: Int = +0

    val l016 = TomlLiteral(+0)

    val m017: Int = Int.MAX_VALUE

    val l017 = TomlLiteral(Int.MAX_VALUE)

    @Test
    fun convertInt() {
        assertEquals(l013.toInt(), m013)
        assertEquals(l014.toInt(), m014)
        assertEquals(l015.toInt(), m015)
        assertEquals(l016.toInt(), m016)
        assertEquals(l017.toInt(), m017)
    }

    val m018: Long = Long.MIN_VALUE

    val l018 = TomlLiteral(Long.MIN_VALUE)

    val m019: Long = -0

    val l019 = TomlLiteral(-0)

    val m020: Long = 0

    val l020 = TomlLiteral(0)

    val m021: Long = +0

    val l021 = TomlLiteral(+0)

    val m022: Long = Long.MAX_VALUE

    val l022 = TomlLiteral(Long.MAX_VALUE)

    @Test
    fun convertLong() {
        assertEquals(l018.toLong(), m018)
        assertEquals(l019.toLong(), m019)
        assertEquals(l020.toLong(), m020)
        assertEquals(l021.toLong(), m021)
        assertEquals(l022.toLong(), m022)
    }

    val m023: Float = Float.NEGATIVE_INFINITY

    val l023 = TomlLiteral(Float.NEGATIVE_INFINITY)

    val m024: Float = -Float.MAX_VALUE

    val l024 = TomlLiteral(-Float.MAX_VALUE)

    val m025: Float = -Float.MIN_VALUE

    val l025 = TomlLiteral(-Float.MIN_VALUE)

    val m026: Float = -0.0f

    val l026 = TomlLiteral(-0.0f)

    val m027: Float = 0.0f

    val l027 = TomlLiteral(0.0f)

    val m028: Float = +0.0f

    val l028 = TomlLiteral(+0.0f)

    val m029: Float = Float.MIN_VALUE

    val l029 = TomlLiteral(Float.MIN_VALUE)

    val m030: Float = Float.MAX_VALUE

    val l030 = TomlLiteral(Float.MAX_VALUE)

    val m031: Float = Float.POSITIVE_INFINITY

    val l031 = TomlLiteral(Float.POSITIVE_INFINITY)

    val m032: Float = Float.NaN

    val l032 = TomlLiteral(Float.NaN)

    @Test
    fun convertFloat() {
        assertEquals(l023.toFloat(), m023, 0.0f)
        assertEquals(l024.toFloat(), m024, 0.0f)
        assertEquals(l025.toFloat(), m025, 0.0f)
        assertEquals(l026.toFloat(), m026, 0.0f)
        assertEquals(l027.toFloat(), m027, 0.0f)
        assertEquals(l028.toFloat(), m028, 0.0f)
        assertEquals(l029.toFloat(), m029, 0.0f)
        assertEquals(l030.toFloat(), m030, 0.0f)
        assertEquals(l031.toFloat(), m031, 0.0f)
        assertTrue { l032.toFloat().isNaN() == m032.isNaN() }
    }

    val m033: Double = Double.NEGATIVE_INFINITY

    val l033 = TomlLiteral(Double.NEGATIVE_INFINITY)

    val m034: Double = -Double.MAX_VALUE

    val l034 = TomlLiteral(-Double.MAX_VALUE)

    val m035: Double = -Double.MIN_VALUE

    val l035 = TomlLiteral(-Double.MIN_VALUE)

    val m036: Double = -0.0

    val l036 = TomlLiteral(-0.0)

    val m037: Double = 0.0

    val l037 = TomlLiteral(0.0)

    val m038: Double = +0.0

    val l038 = TomlLiteral(+0.0)

    val m039: Double = Double.MIN_VALUE

    val l039 = TomlLiteral(Double.MIN_VALUE)

    val m040: Double = Double.MAX_VALUE

    val l040 = TomlLiteral(Double.MAX_VALUE)

    val m041: Double = Double.POSITIVE_INFINITY

    val l041 = TomlLiteral(Double.POSITIVE_INFINITY)

    val m042: Double = Double.NaN

    val l042 = TomlLiteral(Double.NaN)

    @Test
    fun convertDouble() {
        assertEquals(l033.toDouble(), m033, 0.0)
        assertEquals(l034.toDouble(), m034, 0.0)
        assertEquals(l035.toDouble(), m035, 0.0)
        assertEquals(l036.toDouble(), m036, 0.0)
        assertEquals(l037.toDouble(), m037, 0.0)
        assertEquals(l038.toDouble(), m038, 0.0)
        assertEquals(l039.toDouble(), m039, 0.0)
        assertEquals(l040.toDouble(), m040, 0.0)
        assertEquals(l041.toDouble(), m041, 0.0)
        assertTrue { l042.toDouble().isNaN() == m042.isNaN() }
    }

    val m043: UByte = UByte.MIN_VALUE

    val l043 = TomlLiteral(UByte.MIN_VALUE)

    val m044: UByte = UByte.MAX_VALUE

    val l044 = TomlLiteral(UByte.MAX_VALUE)

    @Test
    fun convertUByte() {
        assertEquals(l043.content.toUByte(), m043)
        assertEquals(l044.content.toUByte(), m044)
    }

    val m045: UShort = UShort.MIN_VALUE

    val l045 = TomlLiteral(UShort.MIN_VALUE)

    val m046: UShort = UShort.MAX_VALUE

    val l046 = TomlLiteral(UShort.MAX_VALUE)

    @Test
    fun convertUShort() {
        assertEquals(l045.content.toUShort(), m045)
        assertEquals(l046.content.toUShort(), m046)
    }

    val m047: UInt = UInt.MIN_VALUE

    val l047 = TomlLiteral(UInt.MIN_VALUE)

    val m048: UInt = UInt.MAX_VALUE

    val l048 = TomlLiteral(UInt.MAX_VALUE)

    @Test
    fun convertUInt() {
        assertEquals(l047.content.toUInt(), m047)
        assertEquals(l048.content.toUInt(), m048)
    }

    val m049: ULong = ULong.MIN_VALUE

    val l049 = TomlLiteral(ULong.MIN_VALUE)

    val m050: ULong = ULong.MAX_VALUE

    val l050 = TomlLiteral(ULong.MAX_VALUE)

    @Test
    fun convertULong() {
        assertEquals(l049.content.toULong(), m049)
        assertEquals(l050.content.toULong(), m050)
    }

    val m051: Char = 'a'

    val l051 = TomlLiteral('a')

    val m052: Char = ' '

    val l052 = TomlLiteral(' ')

    val m053: Char = '\n'

    val l053 = TomlLiteral('\n')

    @Test
    fun covertChar() {
        assertEquals(l051.toChar(), m051)
        assertEquals(l052.toChar(), m052)
        assertEquals(l053.toChar(), m053)
    }

    val m054: String = "a \n"

    val l054 = TomlLiteral("a \n")

    @Test
    fun convertString() {
        assertEquals(l054.content, m054)
    }

    val m055: TomlLocalDateTime = TomlLocalDateTime("2023-09-08T18:48:00")

    val l055 = TomlLiteral(TomlLocalDateTime("2023-09-08T18:48:00"))

    val m056: TomlLocalDateTime = TomlLocalDateTime("2023-09-08T18:48:00.500")

    val l056 = TomlLiteral(TomlLocalDateTime("2023-09-08T18:48:00.500"))

    @Test
    fun convertLocalDateTime() {
        assertEquals(l055.toLocalDateTime(), m055)
        assertEquals(l056.toLocalDateTime(), m056)
    }

    val m057: TomlOffsetDateTime = TomlOffsetDateTime("2023-09-08T18:48:00-09:00")

    val l057 = TomlLiteral(TomlOffsetDateTime("2023-09-08T18:48:00-09:00"))

    val m058: TomlOffsetDateTime = TomlOffsetDateTime("2023-09-08T18:48:00-00:00")

    val l058 = TomlLiteral(TomlOffsetDateTime("2023-09-08T18:48:00-00:00"))

    val m059: TomlOffsetDateTime = TomlOffsetDateTime("2023-09-08T18:48:00Z")

    val l059 = TomlLiteral(TomlOffsetDateTime("2023-09-08T18:48:00Z"))

    val m060: TomlOffsetDateTime = TomlOffsetDateTime("2023-09-08T18:48:00+00:00")

    val l060 = TomlLiteral(TomlOffsetDateTime("2023-09-08T18:48:00+00:00"))

    val m061: TomlOffsetDateTime = TomlOffsetDateTime("2023-09-08T18:48:00+09:00")

    val l061 = TomlLiteral(TomlOffsetDateTime("2023-09-08T18:48:00+09:00"))

    val m062: TomlOffsetDateTime = TomlOffsetDateTime("2023-09-08T18:48:00.500+09:00")

    val l062 = TomlLiteral(TomlOffsetDateTime("2023-09-08T18:48:00.500+09:00"))

    @Test
    fun convertOffsetDateTime() {
        assertEquals(l057.toOffsetDateTime(), m057)
        assertEquals(l058.toOffsetDateTime(), m058)
        assertEquals(l059.toOffsetDateTime(), m059)
        assertEquals(l060.toOffsetDateTime(), m060)
        assertEquals(l061.toOffsetDateTime(), m061)
        assertEquals(l062.toOffsetDateTime(), m062)
    }

    val m063: TomlLocalDate = TomlLocalDate("2023-09-08")

    val l063 = TomlLiteral(TomlLocalDate("2023-09-08"))

    @Test
    fun convertLocalDate() {
        assertEquals(l063.toLocalDate(), m063)
    }

    val m064: TomlLocalTime = TomlLocalTime("18:53:00")

    val l064 = TomlLiteral(TomlLocalTime("18:53:00"))

    val m065: TomlLocalTime = TomlLocalTime("18:53:00.500")

    val l065 = TomlLiteral(TomlLocalTime("18:53:00.500"))

    val m066: TomlLocalTime = TomlLocalTime("18:53")

    val l066 = TomlLiteral(TomlLocalTime("18:53"))

    @Test
    fun convertLocalTime() {
        assertEquals(l064.toLocalTime(), m064)
        assertEquals(l065.toLocalTime(), m065)
        assertEquals(l066.toLocalTime(), m066)
    }

    @Serializable
    enum class E {
        @SerialName("bruh") A,
        B,
        C
    }

    val m067: E = E.B

    val l067 = TomlLiteral(E.B)

    @Test
    fun convertEnum() {
        assertEquals(l067.toEnum(), m067)
    }

    @Serializable
    data class M1(
        val l: TomlLiteral
    )

    val m11 = M1(
        l = TomlLiteral(E.A)
    )

    val s11 = """
        l = "bruh"
    """.trimIndent()

    @Test
    fun encodeRegularly() {
        testEncode(M1.serializer(), m11, s11)
    }

    @Test
    fun decodeRegularly() {
        testDecode(M1.serializer(), s11, m11)
    }

    @Serializable
    data class M2(
        val e: TomlElement
    )

    val m21 = M2(
        e = TomlLiteral(TomlLocalDate("2001-01-23"))
    )

    val s21 = """
        e = 2001-01-23
    """.trimIndent()

    @Test
    fun encodeAsElement() {
        testEncode(M2.serializer(), m21, s21)
    }

    @Test
    fun decodeAsElement() {
        testDecode(M2.serializer(), s21, m21)
    }

    val m31 = 0xFEE490

    val l31 = TomlLiteral(0xFEE490)

    @Test
    fun encodeToTomlLiteral() {
        testEncodeTomlElement(Int.serializer(), m31, l31)
    }

    @Test
    fun decodeFromTomlLiteral() {
        testDecodeTomlElement(Int.serializer(), l31, m31)
    }
}
