import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    kotlin("plugin.allopen")
    kotlin("kapt")

    id("me.champeau.jmh")
}

dependencies {
    jmh("org.openjdk.jmh:jmh-core:1.36")
    kaptJmh("org.openjdk.jmh:jmh-generator-annprocess:1.36")

    // tomlkt
    jmh(project(":core"))
    // toml4j
    jmh("com.moandjiezana.toml:toml4j:0.7.2")
    // ktoml
    jmh("com.akuleshov7:ktoml-core:0.5.0")
    // jackson
    jmh("com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.15.1")
    jmh("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.1")
    jmh("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.15.1")
    // night config
    jmh("com.electronwill.night-config:toml:3.6.0")
    // tomlj
    jmh("org.tomlj:tomlj:1.1.0")

    // official JSON
    val serializationVersion: String by rootProject
    jmh("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
}

jmh {
    includes.set(listOf("test.Benchmark"))
}

allOpen {
    annotation("org.openjdk.jmh.annotations.Measurement")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}
