package net.peanuuutz.tomlkt.internal

import kotlinx.serialization.encoding.Decoder
import net.peanuuutz.tomlkt.TomlElement

internal interface TomlDecoder : Decoder {
    fun decodeTomlElement(): TomlElement // Just for convenience
}