rootProject.name = settings.extra["name"] as String

pluginManagement {
    val kotlinVersion = settings.extra["kotlin.version"] as String

    plugins {
        kotlin("multiplatform") version kotlinVersion
        id("org.jetbrains.dokka") version kotlinVersion
    }
}