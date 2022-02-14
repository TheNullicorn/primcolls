import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    // Kotlin language & docs
    kotlin("multiplatform") version "1.6.10"
    id("org.jetbrains.dokka") version "1.6.10"

    // Publishing to Sonatype & Maven Central.
    id("java")
    id("signing")
    id("maven-publish")
}

group = "me.nullicorn"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    // This has to go before JVM compilations, or else the "generator" compilation can't find its
    // source set.
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        // Code generator's source.
        val jvmGenerator by sourceSets.creating {
            kotlin.srcDir("src/generator/kotlin")
            resources.srcDir("src/generator/resources")

            dependencies {
                implementation(kotlin("stdlib"))
            }
        }
    }

    jvm {
        compilations {
            // Target Java 8.
            all { kotlinOptions.jvmTarget = "1.8" }

            // Regular jvmMain compilation.
            val main by getting

            // Separate compilation for the code generator.
            val generator by compilations.creating {
                val compilationName = name

                val runGenerator by tasks.registering(JavaExec::class) {
                    dependsOn("compileGeneratorKotlinJvm", "compileKotlinJvm")

                    mainClass.set("GeneratorKt")
                    classpath = files(
                        runtimeDependencyFiles,
                        "$buildDir/classes/kotlin/jvm/$compilationName",
                    )

                    val generatorInputDir = sourceSets["jvmGenerator"].resources.srcDirs.first()
                    val generatorOutputDir = sourceSets["commonMain"].kotlin.srcDirs.first()

                    args = "--input $generatorInputDir --output $generatorOutputDir"
                        .split(' ')
                        .toList()
                }
            }
        }

        // Use JUnit for unit tests
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(BOTH) {
        nodejs()
        browser()
    }
}

// KDoc HTML generation; required for Maven Central.
val dokkaHtml by tasks.getting(DokkaTask::class)
val javadocJar by tasks.registering(Jar::class) {
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
    from(dokkaHtml.outputDirectory)
}

publishing {
    // Add extra metadata for the JVM jar's pom.xml.
    publications.withType<MavenPublication> {
        val authorUrl = project.extra["author.url"] as String
        val projectName = project.extra["name"] as String
        val projectUrl = "$authorUrl/$projectName"

        artifact(tasks["javadocJar"])

        pom {
            url.set("https://$projectUrl")

            developers {
                developer {
                    name.set("TheNullicorn")
                    email.set("bennullicorn@gmail.com")
                }
            }

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/mit-license.php")
                }
            }

            scm {
                url.set("https://$projectUrl/tree/main")
                connection.set("scm:git:git://$projectUrl.git")
                developerConnection.set("scm:git:ssh://$projectUrl.git")
            }
        }
    }

    repositories {
        maven {
            name = "ossrh"

            val repoId =
                if (version.toString().endsWith("SNAPSHOT")) "snapshot"
                else "release"
            url = uri(project.extra["repo.$repoId.url"] as String)

            credentials {
                // Both values should be set in "~/.gradle/gradle.properties".
                username = (project.extra["ossrhUsername"] as String)
                password = (project.extra["ossrhPassword"] as String)
            }
        }
    }
}

// Sign all of our artifacts for Nexus.
signing {
    sign(publishing.publications)
}