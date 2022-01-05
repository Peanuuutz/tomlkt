@file:OptIn(InternalSerializationApi::class)

package net.peanuuutz.tomlkt.internal

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.peanuuutz.tomlkt.*

internal object TomlElementSerializer : KSerializer<TomlElement> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("net.peanuuutz.tomlkt.TomlElement", PolymorphicKind.SEALED) {
        element("TomlNull", lazyInitializeDescriptor { TomlNullSerializer.descriptor })
        element("TomlLiteral", lazyInitializeDescriptor { TomlLiteralSerializer.descriptor })
        element("TomlArray", lazyInitializeDescriptor { TomlArraySerializer.descriptor })
        element("TomlTable", lazyInitializeDescriptor { TomlTableSerializer.descriptor })
    }

    override fun serialize(encoder: Encoder, value: TomlElement) {
        encoder.asTomlEncoder()
        when (value) {
            TomlNull -> encoder.encodeSerializableValue(TomlNullSerializer, TomlNull)
            is TomlLiteral -> encoder.encodeSerializableValue(TomlLiteralSerializer, value)
            is TomlArray -> encoder.encodeSerializableValue(TomlArraySerializer, value)
            is TomlTable -> encoder.encodeSerializableValue(TomlTableSerializer, value)
        }
    }

    override fun deserialize(decoder: Decoder): TomlElement = decoder.asTomlDecoder().decodeTomlElement()
}

internal object TomlNullSerializer : KSerializer<TomlNull> {
    override val descriptor: SerialDescriptor = buildSerialDescriptor("net.peanuuutz.tomlkt.TomlNull", SerialKind.CONTEXTUAL)

    override fun serialize(encoder: Encoder, value: TomlNull) {
        encoder.asTomlEncoder().encodeNull()
    }

    override fun deserialize(decoder: Decoder): TomlNull = decoder.asTomlDecoder().decodeTomlElement().toTomlNull()
}

internal object TomlLiteralSerializer : KSerializer<TomlLiteral> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("net.peanuuutz.tomlkt.TomlLiteral", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: TomlLiteral) {
        encoder.asTomlEncoder().encodeRawString(value.toString())
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

private fun Encoder.asTomlEncoder(): TomlEncoder = this as? TomlEncoder
    ?: throw IllegalStateException("Expect TomlEncoderAddon, but found ${this::class.simpleName}")

private fun Decoder.asTomlDecoder(): TomlDecoder = this as? TomlDecoder
    ?: throw IllegalStateException("Expect TomlDecoderAddon, but found ${this::class.simpleName}")

@OptIn(ExperimentalSerializationApi::class)
private fun lazyInitializeDescriptor(provider: () -> SerialDescriptor): SerialDescriptor = object : SerialDescriptor {
    private val original: SerialDescriptor by lazy(provider)

    override val elementsCount: Int get() = original.elementsCount
    override val kind: SerialKind get() = original.kind
    override val serialName: String get() = original.serialName

    override fun getElementAnnotations(index: Int): List<Annotation> = original.getElementAnnotations(index)
    override fun getElementDescriptor(index: Int): SerialDescriptor = original.getElementDescriptor(index)
    override fun getElementIndex(name: String): Int = original.getElementIndex(name)
    override fun getElementName(index: Int): String = original.getElementName(index)
    override fun isElementOptional(index: Int): Boolean = original.isElementOptional(index)
}