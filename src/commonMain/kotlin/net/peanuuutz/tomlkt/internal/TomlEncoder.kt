package net.peanuuutz.tomlkt.internal

import kotlinx.serialization.encoding.Encoder
import net.peanuuutz.tomlkt.TomlElement

internal interface TomlEncoder : Encoder {
    fun encodeTomlElement(value: TomlElement) // For convenience
}