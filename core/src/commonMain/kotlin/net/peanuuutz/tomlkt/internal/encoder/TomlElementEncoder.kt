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
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind.CLASS
import kotlinx.serialization.descriptors.StructureKind.LIST
import kotlinx.serialization.descriptors.StructureKind.MAP
import kotlinx.serialization.descriptors.StructureKind.OBJECT
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlConfig
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.internal.isUnsignedInteger
import net.peanuuutz.tomlkt.internal.throwPolymorphicCollection
import net.peanuuutz.tomlkt.internal.throwUnsupportedSerialKind
import net.peanuuutz.tomlkt.toTomlKey

// -------- AbstractTomlElementEncoder --------

internal abstract class AbstractTomlElementEncoder(
    config: TomlConfig,
    serializersModule: SerializersModule
) : AbstractTomlEncoder(config, serializersModule) {
    lateinit var element: TomlElement

    override fun encodeBoolean(value: Boolean) {
        element = TomlLiteral(value)
    }

    override fun encodeByte(value: Byte) {
        element = TomlLiteral(value)
    }

    override fun encodeShort(value: Short) {
        element = TomlLiteral(value)
    }

    override fun encodeInt(value: Int) {
        element = TomlLiteral(value)
    }

    override fun encodeLong(value: Long) {
        element = TomlLiteral(value)
    }

    override fun encodeFloat(value: Float) {
        element = TomlLiteral(value)
    }

    override fun encodeDouble(value: Double) {
        element = TomlLiteral(value)
    }

    override fun encodeChar(value: Char) {
        element = TomlLiteral(value)
    }

    override fun encodeString(value: String) {
        element = TomlLiteral(value)
    }

    override fun encodeNull() {
        element = TomlNull
    }

    override fun encodeTomlElement(value: TomlElement) {
        element = value
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        return if (descriptor.isUnsignedInteger) {
            TomlElementInlineEncoder(this)
        } else {
            this
        }
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return beginStructurePolymorphically(descriptor)
    }

    override fun beginCollection(descriptor: SerialDescriptor, collectionSize: Int): CompositeEncoder {
        return beginCollectionPolymorphically(descriptor, collectionSize)
    }
}

// -------- TomlElementEncoder --------

internal class TomlElementEncoder(
    config: TomlConfig,
    serializersModule: SerializersModule
) : AbstractTomlElementEncoder(config, serializersModule)

// -------- TomlElementInlineEncoder --------

private class TomlElementInlineEncoder(
    delegate: AbstractTomlElementEncoder
) : AbstractTomlInlineEncoder<AbstractTomlElementEncoder>(delegate) {
    override fun encodeByte(value: Byte) {
        delegate.element = TomlLiteral(value.toUByte())
    }

    override fun encodeShort(value: Short) {
        delegate.element = TomlLiteral(value.toUShort())
    }

    override fun encodeInt(value: Int) {
        delegate.element = TomlLiteral(value.toUInt())
    }

    override fun encodeLong(value: Long) {
        delegate.element = TomlLiteral(value.toULong())
    }
}

// -------- AbstractTomlElementCompositeEncoder --------

private abstract class AbstractTomlElementCompositeEncoder(
    delegate: AbstractTomlElementEncoder
) : AbstractTomlElementEncoder(delegate.config, delegate.serializersModule), TomlCompositeEncoder {
    final override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
        return if (descriptor.getElementDescriptor(index).isUnsignedInteger) {
            TomlElementInlineElementEncoder(
                parentDescriptor = descriptor,
                elementIndex = index,
                delegate = this
            )
        } else {
            this
        }
    }

    final override fun endStructure(descriptor: SerialDescriptor) {}
}

// -------- TomlElementInlineElementEncoder --------

private class TomlElementInlineElementEncoder(
    parentDescriptor: SerialDescriptor,
    elementIndex: Int,
    delegate: AbstractTomlElementCompositeEncoder
) : AbstractTomlInlineElementEncoder<AbstractTomlElementCompositeEncoder>(parentDescriptor, elementIndex, delegate) {
    override fun encodeByte(value: Byte) {
        delegate.encodeElement(parentDescriptor, elementIndex) {
            delegate.element = TomlLiteral(value.toUByte())
        }
    }

    override fun encodeShort(value: Short) {
        delegate.encodeElement(parentDescriptor, elementIndex) {
            delegate.element = TomlLiteral(value.toUShort())
        }
    }

    override fun encodeInt(value: Int) {
        delegate.encodeElement(parentDescriptor, elementIndex) {
            delegate.element = TomlLiteral(value.toUInt())
        }
    }

    override fun encodeLong(value: Long) {
        delegate.encodeElement(parentDescriptor, elementIndex) {
            delegate.element = TomlLiteral(value.toULong())
        }
    }
}

