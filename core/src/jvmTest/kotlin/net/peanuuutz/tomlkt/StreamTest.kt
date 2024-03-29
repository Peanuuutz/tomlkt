package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.io.path.reader
import kotlin.io.path.writeText
import kotlin.io.path.writer
import kotlin.test.Test
import kotlin.test.assertEquals

class StreamTest {
    @Serializable
    data class M1(
        val b: Boolean,
        val c: C
    )

    @Serializable
    data class C(
        @TomlBlockArray(4)
        val ss: List<String>
    )

    val m11 = M1(
        b = false,
        c = C(
            ss = listOf(
                "A", "B", "C", "D",
                "E", "F", "G"
            )
        )
    )

    val s11 = """
        b = false
        
        [c]
        ss = [
            "A", "B", "C", "D",
            "E", "F", "G"
        ]
    """.trimIndent()

    val p1 = Path("build/config.toml")

    @Test
    fun encodeToNativeWriter() {
        Toml.encodeToNativeWriter(M1.serializer(), m11, p1.writer())

        val r = p1.readText()

        assertEquals(s11, r)
    }

    @Test
    fun decodeFromNativeReader() {
        p1.writeText(s11)

        val r = Toml.decodeFromNativeReader(M1.serializer(), p1.reader())

        assertEquals(m11, r)
    }
}
