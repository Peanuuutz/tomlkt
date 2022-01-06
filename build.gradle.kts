import java.net.URL

plugins {
    idea
    val kotlinVersion: String by System.getProperties()
    kotlin("multiplatform") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    val dokkaVersion: String by System.getProperties()
    id("org.jetbrains.dokka") version dokkaVersion

    id("maven-publish")
}

val archivesName: String by project
base.archivesBaseName = archivesName

repositories {
    mavenCentral()
}

idea {
    module {
        isDownloadSources = true
    }
}

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(LEGACY) {
        useCommonJs()
    }

    val hostOs = System.getProperty("os.name")
    when {
        hostOs == "Mac OS X" -> macosX64("native")
        hostOs == "Linux" -> linuxX64("native")
        hostOs.startsWith("Windows") -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }
    
    sourceSets {
        val serializationVersion: String by project

        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }

        val commonMain by getting {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
            }
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

tasks.dokkaHtml.configure {
    outputDirectory.set(projectDir.resolve("docs"))

    suppressObviousFunctions.set(false)

    dokkaSourceSets {
        "commonMain" {
            sourceLink {
                localDirectory.set(file("src/commonMain/kotlin"))
                remoteUrl.set(URL("https://github.com/Peanuuutz/tomlkt/blob/master/src/commonMain/kotlin"))
                remoteLineSuffix.set("#L")
            }
        }
    }
}
