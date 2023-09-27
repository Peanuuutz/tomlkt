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
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind.CLASS
import kotlinx.serialization.descriptors.StructureKind.LIST
import kotlinx.serialization.descriptors.StructureKind.MAP
import kotlinx.serialization.descriptors.StructureKind.OBJECT
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import net.peanuuutz.tomlkt.Toml
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.asTomlArray
import net.peanuuutz.tomlkt.asTomlLiteral
import net.peanuuutz.tomlkt.asTomlNull
import net.peanuuutz.tomlkt.asTomlTable
import net.peanuuutz.tomlkt.internal.isUnsignedInteger
import net.peanuuutz.tomlkt.internal.throwUnknownKey
import net.peanuuutz.tomlkt.internal.throwUnsupportedSerialKind
import net.peanuuutz.tomlkt.toBoolean
import net.peanuuutz.tomlkt.toByte
import net.peanuuutz.tomlkt.toChar
import net.peanuuutz.tomlkt.toDouble
import net.peanuuutz.tomlkt.toFloat
import net.peanuuutz.tomlkt.toInt
import net.peanuuutz.tomlkt.toLong
import net.peanuuutz.tomlkt.toShort

// -------- AbstractTomlElementDecoder --------

internal abstract class AbstractTomlElementDecoder(toml: Toml) : AbstractTomlDecoder(toml) {
    abstract val element: TomlElement

    final override fun decodeBoolean(): Boolean {
        return element.asTomlLiteral().toBoolean()
    }

    final override fun decodeByte(): Byte {
        return element.asTomlLiteral().toByte()
    }

    final override fun decodeShort(): Short {
        return element.asTomlLiteral().toShort()
    }

    final override fun decodeInt(): Int {
        return element.asTomlLiteral().toInt()
    }

    final override fun decodeLong(): Long {
        return element.asTomlLiteral().toLong()
    }

    final override fun decodeFloat(): Float {
        return element.asTomlLiteral().toFloat()
    }

    final override fun decodeDouble(): Double {
        return element.asTomlLiteral().toDouble()
    }

    final override fun decodeChar(): Char {
        return element.asTomlLiteral().toChar()
    }

    final override fun decodeString(): String {
        return element.asTomlLiteral().content
    }

    final override fun decodeNull(): Nothing? {
        return element.asTomlNull().content
    }

    final override fun decodeNotNullMark(): Boolean {
        return element != TomlNull
    }

    final override fun decodeTomlElement(): TomlElement {
        return element
    }

    final override fun decodeInline(descriptor: SerialDescriptor): Decoder {
        return if (descriptor.isUnsignedInteger) {
            TomlElementInlineDecoder(this)
        } else {
            this
        }
    }

    final override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        return enumDescriptor.getElementIndex(element.asTomlLiteral().content)
    }

    final override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val discriminator = currentDiscriminator
        currentDiscriminator = null
        return when (val kind = descriptor.kind) {
            CLASS, is PolymorphicKind, OBJECT -> {
                TomlElementClassDecoder(
                    delegate = this,
                    table = element.asTomlTable(),
                    discriminator = discriminator
                )
            }
            LIST -> {
                TomlElementArrayDecoder(
                    delegate = this,
                    array = element.asTomlArray()
                )
            }
            MAP -> {
                TomlElementMapDecoder(
                    delegate = this,
                    table = element.asTomlTable()
                )
            }
            else -> throwUnsupportedSerialKind(kind)
        }
    }
}

// -------- TomlElementDecoder --------

internal class TomlElementDecoder(
    toml: Toml,
    override val element: TomlElement
) : AbstractTomlElementDecoder(toml)

// -------- TomlElementInlineDecoder --------

private class TomlElementInlineDecoder(
    delegate: AbstractTomlElementDecoder
) : AbstractTomlInlineDecoder<AbstractTomlElementDecoder>(delegate) {
    override fun decodeByte(): Byte {
        return delegate.element.asTomlLiteral().content.toUByte().toByte()
    }

    override fun decodeShort(): Short {
        return delegate.element.asTomlLiteral().content.toUShort().toShort()
    }

    override fun decodeInt(): Int {
        return delegate.element.asTomlLiteral().content.toUInt().toInt()
    }

    override fun decodeLong(): Long {
        return delegate.element.asTomlLiteral().content.toULong().toLong()
    }
}

// -------- AbstractTomlElementCompositeDecoder --------

private abstract class AbstractTomlElementCompositeDecoder(
    delegate: AbstractTomlElementDecoder
) : AbstractTomlElementDecoder(delegate.toml), TomlCompositeDecoder {
    final override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
        return if (descriptor.getElementDescriptor(index).isUnsignedInteger) {
            TomlElementInlineElementDecoder(
                parentDescriptor = descriptor,
                elementIndex = index,
                delegate = this
            )
        } else {
            this
        }
    }

    final override fun <T : Any> decodeNullableSerializableElement(
        descriptor: SerialDescriptor,
        index: Int,
        deserializer: DeserializationStrategy<T?>,
        previousValue: T?
    ): T? {
        return decodeElement(descriptor, index) {
            if (element == TomlNull) {
                null
            } else {
                decodeSerializableValue(deserializer)
            }
        }
    }

    override fun endStructure(descriptor: SerialDescriptor) {}
}

