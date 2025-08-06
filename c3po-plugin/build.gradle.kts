plugins {
    kotlin("jvm")
    id("org.jetbrains.intellij") version "1.17.2"
}

group = "dev.amaro.c3po"
version = "2.0.1"

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Core module dependency
    implementation(project(":c3po-core"))

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

intellij {
    version.set("2023.2")
    type.set("IC") // IntelliJ Community for now, can change to AI later
    plugins.set(listOf("android"))
}

tasks {
    // Ensure core module is included in the plugin JAR
    jar {
        from(
            configurations.runtimeClasspath
                .get()
                .filter {
                    it.name.contains("c3po-core")
                }.map { zipTree(it) },
        )
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    // Include core module in sandbox for testing
    prepareSandbox {
        val coreProject = project(":c3po-core")
        from(
            coreProject.tasks.jar
                .get()
                .outputs.files,
        ) {
            into("${pluginName.get()}/lib")
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    // Include core module in final plugin distribution
    buildPlugin {
        val coreProject = project(":c3po-core")
        from(
            coreProject.tasks.jar
                .get()
                .outputs.files,
        ) {
            into("lib")
        }
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("252.*") // Updated to support newer Android Studio versions
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }

    test {
        useJUnitPlatform()
    }
}
