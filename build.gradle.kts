plugins {
    kotlin("multiplatform") version "2.3.21"
}

repositories {
    mavenCentral()
}

kotlin {
    macosArm64 {
        binaries.executable {
            entryPoint = "com.example.scrollkt.main"
        }
    }

    sourceSets {
        val macosArm64Main by getting
    }
}

val packageApp by tasks.creating {
    group = "distribution"
    description = "Packages the executable into a macOS .app bundle"
    dependsOn("linkReleaseExecutableMacosArm64")

    doLast {
        val bundleName = "ScrollKT.app"
        val outputDir = layout.buildDirectory.dir("dist").get().asFile
        val appDir = outputDir.resolve(bundleName)
        val contentsDir = appDir.resolve("Contents")
        val macOSDir = contentsDir.resolve("MacOS")
        val resourcesDir = contentsDir.resolve("Resources")

        macOSDir.mkdirs()
        resourcesDir.mkdirs()

        val binary = layout.buildDirectory.file("bin/macosArm64/releaseExecutable/scroll-kt.kexe").get().asFile
        binary.copyTo(macOSDir.resolve("scroll-kt"), overwrite = true)
        macOSDir.resolve("scroll-kt").setExecutable(true)

        val iconFile = projectDir.resolve("AppIcon.icns")
        if (iconFile.exists()) {
            iconFile.copyTo(resourcesDir.resolve("AppIcon.icns"), overwrite = true)
        }

        val infoPlist = """
            <?xml version="1.0" encoding="UTF-8"?>
            <!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
            <plist version="1.0">
            <dict>
                <key>CFBundleExecutable</key>
                <string>scroll-kt</string>
                <key>CFBundleIconFile</key>
                <string>AppIcon</string>
                <key>CFBundleIdentifier</key>
                <string>com.example.scrollkt</string>
                <key>CFBundleName</key>
                <string>ScrollKT</string>
                <key>CFBundlePackageType</key>
                <string>APPL</string>
                <key>CFBundleShortVersionString</key>
                <string>${project.version}</string>
                <key>LSMinimumSystemVersion</key>
                <string>10.15</string>
                <key>LSUIElement</key>
                <true/>
            </dict>
            </plist>
        """.trimIndent()

        contentsDir.resolve("Info.plist").writeText(infoPlist)
        
        println("App bundle created at: ${appDir.absolutePath}")
    }
}

val packageInstaller by tasks.creating {
    group = "distribution"
    description = "Creates a macOS installer package (.pkg) for the app"
    dependsOn(packageApp)

    doLast {
        val bundleName = "ScrollKT.app"
        val outputDir = layout.buildDirectory.dir("dist").get().asFile
        val appDir = outputDir.resolve(bundleName)
        val pkgName = "ScrollKT.pkg"
        val pkgFile = outputDir.resolve(pkgName)

        val process = ProcessBuilder(
            "pkgbuild",
            "--component", appDir.absolutePath,
            "--install-location", "/Applications",
            pkgFile.absolutePath
        ).inheritIO().start()

        val exitCode = process.waitFor()
        if (exitCode == 0) {
            println("Installer package created at: ${pkgFile.absolutePath}")
        } else {
            throw GradleException("Failed to create installer package, exit code: $exitCode")
        }
    }
}

version = "1.0.0"