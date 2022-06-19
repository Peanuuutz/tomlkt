package net.peanuuutz.tomlkt

import kotlin.test.Test
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer

class SpecialTest {
    @Test
    fun contextual() {
        val stringOrColor = StringOrColor(Color(0x1))
        printIfDebug(Toml.encodeToString(StringOrColor.serializer(), stringOrColor))
        val mapOfStringOrColors: Map<String, Any> = mapOf("Red" to Color(0xFF0000))
        val mapOfStringOrColorsSerializer = MapSerializer(String.serializer(), StringOrColorSerializer)
        printIfDebug(Toml.encodeToString(mapOfStringOrColorsSerializer, mapOfStringOrColors))
        printIfDebug(Toml.encodeToString(
            serializer = MapSerializer(Int.serializer(), mapOfStringOrColorsSerializer),
            value = mapOf(1 to mapOfStringOrColors)
        ))
    }
}