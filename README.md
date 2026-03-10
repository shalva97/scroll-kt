# Scroll-KT

Scroll-KT is a macOS application written in Kotlin/Native that makes mouse scrolling behavior more closely match Windows and Linux by intercepting and modifying scroll events.

## Features

- **Windows-like Scroll**: Modifies scroll wheel events to be more consistent and predictable.
- **System Tray Icon**: Runs as a background "Accessory" application with a convenient tray icon.
- **Start at Login**: Easily toggle automatic launch when macOS starts (using modern `SMAppService` API).
- **Smart Permission Handling**: 
    - Automatically detects if Accessibility permissions are missing.
    - Provides a direct link to macOS Accessibility settings from the tray menu.
    - Automatically retries and starts working as soon as permissions are granted (no restart required).
- **Dynamic Tray Menu**: Shows real-time status and errors (e.g., if a login item fails to register).
- **Standalone App Bundle**: Can be packaged as a standard `.app` for the `/Applications` folder.
- **Installer Package**: Supports building a `.pkg` installer for easy distribution.

## How to Build

### Prerequisites
- macOS with Apple Silicon (M1/M2/M3)
- JDK 11 or higher

### Packaging the App
To create a standalone `ScrollKT.app` bundle:
```bash
./gradlew packageApp
```
The app will be available at `build/dist/ScrollKT.app`. You can move it to your `/Applications` folder.

### Creating an Installer
To create a `.pkg` installer:
```bash
./gradlew packageInstaller
```
The installer will be available at `build/dist/ScrollKT.pkg`.

## Usage

1. Launch **ScrollKT**.
2. If it's your first time, you'll see a tray icon with a warning ⚠️ about **Accessibility permissions**.
3. Click the tray icon and select **"Grant Accessibility permission"**.
4. In System Settings, enable **ScrollKT** under **Privacy & Security > Accessibility**.
5. The app will automatically start working once the toggle is enabled.
6. (Optional) Enable **"Start at Login"** from the tray menu to have the app start automatically.

## Project Structure

- `src/macosArm64Main/kotlin/com/example/scrollkt/`
    - `Main.kt`: Application entry point and lifecycle management.
    - `AppDelegate.kt`: Handles the tray icon, menu updates, and system integrations.
    - `EventTap.kt`: Contains the core logic for intercepting and modifying scroll events.
- `AppIcon.icns`: Abstract application icon.
- `build.gradle.kts`: Build configuration including packaging tasks.

## Customization

In `EventTap.kt`, you can modify `MULTIPLIER_Y` and `MULTIPLIER_X` constants to adjust the scroll speed/sensitivity:
```kotlin
private const val MULTIPLIER_Y = 4L
private const val MULTIPLIER_X = 4L
```
After making changes, rebuild the app using `./gradlew packageApp`.
