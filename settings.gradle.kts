rootProject.name = "tomlkt"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        val kotlinVersion: String by settings
        kotlin("multiplatform") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
        kotlin("plugin.allopen") version kotlinVersion
        kotlin("kapt") version kotlinVersion

        val dokkaVersion: String by settings
        id("org.jetbrains.dokka") version dokkaVersion

        val detektVersion: String by settings
        id("io.gitlab.arturbosch.detekt") version detektVersion
        val jmhVersion: String by settings
        id("me.champeau.jmh") version jmhVersion

        val deployerVersion: String by settings
        id("io.deepmedia.tools.deployer") version deployerVersion
    }
}

include(":core")
include(":benchmark")
