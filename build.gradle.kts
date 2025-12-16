plugins {
    kotlin("multiplatform") version "2.3.0"
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

version = "1.0.0"