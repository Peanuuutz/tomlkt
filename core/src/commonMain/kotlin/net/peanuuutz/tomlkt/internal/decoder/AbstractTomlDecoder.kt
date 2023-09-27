/*
    Copyright 2023 Peanuuutz

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */

package net.peanuuutz.tomlkt.internal.decoder

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.TomlDecoder
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.internal.decodePolymorphically
import net.peanuuutz.tomlkt.internal.findRealDescriptor
import net.peanuuutz.tomlkt.internal.isPrimitiveLike
import net.peanuuutz.tomlkt.internal.isUnsignedInteger
import net.peanuuutz.tomlkt.internal.throwUnsupportedSerialKind

// -------- AbstractTomlDecoder --------

internal abstract class AbstractTomlDecoder(
    final override val toml: Toml
) : TomlDecoder {
    final override val serializersModule: SerializersModule
        get() = toml.serializersModule

    var currentDiscriminator: String? = null

    final override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        return decodeSerializableValuePolymorphically(deserializer)
    }
}

internal fun <T> AbstractTomlDecoder.decodeSerializableValuePolymorphically(
    deserializer: DeserializationStrategy<T>
): T {
    return when {
        deserializer.descriptor.findRealDescriptor(toml.config).isPrimitiveLike -> {
            deserializer.deserialize(this)
        }
        deserializer !is AbstractPolymorphicSerializer<*> -> {
            deserializer.deserialize(this)
        }
        else -> {
            @Suppress("UNCHECKED_CAST")
            decodePolymorphically(
                deserializer = deserializer as AbstractPolymorphicSerializer<Any>
            ) { discriminator ->
                currentDiscriminator = discriminator
            }
        }
    }
}

// -------- AbstractTomlInlineDecoder --------

internal abstract class AbstractTomlInlineDecoder<D : AbstractTomlDecoder>(
    protected val delegate: D
) : TomlDecoder by delegate {
    final override fun decodeInline(descriptor: SerialDescriptor): Decoder {
        return if (descriptor.isUnsignedInteger) this else delegate
    }

    final override fun <T> decodeSerializableValue(deserializer: DeserializationStrategy<T>): T {
        return delegate.decodeSerializableValue(deserializer)
    }
}

// -------- TomlCompositeDecoder --------

internal interface TomlCompositeDecoder : TomlDecoder, CompositeDecoder {
    fun beginElement(
        descriptor: SerialDescriptor,
        index: Int
    )

    fun endElement(
        descriptor: SerialDescriptor,
        index: Int
    )

    override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
        return decodeElement(descriptor, index) {
            decodeBoolean()
        }
    }

    override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
        return decodeElement(descriptor, index) {
            decodeByte()
        }
    }

    override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
        return decodeElement(descriptor, index) {
            decodeShort()
        }
    }

    override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
        return decodeElement(descriptor, index) {
            decodeInt()
        }
    }

    override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
        return decodeElement(descriptor, index) {
            decodeLong()
        }
    }

    override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
        return decodeElement(descriptor, index) {
            decodeFloat()
        }
    }

    override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
        return decodeElement(descriptor, index) {
            decodeDouble()
        }
    }

    override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
        return decodeElement(descriptor, index) {
            decodeChar()
        }
    }

    override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
        return decodeElement(descriptor, index) {
            decodeString()
        }
    }

    override fun <T> decodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T>,
        previousValue: T?
    ): T {
        return decodeElement(descriptor, index) {
            decodeSerializableValue(deserializer)
        }
    }
}

internal inline fun <T> TomlCompositeDecoder.decodeElement(
    descriptor: SerialDescriptor,
    index: Int,
    decode: () -> T
): T {
    beginElement(descriptor, index)
    val value = decode()
    endElement(descriptor, index)
    return value
}

// -------- AbstractTomlInlineElementDecoder --------

internal abstract class AbstractTomlInlineElementDecoder<D : TomlCompositeDecoder>(
    protected val parentDescriptor: SerialDescriptor,
    protected val elementIndex: Int,
    protected val delegate: D
) : TomlDecoder {
    protected var decodedNotNullMark: Boolean = false

    final override val toml: Toml
        get() = delegate.toml

    final override val serializersModule: SerializersModule
        get() = delegate.serializersModule

    final override fun decodeBoolean(): Boolean {
        return if (!decodedNotNullMark) {
            delegate.decodeBooleanElement(parentDescriptor, elementIndex)
        } else {
            val value = delegate.decodeBoolean()
            decodedNotNullMark = false
            value
        }
    }

    final override fun decodeFloat(): Float {
        return if (!decodedNotNullMark) {
            delegate.decodeFloatElement(parentDescriptor, elementIndex)
        } else {
            val value = delegate.decodeFloat()
            decodedNotNullMark = false
            value
        }
    }

    final override fun decodeDouble(): Double {
        return if (!decodedNotNullMark) {
            delegate.decodeDoubleElement(parentDescriptor, elementIndex)
        } else {
            val value = delegate.decodeDouble()
            decodedNotNullMark = false
            value
        }
    }

    final override fun decodeChar(): Char {
        return if (!decodedNotNullMark) {
            delegate.decodeCharElement(parentDescriptor, elementIndex)
        } else {
            val value = delegate.decodeChar()
            decodedNotNullMark = false
            value
        }
    }

    final override fun decodeString(): String {
        return if (!decodedNotNullMark) {
            delegate.decodeStringElement(parentDescriptor, elementIndex)
        } else {
            val value = delegate.decodeString()
            decodedNotNullMark = false
            value
        }
    }

    final override fun decodeNull(): Nothing? {
        return if (!decodedNotNullMark) {
            delegate.decodeElement(parentDescriptor, elementIndex) {
                delegate.decodeNull()
            }
        } else {
            val value = delegate.decodeNull()
            decodedNotNullMark = false
            value
        }
    }

    final override fun decodeNotNullMark(): Boolean {
        return if (!decodedNotNullMark) {
            val isNotNull = delegate.decodeElement(parentDescriptor, elementIndex) {
                delegate.decodeNotNullMark()
            }
            decodedNotNullMark = true
            isNotNull
        } else {
            delegate.decodeNotNullMark()
        }
    }

    final override fun decodeTomlElement(): TomlElement {
        return if (!decodedNotNullMark) {
            delegate.decodeElement(parentDescriptor, elementIndex) {
                delegate.decodeTomlElement()
            }
        } else {
            val value = delegate.decodeTomlElement()
            decodedNotNullMark = false
            value
        }
    }

    final override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        return if (!decodedNotNullMark) {
            delegate.decodeElement(parentDescriptor, elementIndex) {
                delegate.decodeEnum(enumDescriptor)
            }
        } else {
            val value = delegate.decodeEnum(enumDescriptor)
            decodedNotNullMark = false
            value
        }
    }

    final override fun decodeInline(descriptor: SerialDescriptor): Decoder {
        return this // Returns this anyway.
    }

    final override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        throwUnsupportedSerialKind("Cannot decode structures in AbstractTomlInlineElementDecoder")
    }
}
