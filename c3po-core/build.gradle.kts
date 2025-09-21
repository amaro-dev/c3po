plugins {
    kotlin("jvm")
    id("org.jetbrains.kotlinx.kover")
}

group = "dev.amaro.c3po"
version = rootProject.version

repositories {
    mavenCentral()
    google()
}

dependencies {
    // Core dependencies for business logic
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("io.insert-koin:koin-core:3.5.0")
    implementation("dev.amaro:sonic:0.5.1")
    implementation("io.sentry:sentry:7.0.0")
    implementation("commons-io:commons-io:2.11.0")

    // Logging dependencies
    implementation("org.slf4j:slf4j-api:2.0.12")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Analytics dependencies
    implementation("ly.count.sdk:java:24.1.1")

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
                    "*.DebugMiddleware*",
                )
            }
        }
    }
}
