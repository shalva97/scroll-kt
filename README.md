# Intro
Make mouse scroll on MacOS similar to how it works on Linux and Windows

# Run
Donwload latest binary from releases page and run it via terminal. Give it permission to run from System Settings. Then give permission for Accesibility and it should work. As long as the program will run the scroll events will be modified.

# Build
Clone the project, make sure you have Java installed, then run `./gradlew linkReleaseExecutableMacosX64` task. This will create a binary in `build/bin/macosX64/releaseExecutable/scroll-kt.kexe`, which you can run via terminal.

# Customize
In `main.kt` file there are 2 constants `MULTIPLIER_Y` and `MULTIPLIER_X`, which can be changed to any value and then recompile it.
