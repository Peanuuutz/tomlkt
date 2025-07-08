plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
    kotlin("plugin.allopen") apply false
    kotlin("kapt") apply false

    id("org.jetbrains.dokka") apply false

    id("io.gitlab.arturbosch.detekt") apply false
    id("me.champeau.jmh") apply false

    id("io.deepmedia.tools.deployer") apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}
