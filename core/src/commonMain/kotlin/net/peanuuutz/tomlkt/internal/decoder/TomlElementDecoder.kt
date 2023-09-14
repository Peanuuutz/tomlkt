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
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.CompositeDecoder.Companion.UNKNOWN_NAME
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlConfig
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

internal abstract class AbstractTomlElementDecoder(
    config: TomlConfig,
    serializersModule: SerializersModule
) : AbstractTomlDecoder(config, serializersModule) {
    abstract val element: TomlElement

    override fun decodeBoolean(): Boolean {
        return element.asTomlLiteral().toBoolean()
    }

    override fun decodeByte(): Byte {
        return element.asTomlLiteral().toByte()
    }

    override fun decodeShort(): Short {
        return element.asTomlLiteral().toShort()
    }

    override fun decodeInt(): Int {
        return element.asTomlLiteral().toInt()
    }

    override fun decodeLong(): Long {
        return element.asTomlLiteral().toLong()
    }

    override fun decodeFloat(): Float {
        return element.asTomlLiteral().toFloat()
    }

    override fun decodeDouble(): Double {
        return element.asTomlLiteral().toDouble()
    }

    override fun decodeChar(): Char {
        return element.asTomlLiteral().toChar()
    }

    override fun decodeString(): String {
        return element.asTomlLiteral().content
    }

    override fun decodeNull(): Nothing? {
        return element.asTomlNull().content
    }

    override fun decodeNotNullMark(): Boolean {
        return element != TomlNull
    }

    override fun decodeTomlElement(): TomlElement {
        return element
    }

    override fun decodeInline(descriptor: SerialDescriptor): Decoder {
        return if (descriptor.isUnsignedInteger) {
            TomlElementInlineDecoder(this)
        } else {
            this
        }
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        return enumDescriptor.getElementIndex(element.asTomlLiteral().content)
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        val discriminator = currentDiscriminator
        currentDiscriminator = null
        return when (val kind = descriptor.kind) {
            CLASS, is PolymorphicKind, OBJECT -> {
                TomlElementClassDecoder(
                    table = element.asTomlTable(),
                    delegate = this,
                    discriminator = discriminator
                )
            }
            LIST -> {
                TomlElementArrayDecoder(
                    array = element.asTomlArray(),
                    delegate = this
                )
            }
            MAP -> {
                TomlElementMapDecoder(
                    table = element.asTomlTable(),
                    delegate = this
                )
            }
            else -> throwUnsupportedSerialKind(kind)
        }
    }
}

// -------- TomlElementDecoder --------

internal class TomlElementDecoder(
    config: TomlConfig,
    serializersModule: SerializersModule,
    override val element: TomlElement
) : AbstractTomlElementDecoder(config, serializersModule)

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
) : AbstractTomlElementDecoder(delegate.config, delegate.serializersModule), TomlCompositeDecoder {
    override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
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

    override fun <T : Any> decodeNullableSerializableElement(
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
    table: TomlTable,
    delegate: AbstractTomlElementDecoder,
    private val discriminator: String?
) : AbstractTomlElementCompositeDecoder(delegate) {
    private val iterator: Iterator<Map.Entry<String, TomlElement>> = table.iterator()

    private var consumedElementCount: Int = 0

    private var currentElementIndex: Int = 0

    override lateinit var element: TomlElement

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        return when {
            consumedElementCount < descriptor.elementsCount -> decodeBeforeFinished(descriptor)
            iterator.hasNext() -> tryDecodeAfterFinished(descriptor)
            else -> DECODE_DONE
        }
    }

    private fun decodeBeforeFinished(descriptor: SerialDescriptor): Int {
        if (iterator.hasNext().not()) {
            return DECODE_DONE
        }
        val (key, value) = iterator.next()
        if (key == discriminator) {
            return decodeElementIndex(descriptor)
        }
        val index = descriptor.getElementIndex(key)
        if (index == UNKNOWN_NAME && config.ignoreUnknownKeys.not()) {
            throwUnknownKey(key)
        }
        currentElementIndex = index
        element = value
        return index
    }

    private fun tryDecodeAfterFinished(descriptor: SerialDescriptor): Int {
        val key = iterator.next().key
        if (key != discriminator && config.ignoreUnknownKeys.not()) {
            throwUnknownKey(key)
        }
        return decodeElementIndex(descriptor)
    }

    override fun beginElement(descriptor: SerialDescriptor, index: Int) {}

    override fun endElement(descriptor: SerialDescriptor, index: Int) {
        consumedElementCount++
    }
}

// -------- TomlElementMapDecoder --------

private class TomlElementMapDecoder(
    private val table: TomlTable,
    delegate: AbstractTomlElementDecoder
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
    private val array: TomlArray,
    delegate: AbstractTomlElementDecoder,
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
