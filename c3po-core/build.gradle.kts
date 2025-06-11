plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
}

group = "dev.amaro.c3po"
version = "2.0.1"

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Core dependencies for business logic
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
    implementation("io.insert-koin:koin-core:4.0.2")
    implementation("dev.amaro:sonic:0.5.1")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

kover {
    reports {
        filters {
            excludes {
                classes(
                    "*.DebugMiddleware*"
                )
            }
        }
    }
}
