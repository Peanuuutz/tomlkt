# Tomlkt

[![Maven Central](https://img.shields.io/maven-central/v/net.peanuuutz/tomlkt)](https://search.maven.org/artifact/net.peanuuutz/tomlkt)
[![License](https://img.shields.io/github/license/Peanuuutz/tomlkt)](http://www.apache.org/licenses/LICENSE-2.0)

Lightweight and easy to use [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization) plugin for [TOML](https://toml.io/) serialization and deserialization.

*This project is in alpha state. If you find any problem along usage, please raise an [issue](https://github.com/Peanuuutz/tomlkt). :wink:*

## Setup

<details>
<summary>Gradle Kotlin (build.gradle.kts)</summary>

```kotlin
repositories {
    mavenCentral()
}

dependencies {
    implementation("net.peanuuutz:tomlkt:0.1.0")
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
    implementation "net.peanuuutz:tomlkt:0.1.0"
}
```
</details>

<details>
<summary>Maven (.pom)</summary>

```xml
<dependency>
  <groupId>net.peanuuutz</groupId>
  <artifactId>tomlkt-jvm</artifactId>
  <version>0.1.0</version>
</dependency>
```
</details>

*Note: If your project is Kotlin Multiplatform, you can simply add this into commonMain dependencies.*

## Features

|TOML format|Serialization|Deserialization|
|---|---|---|
|[Comment](#Comment)|:heavy_check_mark:|:heavy_check_mark:|
|Key|:heavy_check_mark:|:heavy_check_mark:|
|[String](#String)|:heavy_check_mark:|:heavy_check_mark:|
|Integer|:heavy_check_mark:|:heavy_check_mark:|
|Float|:heavy_check_mark:|:heavy_check_mark:|
|Boolean|:heavy_check_mark:|:heavy_check_mark:|
|[Date Time](#Date-Time)|:x:|:x:|
|Array|:heavy_check_mark:|:heavy_check_mark:|
|[Table](#Table)|:heavy_check_mark:|:heavy_check_mark::grey_question:|
|Inline Table|:heavy_check_mark:|:heavy_check_mark:|
|Array of Tables|:heavy_check_mark:|:heavy_check_mark:|

### Comment

Implemented as an annotation `@Comment` on **properties**:

```kotlin
class IntData(
    @Comment("An integer,", "but is deserialized into Long originally")
    val int: Int
)

IntData(10086)
```

The code above will be encoded into:

```toml
# An integer,
# but is deserialized into Long originally
int = 10086
```

### String

Basic strings are encoded as `"<content>"`. For multilines and literals, put an annotation as below:

```kotlin
class MultilineStringData(
    @Multiline
    val multilineString: String
)

class LiteralStringData(
    @Literal
    val literalString: String
)

MultilineStringData("""
    Do, a deer, a female deer.
    Re, a drop of golden sun.
""".trimIndent())

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

_**Because Kotlin Multiplatform doesn't support this without [additional library](https://github.com/Kotlin/kotlinx-datetime), currently tomlkt doesn't support as well.**_

<font color = 'gray'>*Maybe some day we'll support it for JVM.*</font>:thinking:

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

It will throw `net.peanuuutz.tomlkt.internal.ConflictEntryException`. Due to the reading process of [TomlFileParser](https://github.com/Peanuuutz/tomlkt/tree/dev/src/commonMain/kotlin/net/peanuuutz/tomlkt/internal/parser/TomlFileParser.kt), each time a table head is parsed, the path will be immediately put into the whole [Tree](https://github.com/Peanuuutz/tomlkt/tree/dev/src/commonMain/kotlin/net/peanuuutz/tomlkt/internal/parser/TreeNode.kt), and meanwhile be checked if is already defined. :face_with_head_bandage:

### Extra features

The working process of tomlkt:

* Serialization: Model / TomlElement → File(String)
* Deserialization: File(String) → TomlElement → Model

As you see, if you already have a TOML file, you can have no model class, but still gain access to every entry with the help of [TomlElement](https://github.com/Peanuuutz/tomlkt/tree/dev/src/commonMain/kotlin/net/peanuuutz/tomlkt/TomlElement.kt).

*Note: Due to no context of values in TomlTable(see TomlElement.kt), all of those are encoded as inline(meaning you can't get the same serialized structure between model class and TomlTable).*

_**API document now under construction.**_ :construction: