import org.jetbrains.kotlin.gradle.targets.js.testing.KotlinJsTest

plugins {
    kotlin("multiplatform") version "1.6.10"
    java
    signing
    `maven-publish`
}

group = "me.nullicorn"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    js(IR) {
        binaries.executable()

        val testConfig: KotlinJsTest.() -> Unit = { useMocha { timeout = "10000" } }
        nodejs { testTask(testConfig) }
        browser { testTask(testConfig) }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
    }
}

publishing {
    repositories {
        maven {
            val repoId =
                if (version.toString().endsWith("SNAPSHOT")) "snapshot"
                else "release"

            url = uri(project.extra["repo.$repoId.url"] as String)
        }
    }

    publications {
        create<MavenPublication>("maven") {
            val authorUrl = project.extra["author.url"] as String

            pom {
                url.set("https://$authorUrl/$name")

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
                    url.set("https://$authorUrl/$name/tree/master")
                    connection.set("scm:git:git://$authorUrl/$name.git")
                    developerConnection.set("scm:git:ssh://$authorUrl/$name.git")
                }
            }
        }
    }
}

tasks {
    val javadocJar by creating(Jar::class) {
        dependsOn.add(javadoc)
        archiveClassifier.set("javadoc")
        from(javadoc)
    }

    artifacts {
        archives(sourcesJar)
        archives(javadocJar)
        archives(jar)
    }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(tasks["jar"], tasks["sourcesJar"], tasks["javadocJar"])
}