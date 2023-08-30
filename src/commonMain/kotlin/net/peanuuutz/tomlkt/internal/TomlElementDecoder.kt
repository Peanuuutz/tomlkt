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
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
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
) : Decoder, TomlDecoder {
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
        return this
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
        return when (descriptor.kind) {
            StructureKind.CLASS -> ClassDecoder(element.toTomlTable())
            StructureKind.OBJECT -> ClassDecoder(element.toTomlTable())
            StructureKind.LIST -> ArrayDecoder(element.toTomlArray())
            StructureKind.MAP -> MapDecoder(element.toTomlTable())
            else -> throw UnsupportedSerialKindException(descriptor.kind)
        }
    }

    internal abstract inner class AbstractDecoder : Decoder, CompositeDecoder, TomlDecoder {
        protected var elementIndex: Int = 0

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

        final override fun decodeEnum(enumDescriptor: SerialDescriptor): Int {
            return enumDescriptor.getElementIndex(currentElement.toTomlLiteral().content)
        }

        final override fun decodeInline(descriptor: SerialDescriptor): Decoder {
            return this
        }

        override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder {
            return when (descriptor.kind) {
                StructureKind.CLASS -> ClassDecoder(currentElement.toTomlTable())
                StructureKind.OBJECT -> ClassDecoder(currentElement.toTomlTable())
                StructureKind.LIST -> ArrayDecoder(currentElement.toTomlArray())
                StructureKind.MAP -> MapDecoder(currentElement.toTomlTable())
                else -> throw UnsupportedSerialKindException(descriptor.kind)
            }
        }

        final override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean {
            return decodeSerializableElement(descriptor, index, Boolean.serializer())
        }

        final override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte {
            return decodeSerializableElement(descriptor, index, Byte.serializer())
        }

        final override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short {
            return decodeSerializableElement(descriptor, index, Short.serializer())
        }

        final override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int {
            return decodeSerializableElement(descriptor, index, Int.serializer())
        }

        final override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long {
            return decodeSerializableElement(descriptor, index, Long.serializer())
        }

        final override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float {
            return decodeSerializableElement(descriptor, index, Float.serializer())
        }

        final override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double {
            return decodeSerializableElement(descriptor, index, Double.serializer())
        }

        final override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char {
            return decodeSerializableElement(descriptor, index, Char.serializer())
        }

        final override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String {
            return decodeSerializableElement(descriptor, index, String.serializer())
        }

        final override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder {
            TODO("Not yet implemented")
        }

        final override fun <T : Any> decodeNullableSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            deserializer: DeserializationStrategy<T?>,
            previousValue: T?
        ): T? {
            return if (currentElement == TomlNull) {
                TomlNull.content
            } else {
                deserializer.deserialize(this)
            }
        }

        final override fun <T> decodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            deserializer: DeserializationStrategy<T>,
            previousValue: T?
        ): T {
            return deserializer.deserialize(this)
        }

        final override fun endStructure(descriptor: SerialDescriptor) {}
    }

    internal inner class ArrayDecoder(
        private val array: TomlArray
    ) : AbstractDecoder() {
        override val currentElement: TomlElement
            get() = array[elementIndex++]

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            return if (elementIndex == array.size) {
                DECODE_DONE
            } else {
                elementIndex++
            }
        }

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
            return array.size
        }

        override fun decodeSequentially(): Boolean {
            return true
        }
    }

    internal inner class ClassDecoder(
        table: TomlTable
    ) : AbstractDecoder() {
        override lateinit var currentElement: TomlElement

        private val iterator: Iterator<Map.Entry<String, TomlElement>> = table.iterator()

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            return if (elementIndex < descriptor.elementsCount) {
                if (iterator.hasNext()) {
                    val entry = iterator.next()
                    currentElement = entry.value
                    val index = descriptor.getElementIndex(entry.key)
                    if (index == UNKNOWN_NAME && config.ignoreUnknownKeys.not()) {
                        throw UnknownKeyException(entry.key)
                    }
                    elementIndex++
                    index
                } else {
                    DECODE_DONE
                }
            } else if (iterator.hasNext() && config.ignoreUnknownKeys.not()) {
                throw UnknownKeyException(iterator.next().key)
            } else {
                DECODE_DONE
            }
        }
    }

    internal inner class MapDecoder(
        private val table: TomlTable
    ) : AbstractDecoder() {
        private val iterator: Iterator<TomlElement> = iterator {
            for ((k, v) in table) {
                yield(TomlLiteral(k))
                yield(v)
            }
        }

        override val currentElement: TomlElement
            get() = iterator.next()

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            return if (iterator.hasNext()) elementIndex++ else DECODE_DONE
        }

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int {
            return table.size
        }

        override fun decodeSequentially(): Boolean {
            return true
        }
    }
}
