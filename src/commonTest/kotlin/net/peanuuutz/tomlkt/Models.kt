package net.peanuuutz.tomlkt

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val name: String,
    @Fold
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
    @Comment("Project name")
    val name: String,
    @Comment("Current maintainability, could be HIGH or LOW")
    val maintainability: Maintainability,
    @Multiline @Literal
    val description: String? = null,
    val owner: User,
    @Comment("Thank you! :)")
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

@Serializable
enum class Maintainability { HIGH, LOW }

@Serializable
data class Score(
    val examinee: String,
    val scores: Map<String, Int>
)

@Serializable
class EmptyClass

@Serializable
data class Box<T>(val content: T? = null)