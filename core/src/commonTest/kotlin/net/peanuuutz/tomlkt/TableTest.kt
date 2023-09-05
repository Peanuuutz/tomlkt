package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable
import kotlin.test.Test

/*
    [1]
    elements = [ "p", "c" ]
    tests = [
        { type: "n", format: "[c]" }
    ]

    [2]
    elements = [ "c", "p" ]
    tests = [
        { type: "n", format: "c.k = v" },
        { type: "i", format: "c = v" }
    ]

    [3]
    elements = [ "p", "ec" ]
    tests = [
        { type: "n", format: "[c]" },
        { type: "i", format: "c = v" }
    ]

    [4]
    elements = [ "p", "pm" ]
    tests = [
        { type: "n", format: "[pm]" },
        { type: "e", format: "[pm]" }
    ]

    [5]
    elements = [ "pm", "p" ]
    tests = [
        { type: "n", format: "pm.k = v" },
        { type: "i", format: "pm = v" },
        { type: "e", format: "pm = v" }
    ]

    [6]
    elements = [ "p", "cm" ]
    tests = [
        { type: "n", format: [ "[cm.k]", "k = v" ] ] },
        { type: "i", format: [ "[cm]", "k = v" ] ] },
        { type: "em", format: "[cm]" },
        { type: "s", description: "omit super table" }
    ]

    [7]
    elements = [ "cm", "p" ]
    tests = [
        { type: "n", format: "cm.k.k = v" },
        { type: "i", format: "cm.k = v" },
        { type: "i", format: "cm = v" },
        { type: "em", format: "cm = v" }
    ]

    [8]
    elements = [ "p", "ecm" ]
    tests = [
        { type: "n", format: "[cm.k]" },
        { type: "i", format: [ "[cm]", "k = v" ] },
        { type: "em", format: "[cm]" }
    ]

    [9]
    elements = [ "ecm", "p" ]
    tests = [
        { type: "n", format: "cm.k = v" },
        { type: "i", format: "cm = v" },
        { type: "em", format: "cm = v" }
    ]

    [10]
    elements = [ "p", "lm" ]
    tests = [
        { type: "n", format: [ "[lm]", "k = v" ] },
        { type: "i", format: [ "[lm]", "k = v" ] },
        { type: "el", format: [ "[lm]", "k = v" ] },
        { type: "em", format: "[lm]" }
    ]

    [11]
    elements = [ "lm", "p" ]
    tests = [
        { type: "n", format: "lm.k = v" },
        { type: "i", format: "lm.k = v" },
        { type: "i", format: "lm = v" },
        { type: "el", format: "lm.k = v" },
        { type: [ "el", "i" ], format: "lm = v" },
        { type: "em", format: "lm = v" }
    ]

    [12]
    elements = [ "p", "mm" ]
    tests = [
        { type: "n", format: [ "[mm.k]", "k = v" ] },
        { type: "i", format: [ "[mm]", "k = v" ] ] },
        { type: "eim", format: "[mm.k]" },
        { type: [ "eim", 'i' ], format: [ "[mm]", "k = v" ] },
        { type: "eom", format: "[mm]" }
    ]

    [13]
    elements = [ "mm", "p" ]
    tests = [
        { type: "n", format: "mm.k.k = v" },
        { type: "i", format: "mm.k = v" },
        { type: "i", format: "mm = v" },
        { type: "eim", format: "mm.k = v" },
        { type: [ "eim", "i" ], format: "mm = v" },
        { type: "eom", format: "mm = v" }
    ]
 */
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
        val b: Boolean,
        val c: C2
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
        b = true,
        c = C2()
    )

    val s31 = """
        b = true
        
        [c]
        
    """.trimIndent()

    @Test
    fun encodeEmptyClass() {
        testEncode(M3.serializer(), m31, s31)
    }

    @Test
    fun decodeEmptyClass() {
        testDecode(M3.serializer(), s31, m31)
    }

    val s32 = """
        b = true
        c = {  }
    """.trimIndent()

    @Test
    fun decodeEmptyClassInline() {
        testDecode(M3.serializer(), s32, m31)
    }

    @Serializable
    data class M4(
        val s: String,
        val sm: Map<String, String>
    )

    val m41 = M4(
        s = "",
        sm = mapOf(
            "a" to ""
        )
    )

    val s41 = """
        s = ""
        
        [sm]
        a = ""
    """.trimIndent()

    @Test
    fun encodeMapLikeWithPrimitiveValue() {
        testEncode(M4.serializer(), m41, s41)
    }

    @Test
    fun decodeMapLikeWithPrimitiveValue() {
        testDecode(M4.serializer(), s41, m41)
    }

    val m42 = M4(
        s = "1",
        sm = emptyMap()
    )

    val s42 = """
        s = "1"
        
        [sm]
        
    """.trimIndent()

    @Test
    fun encodeEmptyMapLikeWithPrimitiveValue() {
        testEncode(M4.serializer(), m42, s42)
    }

    @Test
    fun decodeEmptyMapLikeWithPrimitiveValue() {
        testDecode(M4.serializer(), s42, m42)
    }

    @Serializable
    data class M5(
        val sm: Map<String, String>,
        val s: String
    )

    val m51 = M5(
        sm = mapOf(
            "a" to ""
        ),
        s = ""
    )

    val s51 = """
        sm.a = ""
        s = ""
    """.trimIndent()

    @Test
    fun encodeMapLikeWithPrimitiveValueUnstructured() {
        testEncode(M5.serializer(), m51, s51)
    }

    @Test
    fun decodeMapLikeWithPrimitiveValueUnstructured() {
        testDecode(M5.serializer(), s51, m51)
    }

    val s52 = """
        sm = { a = "" }
        s = ""
    """.trimIndent()

    @Test
    fun decodeMapLikeWithPrimitiveValueInline() {
        testDecode(M5.serializer(), s52, m51)
    }

    val m52 = M5(
        sm = emptyMap(),
        s = "A"
    )

    val s53 = """
        sm = {  }
        s = "A"
    """.trimIndent()

    @Test
    fun encodeEmptyMapLikeWithPrimitiveValueUnstructured() {
        testEncode(M5.serializer(), m52, s53)
    }

    @Test
    fun decodeEmptyMapLikeWithPrimitiveValueUnstructured() {
        testDecode(M5.serializer(), s53, m52)
    }

    @Serializable
    data class M6(
        val s: String,
        val cm: Map<String, C1>
    )

    val m61 = M6(
        s = "",
        cm = mapOf(
            "1" to C1(
                s = "1"
            )
        )
    )

    val s61 = """
        s = ""
        
        [cm]
        
        [cm.1]
        s = "1"
    """.trimIndent()

    @Test
    fun encodeMapLikeWithClassValue() {
        testEncode(M6.serializer(), m61, s61)
    }

    @Test
    fun decodeMapLikeWithClassValue() {
        testDecode(M6.serializer(), s61, m61)
    }

    val s62 = """
        s = ""
        
        [cm]
        1 = { s = "1" }
    """.trimIndent()

    @Test
    fun decodeMapLikeWithClassValueInline1() {
        testDecode(M6.serializer(), s62, m61)
    }

    val m62 = M6(
        s = "",
        cm = emptyMap()
    )

    val s63 = """
        s = ""
        
        [cm]
        
    """.trimIndent()

    @Test
    fun encodeEmptyMapLikeWithClassValue() {
        testEncode(M6.serializer(), m62, s63)
    }

    @Test
    fun decodeEmptyMapLikeWithClassValue() {
        testDecode(M6.serializer(), s63, m62)
    }

    val s64 = """
        s = ""
        
        [cm.1]
        s = "1"
    """.trimIndent()

    @Test
    fun decodeWithOmittedSuperTable() {
        testDecode(M6.serializer(), s64, m61)
    }

    @Serializable
    data class M7(
        val cm: Map<String, C1>,
        val s: String
    )

    val m71 = M7(
        cm = mapOf(
            "1" to C1(
                s = "1"
            )
        ),
        s = ""
    )

    val s71 = """
        cm.1.s = "1"
        s = ""
    """.trimIndent()

    @Test
    fun encodeMapLikeWithClassValueUnstructured() {
        testEncode(M7.serializer(), m71, s71)
    }

    @Test
    fun decodeMapLikeWithClassValueUnstructured() {
        testDecode(M7.serializer(), s71, m71)
    }

    val s72 = """
        cm.1 = { s = "1" }
        s = ""
    """.trimIndent()

    @Test
    fun decodeMapLikeWithClassValueInline2() {
        testDecode(M7.serializer(), s72, m71)
    }

    val s73 = """
        cm = { 1 = { s = "1" } }
        s = ""
    """.trimIndent()

    @Test
    fun decodeMapLikeWithClassValueInline3() {
        testDecode(M7.serializer(), s73, m71)
    }

    val m72 = M7(
        cm = emptyMap(),
        s = ""
    )

    val s74 = """
        cm = {  }
        s = ""
    """.trimIndent()

    @Test
    fun encodeEmptyMapLikeWithClassValueUnstructured() {
        testEncode(M7.serializer(), m72, s74)
    }

    @Test
    fun decodeEmptyMapLikeWithClassValueUnstructured() {
        testDecode(M7.serializer(), s74, m72)
    }

    @Serializable
    data class M8(
        val s: String,
        val cm: Map<String, C2>
    )

    val m81 = M8(
        s = "",
        cm = mapOf(
            "1" to C2()
        )
    )

    val s81 = """
        s = ""
        
        [cm]
        
        [cm.1]
        
    """.trimIndent()

    @Test
    fun encodeMapLikeWithEmptyClassValue() {
        testEncode(M8.serializer(), m81, s81)
    }

    @Test
    fun decodeMapLikeWithEmptyClassValue() {
        testDecode(M8.serializer(), s81, m81)
    }

    val s82 = """
        s = ""
        
        [cm]
        1 = {  }
    """.trimIndent()

    @Test
    fun decodeMapLikeWithEmptyClassValueInline1() {
        testDecode(M8.serializer(), s82, m81)
    }

    val m82 = M8(
        s = "",
        cm = emptyMap()
    )

    val s83 = """
        s = ""
        
        [cm]
        
    """.trimIndent()

    @Test
    fun encodeEmptyMapLikeWithEmptyClassValue() {
        testEncode(M8.serializer(), m82, s83)
    }

    @Test
    fun decodeEmptyMapLikeWithEmptyClassValue() {
        testDecode(M8.serializer(), s83, m82)
    }

    @Serializable
    data class M9(
        val cm: Map<String, C2>,
        val s: String
    )

    val m91 = M9(
        cm = mapOf(
            "1" to C2()
        ),
        s = ""
    )

    val s91 = """
        cm.1 = {  }
        s = ""
    """.trimIndent()

    @Test
    fun encodeMapLikeWithEmptyClassValueUnstructured() {
        testEncode(M9.serializer(), m91, s91)
    }

    @Test
    fun decodeMapLikeWithEmptyClassValueUnstructured() {
        testDecode(M9.serializer(), s91, m91)
    }

    val s92 = """
        cm = { 1 = {  } }
        s = ""
    """.trimIndent()

    @Test
    fun decodeMapLikeWithEmptyClassValueInline2() {
        testDecode(M9.serializer(), s92, m91)
    }

    val m92 = M9(
        cm = emptyMap(),
        s = ""
    )

    val s93 = """
        cm = {  }
        s = ""
    """.trimIndent()

    @Test
    fun encodeEmptyMapLikeWithEmptyClassValueUnstructured() {
        testEncode(M9.serializer(), m92, s93)
    }

    @Test
    fun decodeEmptyMapLikeWithEmptyClassValueUnstructured() {
        testDecode(M9.serializer(), s93, m92)
    }

    @Serializable
    data class M10(
        val s: String,
        val lm: Map<String, List<String>>
    )

    val m101 = M10(
        s = "",
        lm = mapOf(
            "1" to listOf(
                "a"
            )
        )
    )

    val s101 = """
        s = ""
        
        [lm]
        1 = [
            "a"
        ]
    """.trimIndent()

    @Test
    fun encodeMapLikeWithCollectionLikeValue() {
        testEncode(M10.serializer(), m101, s101)
    }

    @Test
    fun decodeMapLikeWithCollectionLikeValue() {
        testDecode(M10.serializer(), s101, m101)
    }

    val s102 = """
        s = ""
        
        [lm]
        1 = [ "a" ]
    """.trimIndent()

    @Test
    fun decodeMapLikeWithCollectionLikeValueInline1() {
        testDecode(M10.serializer(), s102, m101)
    }

    val m102 = M10(
        s = "",
        lm = mapOf(
            "1" to emptyList()
        )
    )

    val s103 = """
        s = ""
        
        [lm]
        1 = [  ]
    """.trimIndent()

    @Test
    fun encodeMapLikeWithEmptyCollectionLikeValue() {
        testEncode(M10.serializer(), m102, s103)
    }

    @Test
    fun decodeMapLikeWithEmptyCollectionLikeValue() {
        testDecode(M10.serializer(), s103, m102)
    }

    val m103 = M10(
        s = "",
        lm = emptyMap()
    )

    val s104 = """
        s = ""
        
        [lm]
        
    """.trimIndent()

    @Test
    fun encodeEmptyMapLikeWithCollectionLikeValue() {
        testEncode(M10.serializer(), m103, s104)
    }

    @Test
    fun decodeEmptyMapLikeWithCollectionLikeValue() {
        testDecode(M10.serializer(), s104, m103)
    }

    @Serializable
    data class M11(
        val lm: Map<String, List<String>>,
        val s: String
    )

    val m111 = M11(
        lm = mapOf(
            "0" to listOf(
                "1"
            )
        ),
        s = ""
    )

    val s111 = """
        lm.0 = [
            "1"
        ]
        s = ""
    """.trimIndent()

    @Test
    fun encodeMapLikeWithCollectionLikeValueUnstructured() {
        testEncode(M11.serializer(), m111, s111)
    }

    @Test
    fun decodeMapLikeWithCollectionLikeValueUnstructured() {
        testDecode(M11.serializer(), s111, m111)
    }

    val s112 = """
        lm.0 = [ "1" ]
        s = ""
    """.trimIndent()

    @Test
    fun decodeMapLikeWithCollectionLikeValueInline2() {
        testDecode(M11.serializer(), s112, m111)
    }

    val s113 = """
        lm = { 0 = [ "1" ] }
        s = ""
    """.trimIndent()

    @Test
    fun decodeMapLikeWithCollectionLikeValueInline3() {
        testDecode(M11.serializer(), s113, m111)
    }

    val m112 = M11(
        lm = mapOf(
            "1" to emptyList()
        ),
        s = "1"
    )

    val s114 = """
        lm.1 = [  ]
        s = "1"
    """.trimIndent()

    @Test
    fun encodeMapLikeWithEmptyCollectionLikeValueUnstructured() {
        testEncode(M11.serializer(), m112, s114)
    }

    @Test
    fun decodeMapLikeWithEmptyCollectionLikeValueUnstructured() {
        testDecode(M11.serializer(), s114, m112)
    }

    val s115 = """
        lm = { 1 = [  ] }
        s = "1"
    """.trimIndent()

    @Test
    fun decodeMapLikeWithEmptyCollectionLikeValueInline3() {
        testDecode(M11.serializer(), s115, m112)
    }

    val m113 = M11(
        lm = emptyMap(),
        s = ""
    )

    val s116 = """
        lm = {  }
        s = ""
    """.trimIndent()

    @Test
    fun encodeEmptyMapLikeWithCollectionLikeValueUnstructured() {
        testEncode(M11.serializer(), m113, s116)
    }

    @Test
    fun decodeEmptyMapLikeWithCollectionLikeValueUnstructured() {
        testDecode(M11.serializer(), s116, m113)
    }

    @Serializable
    data class M12(
        val i: Int,
        val mm: Map<String, Map<String, String>>
    )

    val m121 = M12(
        i = 0,
        mm = mapOf(
            "1" to mapOf(
                "." to ""
            )
        )
    )

    val s121 = """
        i = 0
        
        [mm]
        
        [mm.1]
        "." = ""
    """.trimIndent()

    @Test
    fun encodeMapLikeWithMapLikeValue() {
        testEncode(M12.serializer(), m121, s121)
    }

    @Test
    fun decodeMapLikeWithMapLikeValue() {
        testDecode(M12.serializer(), s121, m121)
    }

    val s122 = """
        i = 0
        
        [mm]
        1 = { "." = "" }
    """.trimIndent()

    @Test
    fun decodeMapLikeWithMapLikeValueInline1() {
        testDecode(M12.serializer(), s122, m121)
    }

    val m122 = M12(
        i = 0,
        mm = mapOf(
            "1" to emptyMap()
        )
    )

    val s123 = """
        i = 0
        
        [mm]
        
        [mm.1]
        
    """.trimIndent()

    @Test
    fun encodeMapLikeWithEmptyMapLikeValue() {
        testEncode(M12.serializer(), m122, s123)
    }

    @Test
    fun decodeMapLikeWithEmptyMapLikeValue() {
        testDecode(M12.serializer(), s123, m122)
    }

    val s124 = """
        i = 0
        
        [mm]
        1 = {  }
    """.trimIndent()

    @Test
    fun decodeMapLikeWithEmptyMapLikeValueInline1() {
        testDecode(M12.serializer(), s124, m122)
    }

    val m123 = M12(
        i = 0,
        mm = emptyMap()
    )

    val s125 = """
        i = 0
        
        [mm]
        
    """.trimIndent()

    @Test
    fun encodeEmptyMapLikeWithMapLikeValue() {
        testEncode(M12.serializer(), m123, s125)
    }

    @Test
    fun decodeEmptyMapLikeWithMapLikeValue() {
        testDecode(M12.serializer(), s125, m123)
    }

    @Serializable
    data class M13(
        val mm: Map<String, Map<String, String>>,
        val i: Int
    )

    val m131 = M13(
        mm = mapOf(
            "1" to mapOf(
                "" to ""
            )
        ),
        i = 0
    )

    val s131 = """
        mm.1."" = ""
        i = 0
    """.trimIndent()

    @Test
    fun encodeMapLikeWithMapLikeValueUnstructured() {
        testEncode(M13.serializer(), m131, s131)
    }

    @Test
    fun decodeMapLikeWithMapLikeValueUnstructured() {
        testDecode(M13.serializer(), s131, m131)
    }

    val s132 = """
        mm.1 = { "" = "" }
        i = 0
    """.trimIndent()

    @Test
    fun decodeMapLikeWithMapLikeValueInline2() {
        testDecode(M13.serializer(), s132, m131)
    }

    val s133 = """
        mm = { 1 = { "" = "" } }
        i = 0
    """.trimIndent()

    @Test
    fun decodeMapLikeWithMapLikeValueInline3() {
        testDecode(M13.serializer(), s133, m131)
    }

    val m132 = M13(
        mm = mapOf(
            "1" to emptyMap()
        ),
        i = 0
    )

    val s134 = """
        mm.1 = {  }
        i = 0
    """.trimIndent()

    @Test
    fun encodeMapLikeWithEmptyMapLikeValueUnstructured() {
        testEncode(M13.serializer(), m132, s134)
    }

    @Test
    fun decodeMapLikeWithEmptyMapLikeValueUnstructured() {
        testDecode(M13.serializer(), s134, m132)
    }

    val s135 = """
        mm = { 1 = {  } }
        i = 0
    """.trimIndent()

    @Test
    fun decodeMapLikeWithEmptyMapLikeValueInline2() {
        testDecode(M13.serializer(), s135, m132)
    }

    val m133 = M13(
        mm = emptyMap(),
        i = 0
    )

    val s136 = """
        mm = {  }
        i = 0
    """.trimIndent()

    @Test
    fun encodeEmptyMapLikeWithMapLikeValueUnstructured() {
        testEncode(M13.serializer(), m133, s136)
    }

    @Test
    fun decodeEmptyMapLikeWithMapLikeValueUnstructured() {
        testDecode(M13.serializer(), s136, m133)
    }

    @Serializable
    data class M14(
        @TomlInline
        val c: C1
    )

    val m141 = M14(
        c = C1(
            s = "inline"
        )
    )

    val s141 = """
        c = { s = "inline" }
    """.trimIndent()

    @Test
    fun encodeClassInline() {
        testEncode(M14.serializer(), m141, s141)
    }

    @Serializable
    data class M15(
        @TomlInline
        val bm: Map<String, Boolean>
    )

    val m151 = M15(
        bm = mapOf(
            "a" to true
        )
    )

    val s151 = """
        bm = { a = true }
    """.trimIndent()

    @Test
    fun encodeMapLikeWithPrimitiveValueInline() {
        testEncode(M15.serializer(), m151, s151)
    }

    @Serializable
    data class M16(
        @TomlInline
        val cm: Map<String, C1>
    )

    val m161 = M16(
        cm = mapOf(
            "a" to C1(
                s = "no"
            )
        )
    )

    val s161 = """
        cm = { a = { s = "no" } }
    """.trimIndent()

    @Test
    fun encodeMapLikeWithClassValueInline() {
        testEncode(M16.serializer(), m161, s161)
    }

    @Serializable
    data class M17(
        @TomlInline
        val cm: Map<String, C2>
    )

    val m171 = M17(
        cm = mapOf(
            "a" to C2()
        )
    )

    val s171 = """
        cm = { a = {  } }
    """.trimIndent()

    @Test
    fun encodeMapLikeWithEmptyClassValueInline() {
        testEncode(M17.serializer(), m171, s171)
    }

    @Serializable
    data class M18(
        @TomlInline
        val lm: Map<String, ByteArray>
    )

    val m181 = M18(
        lm = mapOf(
            "a" to byteArrayOf(0)
        )
    )

    val s181 = """
        lm = { a = [ 0 ] }
    """.trimIndent()

    @Test
    fun encodeMapLikeWithCollectionLikeValueInline() {
        testEncode(M18.serializer(), m181, s181)
    }

    val m182 = M18(
        lm = mapOf(
            "b" to byteArrayOf()
        )
    )

    val s182 = """
        lm = { b = [  ] }
    """.trimIndent()

    @Test
    fun encodeMapLikeWithEmptyCollectionLikeValueInline() {
        testEncode(M18.serializer(), m182, s182)
    }

    @Serializable
    data class M19(
        @TomlInline
        val mm: Map<String, Map<String, String>>
    )

    val m191 = M19(
        mm = mapOf(
            "1" to mapOf(
                "1" to "1"
            )
        )
    )

    val s191 = """
        mm = { 1 = { 1 = "1" } }
    """.trimIndent()

    @Test
    fun encodeMapLikeWithMapLikeValueInline() {
        testEncode(M19.serializer(), m191, s191)
    }

    val m192 = M19(
        mm = mapOf(
            "2" to emptyMap()
        )
    )

    val s192 = """
        mm = { 2 = {  } }
    """.trimIndent()

    @Test
    fun encodeMapLikeWithEmptyMapLikeValueInline() {
        testEncode(M19.serializer(), m192, s192)
    }
}
