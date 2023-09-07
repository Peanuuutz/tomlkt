/*
    Copyright 2022 Peanuuutz

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

package net.peanuuutz.tomlkt.internal

import kotlinx.serialization.DeserializationStrategy
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
import net.peanuuutz.tomlkt.TomlDecoder
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlSpecific
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.toBoolean
import net.peanuuutz.tomlkt.toByte
import net.peanuuutz.tomlkt.toChar
import net.peanuuutz.tomlkt.toDouble
import net.peanuuutz.tomlkt.toFloat
import net.peanuuutz.tomlkt.toInt
import net.peanuuutz.tomlkt.toLong
import net.peanuuutz.tomlkt.toShort
import net.peanuuutz.tomlkt.toTomlArray
import net.peanuuutz.tomlkt.toTomlLiteral
import net.peanuuutz.tomlkt.toTomlNull
import net.peanuuutz.tomlkt.toTomlTable

@OptIn(TomlSpecific::class)
internal class TomlElementDecoder(
    private val config: TomlConfig,
    override val serializersModule: SerializersModule,
    private val element: TomlElement
) : TomlDecoder {
    override fun decodeBoolean(): Boolean {
        return element.toTomlLiteral().toBoolean()
    }

    override fun decodeByte(): Byte {
        return element.toTomlLiteral().toByte()
    }

    override fun decodeShort(): Short {
        return element.toTomlLiteral().toShort()
    }

    override fun decodeInt(): Int {
        return element.toTomlLiteral().toInt()
    }

    override fun decodeLong(): Long {
        return element.toTomlLiteral().toLong()
    }

    override fun decodeFloat(): Float {
        return element.toTomlLiteral().toFloat()
    }

    override fun decodeDouble(): Double {
        return element.toTomlLiteral().toDouble()
    }

    override fun decodeChar(): Char {
        return element.toTomlLiteral().toChar()
    }

    override fun decodeString(): String {
        return element.toTomlLiteral().content
    }

    override fun decodeNull(): Nothing? {
        return element.toTomlNull().content
    }

    override fun decodeNotNullMark(): Boolean {
        return element != TomlNull
    }

    override fun decodeTomlElement(): TomlElement {
        return element
    }

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
        return enumDescriptor.getElementIndex(element.toTomlLiteral().content)
    }

    override fun decodeInline(descriptor: SerialDescriptor): Decoder {
        return if (descriptor.isUnsignedInteger) {
            InlineDecoder(
                elementProvider = this::element,
                delegate = this
            )
        } else {
            this
        }
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return when (val kind = descriptor.kind) {
            CLASS -> ClassDecoder(element.toTomlTable())
            OBJECT -> ClassDecoder(element.toTomlTable())
            LIST -> ArrayDecoder(element.toTomlArray())
            MAP -> MapDecoder(element.toTomlTable())
            else -> throwUnsupportedSerialKind(kind)
        }
    }

    private class InlineDecoder(
        private val elementProvider: () -> TomlElement,
        private val delegate: TomlDecoder
    ) : TomlDecoder by delegate {
        override fun decodeByte(): Byte {
            return elementProvider().toTomlLiteral().content.toUByte().toByte()
        }

        override fun decodeShort(): Short {
            return elementProvider().toTomlLiteral().content.toUShort().toShort()
        }

        override fun decodeInt(): Int {
            return elementProvider().toTomlLiteral().content.toUInt().toInt()
        }

        override fun decodeLong(): Long {
            return elementProvider().toTomlLiteral().content.toULong().toLong()
        }

        override fun decodeInline(descriptor: SerialDescriptor): Decoder {
            return if (descriptor.isUnsignedInteger) this else delegate
        }
    }

    private abstract inner class AbstractDecoder : TomlDecoder, CompositeDecoder {
        protected abstract val currentElement: TomlElement

        final override val serializersModule: SerializersModule
            get() = this@TomlElementDecoder.serializersModule

        final override fun decodeBoolean(): Boolean {
            return currentElement.toTomlLiteral().toBoolean()
        }

        final override fun decodeByte(): Byte {
            return currentElement.toTomlLiteral().toByte()
        }

        final override fun decodeShort(): Short {
            return currentElement.toTomlLiteral().toShort()
        }

        final override fun decodeInt(): Int {
            return currentElement.toTomlLiteral().toInt()
        }

        final override fun decodeLong(): Long {
            return currentElement.toTomlLiteral().toLong()
        }

        final override fun decodeFloat(): Float {
            return currentElement.toTomlLiteral().toFloat()
        }

        final override fun decodeDouble(): Double {
            return currentElement.toTomlLiteral().toDouble()
        }

        final override fun decodeChar(): Char {
            return currentElement.toTomlLiteral().toChar()
        }

        final override fun decodeString(): String {
            return currentElement.toTomlLiteral().content
        }

        final override fun decodeNull(): Nothing? {
            return currentElement.toTomlNull().content
        }

        final override fun decodeNotNullMark(): Boolean {
            return currentElement != TomlNull
        }

        final override fun decodeTomlElement(): TomlElement {
            return currentElement
        }

        final override fun decodeInline(descriptor: SerialDescriptor): Decoder {
            return if (descriptor.isUnsignedInteger) {
                InlineDecoder(
                    elementProvider = this::currentElement,
                    delegate = this
                )
            } else {
                this
            }
        }

        final override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
            return enumDescriptor.getElementIndex(currentElement.toTomlLiteral().content)
        }

        override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
            return when (val kind = descriptor.kind) {
                CLASS -> ClassDecoder(currentElement.toTomlTable())
                OBJECT -> ClassDecoder(currentElement.toTomlTable())
                LIST -> ArrayDecoder(currentElement.toTomlArray())
                MAP -> MapDecoder(currentElement.toTomlTable())
                else -> throwUnsupportedSerialKind(kind)
            }
        }

        final override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
            return decodeElement(descriptor, index) {
                decodeBoolean()
            }
        }

        final override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
            return decodeElement(descriptor, index) {
                decodeByte()
            }
        }

        final override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
            return decodeElement(descriptor, index) {
                decodeShort()
            }
        }

        final override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
            return decodeElement(descriptor, index) {
                decodeInt()
            }
        }

        final override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
            return decodeElement(descriptor, index) {
                decodeLong()
            }
        }

        final override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
            return decodeElement(descriptor, index) {
                decodeFloat()
            }
        }

        final override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
            return decodeElement(descriptor, index) {
                decodeDouble()
            }
        }

        final override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
            return decodeElement(descriptor, index) {
                decodeChar()
            }
        }

        final override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
            return decodeElement(descriptor, index) {
                decodeString()
            }
        }

        final override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
            return if (descriptor.getElementDescriptor(index).isUnsignedInteger) {
                InlineElementDecoder(
                    parentDescriptor = descriptor,
                    elementIndex = index
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
                if (currentElement == TomlNull) {
                    null
                } else {
                    deserializer.deserialize(this)
                }
            }
        }

        override fun <T> decodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            deserializer: DeserializationStrategy<T>,
            previousValue: T?
        ): T {
            return decodeElement(descriptor, index) {
                deserializer.deserialize(this)
            }
        }

        protected inline fun <T> decodeElement(
            descriptor: SerialDescriptor,
            index: Int,
            block: () -> T
        ): T {
            beginElement(descriptor, index)
            val value = block()
            endElement(descriptor, index)
            return value
        }

        protected abstract fun beginElement(
            descriptor: SerialDescriptor,
            index: Int
        )

        protected abstract fun endElement(
            descriptor: SerialDescriptor,
            index: Int
        )

        final override fun endStructure(descriptor: SerialDescriptor) {}

        private inner class InlineElementDecoder(
            private val parentDescriptor: SerialDescriptor,
            private val elementIndex: Int
        ) : TomlDecoder {
            private var decodedNotNullMark: Boolean = false

            override val serializersModule: SerializersModule
                get() = this@AbstractDecoder.serializersModule

            override fun decodeBoolean(): Boolean {
                return if (!decodedNotNullMark) {
                    decodeBooleanElement(parentDescriptor, elementIndex)
                } else {
                    val value = this@AbstractDecoder.decodeBoolean()
                    decodedNotNullMark = false
                    value
                }
            }

            override fun decodeByte(): Byte {
                return if (!decodedNotNullMark) {
                    decodeElement(parentDescriptor, elementIndex) {
                        currentElement.toTomlLiteral().content.toUByte().toByte()
                    }
                } else {
                    val value = currentElement.toTomlLiteral().content.toUByte().toByte()
                    decodedNotNullMark = false
                    value
                }
            }

            override fun decodeShort(): Short {
                return if (!decodedNotNullMark) {
                    decodeElement(parentDescriptor, elementIndex) {
                        currentElement.toTomlLiteral().content.toUShort().toShort()
                    }
                } else {
                    val value = currentElement.toTomlLiteral().content.toUShort().toShort()
                    decodedNotNullMark = false
                    value
                }
            }

            override fun decodeInt(): Int {
                return if (!decodedNotNullMark) {
                    decodeElement(parentDescriptor, elementIndex) {
                        currentElement.toTomlLiteral().content.toUInt().toInt()
                    }
                } else {
                    val value = currentElement.toTomlLiteral().content.toUInt().toInt()
                    decodedNotNullMark = false
                    value
                }
            }

            override fun decodeLong(): Long {
                return if (!decodedNotNullMark) {
                    decodeElement(parentDescriptor, elementIndex) {
                        currentElement.toTomlLiteral().content.toULong().toLong()
                    }
                } else {
                    val value = currentElement.toTomlLiteral().content.toULong().toLong()
                    decodedNotNullMark = false
                    value
                }
            }

            override fun decodeFloat(): Float {
                return if (!decodedNotNullMark) {
                    decodeFloatElement(parentDescriptor, elementIndex)
                } else {
                    val value = this@AbstractDecoder.decodeFloat()
                    decodedNotNullMark = false
                    value
                }
            }

            override fun decodeDouble(): Double {
                return if (!decodedNotNullMark) {
                    decodeDoubleElement(parentDescriptor, elementIndex)
                } else {
                    val value = this@AbstractDecoder.decodeDouble()
                    decodedNotNullMark = false
                    value
                }
            }

            override fun decodeChar(): Char {
                return if (!decodedNotNullMark) {
                    decodeCharElement(parentDescriptor, elementIndex)
                } else {
                    val value = this@AbstractDecoder.decodeChar()
                    decodedNotNullMark = false
                    value
                }
            }

            override fun decodeString(): String {
                return if (!decodedNotNullMark) {
                    decodeStringElement(parentDescriptor, elementIndex)
                } else {
                    val value = this@AbstractDecoder.decodeString()
                    decodedNotNullMark = false
                    value
                }
            }

            override fun decodeNull(): Nothing? {
                return if (!decodedNotNullMark) {
                    decodeElement(parentDescriptor, elementIndex) {
                        this@AbstractDecoder.decodeNull()
                    }
                } else {
                    val value = this@AbstractDecoder.decodeNull()
                    decodedNotNullMark = false
                    value
                }
            }

            override fun decodeNotNullMark(): Boolean {
                return if (!decodedNotNullMark) {
                    val isNotNull = decodeElement(parentDescriptor, elementIndex) {
                        this@AbstractDecoder.decodeNotNullMark()
                    }
                    decodedNotNullMark = true
                    isNotNull
                } else {
                    this@AbstractDecoder.decodeNotNullMark()
                }
            }

            override fun decodeTomlElement(): TomlElement {
                return if (!decodedNotNullMark) {
                    decodeElement(parentDescriptor, elementIndex) {
                        this@AbstractDecoder.decodeTomlElement()
                    }
                } else {
                    val value = this@AbstractDecoder.decodeTomlElement()
                    decodedNotNullMark = false
                    value
                }
            }

            override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
                return if (!decodedNotNullMark) {
                    decodeElement(parentDescriptor, elementIndex) {
                        this@AbstractDecoder.decodeEnum(enumDescriptor)
                    }
                } else {
                    val value = this@AbstractDecoder.decodeEnum(enumDescriptor)
                    decodedNotNullMark = false
                    value
                }
            }

            override fun decodeInline(descriptor: SerialDescriptor): Decoder {
                return if (!decodedNotNullMark) {
                    decodeElement(parentDescriptor, elementIndex) {
                        if (descriptor.isUnsignedInteger) this else this@AbstractDecoder
                    }
                } else {
                    val decoder = if (descriptor.isUnsignedInteger) this else this@AbstractDecoder
                    decodedNotNullMark = false
                    decoder
                }
            }

            override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
                return if (!decodedNotNullMark) {
                    decodeElement(parentDescriptor, elementIndex) {
                        this@AbstractDecoder.beginStructure(descriptor)
                    }
                } else {
                    val decoder = this@AbstractDecoder.beginStructure(descriptor)
                    decodedNotNullMark = false
                    decoder
                }
            }
        }
    }

    private inner class ClassDecoder(
        table: TomlTable
    ) : AbstractDecoder() {
        private val iterator: Iterator<Map.Entry<String, TomlElement>> = table.iterator()

        private var consumedElementCount: Int = 0

        private var currentElementIndex: Int = 0

        override lateinit var currentElement: TomlElement

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            return when {
                consumedElementCount < descriptor.elementsCount -> {
                    if (iterator.hasNext()) {
                        val (key, value) = iterator.next()
                        currentElement = value
                        val index = descriptor.getElementIndex(key)
                        if (index == UNKNOWN_NAME && config.ignoreUnknownKeys.not()) {
                            throwUnknownKey(key)
                        }
                        currentElementIndex = index
                        index
                    } else {
                        DECODE_DONE
                    }
                }
                iterator.hasNext() && config.ignoreUnknownKeys.not() -> {
                    throwUnknownKey(iterator.next().key)
                }
                else -> DECODE_DONE
            }
        }

        override fun beginElement(descriptor: SerialDescriptor, index: Int) {}

        override fun endElement(descriptor: SerialDescriptor, index: Int) {
            consumedElementCount++
        }
    }

    private inner class MapDecoder(
        private val table: TomlTable
    ) : AbstractDecoder() {
        private val iterator: Iterator<TomlElement> = iterator {
            for ((k, v) in table) {
                yield(TomlLiteral(k))
                yield(v)
            }
        }

        private var currentElementIndex: Int = 0

        override lateinit var currentElement: TomlElement

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            return if (iterator.hasNext()) {
                currentElementIndex
            } else {
                DECODE_DONE
            }
        }

        override fun beginElement(descriptor: SerialDescriptor, index: Int) {
            currentElement = iterator.next()
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

    private inner class ArrayDecoder(
        private val array: TomlArray
    ) : AbstractDecoder() {
        private var currentElementIndex: Int = 0

        override lateinit var currentElement: TomlElement

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            return if (currentElementIndex != array.size) {
                currentElementIndex
            } else {
                DECODE_DONE
            }
        }

        override fun beginElement(descriptor: SerialDescriptor, index: Int) {
            currentElement = array[currentElementIndex]
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
}
