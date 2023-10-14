# tomlkt

[![Maven Central](https://img.shields.io/maven-central/v/net.peanuuutz.tomlkt/tomlkt)](https://search.maven.org/artifact/net.peanuuutz.tomlkt/tomlkt)
[![License](https://img.shields.io/github/license/Peanuuutz/tomlkt)](http://www.apache.org/licenses/LICENSE-2.0)

Powerful and easy to use [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) plugin for [TOML](https://toml.io/) serialization and
deserialization.

*If you find any problem along usage, please raise an [issue](https://github.com/Peanuuutz/tomlkt/issues).* :wink:

## Setup

<details open>
<summary>Gradle Kotlin (build.gradle.kts)</summary>

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("net.peanuuutz.tomlkt:tomlkt:0.3.6")
}
```
</details>

<details>
<summary>Gradle Groovy (build.gradle)</summary>

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "net.peanuuutz.tomlkt:tomlkt:0.3.6"
}
```
</details>

<details>
<summary>Maven (.pom)</summary>

```xml
<dependency>
  <groupId>net.peanuuutz.tomlkt</groupId>
  <artifactId>tomlkt-jvm</artifactId>
  <version>0.3.6</version>
</dependency>
```
</details>

*Note: If your project is Kotlin Multiplatform, you can simply add this into commonMain
dependencies.*

## Quick Start

Write some config:

```toml
name = "Peanuuutz"

[account]
username = "Peanuuutz"
password = "123456"
```

Write some code:

```kotlin
@Serializable
data class User(
    val name: String,
    val account: Account?
)

@Serializable
data class Account(
    val username: String,
    val password: String
)

fun main() {
    // Here we use JVM.
    val tomlString = Paths.get("...").readText()
    // Either is OK, but to explicitly pass a serializer is faster.
    val user = Toml.decodeFromString(User.serializer(), tomlString)
    val user = Toml.decodeFromString<User>(tomlString)
    // That's it!

    // By the way if you need some configuration.
    val toml = Toml {
        ignoreUnknownKeys = true
    }
    // Use toml to apply the change.

    // Serialization.
    val anotherUser = User("Anonymous", null)
    // Again, better to explicitly pass a serializer.
    val config = Toml.encodeToString(User.serializer(), anotherUser)
    Paths.get("...").writeText(config)
    // Done.
}
```

## Features

| TOML Data Type          | Serialization      | Deserialization                   |
|-------------------------|--------------------|-----------------------------------|
| [Comment](#Comment)     | :heavy_check_mark: | :heavy_check_mark:                |
| Key                     | :heavy_check_mark: | :heavy_check_mark:                |
| [String](#String)       | :heavy_check_mark: | :heavy_check_mark:                |
| Integer                 | :heavy_check_mark: | :heavy_check_mark:                |
| Float                   | :heavy_check_mark: | :heavy_check_mark:                |
| Boolean                 | :heavy_check_mark: | :heavy_check_mark:                |
| [Date Time](#Date-Time) | :heavy_check_mark: | :heavy_check_mark:                |
| Array                   | :heavy_check_mark: | :heavy_check_mark:                |
| [Table](#Table)         | :heavy_check_mark: | :heavy_check_mark::grey_question: |
| Inline Table            | :heavy_check_mark: | :heavy_check_mark:                |
| Array of Tables         | :heavy_check_mark: | :heavy_check_mark:                |

### Comment

Implemented as an annotation `@TomlComment` on **properties**:

```kotlin
class IntData(
    @TomlComment("""
        An integer,
        but is decoded into Long originally
    """)
    val int: Int
)
IntData(10086)
```

The code above will be encoded into:

```toml
# An integer,
# but is decoded into Long originally
int = 10086
```

### String

Basic strings are encoded as `"<content>"`. For multilines and literals, put an annotation as
below:

```kotlin
class MultilineStringData(
    @TomlMultilineString
    val multilineString: String
)
MultilineStringData("""
    Do, a deer, a female deer.
    Re, a drop of golden sun.
""".trimIndent())

class LiteralStringData(
    @TomlLiteralString
    val literalString: String
)
LiteralStringData("C:\\Users\\<User>\\.m2\\repositories")
```

The code above will be encoded into:

```toml
multilineString = """
Do, a deer, a female deer.
Re, a drop of golden sun."""

literalString = 'C:\Users\<User>\.m2\repositories'
```

You can use both annotations to get multiline literal string.

### Date Time

TOML supports several date time formats, so does tomlkt. tomlkt declares `TomlLocalDateTime`,
`TomlOffsetDateTime`, `TomlLocalDate`, `TomlLocalTime` as expect types with builtin support for
serialization (`@Serializable`).

The mapping of these expect types are as follows:

| tomlkt             | java.time      | kotlinx.datetime |
|--------------------|----------------|------------------|
| TomlLocalDateTime  | LocalDateTime  | LocalDateTime    |
| TomlOffsetDateTime | OffsetDateTime | Instant          |
| TomlLocalDate      | LocalDate      | LocalDate        |
| TomlLocalTime      | LocalTime      | LocalTime        |

[TomlLiteral](https://github.com/Peanuuutz/tomlkt/tree/master/core/src/commonMain/kotlin/net/peanuuutz/tomlkt/TomlElement.kt) is the default intermediate representation of a date time. For conversion,
simply use `TomlLiteral(TomlLocalDateTime)` to create a `TomlLiteral` from a `TomlLocalDateTime`
(true for other types), and `TomlLiteral.toLocalDateTime()` for the other way.

If you'd like to provide a custom serializer, use `NativeLocalDateTime` and the like as raw types.

### Table

:grey_question:: There's an internal issue. When you define super-table **before** the sub-table:

```toml
[x]
[x.y]
```

It will be successfully parsed, but if you define after that:

```toml
[x.y]
[x]
```

It will throw `net.peanuuutz.tomlkt.internal.ConflictEntryException`. Due to the reading process
of [TomlFileParser](https://github.com/Peanuuutz/tomlkt/tree/master/core/src/commonMain/kotlin/net/peanuuutz/tomlkt/internal/parser/TomlFileParser.kt), each time a table head is parsed, the path will be immediately put into
the whole [tree](https://github.com/Peanuuutz/tomlkt/tree/master/core/src/commonMain/kotlin/net/peanuuutz/tomlkt/internal/parser/TreeNode.kt), and meanwhile be checked if is already defined.

Better to keep super-table first!

### Extra Features

The working process of tomlkt:

* Serialization: Model → (TomlElementEncoder) → TomlElement → (TomlElementEmitter) → File.
* Deserialization: File → (TomlElementParser) → TomlElement → (TomlElementDecoder) → Model.

As you see, if you already have a TOML file, you can have no model class, but still gain access
to every entry with the help of [TomlElement](https://github.com/Peanuuutz/tomlkt/tree/master/core/src/commonMain/kotlin/net/peanuuutz/tomlkt/TomlElement.kt).

*Note: All the metadata coming from [annotations](https://github.com/Peanuuutz/tomlkt/tree/master/core/src/commonMain/kotlin/net/peanuuutz/tomlkt/Annotations.kt) is ignored in the parser, meaning that
despite you could parse a file into an element, you cannot emit it back to file fully as is.*

For other information, view [API docs](https://peanuuutz.github.io/tomlkt/).
