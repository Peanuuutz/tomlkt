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

package net.peanuuutz.tomlkt.internal.encoder

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlEncoder
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.internal.encodePolymorphically
import net.peanuuutz.tomlkt.internal.findRealDescriptor
import net.peanuuutz.tomlkt.internal.isPrimitiveLike
import net.peanuuutz.tomlkt.internal.isUnsignedInteger
import net.peanuuutz.tomlkt.internal.throwUnsupportedSerialKind
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

// -------- AbstractTomlEncoder --------

internal abstract class AbstractTomlEncoder(
    final override val toml: Toml
) : TomlEncoder {
    final override val serializersModule: SerializersModule
        get() = toml.serializersModule

    var currentDiscriminator: String? = null

    final override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        encodeSerializableValuePolymorphically(serializer, value)
    }
}

internal fun <T> AbstractTomlEncoder.encodeSerializableValuePolymorphically(
    serializer: SerializationStrategy<T>,
    value: T
) {
    when {
        serializer.descriptor.findRealDescriptor(toml.config).isPrimitiveLike -> {
            serializer.serialize(this, value)
        }
        serializer !is AbstractPolymorphicSerializer<*> -> {
            serializer.serialize(this, value)
        }
        else -> {
            @Suppress("UNCHECKED_CAST")
            encodePolymorphically(
                serializer = serializer as AbstractPolymorphicSerializer<Any>,
                value = value as Any
            ) { discriminator ->
                currentDiscriminator = discriminator
            }
        }
    }
}

// -------- AbstractTomlInlineEncoder --------

internal abstract class AbstractTomlInlineEncoder<E : AbstractTomlEncoder>(
    protected val delegate: E
) : TomlEncoder by delegate {
    final override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        return if (descriptor.isUnsignedInteger) this else delegate
    }

    final override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        return delegate.beginCollection(descriptor, collectionSize)
    }

    final override fun <T> encodeSerializableValue(serializer: SerializationStrategy<T>, value: T) {
        delegate.encodeSerializableValue(serializer, value)
    }
}

// -------- TomlCompositeEncoder --------

internal interface TomlCompositeEncoder : TomlEncoder, CompositeEncoder {
    fun encodeDiscriminatorElement(
        discriminator: String,
        serialName: String,
        isEmptyStructure: Boolean
    )

    fun beginElement(
        descriptor: SerialDescriptor,
        index: Int
    )

    fun endElement(
        descriptor: SerialDescriptor,
        index: Int
    )

    override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
        encodeElement(descriptor, index) {
            encodeBoolean(value)
        }
    }

    override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
        encodeElement(descriptor, index) {
            encodeByte(value)
        }
    }

    override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
        encodeElement(descriptor, index) {
            encodeShort(value)
        }
    }

    override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
        encodeElement(descriptor, index) {
            encodeInt(value)
        }
    }

    override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
        encodeElement(descriptor, index) {
            encodeLong(value)
        }
    }

    override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
        encodeElement(descriptor, index) {
            encodeFloat(value)
        }
    }

    override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
        encodeElement(descriptor, index) {
            encodeDouble(value)
        }
    }

    override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
        encodeElement(descriptor, index) {
            encodeChar(value)
        }
    }

    override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
        encodeElement(descriptor, index) {
            encodeString(value)
        }
    }

    override fun <T : Any> encodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T?
    ) {
        when (value) { // Revisit type-specific encoding.
            null -> {
                encodeSerializableElement(descriptor, index, TomlNull.serializer(), TomlNull)
            }
            is Boolean -> {
                encodeBooleanElement(descriptor, index, value)
            }
            is Byte -> {
                encodeByteElement(descriptor, index, value)
            }
            is Short -> {
                encodeShortElement(descriptor, index, value)
            }
            is Int -> {
                encodeIntElement(descriptor, index, value)
            }
            is Long -> {
                encodeLongElement(descriptor, index, value)
            }
            is Float -> {
                encodeFloatElement(descriptor, index, value)
            }
            is Double -> {
                encodeDoubleElement(descriptor, index, value)
            }
            is Char -> {
                encodeCharElement(descriptor, index, value)
            }
            is String -> {
                encodeStringElement(descriptor, index, value)
            }
            else -> {
                encodeSerializableElement(descriptor, index, serializer, value)
            }
        }
    }

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        encodeElement(descriptor, index) {
            encodeSerializableValue(serializer, value)
        }
    }
}

internal inline fun TomlCompositeEncoder.encodeElement(
    descriptor: SerialDescriptor,
    index: Int,
    encode: () -> Unit
) {
    contract { callsInPlace(encode, InvocationKind.EXACTLY_ONCE) }

    beginElement(descriptor, index)
    encode()
    endElement(descriptor, index)
}

internal fun AbstractTomlEncoder.onBeginStructurePolymorphically(
    compositeEncoder: TomlCompositeEncoder,
    descriptor: SerialDescriptor,
    isEmptyStructure: Boolean
) {
    val discriminator = currentDiscriminator ?: return
    currentDiscriminator = null
    compositeEncoder.encodeDiscriminatorElement(
        discriminator = discriminator,
        serialName = descriptor.serialName,
        isEmptyStructure = isEmptyStructure
    )
}

// -------- AbstractTomlInlineElementEncoder --------

internal abstract class AbstractTomlInlineElementEncoder<E : TomlCompositeEncoder>(
    protected val parentDescriptor: SerialDescriptor,
    protected val elementIndex: Int,
    protected val delegate: E
) : TomlEncoder {
    final override val toml: Toml
        get() = delegate.toml

    final override val serializersModule: SerializersModule
        get() = delegate.serializersModule

    final override fun encodeBoolean(value: Boolean) {
        delegate.encodeBooleanElement(parentDescriptor, elementIndex, value)
    }

    final override fun encodeFloat(value: Float) {
        delegate.encodeFloatElement(parentDescriptor, elementIndex, value)
    }

    final override fun encodeDouble(value: Double) {
        delegate.encodeDoubleElement(parentDescriptor, elementIndex, value)
    }

    final override fun encodeChar(value: Char) {
        delegate.encodeCharElement(parentDescriptor, elementIndex, value)
    }

    final override fun encodeString(value: String) {
        delegate.encodeStringElement(parentDescriptor, elementIndex, value)
    }

    final override fun encodeNull() {
        delegate.encodeSerializableElement(parentDescriptor, elementIndex, TomlNull.serializer(), TomlNull)
    }

    final override fun encodeTomlElement(value: TomlElement) {
        delegate.encodeSerializableElement(parentDescriptor, elementIndex, TomlElement.serializer(), value)
    }

    final override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        delegate.encodeElement(parentDescriptor, elementIndex) {
            delegate.encodeEnum(enumDescriptor, index)
        }
    }

    final override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        return this // Returns this anyway.
    }

    final override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        throwUnsupportedSerialKind("Cannot encode structures in AbstractTomlInlineElementEncoder")
    }
}
