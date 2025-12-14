# Aido

An Android application built with Kotlin and Gradle.

## Prerequisites

- Android Studio Arctic Fox or later
- JDK 11 or higher
- Android SDK (API level as specified in build.gradle.kts)
- Gradle 7.0 or higher

## Setup

1. Clone the repository
2. Open the project in Android Studio
3. Sync Gradle files
4. Wait for dependencies to download

## Building the Project

### Using Android Studio
- Click on **Build** > **Make Project** or press `Ctrl+F9`
- To build APK: **Build** > **Build Bundle(s) / APK(s)** > **Build APK(s)**

### Using Command Line

**Windows:**
```bash
gradlew assembleDebug
```

**Linux/Mac:**
```bash
./gradlew assembleDebug
```

## Running the App

### Using Android Studio
- Connect an Android device or start an emulator
- Click the **Run** button or press `Shift+F10`

### Using Command Line
```bash
gradlew installDebug
```
<!--
## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/          # Application source code
│   │   ├── res/           # Resources (layouts, drawables, etc.)
│   │   └── AndroidManifest.xml
│   ├── androidTest/       # Instrumented tests
│   └── test/              # Unit tests
├── build.gradle.kts       # App module build configuration
└── proguard-rules.pro     # ProGuard rules
```
-->
## Technologies Used

- **Language:** Kotlin
- **Build System:** Gradle (Kotlin DSL)
- **Minimum SDK:** (Check app/build.gradle.kts)
- **Target SDK:** (Check app/build.gradle.kts)

## Testing

### Run Unit Tests
```bash
gradlew test
```

### Run Instrumented Tests
```bash
gradlew connectedAndroidTest
```

