package net.peanuuutz.tomlkt

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlin.test.assertEquals
import kotlin.test.assertFails

fun <T> testEncode(
    serializer: SerializationStrategy<T>,
    value: T,
    expect: String,
    toml: Toml = Toml
) {
    val actual = toml.encodeToString(serializer, value)

    assertEquals(expect, actual)
}

fun <T> testEncodeTomlElement(
    serializer: SerializationStrategy<T>,
    value: T,
    expect: TomlElement,
    toml: Toml = Toml
) {
    val actual = toml.encodeToTomlElement(serializer, value)

    assertEquals(expect, actual)
}

fun <T> testDecode(
    deserializer: DeserializationStrategy<T>,
    string: String,
    expect: T,
    toml: Toml = Toml
) {
    val actual = toml.decodeFromString(deserializer, string)

    assertEquals(expect, actual)
}

fun <T> testDecodeTomlElement(
    deserializer: DeserializationStrategy<T>,
    value: TomlElement,
    expect: T,
    toml: Toml = Toml
) {
    val actual = toml.decodeFromTomlElement(deserializer, value)

    assertEquals(expect, actual)
}

fun testInvalid(
    string: String,
    toml: Toml = Toml
) {
    assertFails { toml.parseToTomlTable(string) }
}
