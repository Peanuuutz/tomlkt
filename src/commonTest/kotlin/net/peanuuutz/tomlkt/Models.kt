package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable

@Serializable
data class User(
    @TomlComment("Name of this user")
    val name: String,
    @TomlInline
    val account: Account? = null
)

@Serializable
data class Account(
    val username: String,
    val password: String
)

val owner = User("Peanuuutz", Account("peanuuutz", "123456"))
val cooperator = User("Anonymous")

@Serializable
data class Project(
    @TomlComment("Project name")
    val name: String,
    @TomlComment("""
        Current maintainability
        Could be HIGH or LOW
    """)
    val maintainability: Maintainability,
    @TomlMultilineString @TomlLiteralString
    val description: String? = null,
    val owner: User,
    @TomlComment("Thank you! :)")
    val contributors: Set<User> = setOf(owner)
)

val tomlProject = Project(
    name = "tomlkt",
    maintainability = Maintainability.HIGH,
    description = """
        This is my first project, so sorry for any inconvenience! \
        Anyway, constructive criticism is welcomed. :)
    """.trimIndent(),
    owner = owner,
    contributors = setOf(owner, cooperator)
)

val yamlProject = Project(
    name = "yamlkt",
    maintainability = Maintainability.LOW,
    owner = User("Him188")
)

val projects = mapOf("Toml" to tomlProject, "Yaml" to yamlProject)

@Serializable
enum class Maintainability { HIGH, LOW }

@Serializable
data class Score(
    val examinee: String,
    val scores: Map<String, Int>
)

val exampleScore = Score(
    examinee = "Loney Chou",
    scores = mapOf("Listening" to 80, "Reading" to 95)
)

@Serializable
class EmptyClass

@Serializable
data class Box<T>(val content: T? = null)

@Serializable
data class ByteCode(@TomlInteger(TomlInteger.Base.BIN) val code: Byte)

@Serializable
data class Color(@TomlInteger(TomlInteger.Base.HEX) val value: Long)