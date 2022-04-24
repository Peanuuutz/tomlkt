import java.net.URL

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.dokka")
    kotlin("plugin.serialization")

    idea
    `maven-publish`
    signing
}

val archivesName: String by project
base.archivesBaseName = archivesName

repositories {
    mavenCentral()
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

val docsDir = projectDir.resolve("docs")

tasks {
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
}

afterEvaluate {
    configure<PublishingExtension> {
        publications.all {
            (this as? MavenPublication)?.artifactId = project.name +
                    "-$name".takeUnless { "kotlinMultiplatform" in it }.orEmpty()
        }
    }
}

publishing {
    repositories {
        maven {
            url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
            credentials {
                username = System.getProperty("credentials.username")
                password = System.getProperty("credentials.password")
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
    sign(publishing.publications)
}