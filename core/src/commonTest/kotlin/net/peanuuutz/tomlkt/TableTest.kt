package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable
import kotlin.test.Test

class TableTest {
    @Serializable
    data class M1(
        val b: Boolean,
        val c: C1
    )

    @Serializable
    data class C1(
        val s: String
    )

    val m11 = M1(
        b = true,
        c = C1(
            s = "bla"
        )
    )

    val s11 = """
        b = true
        
        [c]
        s = "bla"
    """.trimIndent()

    @Test
    fun encodeClass() {
        testEncode(M1.serializer(), m11, s11)
    }

    @Test
    fun decodeClass() {
        testDecode(M1.serializer(), s11, m11)
    }

    @Serializable
    data class M2(
        val c: C1,
        val b: Boolean
    )

    val m21 = M2(
        c = C1(
            s = "huh"
        ),
        b = false
    )

    val s21 = """
        c.s = "huh"
        b = false
    """.trimIndent()

    @Test
    fun encodeClassUnstructured() {
        testEncode(M2.serializer(), m21, s21)
    }

    @Test
    fun decodeClassUnstructured() {
        testDecode(M2.serializer(), s21, m21)
    }

    val s22 = """
        c = { s = "huh" }
        b = false
    """.trimIndent()

    @Test
    fun decodeClassInline() {
        testDecode(M2.serializer(), s22, m21)
    }

    @Serializable
    data class M3(
        val s: String,
        val sm: Map<String, String>
    )

    val m31 = M3(
        s = "",
        sm = mapOf(
            "a" to ""
        )
    )

    val s31 = """
        s = ""
        
        [sm]
        a = ""
    """.trimIndent()

    @Test
    fun encodeMapLikeWithPrimitiveValue() {
        testEncode(M3.serializer(), m31, s31)
    }

    @Test
    fun decodeMapLikeWithPrimitiveValue() {
        testDecode(M3.serializer(), s31, m31)
    }

    @Serializable
    data class M4(
        val sm: Map<String, String>,
        val s: String
    )

    val m41 = M4(
        sm = mapOf(
            "a" to ""
        ),
        s = ""
    )

    val s41 = """
        sm.a = ""
        s = ""
    """.trimIndent()

    @Test
    fun encodeMapLikeWithPrimitiveValueUnstructured() {
        testEncode(M4.serializer(), m41, s41)
    }

    @Test
    fun decodeMapLikeWithPrimitiveValueUnstructured() {
        testDecode(M4.serializer(), s41, m41)
    }

    val s42 = """
        sm = { a = "" }
        s = ""
    """.trimIndent()

    @Test
    fun decodeMapLikeWithPrimitiveValueInline() {
        testDecode(M4.serializer(), s42, m41)
    }

    @Serializable
    data class M5(
        val s: String,
        val cm: Map<String, C1>
    )

    val m51 = M5(
        s = "",
        cm = mapOf(
            "1" to C1(
                s = "1"
            )
        )
    )

    val s51 = """
        s = ""
        
        [cm]
        
        [cm.1]
        s = "1"
    """.trimIndent()

    @Test
    fun encodeMapLikeWithClassValue() {
        testEncode(M5.serializer(), m51, s51)
    }

    @Test
    fun decodeMapLikeWithClassValue() {
        testDecode(M5.serializer(), s51, m51)
    }

    val s52 = """
        s = ""
        
        [cm.1]
        s = "1"
    """.trimIndent()

    @Test
    fun decodeWithOmittedSuperTable() {
        testDecode(M5.serializer(), s52, m51)
    }

    @Serializable
    data class M6(
        val cm: Map<String, C1>,
        val s: String
    )

    val m61 = M6(
        cm = mapOf(
            "1" to C1(
                s = "1"
            )
        ),
        s = ""
    )

    val s61 = """
        cm.1.s = "1"
        s = ""
    """.trimIndent()

    @Test
    fun encodeMapLikeWithClassValueUnstructured() {
        testEncode(M6.serializer(), m61, s61)
    }

    @Test
    fun decodeMapLikeWithClassValueUnstructured() {
        testDecode(M6.serializer(), s61, m61)
    }

    val s62 = """
        cm.1 = { s = "1" }
        s = ""
    """.trimIndent()

    @Test
    fun decodeMapLikeWithClassValueInline1() {
        testDecode(M6.serializer(), s62, m61)
    }

    val s63 = """
        cm = { 1 = { s = "1" } }
        s = ""
    """.trimIndent()

    @Test
    fun decodeMapLikeWithClassValueInline2() {
        testDecode(M6.serializer(), s63, m61)
    }

    @Serializable
    data class M7(
        val s: String,
        val lm: Map<String, List<String>>
    )

    val m71 = M7(
        s = "",
        lm = mapOf(
            "1" to listOf(
                "a"
            )
        )
    )

    val s71 = """
        s = ""
        
        [lm]
        1 = [
            "a"
        ]
    """.trimIndent()

    @Test
    fun encodeMapLikeWithCollectionLikeValue() {
        testEncode(M7.serializer(), m71, s71)
    }

    @Test
    fun decodeMapLikeWithCollectionLikeValue() {
        testDecode(M7.serializer(), s71, m71)
    }

    @Serializable
    data class M8(
        val lm: Map<String, List<String>>,
        val s: String
    )

    val m81 = M8(
        lm = mapOf(
            "0" to listOf(
                "1"
            )
        ),
        s = ""
    )

    val s81 = """
        lm.0 = [
            "1"
        ]
        s = ""
    """.trimIndent()

    @Test
    fun encodeMapLikeWithCollectionLikeValueUnstructured() {
        testEncode(M8.serializer(), m81, s81)
    }

    @Test
    fun decodeMapLikeWithCollectionLikeValueUnstructured() {
        testDecode(M8.serializer(), s81, m81)
    }

    val s82 = """
        lm = { 0 = [ "1" ] }
        s = ""
    """.trimIndent()

    @Test
    fun decodeMapLikeWithCollectionLikeValueInline() {
        testDecode(M8.serializer(), s82, m81)
    }
}
