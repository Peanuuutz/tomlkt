/*
    Copyright 2017-2020 JetBrains s.r.o
    Modifications Copyright 2023 Peanuuutz

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

package net.peanuuutz.tomlkt

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.serializerOrNull
import net.peanuuutz.tomlkt.internal.throwSubclassNotRegistered
import kotlin.reflect.KClass

/**
 * Base class for encoding and decoding subclasses of [baseClass] without a
 * [classDiscriminator][TomlConfigBuilder.classDiscriminator].
 *
 * The standard process of polymorphic serialization requires an extra
 * property to denote the underlying type, so that if two subclasses have the
 * exact same properties, it is possible to create an instance with the correct
 * type. However, this is not the common case. Normally different subclasses
 * would just have different properties. In this situation, a special serializer
 * is needed to support serialization without the discriminator.
 *
 * Example usage:
 *
 * ```kotlin
 * @Serializable(PlacementSerializer::class)
 * sealed class Placement {
 *     @Serializable
 *     data object Unplaced : Placement()
 *
 *     @Serializable
 *     data class Placed(val x: Double, val y: Double) : Placement()
 * }
 *
 * object PlacementSerializer : TomlContentPolymorphicSerializer<Placement>(
 *     baseClass = Placement::class,
 *     serialName = "Placement" // Optional change.
 * ) {
 *     override fun selectDeserializer(element: TomlElement) = when {
 *         element.asTomlTable().isNotEmpty() -> Placement.Placed.serializer()
 *         else -> Placement.Unplaced.serializer()
 *     }
 * }
 *
 * val unplaced = ""
 * Toml.decodeFromString<Placement>(unplaced) // Unplaced
 *
 * val placed = """
 *     x = 0.0
 *     y = 0.0
 * """.trimIndent()
 * Toml.decodeFromString<Placement>(placed) // Placed(x=0.0, y=0.0)
 * ```
 */
public abstract class TomlContentPolymorphicSerializer<T : Any>(
    private val baseClass: KClass<T>,
    serialName: String = "TomlContentPolymorphicSerializer<${baseClass.simpleName}>"
) : KSerializer<T> {
    /**
     * Selects a [DeserializationStrategy] for decoding [element].
     */
    protected abstract fun selectDeserializer(element: TomlElement): DeserializationStrategy<T>

    // ======== Internal ========

    final override val descriptor: SerialDescriptor = buildSerialDescriptor(
        serialName = serialName,
        kind = PolymorphicKind.SEALED
    )

    final override fun serialize(encoder: Encoder, value: T) {
        val realSerializer = encoder.serializersModule.getPolymorphic(baseClass, value)
            ?: value::class.serializerOrNull()
            ?: throwSubclassNotRegistered(value::class, baseClass)
        @Suppress("UNCHECKED_CAST")
        realSerializer as KSerializer<T>
        realSerializer.serialize(encoder, value)
    }

    final override fun deserialize(decoder: Decoder): T {
        decoder.asTomlDecoder()
        val element = decoder.decodeTomlElement()
        val deserializer = selectDeserializer(element)
        return decoder.toml.decodeFromTomlElement(deserializer, element)
    }
}
