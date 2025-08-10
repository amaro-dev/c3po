import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("io.sentry.jvm.gradle") version "5.2.0"
    id("org.jetbrains.kotlinx.kover")
}

group = "dev.amaro.c3po"
version = "2.0.1"
val mainClassName = "Mainkt"
val mainClassPath = "$group.$mainClassName"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

sentry {
    includeSourceContext = true
    org = "amaro-dev"
    projectName = "C3PO"
    authToken = System.getenv("SENTRY_AUTH_TOKEN")
        ?: "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"
}

dependencies {
    // Core module dependency
    implementation(project(":c3po-core"))

    // Sonic library for Redux/State Management
    implementation("dev.amaro:sonic:0.5.1")

    // Desktop UI dependencies
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.runtime:runtime:1.7.3")
    implementation(project.dependencies.platform("io.insert-koin:koin-bom:4.0.2"))
    implementation("io.insert-koin:koin-core")
    implementation("com.composables.ui:menu:1.4.0")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "c3po"
            packageVersion = project.version.toString()
            appResourcesRootDir.set(project.layout.projectDirectory.dir("src/main/resources"))
            macOS {
                iconFile.set(project.file("src/main/resources/icon.icns"))
                entitlementsFile.set(project.file("src/default.entitlements"))
            }
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "ui.*",
                    "socket.SocketClient*",
                    "di.*",
                    "ui.**.*",
                    "*.DebugMiddleware*",
                )
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
                "Class-Path" to classpath.map { cp -> cp.joinToString(" ") { it.absolutePath } },
            )
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to archiveVersion,
                "Main-Class" to mainClasses,
            )
        }
    }
}
