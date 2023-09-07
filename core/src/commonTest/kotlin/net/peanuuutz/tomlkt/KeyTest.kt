package net.peanuuutz.tomlkt

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.test.Test

class KeyTest {
    @Serializable
    data class M1(
        val k: String
    )

    val m11 = M1(
        k = "Hi"
    )

    val s11 = """
        k = "Hi"
    """.trimIndent()

    @Test
    fun encodeBareKeyRegularly() {
        testEncode(M1.serializer(), m11, s11)
    }

    @Test
    fun decodeBareKeyRegularly() {
        testDecode(M1.serializer(), s11, m11)
    }

    @Serializable
    data class M2(
        @SerialName("1")
        val o: String
    )

    val m21 = M2(
        o = ""
    )

    val s21 = """
        1 = ""
    """.trimIndent()

    @Test
    fun encodeBareKeyWithNumber() {
        testEncode(M2.serializer(), m21, s21)
    }

    @Test
    fun decodeBareKeyWithNumber() {
        testDecode(M2.serializer(), s21, m21)
    }

    @Serializable
    data class M3(
        @SerialName("t-h_e")
        val k: String
    )

    val m31 = M3(
        k = "key"
    )

    val s31 = """
        t-h_e = "key"
    """.trimIndent()

    @Test
    fun encodeBareKeyWithHyphen() {
        testEncode(M3.serializer(), m31, s31)
    }

    @Test
    fun decodeBareKeyWithHyphen() {
        testDecode(M3.serializer(), s31, m31)
    }

    @Serializable
    data class M4(
        @SerialName("t_h_e")
        val k: String
    )

    val m41 = M4(
        k = "key"
    )

    val s41 = """
        t_h_e = "key"
    """.trimIndent()

    @Test
    fun encodeBareKeyWithUnderscore() {
        testEncode(M4.serializer(), m41, s41)
    }

    @Test
    fun decodeBareKeyWithUnderscore() {
        testDecode(M4.serializer(), s41, m41)
    }

    @Serializable
    data class M5(
        @SerialName("t.h e")
        val k: String
    )

    val m51 = M5(
        k = "key"
    )

    val s51 = """
        "t.h e" = "key"
    """.trimIndent()

    @Test
    fun encodeWrappedKey() {
        testEncode(M5.serializer(), m51, s51)
    }

    @Test
    fun decodeWrappedKey() {
        testDecode(M5.serializer(), s51, m51)
    }

    @Serializable
    data class M6(
        @SerialName("\n")
        val k: String
    )

    val m61 = M6(
        k = ""
    )

    val s61 = """
        "\n" = ""
    """.trimIndent()

    @Test
    fun encodeWrappedKeyWithControlCharacter() {
        testEncode(M6.serializer(), m61, s61)
    }

    @Test
    fun decodeWrappedKeyWithControlCharacter() {
        testDecode(M6.serializer(), s61, m61)
    }

    @Serializable
    data class M7(
        @SerialName("ʎǝʞ")
        val k: String
    )

    val m71 = M7(
        k = ""
    )

    val s71 = """
        "ʎǝʞ" = ""
    """.trimIndent()

    @Test
    fun encodeWrappedKeyWithUnicode() {
        testEncode(M7.serializer(), m71, s71)
    }

    @Test
    fun decodeWrappedKeyWithUnicode() {
        testDecode(M7.serializer(), s71, m71)
    }

    @Serializable
    data class M8(
        @SerialName("\\n")
        val k: String
    )

    val m81 = M8(
        k = "surprise"
    )

    val s81 = """
        '\n' = "surprise"
    """.trimIndent()

    @Test
    fun decodeWrappedKeyWithLiteral() {
        testDecode(M8.serializer(), s81, m81)
    }

    @Serializable
    data class M9(
        @SerialName("")
        val k: String
    )

    val m91 = M9(
        k = ""
    )

    val s91 = """
        "" = ""
    """.trimIndent()

    @Test
    fun encodeEmptyKey() {
        testEncode(M9.serializer(), m91, s91)
    }

    @Test
    fun decodeEmptyKey() {
        testDecode(M9.serializer(), s91, m91)
    }

    val s92 = """
        '' = ''
    """.trimIndent()

    @Test
    fun decodeEmptyKeyWithLiteral() {
        testDecode(M9.serializer(), s92, m91)
    }

    @Serializable
    data class M10(
        val c: C1,
        val i: Int
    )

    @Serializable
    data class C1(
        val k: String
    )

    val m101 = M10(
        c = C1(
            k = "inner"
        ),
        i = 0
    )

    val s101 = """
        c.k = "inner"
        i = 0
    """.trimIndent()

    @Test
    fun encodeDottedBareKey() {
        testEncode(M10.serializer(), m101, s101)
    }

    @Test
    fun decodeDottedBareKey() {
        testDecode(M10.serializer(), s101, m101)
    }

    @Serializable
    data class M11(
        val c: C2,
        val i: Int
    )

    @Serializable
    data class C2(
        @SerialName(" ")
        val k: String
    )

    val m111 = M11(
        c = C2(
            k = "?"
        ),
        i = -1
    )

    val s111 = """
        c." " = "?"
        i = -1
    """.trimIndent()

    @Test
    fun encodeDottedWrappedKey() {
        testEncode(M11.serializer(), m111, s111)
    }

    @Test
    fun decodeDottedWrappedKey() {
        testDecode(M11.serializer(), s111, m111)
    }

    @Serializable
    data class M12(
        val c: C3,
        val i: Int
    )

    @Serializable
    data class C3(
        @SerialName("\\n")
        val k: String
    )

    val m121 = M12(
        c = C3(
            k = "?"
        ),
        i = 1
    )

    val s121 = """
        c.'\n' = "?"
        i = 1
    """.trimIndent()

    @Test
    fun decodeDottedWrappedKeyWithLiteral() {
        testDecode(M12.serializer(), s121, m121)
    }

    val s122 = """
        c . '\n' = "?"
        i = 1
    """.trimIndent()

    @Test
    fun decodeDottedKeyWithSpace() {
        testDecode(M12.serializer(), s122, m121)
    }

    @Serializable
    data class M13(
        val c: C4,
        val b: Boolean
    )

    @Serializable
    data class C4(
        val i: Int,
        val f: Float
    )

    val m131 = M13(
        c = C4(
            i = 0,
            f = 0.1f
        ),
        b = true
    )

    val s131 = """
        c.i = 0
        b = true
        c.f = 0.1
    """.trimIndent()

    @Test
    fun decodeDottedKeyWithoutOrder() {
        testDecode(M13.serializer(), s131, m131)
    }
}
