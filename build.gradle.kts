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
        val macosX64Main by getting
    }
}

version = "0.0.1"