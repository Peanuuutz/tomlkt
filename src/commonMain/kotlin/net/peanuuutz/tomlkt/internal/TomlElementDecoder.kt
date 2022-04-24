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
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.modules.SerializersModule
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.toTomlLiteral
import net.peanuuutz.tomlkt.toTomlArray
import net.peanuuutz.tomlkt.toTomlTable
import net.peanuuutz.tomlkt.toBoolean
import net.peanuuutz.tomlkt.toByte
import net.peanuuutz.tomlkt.toShort
import net.peanuuutz.tomlkt.toInt
import net.peanuuutz.tomlkt.toLong
import net.peanuuutz.tomlkt.toFloat
import net.peanuuutz.tomlkt.toDouble
import net.peanuuutz.tomlkt.toChar
import net.peanuuutz.tomlkt.toTomlNull
import net.peanuuutz.tomlkt.TomlConfig

internal class TomlElementDecoder(
    private val config: TomlConfig,
    override val serializersModule: SerializersModule = config.serializersModule,
    private val element: TomlElement
) : Decoder, TomlDecoder {
    override fun decodeBoolean(): Boolean = element.toTomlLiteral().toBoolean()
    override fun decodeByte(): Byte = element.toTomlLiteral().toByte()
    override fun decodeShort(): Short = element.toTomlLiteral().toShort()
    override fun decodeInt(): Int = element.toTomlLiteral().toInt()
    override fun decodeLong(): Long = element.toTomlLiteral().toLong()
    override fun decodeFloat(): Float = element.toTomlLiteral().toFloat()
    override fun decodeDouble(): Double = element.toTomlLiteral().toDouble()
    override fun decodeChar(): Char = element.toTomlLiteral().toChar()
    override fun decodeString(): String = element.toTomlLiteral().content
    override fun decodeNull(): Nothing? = element.toTomlNull().content
    override fun decodeNotNullMark(): Boolean = element != TomlNull
    override fun decodeTomlElement(): TomlElement = element // Internal

    override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = enumDescriptor.getElementIndex(element.toTomlLiteral().content)
    override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder = this

    override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = when (descriptor.kind) {
        StructureKind.CLASS -> ClassDecoder(element.toTomlTable())
        StructureKind.LIST -> ArrayDecoder(element.toTomlArray())
        StructureKind.MAP -> MapDecoder(element.toTomlTable())
        else -> throw UnsupportedSerialKindException(descriptor.kind)
    }

    internal abstract inner class AbstractDecoder : Decoder, CompositeDecoder, TomlDecoder {
        protected var elementIndex: Int = 0

        protected abstract val currentElement: TomlElement

        final override val serializersModule: SerializersModule = this@TomlElementDecoder.serializersModule

        final override fun decodeBoolean(): Boolean = currentElement.toTomlLiteral().toBoolean()
        final override fun decodeByte(): Byte = currentElement.toTomlLiteral().toByte()
        final override fun decodeShort(): Short = currentElement.toTomlLiteral().toShort()
        final override fun decodeInt(): Int = currentElement.toTomlLiteral().toInt()
        final override fun decodeLong(): Long = currentElement.toTomlLiteral().toLong()
        final override fun decodeFloat(): Float = currentElement.toTomlLiteral().toFloat()
        final override fun decodeDouble(): Double = currentElement.toTomlLiteral().toDouble()
        final override fun decodeChar(): Char = currentElement.toTomlLiteral().toChar()
        final override fun decodeString(): String = currentElement.toTomlLiteral().content
        final override fun decodeNull(): Nothing? = currentElement.toTomlNull().content
        final override fun decodeNotNullMark(): Boolean = currentElement != TomlNull
        final override fun decodeTomlElement(): TomlElement = currentElement // Internal

        final override fun decodeEnum(enumDescriptor: SerialDescriptor): Int = enumDescriptor.getElementIndex(currentElement.toTomlLiteral().content)
        final override fun decodeInline(inlineDescriptor: SerialDescriptor): Decoder = this

        override fun beginStructure(descriptor: SerialDescriptor): CompositeDecoder = when (descriptor.kind) {
            StructureKind.CLASS -> ClassDecoder(currentElement.toTomlTable())
            StructureKind.LIST -> ArrayDecoder(currentElement.toTomlArray())
            StructureKind.MAP -> MapDecoder(currentElement.toTomlTable())
            else -> throw UnsupportedSerialKindException(descriptor.kind)
        }

        final override fun decodeBooleanElement(descriptor: SerialDescriptor, index: Int): Boolean = decodeSerializableElement(descriptor, index, Boolean.serializer())
        final override fun decodeByteElement(descriptor: SerialDescriptor, index: Int): Byte = decodeSerializableElement(descriptor, index, Byte.serializer())
        final override fun decodeShortElement(descriptor: SerialDescriptor, index: Int): Short = decodeSerializableElement(descriptor, index, Short.serializer())
        final override fun decodeIntElement(descriptor: SerialDescriptor, index: Int): Int = decodeSerializableElement(descriptor, index, Int.serializer())
        final override fun decodeLongElement(descriptor: SerialDescriptor, index: Int): Long = decodeSerializableElement(descriptor, index, Long.serializer())
        final override fun decodeFloatElement(descriptor: SerialDescriptor, index: Int): Float = decodeSerializableElement(descriptor, index, Float.serializer())
        final override fun decodeDoubleElement(descriptor: SerialDescriptor, index: Int): Double = decodeSerializableElement(descriptor, index, Double.serializer())
        final override fun decodeCharElement(descriptor: SerialDescriptor, index: Int): Char = decodeSerializableElement(descriptor, index, Char.serializer())
        final override fun decodeStringElement(descriptor: SerialDescriptor, index: Int): String = decodeSerializableElement(descriptor, index, String.serializer())

        final override fun decodeInlineElement(descriptor: SerialDescriptor, index: Int): Decoder = this

        final override fun <T : Any> decodeNullableSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            deserializer: DeserializationStrategy<T?>,
            previousValue: T?
        ): T? = if (currentElement == TomlNull) TomlNull.content else deserializer.deserialize(this)

        final override fun <T> decodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            deserializer: DeserializationStrategy<T>,
            previousValue: T?
        ): T = deserializer.deserialize(this)

        final override fun endStructure(descriptor: SerialDescriptor) {}
    }

    internal inner class ArrayDecoder(private val array: TomlArray) : AbstractDecoder() {
        override val currentElement: TomlElement get() = array[elementIndex++]

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int = if (elementIndex == array.size) CompositeDecoder.DECODE_DONE else elementIndex++

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = array.size

        override fun decodeSequentially(): Boolean = true
    }

    internal inner class ClassDecoder(table: TomlTable) : AbstractDecoder() {
        override lateinit var currentElement: TomlElement

        private val iterator: Iterator<Map.Entry<String, TomlElement>> = table.iterator()

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
            return if (elementIndex < descriptor.elementsCount) {
                if (iterator.hasNext()) {
                    val entry = iterator.next()
                    currentElement = entry.value
                    val index = descriptor.getElementIndex(entry.key)
                    if (index == CompositeDecoder.UNKNOWN_NAME && !config.ignoreUnknownKeys)
                        throw UnknownKeyException(entry.key)
                    elementIndex++
                    index
                } else CompositeDecoder.DECODE_DONE
            } else if (iterator.hasNext() && !config.ignoreUnknownKeys) {
                throw UnknownKeyException(iterator.next().key)
            } else CompositeDecoder.DECODE_DONE
        }
    }

    internal inner class MapDecoder(private val table: TomlTable) : AbstractDecoder() {
        private val iterator: Iterator<TomlElement> = iterator {
            table.forEach { (k, v) ->
                yield(TomlLiteral(k))
                yield(v)
            }
        }

        override val currentElement: TomlElement
            get() = iterator.next()

        override fun decodeElementIndex(descriptor: SerialDescriptor): Int = if (iterator.hasNext()) elementIndex++ else CompositeDecoder.DECODE_DONE

        override fun decodeCollectionSize(descriptor: SerialDescriptor): Int = table.size

        override fun decodeSequentially(): Boolean = true
    }
}