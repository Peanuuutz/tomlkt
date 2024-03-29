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

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind.CONTEXTUAL
import kotlinx.serialization.descriptors.SerialKind.ENUM
import kotlinx.serialization.descriptors.getContextualDescriptor
import net.peanuuutz.tomlkt.TomlArray
import net.peanuuutz.tomlkt.TomlConfig
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.TomlNull
import net.peanuuutz.tomlkt.TomlTable

private val UnsignedIntegerDescriptors: Set<SerialDescriptor> = setOf(
    UByte.serializer().descriptor,
    UShort.serializer().descriptor,
    UInt.serializer().descriptor,
    ULong.serializer().descriptor
)

private val TomlElementSerializers: Set<KSerializer<*>> = setOf(
    TomlElement.serializer(),
    TomlNull.serializer(),
    TomlLiteral.serializer(),
    TomlArray.serializer(),
    TomlTable.serializer()
)

internal val SerialDescriptor.isPrimitiveLike: Boolean
    get() {
        val kind = kind
        return kind is PrimitiveKind || kind == ENUM
    }

internal fun SerialDescriptor.findRealDescriptor(config: TomlConfig): SerialDescriptor {
    return when {
        kind == CONTEXTUAL -> {
            config.serializersModule
                .getContextualDescriptor(this)
                ?.findRealDescriptor(config)
                ?: this
        }
        isInline -> getElementDescriptor(0).findRealDescriptor(config)
        else -> this
    }
}

internal val SerialDescriptor.isUnsignedInteger: Boolean
    get() = isInline && this in UnsignedIntegerDescriptors

internal val SerializationStrategy<*>.isTomlElement: Boolean
    get() = this in TomlElementSerializers

internal val DeserializationStrategy<*>.isTomlElement: Boolean
    get() = this in TomlElementSerializers
