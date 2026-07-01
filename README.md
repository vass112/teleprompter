# Teleprompter

A modern, minimal, elegant Android teleprompter application that displays a scrolling script as a floating overlay on top of any camera application.

Record videos with any camera app (Samsung Camera, Google Camera, Instagram, TikTok, WhatsApp, Open Camera) while reading your script from the floating overlay.

## Features

- **Floating Overlay** - Works above every app using SYSTEM_ALERT_WINDOW permission
- **Script Editor** - Create, edit, delete, duplicate, and rename scripts with autosave
- **Auto Scroll** - Smooth 60 FPS pixel-perfect scrolling with adjustable speed
- **Draggable** - Drag the overlay anywhere on screen
- **Resizable** - Resize by dragging corners
- **Minimizable** - Collapse into a small floating bubble
- **Play / Pause / Stop** - Full transport controls
- **Text Customization** - Font size, color, bold, line spacing, alignment
- **Dark Mode** - Elegant AMOLED black theme with blue accent
- **Reading Indicator** - Highlight current reading line
- **Lock Mode** - Prevent accidental movement
- **Gesture Support** - One-finger drag, two-finger resize, double-tap play/pause
- **Search** - Search within scripts
- **Favorites** - Mark scripts as favorites
- **Word Counter** - Live word and character count
- **Estimated Reading Time** - Auto-calculated

## Screenshots

*(Add screenshots here)*

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM
- **Database:** Room
- **DI:** Hilt
- **State:** StateFlow + Coroutines
- **Navigation:** Navigation Compose
- **Min SDK:** Android 10 (API 29)
- **Target SDK:** Android 14 (API 34)

## Project Structure

```
app/
├── src/main/
│   ├── java/com/teleprompter/app/
│   │   ├── di/              # Dependency injection modules
│   │   ├── data/
│   │   │   ├── db/          # Room database, DAOs, entities
│   │   │   └── repository/  # Data repositories
│   │   ├── ui/
│   │   │   ├── theme/       # Colors, typography, shapes, theme
│   │   │   ├── navigation/  # Navigation graph
│   │   │   ├── screens/
│   │   │   │   ├── scriptlist/  # Script list screen
│   │   │   │   ├── editor/      # Script editor screen
│   │   │   │   └── settings/    # Settings screen
│   │   │   └── components/  # Reusable composables
│   │   ├── service/         # Floating overlay service
│   │   └── util/            # Utilities
│   ├── res/                 # Resources
│   └── AndroidManifest.xml
├── build.gradle.kts
└── settings.gradle.kts
```

## Building

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/vass112/teleprompter.git
   ```

2. Open the project in Android Studio.

3. Sync Gradle and let it download dependencies.

4. Build and run on a device or emulator running Android 10+.

### Building from command line

```bash
./gradlew assembleDebug
```

## Usage

1. Open the app
2. Create a new script or select an existing one
3. Grant the "Display over other apps" permission when prompted
4. Tap "Start Overlay"
5. Open any camera app (Samsung Camera, Instagram, TikTok, etc.)
6. Start recording
7. Read your script from the floating overlay
8. Use the controls to play, pause, adjust speed, or minimize the overlay

## Permissions

- **SYSTEM_ALERT_WINDOW** - Required to display the floating overlay above other apps
- **FOREGROUND_SERVICE** - Required to keep the overlay running reliably

## License

MIT License
