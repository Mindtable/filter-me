import org.jetbrains.compose.desktop.application.dsl.TargetFormat

val composeVersion = "1.5.1"

plugins {
    kotlin("multiplatform") version "1.9.10"
    id("org.jetbrains.compose") version "1.5.1" // I had to duplicate it bc gradle.kts have no special tooling for that
}

group = "ru.itmo.graphics"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    val osName = System.getProperty("os.name")
    val targetOs = when {
        osName == "Mac OS X" -> "macos"
        osName.startsWith("Win") -> "windows"
        osName.startsWith("Linux") -> "linux"
        else -> error("Unsupported OS: $osName")
    }

    val targetArch = when (val osArch = System.getProperty("os.arch")) {
        "x86_64", "amd64" -> "x64"
        "aarch64" -> "arm64"
        else -> error("Unsupported arch: $osArch")
    }

    val version = "0.7.70" // or any more recent version
    val target = "$targetOs-$targetArch"

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation(compose.material3)
                implementation("org.jetbrains.compose.ui:ui-tooling-preview:$composeVersion")
                implementation("org.jetbrains.skiko:skiko-awt-runtime-$target:$version")
                implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
                implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
                implementation("org.apache.logging.log4j:log4j-api:2.20.0")
                implementation("org.apache.logging.log4j:log4j-core:2.20.0")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb, TargetFormat.Exe)
            packageName = "nascar-95"
            packageVersion = "1.0.0"
        }
        jvmArgs(
            "-Dapple.awt.application.appearance=system",
        )
    }
}
