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

val archivesName: String by project
base.archivesName.set(archivesName)

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    js(IR) {
        browser()
        nodejs()
    }

    mingwX64()
    macosArm64()
    macosX64()
    ios()
    linuxX64()

    sourceSets {
        val serializationVersion: String by project

        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
        }

        getByName("commonMain") {
            dependencies {
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:$serializationVersion")
            }
        }

        getByName("commonTest") {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }

        getByName("jvmTest") {
            dependencies {
                implementation(kotlin("test-junit5"))
                implementation("org.junit.jupiter:junit-jupiter-api:5.6.0")

                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
            }
        }

        getByName("jsTest") {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
    }
}

val docsDir = projectDir.resolve("docs")

tasks {
    withType<KotlinJvmTest> {
        useJUnitPlatform()
    }

    dokkaHtml {
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
    config = files("$projectDir/format/detekt.yml")
}

afterEvaluate {
    configure<PublishingExtension> {
        publications.withType<MavenPublication> {
            val classifier = if (name.contains("multiplatform", true)) "" else "-$name"
            artifactId = "tomlkt$classifier"
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")

            credentials {
                username = System.getProperty("mavenCentralUsername")
                password = System.getProperty("mavenCentralPassword")
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
    val key = System.getProperty("inMemorySigningKey")
    if (key != null) {
        val password = System.getProperty("inMemorySigningKeyPassword")
        useInMemoryPgpKeys(key, password)
    }
    sign(publishing.publications)
}
