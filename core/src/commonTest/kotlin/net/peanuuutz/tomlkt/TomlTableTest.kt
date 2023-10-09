package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable
import kotlin.test.Test

class TomlTableTest {
    @Serializable
    data class M1(
        val i: Int,
        val t: TomlTable
    )

    val m11 = M1(
        i = 0,
        t = TomlTable(
            'a' to 'b'
        )
    )

    val s11 = """
        i = 0
        
        [t]
        a = "b"
    """.trimIndent()

    @Test
    fun encodeWithPrimitiveValue() {
        testEncode(M1.serializer(), m11, s11)
    }

    @Test
    fun decodeWithPrimitiveValue() {
        testDecode(M1.serializer(), s11, m11)
    }

    val m12 = M1(
        i = 0,
        t = TomlTable(
            1 to listOf(
                'a',
                -1
            )
        )
    )

    val s12 = """
        i = 0
        
        [t]
        1 = [
            "a",
            -1
        ]
    """.trimIndent()

    @Test
    fun encodeWithCollectionLikeValue() {
        testEncode(M1.serializer(), m12, s12)
    }

    @Test
    fun decodeWithCollectionLikeValue() {
        testDecode(M1.serializer(), s12, m12)
    }

    val m13 = M1(
        i = 0,
        t = TomlTable(
            true to emptyArray<Int>()
        )
    )

    val s13 = """
        i = 0
        
        [t]
        true = [  ]
    """.trimIndent()

    @Test
    fun encodeWithEmptyCollectionLikeValue() {
        testEncode(M1.serializer(), m13, s13)
    }

    @Test
    fun decodeWithEmptyCollectionLikeValue() {
        testDecode(M1.serializer(), s13, m13)
    }

    val m14 = M1(
        i = 0,
        t = TomlTable(
            true to mapOf(
                true to false
            )
        )
    )

    val s14 = """
        i = 0
        
        [t]
        
        [t.true]
        true = false
    """.trimIndent()

    @Test
    fun encodeWithMapLikeValue() {
        testEncode(M1.serializer(), m14, s14)
    }

    @Test
    fun decodeWithMapLikeValue() {
        testDecode(M1.serializer(), s14, m14)
    }

    val m15 = M1(
        i = 0,
        t = TomlTable(
            true to emptyMap<Any?, Any?>()
        )
    )

    val s15 = """
        i = 0
        
        [t]
        
        [t.true]
        
    """.trimIndent()

    @Test
    fun encodeWithEmptyMapLikeValue() {
        testEncode(M1.serializer(), m15, s15)
    }

    @Test
    fun decodeWithEmptyMapLikeValue() {
        testDecode(M1.serializer(), s15, m15)
    }

    val m16 = M1(
        i = 0,
        t = TomlTable(
            1 to mapOf(1 to 1),
            2 to 2
        )
    )

    val s16 = """
        i = 0
        
        [t]
        2 = 2
        
        [t.1]
        1 = 1
    """.trimIndent()

    @Test
    fun encodeMapLikeValueSorted() {
        testEncode(M1.serializer(), m16, s16)
    }

    val m17 = M1(
        i = 0,
        t = TomlTable(
            1 to emptyMap<Any?, Any?>(),
            2 to 2
        )
    )

    val s17 = """
        i = 0
        
        [t]
        2 = 2
        
        [t.1]
        
    """.trimIndent()

    @Test
    fun encodeEmptyMapLikeValueSorted() {
        testEncode(M1.serializer(), m17, s17)
    }

    val m18 = M1(
        i = 0,
        t = TomlTable.Empty
    )

    val s18 = """
        i = 0
        
        [t]
        
    """.trimIndent()

    @Test
    fun encodeEmpty() {
        testEncode(M1.serializer(), m18, s18)
    }

    @Test
    fun decodeEmpty() {
        testDecode(M1.serializer(), s18, m18)
    }

    @Serializable
    data class M2(
        val t: TomlTable,
        val i: Int
    )

    val m21 = M2(
        t = TomlTable(
            1 to 1
        ),
        i = 0
    )

    val s21 = """
        t.1 = 1
        i = 0
    """.trimIndent()

    @Test
    fun encodeWithPrimitiveValueUnstructured() {
        testEncode(M2.serializer(), m21, s21)
    }

    @Test
    fun decodeWithPrimitiveValueUnstructured() {
        testDecode(M2.serializer(), s21, m21)
    }

    val m22 = M2(
        t = TomlTable(
            1 to listOf(
                ""
            )
        ),
        i = 0
    )

    val s22 = """
        t.1 = [
            ""
        ]
        i = 0
    """.trimIndent()

    @Test
    fun encodeWithCollectionLikeValueUnstructured() {
        testEncode(M2.serializer(), m22, s22)
    }

    @Test
    fun decodeWithCollectionLikeValueUnstructured() {
        testDecode(M2.serializer(), s22, m22)
    }

    val m23 = M2(
        t = TomlTable(
            '\n' to emptyList<Any?>()
        ),
        i = 0
    )

    val s23 = """
        t."\n" = [  ]
        i = 0
    """.trimIndent()

    @Test
    fun encodeWithEmptyCollectionLikeValueUnstructured() {
        testEncode(M2.serializer(), m23, s23)
    }

    @Test
    fun decodeWithEmptyCollectionLikeValueUnstructured() {
        testDecode(M2.serializer(), s23, m23)
    }

    val m24 = M2(
        t = TomlTable(
            "" to mapOf(
                "" to ""
            )
        ),
        i = 0
    )

    val s24 = """
        t.""."" = ""
        i = 0
    """.trimIndent()

    @Test
    fun encodeWithMapLikeValueUnstructured() {
        testEncode(M2.serializer(), m24, s24)
    }

    @Test
    fun decodeWithMapLikeValueUnstructured() {
        testDecode(M2.serializer(), s24, m24)
    }

    val m25 = M2(
        t = TomlTable(
            "" to emptyMap<Any?, Any?>()
        ),
        i = 0
    )

    val s25 = """
        t."" = {  }
        i = 0
    """.trimIndent()

    @Test
    fun encodeWithEmptyMapLikeValueUnstructured() {
        testEncode(M2.serializer(), m25, s25)
    }

    @Test
    fun decodeWithEmptyMapLikeValueUnstructured() {
        testDecode(M2.serializer(), s25, m25)
    }

    val m26 = M2(
        t = TomlTable(
            1 to mapOf(1 to 1),
            2 to 2
        ),
        i = 0
    )

    val s26 = """
        t.2 = 2
        t.1.1 = 1
        i = 0
    """.trimIndent()

    @Test
    fun encodeMapLikeValueSortedUnstructured() {
        testEncode(M2.serializer(), m26, s26)
    }

    val m27 = M2(
        t = TomlTable(
            1 to emptyMap<Any?, Any?>(),
            2 to 2
        ),
        i = 0
    )

    val s27 = """
        t.2 = 2
        t.1 = {  }
        i = 0
    """.trimIndent()

    @Test
    fun encodeEmptyMapLikeValueSortedUnstructured() {
        testEncode(M2.serializer(), m27, s27)
    }

    val m28 = M2(
        t = TomlTable.Empty,
        i = 0
    )

    val s28 = """
        t = {  }
        i = 0
    """.trimIndent()

    @Test
    fun encodeEmptyUnstructured() {
        testEncode(M2.serializer(), m28, s28)
    }

    @Test
    fun decodeEmptyUnstructured() {
        testDecode(M2.serializer(), s28, m28)
    }
}
