plugins {
    // Kotlin language & docs
    kotlin("multiplatform") version "1.6.10"
    id("org.jetbrains.dokka") version "1.6.10"

    // Publishing to Sonatype & Maven Central.
    id("java")
    id("signing")
    id("maven-publish")
}

apply(from = "gradle/publish.gradle.kts")

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