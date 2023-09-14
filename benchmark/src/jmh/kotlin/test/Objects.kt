package test

import kotlinx.datetime.Instant
import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.datetime.toKotlinInstant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import net.peanuuutz.tomlkt.NativeOffsetDateTime
import net.peanuuutz.tomlkt.TomlClassDiscriminator
import net.peanuuutz.tomlkt.TomlDecoder
import net.peanuuutz.tomlkt.TomlEncoder
import net.peanuuutz.tomlkt.TomlOffsetDateTimeSerializer
import org.intellij.lang.annotations.Language
import java.time.ZoneOffset

fun main() {
    val string = """
        i = 0

        [data]
        list = [
            "any",
            { type = "integer", value = 0 }
        ]
    """.trimIndent()

    val value = TomlObjects.tomlkt.decodeFromString(
        deserializer = String.serializer(),
        string = string,
        "data", "list", 0
    )

    println(value)
}

@Serializable
data class Config(
    val title: String,
    val owner: Owner,
    val database: Database,
//    val servers: Map<String, Server>
)

@Serializable
data class Owner(
    val name: String,
    val birthday: @Serializable(OffsetDateTimeSerializer::class) Any
)

@Serializable
data class Database(
    val enabled: Boolean,
    val ports: List<Short>,
    val temperature: Temperature
)

@Serializable
data class Temperature(
    val cpu: Float,
    val case: Float
)

@Serializable
data class Server(
    val ip: String,
    val role: String
)

@Language("toml")
const val SampleConfig: String = """
# This is a TOML document

title = "TOML Example"

[owner]
name = "Tom Preston-Werner"
birthday = 1979-05-27T07:32:00-08:00

[database]
enabled = true
ports = [ 8000, 8001, 8002 ]
temperature = { cpu = 79.5, case = 72.0 }

# [servers]

# [servers.alpha]
# ip = "10.0.0.1"
# role = "frontend"

# [servers.beta]
# ip = "10.0.0.2"
# role = "backend"
"""

object OffsetDateTimeSerializer : KSerializer<Any> {
    override val descriptor: SerialDescriptor
        get() = InstantIso8601Serializer.descriptor

    override fun serialize(encoder: Encoder, value: Any) {
        if (encoder is TomlEncoder) {
            when (value) {
                is NativeOffsetDateTime -> {
                    encoder.encodeSerializableValue(TomlOffsetDateTimeSerializer(), value)
                }
                is Instant -> {
                    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
                    val converted = value.value.atOffset(ZoneOffset.UTC)
                    encoder.encodeSerializableValue(TomlOffsetDateTimeSerializer(), converted)
                }
            }
        } else {
            when (value) {
                is NativeOffsetDateTime -> {
                    val converted = value.toInstant().toKotlinInstant()
                    encoder.encodeSerializableValue(InstantIso8601Serializer, converted)
                }
                is Instant -> {
                    encoder.encodeSerializableValue(InstantIso8601Serializer, value)
                }
            }
        }
    }

    override fun deserialize(decoder: Decoder): Any {
        return if (decoder is TomlDecoder) {
            decoder.decodeSerializableValue(TomlOffsetDateTimeSerializer())
        } else {
            decoder.decodeSerializableValue(InstantIso8601Serializer)
        }
    }
}
