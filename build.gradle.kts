plugins {
    kotlin("multiplatform") version "1.8.20"
}

repositories {
    mavenCentral()
}

kotlin {
    macosX64 {
        binaries.executable {
            entryPoint = "main"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.5")
            }
        }
        val macosX64Main by getting
    }
}

version = "0.0.1"