package net.peanuuutz.tomlkt

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
        printIfDebug(tomlString) // Since '\n' will be converted to "\\n"
        assertEquals(TomlLiteral(Maintainability.HIGH).content, "HIGH")
        assertEquals(TomlLiteral("LOW").toEnum(), Maintainability.LOW)
    }

    @Test
    fun tomlArray() {
        val array = TomlArray(listOf('1', null, '\b', true))
        assertEquals(array[0].toTomlLiteral().toInt(), 1)
        val tomlString = Toml.encodeToString(array) // Maybe add an 'alwaysFoldArrayOfPrimitive' config...
        printIfDebug(tomlString)
    }

    @Test
    fun tomlTable() {
        val table = TomlTable(mapOf("1" to null, 1 to 'b', '\b' to listOf(true))) // "1" is equal to 1 when converted to key
        assertEquals(table[1]!!.toTomlLiteral().toChar(), 'b')
        val tomlString = Toml.encodeToString(table)
        printIfDebug(tomlString)
    }

    @Test
    fun elementToString() {
        printIfDebug(TomlArray(listOf('1', null, '\b', true)))
        printIfDebug(TomlTable(mapOf('1' to null, '\b' to true))) // Note the difference where strings behave
    }
}

