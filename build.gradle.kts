import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "dev.amaro"
version = "1.0-SNAPSHOT"
val mainClassName = "Mainkt"
val mainClassPath = "$group.$mainClassName"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("dev.amaro:sonic:0.4.1")
    implementation("com.composables.ui:menu:1.4.0")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "c3po"
            packageVersion = "1.0.0"
            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/main/resources"))
            macOS {
                iconFile.set(project.file("src/main/resources/icon.icns"))
                entitlementsFile.set(project.file("src/default.entitlements"))
            }
        }
    }
}

tasks {
    jar {
        val classpath = configurations.runtimeClasspath
        inputs.files(classpath).withNormalizer(ClasspathNormalizer::class.java)
        manifest {
            attributes(
                "Class-Path" to classpath.map { cp -> cp.joinToString(" ") { it.absolutePath } }
            )
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to archiveVersion,
                "Main-Class" to mainClasses,
            )
        }
    }
}
