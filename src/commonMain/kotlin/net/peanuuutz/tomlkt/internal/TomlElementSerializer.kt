@file:OptIn(InternalSerializationApi::class)

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

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlTable
import net.peanuuutz.tomlkt.asTomlDecoder
import net.peanuuutz.tomlkt.asTomlEncoder
import net.peanuuutz.tomlkt.toTomlNull
import net.peanuuutz.tomlkt.toTomlLiteral
import net.peanuuutz.tomlkt.toTomlArray
import net.peanuuutz.tomlkt.toTomlTable

internal object TomlElementSerializer : KSerializer<TomlElement> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("net.peanuuutz.tomlkt.TomlElement", SerialKind.CONTEXTUAL)

    override fun serialize(encoder: Encoder, value: TomlElement) {
        encoder.asTomlEncoder().encodeTomlElement(value)
    }

    override fun deserialize(decoder: Decoder): TomlElement = decoder.asTomlDecoder().decodeTomlElement()
}

internal object TomlNullSerializer : KSerializer<TomlNull> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("net.peanuuutz.tomlkt.TomlNull", SerialKind.CONTEXTUAL)

    override fun serialize(encoder: Encoder, value: TomlNull) {
        encoder.asTomlEncoder().encodeTomlElement(TomlNull)
    }

    override fun deserialize(decoder: Decoder): TomlNull = decoder.asTomlDecoder().decodeTomlElement().toTomlNull()
}

internal object TomlLiteralSerializer : KSerializer<TomlLiteral> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("net.peanuuutz.tomlkt.TomlLiteral", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TomlLiteral) {
        encoder.asTomlEncoder().encodeTomlElement(value)
    }

    override fun deserialize(decoder: Decoder): TomlLiteral = decoder.asTomlDecoder().decodeTomlElement().toTomlLiteral()
}

internal object TomlArraySerializer : KSerializer<TomlArray> {
    private val delegate: KSerializer<List<TomlElement>> = ListSerializer(TomlElementSerializer)

    override val descriptor: SerialDescriptor = object : SerialDescriptor by delegate.descriptor {
        override val serialName: String = "net.peanuuutz.tomlkt.TomlArray"
    }

    override fun serialize(encoder: Encoder, value: TomlArray) {
        delegate.serialize(encoder.asTomlEncoder(), value)
    }

    override fun deserialize(decoder: Decoder): TomlArray = decoder.asTomlDecoder().decodeTomlElement().toTomlArray()
}

internal object TomlTableSerializer : KSerializer<TomlTable> {
    private val delegate: KSerializer<Map<String, TomlElement>> = MapSerializer(String.serializer(), TomlElementSerializer)

    override val descriptor: SerialDescriptor = object : SerialDescriptor by delegate.descriptor {
        override val serialName: String = "net.peanuuutz.tomlkt.TomlTable"
    }

    override fun serialize(encoder: Encoder, value: TomlTable) {
        delegate.serialize(encoder.asTomlEncoder(), value)
    }

    override fun deserialize(decoder: Decoder): TomlTable = decoder.asTomlDecoder().decodeTomlElement().toTomlTable()
}