rootProject.name = "tomlkt"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    plugins {
        val kotlinVersion: String by settings
        kotlin("multiplatform") version kotlinVersion
        id("org.jetbrains.dokka") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }
}