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

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.encoding.CompositeEncoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.modules.SerializersModule
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlConfig
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlEncoder
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlSpecific
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.toTomlKey

@OptIn(TomlSpecific::class)
internal class TomlElementEncoder(
    private val config: TomlConfig,
    override val serializersModule: SerializersModule = config.serializersModule
) : Encoder, TomlEncoder {
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

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }

    override fun encodeInline(descriptor: SerialDescriptor): Encoder {
        return this
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return beginStructure(descriptor, this::element::set)
    }

    private inline fun beginStructure(
        descriptor: SerialDescriptor,
        elementConsumer: (TomlElement) -> Unit
    ) : CompositeEncoder = when (descriptor.kind) {
        StructureKind.CLASS, StructureKind.OBJECT -> {
            val builder = mutableMapOf<String, TomlElement>()
            elementConsumer(TomlTable(builder))
            ClassEncoder(builder)
        }
        StructureKind.LIST -> {
            val builder = mutableListOf<TomlElement>()
            elementConsumer(TomlArray(builder))
            ArrayEncoder(builder)
        }
        StructureKind.MAP -> {
            val builder = mutableMapOf<String, TomlElement>()
            elementConsumer(TomlTable(builder))
            MapEncoder(builder)
        }
        else -> throw UnsupportedSerialKindException(descriptor.kind)
    }

    internal abstract inner class AbstractEncoder : Encoder, CompositeEncoder, TomlEncoder {
        lateinit var currentElement: TomlElement

        final override val serializersModule: SerializersModule
            get() = this@TomlElementEncoder.serializersModule

        final override fun encodeBoolean(value: Boolean) {
            currentElement = TomlLiteral(value)
        }

        final override fun encodeByte(value: Byte) {
            currentElement = TomlLiteral(value)
        }

        final override fun encodeShort(value: Short) {
            currentElement = TomlLiteral(value)
        }

        final override fun encodeInt(value: Int) {
            currentElement = TomlLiteral(value)
        }

        final override fun encodeLong(value: Long) {
            currentElement = TomlLiteral(value)
        }

        final override fun encodeFloat(value: Float) {
            currentElement = TomlLiteral(value)
        }

        final override fun encodeDouble(value: Double) {
            currentElement = TomlLiteral(value)
        }

        final override fun encodeChar(value: Char) {
            currentElement = TomlLiteral(value)
        }

        final override fun encodeString(value: String) {
            currentElement = TomlLiteral(value)
        }

        final override fun encodeNull() {
            currentElement = TomlNull
        }

        final override fun encodeTomlElement(value: TomlElement) {
            currentElement = value
        }

        final override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
            encodeString(enumDescriptor.getElementName(index))
        }

        final override fun encodeInline(descriptor: SerialDescriptor): Encoder {
            return this
        }

        final override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
            return beginStructure(descriptor, this::currentElement::set)
        }

        final override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
            encodeSerializableElement(descriptor, index, Boolean.serializer(), value)
        }

        final override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
            encodeSerializableElement(descriptor, index, Byte.serializer(), value)
        }

        final override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
            encodeSerializableElement(descriptor, index, Short.serializer(), value)
        }

        final override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
            encodeSerializableElement(descriptor, index, Int.serializer(), value)
        }

        final override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
            encodeSerializableElement(descriptor, index, Long.serializer(), value)
        }

        final override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
            encodeSerializableElement(descriptor, index, Float.serializer(), value)
        }

        final override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
            encodeSerializableElement(descriptor, index, Double.serializer(), value)
        }

        final override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
            encodeSerializableElement(descriptor, index, Char.serializer(), value)
        }

        final override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
            encodeSerializableElement(descriptor, index, String.serializer(), value)
        }

        final override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
            TODO("Not yet implemented")
        }

        final override fun <T : Any> encodeNullableSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T?
        ) {
            if (value == null) {
                currentElement = TomlNull
            } else {
                encodeSerializableElement(descriptor, index, serializer, value)
            }
        }

        final override fun endStructure(descriptor: SerialDescriptor) {}
    }

    internal inner class ArrayEncoder(
        private val builder: MutableList<TomlElement>
    ) : AbstractEncoder() {
        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            serializer.serialize(this, value)
            builder.add(currentElement)
        }
    }

    internal inner class ClassEncoder(
        private val builder: MutableMap<String, TomlElement>
    ) : AbstractEncoder() {
        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            val key = descriptor.getElementName(index)
            serializer.serialize(this, value)
            builder[key] = currentElement
        }
    }

    internal inner class MapEncoder(
        private val builder: MutableMap<String, TomlElement>
    ) : AbstractEncoder() {
        private var isKey: Boolean = true

        private lateinit var key: String

        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            if (isKey) {
                key = value.toTomlKey()
            } else {
                serializer.serialize(this, value)
                builder[key] = currentElement
            }
            isKey = !isKey
        }
    }
}
