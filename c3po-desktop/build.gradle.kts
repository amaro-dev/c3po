import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.22"
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("io.sentry.jvm.gradle") version "5.2.0"
    id("org.jetbrains.kotlinx.kover")
}

group = "dev.amaro.c3po"
version = rootProject.version
val mainClassName = "MainKt"
val mainClassPath = "$group.$mainClassName"

// Read analytics defaults from environment (provided by CI)
val analyticsServerUrlEnv = providers.environmentVariable("C3PO_ANALYTICS_SERVER_URL").orNull
val analyticsAppKeyEnv = providers.environmentVariable("C3PO_ANALYTICS_APP_KEY").orNull

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

// Ensure Android-specific coroutines are not pulled into the desktop runtime
configurations.all {
    exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-android")
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
    implementation(compose.material3)
    implementation(compose.material)
    implementation(compose.materialIconsExtended)

    implementation("org.jetbrains.compose.runtime:runtime:1.7.3")
    implementation(project.dependencies.platform("io.insert-koin:koin-bom:4.0.2"))
    implementation("io.insert-koin:koin-core")
    implementation("com.composables.ui:menu:1.4.0")


    // JSON serialization for GitHub API
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    // BouncyCastle for Ed25519 signature verification
    implementation("org.bouncycastle:bcprov-jdk18on:1.78")

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
        // Embed analytics defaults into launcher as JVM args only when provided
        if (!analyticsServerUrlEnv.isNullOrBlank() && !analyticsAppKeyEnv.isNullOrBlank()) {
            jvmArgs(
                "-Danalytics.serverUrl=$analyticsServerUrlEnv",
                "-Danalytics.appKey=$analyticsAppKeyEnv",
            )
        }
        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "c3po"
            packageVersion = project.version.toString()
            modules("java.base", "java.desktop", "java.net.http", "java.naming", "java.security.jgss")
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
                "Main-Class" to mainClassPath,
            )
        }
    }

    // Custom task to create ZIP distribution for auto-updates
    register<Zip>("packageZipDistribution") {
        group = "distribution"
        description = "Creates a ZIP distribution for auto-updates (preserves security attributes)"
        
        dependsOn("createDistributable")

        // Re-sign app bundle with correct Runtime Version before creating ZIP
        doFirst {
            val appDir = layout.buildDirectory.dir("compose/binaries/main/app/c3po.app").get().asFile
            if (appDir.exists()) {
                println("Re-signing app bundle with Runtime Version 11.1.0 for ZIP distribution...")

                try {
                    val reSignCommand = listOf(
                        "/usr/bin/codesign",
                        "--force",
                        "--sign", "-",
                        "--preserve-metadata=identifier,entitlements,flags",
                        "--options", "runtime",
                        "--runtime-version", "11.1.0",
                        appDir.absolutePath
                    )

                    val process = ProcessBuilder(reSignCommand)
                        .redirectErrorStream(true)
                        .start()

                    val exitCode = process.waitFor()
                    val output = process.inputStream.bufferedReader().readText()

                    if (exitCode != 0) {
                        println("Warning: Failed to re-sign with specific runtime version ($exitCode): $output")

                        // Fallback: preserve original runtime
                        val fallbackCommand = listOf(
                            "/usr/bin/codesign",
                            "--force",
                            "--sign", "-",
                            "--preserve-metadata=identifier,entitlements,flags,runtime",
                            appDir.absolutePath
                        )

                        val fallbackProcess = ProcessBuilder(fallbackCommand)
                            .redirectErrorStream(true)
                            .start()

                        val fallbackExitCode = fallbackProcess.waitFor()
                        if (fallbackExitCode == 0) {
                            println("Successfully re-signed with fallback method (preserving original runtime)")
                        } else {
                            println("Warning: Fallback re-signing also failed")
                        }
                    } else {
                        println("Successfully re-signed app bundle with Runtime Version 11.1.0")
                    }

                } catch (e: Exception) {
                    println("Warning: Exception during app bundle re-signing: ${e.message}")
                }
            }
        }
        
        from(layout.buildDirectory.dir("compose/binaries/main/app"))
        archiveFileName.set("c3po-${project.version}-macos.zip")
        destinationDirectory.set(layout.buildDirectory.dir("compose/binaries/main"))
        
        // Preserve file permissions and symbolic links
        includeEmptyDirs = true
        isPreserveFileTimestamps = true
    }

// Apply verification tasks from separate file
    apply(from = "verification.gradle.kts")

// Integrate verification into build pipeline
    build {
        dependsOn("validateModules")  // Static analysis validation
    }
}
