import io.gitlab.arturbosch.detekt.Detekt
import org.jetbrains.kotlin.gradle.targets.jvm.tasks.KotlinJvmTest
import java.net.URL

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")

    id("io.gitlab.arturbosch.detekt")

    `maven-publish`
    signing
}

val archivesName: String by rootProject
base.archivesName.set(archivesName)

kotlin {
    explicitApi()

    jvm {
        compilations {
            all {
                kotlinOptions {
                    jvmTarget = "1.8"
                }
            }
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

    sourceSets {
        val serializationVersion: String by rootProject
        val datetimeVersion: String by rootProject

        applyDefaultHierarchyTemplate()

        all {
            languageSettings {
                optIn("kotlin.RequiresOptIn")
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
    }
}

val docsDir = rootDir.resolve("docs")

tasks {
    withType<KotlinJvmTest> {
        useJUnitPlatform()
    }

    dokkaHtml {
        moduleName.set("tomlkt")

        outputDirectory.set(docsDir)

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

    create<Delete>("deleteOldDocs") {
        group = "documentation"
        delete(docsDir)
    }

    create<Jar>("createJavadocByDokka") {
        group = "documentation"
        dependsOn("deleteOldDocs", dokkaHtml)
        archiveClassifier.set("javadoc")
        from(docsDir)
    }

    withType<Detekt> {
        reports {
            sarif.required.set(false)
            xml.required.set(false)
            html.required.set(false)
            md.required.set(false)
        }
    }

    check {
        dependsOn(getByName("detektMetadataMain"))
    }
}

detekt {
    config.from(files("$rootDir/format/detekt.yml"))
}

afterEvaluate {
    configure<PublishingExtension> {
        publications.withType<MavenPublication> {
            val classifier = if (name.contains("multiplatform", true)) "" else "-$name"
            artifactId = "tomlkt$classifier"
        }
    }
}

val systemUsername = findProperty("mavenCentralUsername")?.toString()
val systemPassword = findProperty("mavenCentralPassword")?.toString()
val systemSigningKey = findProperty("inMemorySigningKey")?.toString()
val systemSigningPassword = findProperty("inMemorySigningKeyPassword")?.toString()

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
            artifact(tasks["createJavadocByDokka"])

            pom {
                name.set("tomlkt")
                description.set("TOML support for kotlinx.serialization")
                url.set("https://github.com/Peanuuutz/tomlkt")

                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0")
                    }
                }

                issueManagement {
                    system.set("Github")
                    url.set("https://github.com/Peanuuutz/tomlkt/issues")
                }

                scm {
                    connection.set("https://github.com/Peanuuutz/tomlkt.git")
                    url.set("https://github.com/Peanuuutz/tomlkt")
                }

                developers {
                    developer {
                        name.set("Peanuuutz")
                    }
                }
            }
        }
    }
}

signing {
    if (systemUsername != null) {
        useInMemoryPgpKeys(systemSigningKey, systemSigningPassword)
    }
    sign(publishing.publications)
}
