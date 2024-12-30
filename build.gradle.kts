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
    testImplementation(platform("org.junit:junit-bom:5.11.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("com.willowtreeapps.assertk:assertk:0.28.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg)
            packageName = "c3po"
            packageVersion = "1.2.1"
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
