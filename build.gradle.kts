plugins {
    kotlin("multiplatform") apply false
    kotlin("plugin.serialization") apply false
    kotlin("plugin.allopen") apply false
    kotlin("kapt") apply false

    id("org.jetbrains.dokka") apply false

    id("io.gitlab.arturbosch.detekt") apply false
    id("me.champeau.jmh") apply false

    id("com.vanniktech.maven.publish") apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}
