package net.peanuuutz.tomlkt

import kotlinx.serialization.builtins.serializer
import net.peanuuutz.tomlkt.internal.unescape
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.measureTimedValue

class DecoderTest {
    @Test
    fun parseTomlInteger() {
        val (integers, time) = measureTimedValue { Toml.parseToTomlTable(integers) }
        printIfDebug(integers)
        printIfDebug(time)
        assertEquals(integers["two"]?.toTomlLiteral()?.toIntOrNull(), 4)
        assertEquals(integers["eight"]?.toTomlLiteral()?.toIntOrNull(), 64)
        assertEquals(integers["ten"]?.toTomlLiteral()?.toIntOrNull(), -100)
        assertEquals(integers["sixteen"]?.toTomlLiteral()?.toIntOrNull(), 256)
    }

    @Test
    fun parseDateTime() {
        val (dateTimes, time) = measureTimedValue { Toml.parseToTomlTable(dateTimes) }
        printIfDebug(dateTimes)
        printIfDebug(time)
        assertEquals(
            expected = dateTimes["local-date-time"]?.toTomlLiteral()?.toLocalDateTimeOrNull(),
            actual = TomlLocalDateTime("2020-01-01T20:00:00.5")
        )
        assertEquals(
            expected = dateTimes["offset-date-time"]?.toTomlLiteral()?.toOffsetDateTimeOrNull(),
            actual = TomlOffsetDateTime("1999-09-09T09:09:09.999999-09:00")
        )
        assertEquals(
            expected = dateTimes["local-date"]?.toTomlLiteral()?.toLocalDateOrNull(),
            actual = TomlLocalDate("2020-01-01")
        )
        assertEquals(
            expected = dateTimes["local-time"]?.toTomlLiteral()?.toLocalTimeOrNull(),
            actual = TomlLocalTime("09:09:09.999999")
        )
    }

    @Test
    fun parseHugeConfig() {
        val (table, time) = measureTimedValue { Toml.parseToTomlTable(cargo) }
        printIfDebug(table)
        printIfDebug(time)
        assertEquals(table["package", "version"]?.toTomlLiteral()?.content, "0.0.1")
    }

    @Test
    fun decodeClassAndList() {
        val project = Toml.decodeFromString(Project.serializer(), project)
        printIfDebug(project)
        assertEquals(project.maintainability, Maintainability.HIGH)
    }

    @Test
    fun decodeInlineClass() {
        val boxedUInt = Toml.decodeFromString(Box.serializer(UInt.serializer()), "content = 0x10")
        printIfDebug(boxedUInt)
        assertEquals(boxedUInt.content, 16L.toUInt())
        val externalModule = Toml.decodeFromString(Module.serializer(), externalModule)
        printIfDebug(externalModule)
        assertEquals(externalModule.id, 4321234L.toULong())
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
    fun decodeDateTime() {
        val randomTask = Toml.decodeFromString(Task.serializer(), randomTask)
        printIfDebug(randomTask)
        assertEquals(randomTask, task)
    }

    @Test
    fun unescape() {
        assertEquals(thirdLyrics.trimIndent().unescape(), "Oops we broke up,\nwe're better off as friends.")
    }
}
