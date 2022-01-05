package net.peanuuutz.tomlkt.internal.parser

import net.peanuuutz.tomlkt.TomlElement

internal interface TomlParser<T : TomlElement> {
    fun parse(): T
}