package net.peanuuutz.tomlkt

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlin.test.assertEquals
import kotlin.test.assertFails

fun <T> testEncode(
    serializer: SerializationStrategy<T>,
    value: T,
    expect: String
) {
    val actual = Toml.encodeToString(serializer, value)

    assertEquals(expect, actual)
}

fun <T> testDecode(
    deserializer: DeserializationStrategy<T>,
    string: String,
    expect: T
) {
    val actual = Toml.decodeFromString(deserializer, string)

    assertEquals(expect, actual)
}

fun testInvalid(string: String) {
    assertFails { Toml.parseToTomlTable(string) }
}