// -------- TomlElementClassEncoder --------

private class TomlElementClassEncoder(
    delegate: AbstractTomlElementEncoder,
    private val builder: MutableMap<String, TomlElement>
) : AbstractTomlElementCompositeEncoder(delegate) {
    private lateinit var currentKey: String

    override fun encodeDiscriminatorElement(discriminator: String, serialName: String, isEmptyStructure: Boolean) {
        builder[discriminator] = TomlLiteral(serialName)
    }

    override fun beginElement(descriptor: SerialDescriptor, index: Int) {
        currentKey = descriptor.getElementName(index)
    }

    override fun endElement(descriptor: SerialDescriptor, index: Int) {
        builder[currentKey] = element
    }
}

// -------- TomlElementMapEncoder --------

private class TomlElementMapEncoder(
    delegate: AbstractTomlElementEncoder,
    private val builder: MutableMap<String, TomlElement>
) : AbstractTomlElementCompositeEncoder(delegate) {
    private var isKey: Boolean = true

    private lateinit var currentKey: String

    override fun <T> encodeSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        serializer: SerializationStrategy<T>,
        value: T
    ) {
        if (isKey) {
            currentKey = value.toTomlKey()
        } else {
            encodeSerializableValue(serializer, value)
            builder[currentKey] = element
        }
        isKey = !isKey
    }

    override fun encodeDiscriminatorElement(discriminator: String, serialName: String, isEmptyStructure: Boolean) {
        throwPolymorphicCollection()
    }

    // This shouldn't be called.
    override fun beginElement(descriptor: SerialDescriptor, index: Int) {}

    // This shouldn't be called.
    override fun endElement(descriptor: SerialDescriptor, index: Int) {}
}

// -------- TomlElementArrayEncoder --------

private class TomlElementArrayEncoder(
    delegate: AbstractTomlElementEncoder,
    private val builder: MutableList<TomlElement>
) : AbstractTomlElementCompositeEncoder(delegate) {
    override fun encodeDiscriminatorElement(discriminator: String, serialName: String, isEmptyStructure: Boolean) {
        throwPolymorphicCollection()
    }

    override fun beginElement(descriptor: SerialDescriptor, index: Int) {}

    override fun endElement(descriptor: SerialDescriptor, index: Int) {
        builder.add(element)
    }
}

// -------- Utils --------

private fun AbstractTomlElementEncoder.beginStructurePolymorphically(
    descriptor: SerialDescriptor
): CompositeEncoder {
    val compositeEncoder = when (val kind = descriptor.kind) {
        CLASS, is PolymorphicKind, OBJECT -> {
            val builder = mutableMapOf<String, TomlElement>()
            element = TomlTable(builder)
            TomlElementClassEncoder(this, builder)
        }
        else -> throwUnsupportedSerialKind(kind)
    }
    onBeginStructurePolymorphically(
        compositeEncoder = compositeEncoder,
        descriptor = descriptor,
        isEmptyStructure = descriptor.elementsCount == 0
    )
    return compositeEncoder
}

// Currently polymorphic collection-like types are not supported.
@Suppress("UNUSED_PARAMETER")
private fun AbstractTomlElementEncoder.beginCollectionPolymorphically(
    descriptor: SerialDescriptor,
    collectionSize: Int
): CompositeEncoder {
    return when (val kind = descriptor.kind) {
        LIST -> {
            val builder = mutableListOf<TomlElement>()
            element = TomlArray(builder)
            TomlElementArrayEncoder(this, builder)
        }
        MAP -> {
            val builder = mutableMapOf<String, TomlElement>()
            element = TomlTable(builder)
            TomlElementMapEncoder(this, builder)
        }
        else -> throwUnsupportedSerialKind(kind)
    }
}
