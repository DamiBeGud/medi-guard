# MediGuard

MediGuard is a native Android medication reminder app written in Kotlin. It helps users add medication reminders, receive exact notifications, confirm intake, and view a simple intake history.

The app is designed in German with a calm, accessible clinical interface for older adults.

## Features

- Add medication name, dosage, and reminder time
- View today’s medication reminders
- Confirm medication intake with `Eingenommen`
- Snooze reminders with `Später erinnern`
- View confirmed intakes in `Verlauf`
- Open medication details
- Edit, pause, or delete reminders
- Privacy/safety information screen
- Exact reminders using Android `AlarmManager`
- Direct Boot support after device restart

## Privacy Behavior

MediGuard separates sensitive and non-sensitive data:

- Medication names, dosages, and intake history are stored in Room using normal Credential Encrypted app storage.
- Minimal alarm metadata is stored in Device Protected Storage:
  - medication ID
  - reminder hour
  - reminder minute
  - alarm request code

Before the first unlock after a restart, notifications are generic only:

```text
Zeit für Ihre Medikamente!
```

After unlock, full medication details may be shown.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM with ViewModel and StateFlow
- Navigation Compose
- Room
- AlarmManager
- NotificationCompat
- Coroutines and Flow

## Project Structure

```text
app/src/main/java/com/medi/guard/
├── alarm/          # Exact alarms and Direct Boot receivers
├── data/           # Room, Direct Boot storage, repositories
├── notification/   # Reminder notifications
├── ui/             # Compose screens, navigation, theme, ViewModels
├── MainActivity.kt
└── MediGuardApplication.kt
```

## Build

Open the project in Android Studio and run:

```bash
./gradlew :app:assembleDebug
```

The debug APK is created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Run in Android Studio

1. Open the project in Android Studio.
2. Let Gradle sync finish.
3. Create or start an emulator in Device Manager.
4. Select the emulator/device.
5. Press Run.

## Checks

Run:

```bash
./gradlew check
```

This runs the available tests and Android lint checks.
