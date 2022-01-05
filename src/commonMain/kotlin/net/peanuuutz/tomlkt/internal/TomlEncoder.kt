package net.peanuuutz.tomlkt.internal

import kotlinx.serialization.encoding.Encoder

internal interface TomlEncoder : Encoder {
    fun encodeRawString(value: String) // For convenience
}