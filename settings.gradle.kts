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

        val dokkaVersion: String by settings
        id("org.jetbrains.dokka") version dokkaVersion

        val detektVersion: String by settings
        id("io.gitlab.arturbosch.detekt") version detektVersion
    }
}
