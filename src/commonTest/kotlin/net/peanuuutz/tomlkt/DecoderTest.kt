package net.peanuuutz.tomlkt

import kotlinx.serialization.builtins.serializer
import net.peanuuutz.tomlkt.internal.unescape
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime

class DecoderTest {
    @OptIn(ExperimentalTime::class)
    @Test
    fun parseTest() {
        lateinit var table: TomlTable
        val time = measureTime { table = Toml.parseToTomlTable(cargo) }
        printIfDebug(time)
        printIfDebug(table)
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
        printIfDebug(thirdLyrics.unescape())
    }
}