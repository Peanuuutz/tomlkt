import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import java.net.URL

// Plugins

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")

    id("io.gitlab.arturbosch.detekt")

    `maven-publish`
    signing
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

tasks {
    dokkaHtml {
        moduleName = "tomlkt"

        outputDirectory = docsDir

        dokkaSourceSets {
            "commonMain" {
                sourceLink {
                    localDirectory = file("src/commonMain/kotlin")
                    remoteUrl = URL("https://github.com/Peanuuutz/tomlkt/blob/master/src/commonMain/kotlin")
                    remoteLineSuffix = "#L"
                }
            }
        }
    }

    create<Delete>("deleteOldDocs") {
        group = "documentation"
        delete(docsDir)
    }

    create<Jar>("createJavadocByDokka") {
        group = "documentation"
        dependsOn("deleteOldDocs", dokkaHtml)
        archiveClassifier = "javadoc"
        from(docsDir)
    }
}

// Signing

val systemUsername = findProperty("mavenCentralUsername")?.toString()
val systemPassword = findProperty("mavenCentralPassword")?.toString()
val systemSigningKey = findProperty("inMemorySigningKey")?.toString()
val systemSigningPassword = findProperty("inMemorySigningKeyPassword")?.toString()

signing {
    if (systemUsername != null) {
        useInMemoryPgpKeys(systemSigningKey, systemSigningPassword)
    }
    sign(publishing.publications)
}

tasks {
    val signTasks = withType<Sign>().toTypedArray()

    withType<AbstractPublishToMaven> {
        dependsOn(*signTasks)
    }
}

// Publication

publishing {
    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")

            credentials {
                username = systemUsername ?: System.getProperty("mavenCentralUsername")
                password = systemPassword ?: System.getProperty("mavenCentralPassword")
            }
        }
    }

    publications {
        withType<MavenPublication> {
            val classifier = if (name.contains("multiplatform", true)) "" else "-$name"
            artifactId = "tomlkt$classifier"

            artifact(tasks["createJavadocByDokka"])

            pom {
                name = "tomlkt"
                description = "TOML support for kotlinx.serialization"
                url = "https://github.com/Peanuuutz/tomlkt"

                licenses {
                    license {
                        name = "Apache-2.0"
                        url = "https://www.apache.org/licenses/LICENSE-2.0"
                    }
                }

                issueManagement {
                    system = "Github"
                    url = "https://github.com/Peanuuutz/tomlkt/issues"
                }

                scm {
                    connection = "https://github.com/Peanuuutz/tomlkt.git"
                    url = "https://github.com/Peanuuutz/tomlkt"
                }

                developers {
                    developer {
                        name = "Peanuuutz"
                    }
                }
            }
        }
    }
}
