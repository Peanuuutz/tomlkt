package net.peanuuutz.tomlkt

import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import net.peanuuutz.tomlkt.internal.escape
import kotlin.test.Test
import kotlin.test.assertEquals

class EncoderTest {
    @Test
    fun encodeTomlInteger() {
        printIfDebug(Toml.encodeToString(ByteCode.serializer(), ByteCode(0b1010)))
        printIfDebug(Toml.encodeToString(Color.serializer(), Color(0xC0101010)))
    }

    @Test
    fun encodeClass() {
        printIfDebug(Toml.encodeToString(Project.serializer(), tomlProject))
    }

    @Test
    fun encodeMap() {
        printIfDebug(Toml.encodeToString(MapSerializer(String.serializer(), Project.serializer()), projects))
        printIfDebug("-----")
        printIfDebug(Toml.encodeToString(Score.serializer(), exampleScore))
    }

    @Test
    fun encodeEmptyClass() {
        printIfDebug(Toml.encodeToString(EmptyClass.serializer(), EmptyClass()))
    }

    @Test
    fun encodeGeneric() {
        printIfDebug(Toml.encodeToString(Box.serializer(Int.serializer()), Box(1)))
    }

    @Test
    fun encodeToTomlLiteral() {
        val int = Toml.encodeToTomlElement(Int.serializer(), 2)
        val string = Toml.encodeToTomlElement(String.serializer(), "I\n&\nU")

        assertEquals(int.toTomlLiteral().toInt(), 2)
        assertEquals(string.toTomlLiteral().content, "I\n&\nU")
    }

    @Test
    fun encodeToTomlTable() {
        val scoreAsTable = Toml.encodeToTomlElement(Score.serializer(), exampleScore)
        printIfDebug(Toml.decodeFromTomlElement(Score.serializer(), scoreAsTable))
        assertEquals(scoreAsTable.toTomlTable()["examinee"]?.toTomlLiteral()?.content, "Loney Chou")
    }

    @Test
    fun escape() {
        assertEquals(anotherLyrics.trimIndent().escape(), "Oops my baby,\\nyou woke up in my bed.")
    }
}