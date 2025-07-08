@file:OptIn(ExperimentalWasmDsl::class)

import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import java.net.URI

// Plugins

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")

    id("io.gitlab.arturbosch.detekt")

    id("io.deepmedia.tools.deployer")
}

// Archives Metadata

val archivesName: String by rootProject
base.archivesName = archivesName

// Kotlin

kotlin {
    explicitApi()

    jvm {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget = JvmTarget.JVM_1_8
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    mingwX64()
    macosArm64()
    macosX64()
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxArm64()
    linuxX64()
    wasmJs {
        browser()
        nodejs()
    }

    sourceSets {
        val serializationVersion: String by rootProject
        val datetimeVersion: String by rootProject

        applyDefaultHierarchyTemplate()

        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
                optIn("kotlin.ExperimentalSubclassOptIn")
                optIn("kotlin.contracts.ExperimentalContracts")
                optIn("kotlinx.serialization.ExperimentalSerializationApi")
                optIn("kotlinx.serialization.InternalSerializationApi")
                optIn("net.peanuuutz.tomlkt.TomlSpecific")
            }
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

        val kotlinxMain by creating {
            dependsOn(commonMain)

            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion")
            }
        }

        val jsMain by getting {
            dependsOn(kotlinxMain)
        }

        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }

        val mingwX64Main by getting {
            dependsOn(kotlinxMain)
        }

        val macosArm64Main by getting {
            dependsOn(kotlinxMain)
        }

        val macosX64Main by getting {
            dependsOn(kotlinxMain)
        }

        val iosX64Main by getting {
            dependsOn(kotlinxMain)
        }

        val iosArm64Main by getting {
            dependsOn(kotlinxMain)
        }

        val iosSimulatorArm64Main by getting {
            dependsOn(kotlinxMain)
        }

        val linuxArm64Main by getting {
            dependsOn(kotlinxMain)
        }

        val linuxX64Main by getting {
            dependsOn(kotlinxMain)
        }

        val wasmJsMain by getting {
            dependsOn(kotlinxMain)
        }

        val wasmJsTest by getting {
            dependencies {
                implementation(kotlin("test-wasm-js"))
            }
        }
    }
}

// Linter

detekt {
    config.from(files("$rootDir/format/detekt.yml"))
}

tasks {
    withType<Detekt> {
        reports {
            sarif.required = false
            xml.required = false
            html.required = false
            md.required = false
        }
    }
}

// Tests

tasks {
    withType<KotlinJvmTest> {
        useJUnitPlatform()
    }

    check {
        dependsOn(getByName("detektMetadataMain"))
    }
}

// Documentation

val docsDir = rootDir.resolve("docs")

dokka {
    moduleName = "tomlkt"

    dokkaSourceSets {
        commonMain {
            sourceLink {
                localDirectory = file("src/commonMain/kotlin")
                remoteUrl = URI("https://github.com/Peanuuutz/tomlkt/blob/master/src/commonMain/kotlin")
                remoteLineSuffix = "#L"
            }
        }
    }

    dokkaPublications {
        html {
            outputDirectory = docsDir
        }
    }
}

tasks {
    create<Delete>("deleteOldDocs") {
        group = "documentation"
        delete(docsDir)
    }

    create<Jar>("createJavadocByDokka") {
        group = "documentation"
        dependsOn("deleteOldDocs", "dokkaGenerate")
        archiveClassifier = "javadoc"
        from(docsDir)
    }
}

// Deployment

deployer {
    centralPortalSpec {
        content {
            kotlinComponents {
                docs(tasks["createJavadocByDokka"])

                projectInfo {
                    name = "tomlkt"
                    description = "TOML support for kotlinx.serialization"
                    url = "https://github.com/Peanuuutz/tomlkt"

                    license {
                        name = "Apache-2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }

                    scm {
                        fromGithub("Peanuuutz", "tomlkt")
                    }

                    developer {
                        name = "Peanuuutz"
                    }
                }
            }
        }

        auth {
            user = secret("mavenCentralUsername")
            password = secret("mavenCentralPassword")
        }

        signing {
            key = secret("inMemorySigningKey")
            password = secret("inMemorySigningKeyPassword")
        }

        allowMavenCentralSync = false
    }
}

//publishing {
//    publications {
//        withType<MavenPublication> {
//            val classifier = if (name.contains("multiplatform", true)) "" else "-$name"
//            artifactId = "tomlkt$classifier"
//        }
//    }
//}
