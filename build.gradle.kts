plugins {
    kotlin("jvm") version "2.0.0" apply false
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.compose.compiler) apply false
    id("org.jetbrains.kotlinx.kover") version "0.9.1" apply false
}

group = "dev.amaro.c3po"
version = "2.0.1"

allprojects {
    repositories {
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        google()
    }
}

subprojects {
    group = rootProject.group
    version = rootProject.version
}
