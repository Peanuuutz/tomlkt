package test

import kotlinx.datetime.serializers.InstantIso8601Serializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import net.peanuuutz.tomlkt.TomlDecoder
import net.peanuuutz.tomlkt.TomlOffsetDateTimeSerializer
import org.intellij.lang.annotations.Language

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
        TODO("Not yet implemented")
    }

    override fun deserialize(decoder: Decoder): Any {
        return if (decoder is TomlDecoder) {
            TomlOffsetDateTimeSerializer().deserialize(decoder)
        } else {
            InstantIso8601Serializer.deserialize(decoder)
        }
    }
}