// -------- TomlElementInlineElementDecoder --------

private class TomlElementInlineElementDecoder(
    parentDescriptor: SerialDescriptor,
    elementIndex: Int,
    delegate: AbstractTomlElementCompositeDecoder
) : AbstractTomlInlineElementDecoder<AbstractTomlElementCompositeDecoder>(parentDescriptor, elementIndex, delegate) {
    override fun decodeByte(): Byte {
        return if (!decodedNotNullMark) {
            delegate.decodeElement(parentDescriptor, elementIndex) {
                delegate.element.asTomlLiteral().content.toUByte().toByte()
            }
        } else {
            val value = delegate.element.asTomlLiteral().content.toUByte().toByte()
            decodedNotNullMark = false
            value
        }
    }

    override fun decodeShort(): Short {
        return if (!decodedNotNullMark) {
            delegate.decodeElement(parentDescriptor, elementIndex) {
                delegate.element.asTomlLiteral().content.toUShort().toShort()
            }
        } else {
            val value = delegate.element.asTomlLiteral().content.toUShort().toShort()
            decodedNotNullMark = false
            value
        }
    }

    override fun decodeInt(): Int {
        return if (!decodedNotNullMark) {
            delegate.decodeElement(parentDescriptor, elementIndex) {
                delegate.element.asTomlLiteral().content.toUInt().toInt()
            }
        } else {
            val value = delegate.element.asTomlLiteral().content.toUInt().toInt()
            decodedNotNullMark = false
            value
        }
    }

    override fun decodeLong(): Long {
        return if (!decodedNotNullMark) {
            delegate.decodeElement(parentDescriptor, elementIndex) {
                delegate.element.asTomlLiteral().content.toULong().toLong()
            }
        } else {
            val value = delegate.element.asTomlLiteral().content.toULong().toLong()
            decodedNotNullMark = false
            value
        }
    }
}

// -------- TomlElementClassDecoder --------

private class TomlElementClassDecoder(
    delegate: AbstractTomlElementDecoder,
    private val table: TomlTable,
    private val discriminator: String?
) : AbstractTomlElementCompositeDecoder(delegate) {
    private var allowImplicitNull: Boolean = false

    private var currentElementIndex: Int = 0

    override lateinit var element: TomlElement

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        while (currentElementIndex < descriptor.elementsCount) {
            val index = currentElementIndex
            val key = descriptor.getElementName(index)
            allowImplicitNull = false
            currentElementIndex++
            when {
                key in table -> {
                    element = table[key]!!
                    return index
                }
                allowImplicitNull(descriptor, index) -> {
                    element = TomlNull
                    return index
                }
            }
        }
        return DECODE_DONE
    }

    private fun allowImplicitNull(
        descriptor: SerialDescriptor,
        index: Int
    ): Boolean {
        val allow = toml.config.explicitNulls.not() &&
                descriptor.isElementOptional(index).not() &&
                descriptor.getElementDescriptor(index).isNullable
        allowImplicitNull = allow
        return allow
    }

    override fun beginElement(descriptor: SerialDescriptor, index: Int) {}

    override fun endElement(descriptor: SerialDescriptor, index: Int) {}

    override fun endStructure(descriptor: SerialDescriptor) {
        if (toml.config.ignoreUnknownKeys) {
            return
        }
        // Validate keys.
        val expectedKeys = descriptor.elementNames.toSet()
        for (key in table.keys) {
            if (key !in expectedKeys && key != discriminator) {
                throwUnknownKey(key)
            }
        }
    }
}

// -------- TomlElementMapDecoder --------

private class TomlElementMapDecoder(
    delegate: AbstractTomlElementDecoder,
    private val table: TomlTable
) : AbstractTomlElementCompositeDecoder(delegate) {
    private val iterator: Iterator<TomlElement> = iterator {
        for ((k, v) in table) {
            yield(TomlLiteral(k))
            yield(v)
        }
    }

    private var currentElementIndex: Int = 0

    override lateinit var element: TomlElement

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (iterator.hasNext()) {
            currentElementIndex
        } else {
            DECODE_DONE
        }
    }

    override fun beginElement(descriptor: SerialDescriptor, index: Int) {
        element = iterator.next()
    }

    override fun endElement(descriptor: SerialDescriptor, index: Int) {
        currentElementIndex++
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        return table.size
    }

    override fun decodeSequentially(): Boolean {
        return true
    }
}

// -------- TomlElementArrayDecoder --------

private class TomlElementArrayDecoder(
    delegate: AbstractTomlElementDecoder,
    private val array: TomlArray
) : AbstractTomlElementCompositeDecoder(delegate) {
    private var currentElementIndex: Int = 0

    override lateinit var element: TomlElement

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return if (currentElementIndex != array.size) {
            currentElementIndex
        } else {
            DECODE_DONE
        }
    }

    override fun beginElement(descriptor: SerialDescriptor, index: Int) {
        element = array[currentElementIndex]
    }

    override fun endElement(descriptor: SerialDescriptor, index: Int) {
        currentElementIndex++
    }

    override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
        return array.size
    }

    override fun decodeSequentially(): Boolean {
        return true
    }
}
