import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform") version "1.9.0"
    id("org.jetbrains.compose") version "1.5.0"
}

group = "ru.itmo.graphics"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
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
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "nascar-95"
            packageVersion = "1.0.0"
        }
        jvmArgs(
            "-Xmx1600M",
            "-Xms1200M",
        )
    }
}