package net.peanuuutz.tomlkt

import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlin.test.Test
import kotlin.test.assertEquals

class TomlElementTest {
    @Test
    fun tomlNull() {
        val tomlString = Toml.encodeToString(TomlNull)
        printIfDebug(tomlString)
    }

    @Test
    fun tomlLiteral() {
        val tomlString = Toml.encodeToString(TomlLiteral(lyrics))
        printIfDebug(tomlString) // Since '\n' will be converted to "\\n".
        assertEquals(TomlLiteral(Maintainability.HIGH).content, "HIGH")
        assertEquals(TomlLiteral("LOW").toEnum(), Maintainability.LOW)
        printIfDebug("-----")
        val dateTime = TomlOffsetDateTime("2023-08-30T17:25:00-07:00")
        val tomlStringWithDateTime = Toml.encodeToString(TomlLiteral(dateTime))
        printIfDebug(tomlStringWithDateTime)
    }

    @Test
    fun tomlArray() {
        val array = TomlArray(listOf('1', null, '\b', true))
        assertEquals(array[0].toTomlLiteral().toInt(), 1)
        val tomlString = Toml.encodeToString(array) // Maybe add an 'alwaysInlineArrayOfPrimitive' config.
        printIfDebug(tomlString)
    }

    @Test
    fun tomlTable() {
        val table = TomlTable(mapOf("1" to null, 1 to 'b', '\b' to listOf(true))) // "1" is equal to 1.
        assertEquals(table[1]!!.toTomlLiteral().toChar(), 'b')
        val tomlString = Toml.encodeToString(table)
        printIfDebug(tomlString)
    }

    @Test
    fun elementToString() {
        printIfDebug(TomlArray(listOf('1', null, '\b', true)))
        printIfDebug(TomlTable(mapOf('1' to null, '\b' to true))) // Note the difference where strings behave.
    }

    @Test
    fun nestedElement() {
        val boxedArray = Box(TomlArray(listOf(1, 2)))
        printIfDebug(Toml.encodeToString(Box.serializer(TomlArray.serializer()), boxedArray))
        printIfDebug("-----")
        val mapOfTables = mapOf(1 to TomlTable(mapOf(1 to 1)))
        val mapOfTablesSerializer = MapSerializer(Int.serializer(), TomlTable.serializer())
        printIfDebug(Toml.encodeToString(
            serializer = mapOfTablesSerializer,
            value = mapOfTables
        ))
        printIfDebug("-----")
        printIfDebug(Toml.encodeToString(
            serializer = MapSerializer(Int.serializer(), mapOfTablesSerializer),
            value = mapOf(1 to mapOfTables)
        ))
        printIfDebug("-----")
        printIfDebug(Toml.encodeToString(
            serializer = Box.serializer(MapSerializer(Int.serializer(), Box.serializer(mapOfTablesSerializer))),
            value = Box(mapOf(1 to Box(mapOfTables)))
        ))
    }
}
