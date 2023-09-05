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
    fun encodeCollectionLikeWithClassElement() {
        testEncode(M1.serializer(), m11, s11)
    }

    @Test
    fun decodeCollectionLikeWithClassElement() {
        testDecode(M1.serializer(), s11, m11)
    }

    val m12 = M1(
        b = false,
        cs = emptyList()
    )

    val s12 = """
        b = false
        cs = [  ]
    """.trimIndent()

    @Test
    fun encodeEmptyCollectionLikeWithClassElement() {
        testEncode(M1.serializer(), m12, s12)
    }

    @Test
    fun decodeEmptyCollectionLikeWithClassElement() {
        testDecode(M1.serializer(), s12, m12)
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
    fun encodeCollectionLikeWithClassElementUnstructured() {
        testEncode(M2.serializer(), m21, s21)
    }

    @Test
    fun decodeCollectionLikeWithClassElementUnstructured() {
        testDecode(M2.serializer(), s21, m21)
    }

    val s22 = """
        cs = [ { s = "😋" } ]
        b = true
    """.trimIndent()

    @Test
    fun decodeCollectionLikeWithClassElementInline() {
        testDecode(M2.serializer(), s22, m21)
    }

    val m22 = M2(
        cs = emptyList(),
        b = false
    )

    val s23 = """
        cs = [  ]
        b = false
    """.trimIndent()

    @Test
    fun encodeEmptyCollectionLikeWithClassElementUnstructured() {
        testEncode(M2.serializer(), m22, s23)
    }

    @Test
    fun decodeEmptyCollectionLikeWithClassElementUnstructured() {
        testDecode(M2.serializer(), s23, m22)
    }

    @Serializable
    data class M3(
        val s: String,
        val cs: List<C2>
    )

    @Serializable
    class C2 {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is C2) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    val m31 = M3(
        s = "title",
        cs = listOf(
            C2()
        )
    )

    val s31 = """
        s = "title"
        
        [[cs]]
        
    """.trimIndent()

    @Test
    fun encodeCollectionLikeWithEmptyClassElement() {
        testEncode(M3.serializer(), m31, s31)
    }

    @Test
    fun decodeCollectionLikeWithEmptyClassElement() {
        testDecode(M3.serializer(), s31, m31)
    }

    val m32 = M3(
        s = "empty",
        cs = emptyList()
    )

    val s32 = """
        s = "empty"
        cs = [  ]
    """.trimIndent()

    @Test
    fun encodeEmptyCollectionLikeWithEmptyClassElement() {
        testEncode(M3.serializer(), m32, s32)
    }

    @Test
    fun decodeEmptyCollectionLikeWithEmptyClassElement() {
        testDecode(M3.serializer(), s32, m32)
    }

    @Serializable
    data class M4(
        val cs: List<C2>,
        val b: Boolean
    )

    val m41 = M4(
        cs = listOf(
            C2()
        ),
        b = true
    )

    val s41 = """
        cs = [
            {  }
        ]
        b = true
    """.trimIndent()

    @Test
    fun encodeCollectionLikeWithEmptyClassElementUnstructured() {
        testEncode(M4.serializer(), m41, s41)
    }

    @Test
    fun decodeCollectionLikeWithEmptyClassElementUnstructured() {
        testDecode(M4.serializer(), s41, m41)
    }

    val s42 = """
        cs = [ {  } ]
        b = true
    """.trimIndent()

    @Test
    fun decodeCollectionLikeWithEmptyClassElementInline() {
        testDecode(M4.serializer(), s42, m41)
    }

    val m42 = M4(
        cs = emptyList(),
        b = false
    )

    val s43 = """
        cs = [  ]
        b = false
    """.trimIndent()

    @Test
    fun encodeEmptyCollectionLikeWithEmptyClassElementUnstructured() {
        testEncode(M4.serializer(), m42, s43)
    }

    @Test
    fun decodeEmptyCollectionLikeWithEmptyClassElementUnstructured() {
        testDecode(M4.serializer(), s43, m42)
    }

    @Serializable
    data class M5(
        val i: Int,
        val ms: List<Map<String, String>>
    )

    val m51 = M5(
        i = 0,
        ms = listOf(
            mapOf(
                "0" to "0"
            )
        )
    )

    val s51 = """
        i = 0
        
        [[ms]]
        0 = "0"
    """.trimIndent()

    @Test
    fun encodeCollectionLikeWithMapLikeElement() {
        testEncode(M5.serializer(), m51, s51)
    }

    @Test
    fun decodeCollectionLikeWithMapLikeElement() {
        testDecode(M5.serializer(), s51, m51)
    }

    val m52 = M5(
        i = 0,
        ms = listOf(
            emptyMap()
        )
    )

    val s52 = """
        i = 0
        
        [[ms]]
        
    """.trimIndent()

    @Test
    fun encodeCollectionLikeWithEmptyMapLikeElement() {
        testEncode(M5.serializer(), m52, s52)
    }

    @Test
    fun decodeCollectionLikeWithEmptyMapLikeElement() {
        testDecode(M5.serializer(), s52, m52)
    }

    val m53 = M5(
        i = 0,
        ms = emptyList()
    )

    val s53 = """
        i = 0
        ms = [  ]
    """.trimIndent()

    @Test
    fun encodeEmptyCollectionLikeWithMapLikeElement() {
        testEncode(M5.serializer(), m53, s53)
    }

    @Test
    fun decodeEmptyCollectionLikeWithMapLikeElement() {
        testDecode(M5.serializer(), s53, m53)
    }

    @Serializable
    data class M6(
        val ms: List<Map<String, String>>,
        val i: Int
    )

    val m61 = M6(
        ms = listOf(
            mapOf(
                "0" to "0"
            )
        ),
        i = 0
    )

    val s61 = """
        ms = [
            { 0 = "0" }
        ]
        i = 0
    """.trimIndent()

    @Test
    fun encodeCollectionLikeWithMapLikeElementUnstructured() {
        testEncode(M6.serializer(), m61, s61)
    }

    @Test
    fun decodeCollectionLikeWithMapLikeElementUnstructured() {
        testDecode(M6.serializer(), s61, m61)
    }

    val s62 = """
        ms = [ { 0 = "0" } ]
        i = 0
    """.trimIndent()

    @Test
    fun decodeCollectionLikeWithMapLikeElementInline() {
        testDecode(M6.serializer(), s62, m61)
    }

    val m62 = M6(
        ms = listOf(
            emptyMap()
        ),
        i = 0
    )

    val s63 = """
        ms = [
            {  }
        ]
        i = 0
    """.trimIndent()

    @Test
    fun encodeCollectionLikeWithEmptyMapLikeElementUnstructured() {
        testEncode(M6.serializer(), m62, s63)
    }

    @Test
    fun decodeCollectionLikeWithEmptyMapLikeElementUnstructured() {
        testDecode(M6.serializer(), s63, m62)
    }

    val s64 = """
        ms = [ {  } ]
        i = 0
    """.trimIndent()

    @Test
    fun decodeCollectionLikeWithEmptyMapLikeElementInline() {
        testDecode(M6.serializer(), s64, m62)
    }

    val m63 = M6(
        ms = emptyList(),
        i = 1
    )

    val s65 = """
        ms = [  ]
        i = 1
    """.trimIndent()

    @Test
    fun encodeEmptyCollectionLikeWithMapLikeElementUnstructured() {
        testEncode(M6.serializer(), m63, s65)
    }

    @Test
    fun decodeEmptyCollectionLikeWithMapLikeElementUnstructured() {
        testDecode(M6.serializer(), s65, m63)
    }
}
