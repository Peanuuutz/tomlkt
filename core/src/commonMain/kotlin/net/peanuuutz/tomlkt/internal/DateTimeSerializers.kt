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

package net.peanuuutz.tomlkt.internal

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.peanuuutz.tomlkt.NativeLocalDate
import net.peanuuutz.tomlkt.NativeLocalDateTime
import net.peanuuutz.tomlkt.NativeLocalTime
import net.peanuuutz.tomlkt.NativeOffsetDateTime
import net.peanuuutz.tomlkt.TomlDecoder
import net.peanuuutz.tomlkt.TomlEncoder
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.asTomlLiteral
import net.peanuuutz.tomlkt.toLocalDate
import net.peanuuutz.tomlkt.toLocalDateTime
import net.peanuuutz.tomlkt.toLocalTime
import net.peanuuutz.tomlkt.toOffsetDateTime

internal object LocalDateTimeSerializer : KSerializer<NativeLocalDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "net.peanuuutz.tomlkt.NativeLocalDateTime",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: NativeLocalDateTime) {
        if (encoder is TomlEncoder) {
            encoder.encodeTomlElement(TomlLiteral(value))
        } else {
            encoder.encodeString(value.toString())
        }
    }

    override fun deserialize(decoder: Decoder): NativeLocalDateTime {
        return if (decoder is TomlDecoder) {
            decoder.decodeTomlElement().asTomlLiteral().toLocalDateTime()
        } else {
            NativeLocalDateTime(decoder.decodeString())
        }
    }
}

internal object OffsetDateTimeSerializer : KSerializer<NativeOffsetDateTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "net.peanuuutz.tomlkt.NativeOffsetDateTime",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: NativeOffsetDateTime) {
        if (encoder is TomlEncoder) {
            encoder.encodeTomlElement(TomlLiteral(value))
        } else {
            encoder.encodeString(value.toString())
        }
    }

    override fun deserialize(decoder: Decoder): NativeOffsetDateTime {
        return if (decoder is TomlDecoder) {
            decoder.decodeTomlElement().asTomlLiteral().toOffsetDateTime()
        } else {
            NativeOffsetDateTime(decoder.decodeString())
        }
    }
}

internal object LocalDateSerializer : KSerializer<NativeLocalDate> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "net.peanuuutz.tomlkt.NativeLocalDate",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: NativeLocalDate) {
        if (encoder is TomlEncoder) {
            encoder.encodeTomlElement(TomlLiteral(value))
        } else {
            encoder.encodeString(value.toString())
        }
    }

    override fun deserialize(decoder: Decoder): NativeLocalDate {
        return if (decoder is TomlDecoder) {
            decoder.decodeTomlElement().asTomlLiteral().toLocalDate()
        } else {
            NativeLocalDate(decoder.decodeString())
        }
    }
}

internal object LocalTimeSerializer : KSerializer<NativeLocalTime> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "net.peanuuutz.tomlkt.NativeLocalTime",
        kind = PrimitiveKind.STRING
    )

    override fun serialize(encoder: Encoder, value: NativeLocalTime) {
        if (encoder is TomlEncoder) {
            encoder.encodeTomlElement(TomlLiteral(value))
        } else {
            encoder.encodeString(value.toString())
        }
    }

    override fun deserialize(decoder: Decoder): NativeLocalTime {
        return if (decoder is TomlDecoder) {
            decoder.decodeTomlElement().asTomlLiteral().toLocalTime()
        } else {
            NativeLocalTime(decoder.decodeString())
        }
    }
}
