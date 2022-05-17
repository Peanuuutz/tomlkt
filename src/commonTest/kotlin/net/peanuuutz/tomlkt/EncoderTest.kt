package net.peanuuutz.tomlkt

import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import net.peanuuutz.tomlkt.internal.escape
import kotlin.test.Test

class EncoderTest {
    @Test
    fun encodeClass() {
        printIfDebug(Toml.encodeToString(Project.serializer(), tomlProject))
    }

    @Test
    fun encodeMap() {
        val projects = mapOf("Toml" to tomlProject, "Yaml" to yamlProject)
        val score = Score(
            examinee = "Loney Chou",
            scores = mapOf("Listening" to 80, "Reading" to 95)
        )

        printIfDebug(Toml.encodeToString(MapSerializer(String.serializer(), Project.serializer()), projects))
        printIfDebug("-----")
        printIfDebug(Toml.encodeToString(Score.serializer(), score))
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
    fun encodeToTomlElement() {
        val projects = mapOf("Toml" to tomlProject, "Yaml" to yamlProject)
        val score = Score(
            examinee = "Loney Chou",
            scores = mapOf("Listening" to 80, "Reading" to 95)
        )

        printIfDebug(Toml.encodeToTomlElement(Int.serializer(), 2))
        printIfDebug(Toml.encodeToTomlElement(String.serializer(), "I\n&\nU"))
        printIfDebug("-----")
        printIfDebug(Toml.encodeToTomlElement(MapSerializer(String.serializer(), Project.serializer()), projects))
        printIfDebug("-----")
        val scoreAsTable = Toml.encodeToTomlElement(Score.serializer(), score)
        printIfDebug(Toml.decodeFromTomlElement(Score.serializer(), scoreAsTable))
    }

    @Test
    fun escape() {
        printIfDebug(anotherLyrics.escape())
    }
}