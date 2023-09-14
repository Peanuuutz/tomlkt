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
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.PolymorphicKind.SEALED
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.SerialKind.ENUM
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.encoding.AbstractDecoder
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.findPolymorphicSerializer
import kotlinx.serialization.internal.AbstractPolymorphicSerializer
import kotlinx.serialization.modules.SerializersModule
import net.peanuuutz.tomlkt.TomlClassDiscriminator
import net.peanuuutz.tomlkt.TomlConfig
import net.peanuuutz.tomlkt.asTomlLiteral
import net.peanuuutz.tomlkt.asTomlTable
import net.peanuuutz.tomlkt.internal.decoder.AbstractTomlDecoder
import net.peanuuutz.tomlkt.internal.encoder.AbstractTomlEncoder

internal fun SerialDescriptor.findDiscriminator(config: TomlConfig): String {
    for (annotation in annotations) {
        if (annotation is TomlClassDiscriminator) {
            return annotation.discriminator
        }
    }
    return config.classDiscriminator
}

internal inline fun AbstractTomlEncoder.encodePolymorphically(
    serializer: AbstractPolymorphicSerializer<Any>,
    value: Any,
    callback: (discriminator: String) -> Unit
) {
    val discriminator = serializer.descriptor.findDiscriminator(config)
    val realSerializer = serializer.findPolymorphicSerializer(this, value)
    validateDiscriminator(serializer.descriptor, realSerializer.descriptor, discriminator)
    validateKind(realSerializer.descriptor)
    callback(discriminator)
    realSerializer.serialize(this, value)
}

private fun validateDiscriminator(
    baseDescriptor: SerialDescriptor,
    realDescriptor: SerialDescriptor,
    discriminator: String
) {
    when {
        baseDescriptor.kind !is SEALED -> {}
        discriminator !in realDescriptor.elementNames -> {}
        else -> {
            val message = "Subclass ${realDescriptor.serialName} cannot be serialized as base class " +
                    "${baseDescriptor.serialName} because one of the property serial name conflicts with class " +
                    "discriminator $discriminator. Please rename the conflicting property, or annotate it with " +
                    "@SerialName providing another serial name"
            error(message)
        }
    }
}

private fun validateKind(realDescriptor: SerialDescriptor) {
    when (realDescriptor.kind) {
        ENUM, is PrimitiveKind -> {
            error("Primitive like ${realDescriptor.serialName} cannot be serialized with class discriminator")
        }
        is PolymorphicKind -> {
            error("Subclass ${realDescriptor.serialName} cannot be PolymorphicKind again")
        }
        else -> {}
    }
}

internal inline fun <T> AbstractTomlDecoder.decodePolymorphically(
    deserializer: AbstractPolymorphicSerializer<Any>,
    callback: (discriminator: String) -> Unit
): T {
    val discriminator = deserializer.descriptor.findDiscriminator(config)
    val table = decodeTomlElement().asTomlTable()
    val type = table[discriminator]?.asTomlLiteral()?.content
    val dummyDecoder = configureDummyDecoder(serializersModule)
    @Suppress("UNCHECKED_CAST")
    val realDeserializer = deserializer.findPolymorphicSerializer(dummyDecoder, type) as DeserializationStrategy<T>
    callback(discriminator)
    return realDeserializer.deserialize(this)
}

private fun configureDummyDecoder(serializersModule: SerializersModule): CompositeDecoder {
    val decoder = DummyDecoder
    decoder.serializersModule = serializersModule
    return decoder
}

// findPolymorphicSerializer requires an unused CompositeDecoder.
private object DummyDecoder : AbstractDecoder() {
    override lateinit var serializersModule: SerializersModule

    override fun decodeElementIndex(descriptor: SerialDescriptor): Int {
        error("DummyDecoder should only be used to retrieve serializers via serializersModule")
    }
}
