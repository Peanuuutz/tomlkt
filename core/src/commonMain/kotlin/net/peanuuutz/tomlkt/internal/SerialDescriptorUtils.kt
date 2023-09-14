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

import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind.CONTEXTUAL
import kotlinx.serialization.descriptors.SerialKind.ENUM
import kotlinx.serialization.descriptors.StructureKind.CLASS
import kotlinx.serialization.descriptors.StructureKind.LIST
import kotlinx.serialization.descriptors.StructureKind.MAP
import kotlinx.serialization.descriptors.StructureKind.OBJECT
import kotlinx.serialization.descriptors.getContextualDescriptor
import net.peanuuutz.tomlkt.TomlConfig

private const val TomlTableSerialName: String = "net.peanuuutz.tomlkt.TomlTable"

internal val SerialDescriptor.isPrimitiveLike: Boolean
    get() {
        val kind = kind
        return kind is PrimitiveKind || kind == ENUM
    }

internal val SerialDescriptor.isCollection: Boolean
    get() {
        val kind = kind
        return kind == LIST || kind == MAP
    }

internal val SerialDescriptor.isTable: Boolean
    get() {
        val kind = kind
        return kind == CLASS || kind is PolymorphicKind || kind == OBJECT || kind == MAP
    }

internal val SerialDescriptor.isArrayOfTable: Boolean
    get() {
        if (kind != LIST) {
            return false
        }
        val elementDescriptor = getElementDescriptor(0)
        return elementDescriptor.isTable && elementDescriptor.isInline.not()
    }

internal val SerialDescriptor.isTableLike: Boolean
    get() = isTable || isArrayOfTable

internal val SerialDescriptor.isExactlyTomlTable: Boolean
    get() {
        if (!isNullable) {
            return false
        }
        return serialName.removeSuffix("?") == TomlTableSerialName
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
