package net.peanuuutz.tomlkt

import kotlinx.serialization.builtins.serializer
import net.peanuuutz.tomlkt.internal.unescape
import kotlin.test.Test
import kotlin.test.assertEquals

class DecoderTest {
    @Test
    fun parseTomlInteger() {
        val integers = Toml.parseToTomlTable(integers)
        printIfDebug(integers)
        assertEquals(integers["two"]?.toTomlLiteral()?.toIntOrNull(), 4)
        assertEquals(integers["eight"]?.toTomlLiteral()?.toIntOrNull(), 64)
        assertEquals(integers["ten"]?.toTomlLiteral()?.toIntOrNull(), -100)
        assertEquals(integers["sixteen"]?.toTomlLiteral()?.toIntOrNull(), 256)
    }

    @Test
    fun parseHugeConfig() {
        val table = Toml.parseToTomlTable(cargo)
        printIfDebug(table)
        assertEquals(table["package", "version"]?.toTomlLiteral()?.content, "0.0.1")
    }

    @Test
    fun decodeClassAndList() {
        val project = Toml.decodeFromString(Project.serializer(), project)
        printIfDebug(project)
        assertEquals(project.maintainability, Maintainability.HIGH)
    }

    @Test
    fun decodeGeneric() {
        val box = Toml.decodeFromString(Box.serializer(Boolean.serializer()), boxContent)
        printIfDebug(box)
        assertEquals(box.content, null)
    }

    @Test
    fun decodeMap() {
        val score = Toml.decodeFromString(Score.serializer(), score)
        printIfDebug(score)
        assertEquals(score.scores["Listening"]?.equals(91), true)
    }

    @Test
    fun unescape() {
        assertEquals(thirdLyrics.trimIndent().unescape(), "Oops we broke up,\nwe're better off as friends.")
    }
}