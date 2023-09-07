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
import net.peanuuutz.tomlkt.TomlEncoder
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlSpecific
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.toTomlKey

@OptIn(TomlSpecific::class)
internal class TomlElementEncoder(
    private val config: TomlConfig,
    override val serializersModule: SerializersModule
) : TomlEncoder {
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
            InlineEncoder(
                elementConsumer = this::element::set,
                delegate = this
            )
        } else {
            this
        }
    }

    override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
        encodeString(enumDescriptor.getElementName(index))
    }

    override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
        return beginStructure(descriptor, this::element::set)
    }

    private fun beginStructure(
        descriptor: SerialDescriptor,
        elementConsumer: (TomlElement) -> Unit
    ) : CompositeEncoder {
        return when (val kind = descriptor.kind) {
            CLASS, OBJECT -> {
                val builder = mutableMapOf<String, TomlElement>()
                elementConsumer(TomlTable(builder))
                ClassEncoder(builder)
            }
            LIST -> {
                val builder = mutableListOf<TomlElement>()
                elementConsumer(TomlArray(builder))
                ArrayEncoder(builder)
            }
            MAP -> {
                val builder = mutableMapOf<String, TomlElement>()
                elementConsumer(TomlTable(builder))
                MapEncoder(builder)
            }
            else -> throwUnsupportedSerialKind(kind)
        }
    }

    private class InlineEncoder(
        private val elementConsumer: (TomlElement) -> Unit,
        private val delegate: TomlEncoder
    ) : TomlEncoder by delegate {
        override fun encodeByte(value: Byte) {
            elementConsumer(TomlLiteral(value.toUByte()))
        }

        override fun encodeShort(value: Short) {
            elementConsumer(TomlLiteral(value.toUShort()))
        }

        override fun encodeInt(value: Int) {
            elementConsumer(TomlLiteral(value.toUInt()))
        }

        override fun encodeLong(value: Long) {
            elementConsumer(TomlLiteral(value.toULong()))
        }

        override fun encodeInline(descriptor: SerialDescriptor): Encoder {
            return if (descriptor.isUnsignedInteger) this else delegate
        }
    }

    private abstract inner class AbstractEncoder : TomlEncoder, CompositeEncoder {
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

        final override fun encodeInline(descriptor: SerialDescriptor): Encoder {
            return if (descriptor.isUnsignedInteger) {
                InlineEncoder(
                    elementConsumer = this::currentElement::set,
                    delegate = this
                )
            } else {
                this
            }
        }

        final override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
            encodeString(enumDescriptor.getElementName(index))
        }

        final override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
            return beginStructure(descriptor, this::currentElement::set)
        }

        final override fun encodeBooleanElement(descriptor: SerialDescriptor, index: Int, value: Boolean) {
            encodeElement(descriptor, index) {
                encodeBoolean(value)
            }
        }

        final override fun encodeByteElement(descriptor: SerialDescriptor, index: Int, value: Byte) {
            encodeElement(descriptor, index) {
                encodeByte(value)
            }
        }

        final override fun encodeShortElement(descriptor: SerialDescriptor, index: Int, value: Short) {
            encodeElement(descriptor, index) {
                encodeShort(value)
            }
        }

        final override fun encodeIntElement(descriptor: SerialDescriptor, index: Int, value: Int) {
            encodeElement(descriptor, index) {
                encodeInt(value)
            }
        }

        final override fun encodeLongElement(descriptor: SerialDescriptor, index: Int, value: Long) {
            encodeElement(descriptor, index) {
                encodeLong(value)
            }
        }

        final override fun encodeFloatElement(descriptor: SerialDescriptor, index: Int, value: Float) {
            encodeElement(descriptor, index) {
                encodeFloat(value)
            }
        }

        final override fun encodeDoubleElement(descriptor: SerialDescriptor, index: Int, value: Double) {
            encodeElement(descriptor, index) {
                encodeDouble(value)
            }
        }

        final override fun encodeCharElement(descriptor: SerialDescriptor, index: Int, value: Char) {
            encodeElement(descriptor, index) {
                encodeChar(value)
            }
        }

        final override fun encodeStringElement(descriptor: SerialDescriptor, index: Int, value: String) {
            encodeElement(descriptor, index) {
                encodeString(value)
            }
        }

        final override fun encodeInlineElement(descriptor: SerialDescriptor, index: Int): Encoder {
            return if (descriptor.getElementDescriptor(index).isUnsignedInteger) {
                InlineElementEncoder(
                    parentDescriptor = descriptor,
                    elementIndex = index
                )
            } else {
                this
            }
        }

        final override fun <T : Any> encodeNullableSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T?
        ) {
            if (value == null) {
                encodeSerializableElement(descriptor, index, TomlNull.serializer(), TomlNull)
            } else {
                encodeSerializableElement(descriptor, index, serializer, value)
            }
        }

        override fun <T> encodeSerializableElement(
            descriptor: SerialDescriptor,
            index: Int,
            serializer: SerializationStrategy<T>,
            value: T
        ) {
            encodeElement(descriptor, index) {
                serializer.serialize(this, value)
            }
        }

        protected inline fun encodeElement(
            descriptor: SerialDescriptor,
            index: Int,
            block: () -> Unit
        ) {
            beginElement(descriptor, index)
            block()
            endElement(descriptor, index)
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

        private inner class InlineElementEncoder(
            private val parentDescriptor: SerialDescriptor,
            private val elementIndex: Int
        ) : TomlEncoder {
            override val serializersModule: SerializersModule
                get() = this@AbstractEncoder.serializersModule

            override fun encodeBoolean(value: Boolean) {
                encodeBooleanElement(parentDescriptor, elementIndex, value)
            }

            override fun encodeByte(value: Byte) {
                encodeElement(parentDescriptor, elementIndex) {
                    currentElement = TomlLiteral(value.toUByte())
                }
            }

            override fun encodeShort(value: Short) {
                encodeElement(parentDescriptor, elementIndex) {
                    currentElement = TomlLiteral(value.toUShort())
                }
            }

            override fun encodeInt(value: Int) {
                encodeElement(parentDescriptor, elementIndex) {
                    currentElement = TomlLiteral(value.toUInt())
                }
            }

            override fun encodeLong(value: Long) {
                encodeElement(parentDescriptor, elementIndex) {
                    currentElement = TomlLiteral(value.toULong())
                }
            }

            override fun encodeFloat(value: Float) {
                encodeFloatElement(parentDescriptor, elementIndex, value)
            }

            override fun encodeDouble(value: Double) {
                encodeDoubleElement(parentDescriptor, elementIndex, value)
            }

            override fun encodeChar(value: Char) {
                encodeCharElement(parentDescriptor, elementIndex, value)
            }

            override fun encodeString(value: String) {
                encodeStringElement(parentDescriptor, elementIndex, value)
            }

            override fun encodeNull() {
                encodeElement(parentDescriptor, elementIndex) {
                    currentElement = TomlNull
                }
            }

            override fun encodeTomlElement(value: TomlElement) {
                encodeElement(parentDescriptor, elementIndex) {
                    currentElement = value
                }
            }

            override fun encodeEnum(enumDescriptor: SerialDescriptor, index: Int) {
                encodeElement(parentDescriptor, elementIndex) {
                    this@AbstractEncoder.encodeEnum(enumDescriptor, index)
                }
            }

            override fun encodeInline(descriptor: SerialDescriptor): Encoder {
                return if (descriptor.isUnsignedInteger) this else this@AbstractEncoder
            }

            override fun beginStructure(descriptor: SerialDescriptor): CompositeEncoder {
                return this@AbstractEncoder.beginStructure(descriptor)
            }
        }
    }

    private inner class ClassEncoder(
        private val builder: MutableMap<String, TomlElement>
    ) : AbstractEncoder() {
        private lateinit var currentKey: String

        override fun beginElement(descriptor: SerialDescriptor, index: Int) {
            currentKey = descriptor.getElementName(index)
        }

        override fun endElement(descriptor: SerialDescriptor, index: Int) {
            builder[currentKey] = currentElement
        }
    }

    private inner class MapEncoder(
        private val builder: MutableMap<String, TomlElement>
    ) : AbstractEncoder() {
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
                serializer.serialize(this, value)
                builder[currentKey] = currentElement
            }
            isKey = !isKey
        }

        // This shouldn't be called.
        override fun beginElement(descriptor: SerialDescriptor, index: Int) {}

        // This shouldn't be called.
        override fun endElement(descriptor: SerialDescriptor, index: Int) {}
    }

    private inner class ArrayEncoder(
        private val builder: MutableList<TomlElement>
    ) : AbstractEncoder() {
        override fun beginElement(descriptor: SerialDescriptor, index: Int) {}

        override fun endElement(descriptor: SerialDescriptor, index: Int) {
            builder.add(currentElement)
        }
    }
}
