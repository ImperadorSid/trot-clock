# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

TrotClock — standalone Wear OS walk/jog interval timer built with Jetpack Compose for Wear OS and Material 3. Single-module project targeting API 30+ (Android 11+).

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK
./gradlew installDebug           # Install debug APK to connected Wear device/emulator
./gradlew lint                   # Run Android lint
./gradlew connectedAndroidTest   # Run instrumented tests on device
```

## Architecture

- **Pattern**: MVVM + Clean Architecture (single-module)
- **Package**: `com.imperadorsid.runningtracker` (legacy name, app display name is TrotClock)

### Layer Overview

```
UI Layer (presentation/)
  Compose Screens ← ViewModels ← UiState sealed classes
       │
Service Layer (service/)
  RunTrackingService (foreground) — owns exercise state, exposes StateFlow
       │
Data Layer (data/)
  Repositories → Room DB (run history) / DataStore (preferences)
```

### Package Structure

```
com.imperadorsid.runningtracker/
├── data/
│   ├── repository/          # Repository implementations
│   └── local/
│       ├── db/              # Room database, DAOs, entities
│       └── datastore/       # DataStore preferences
├── domain/
│   ├── model/               # Domain models (Run, RunStats, LocationPoint)
│   └── repository/          # Repository interfaces
├── presentation/
│   ├── MainActivity.kt      # Entry point (ComponentActivity + setContent)
│   ├── navigation/          # Wear NavGraph (SwipeDismissableNavHost)
│   ├── screen/              # Feature screens with co-located ViewModels
│   ├── component/           # Reusable composables
│   └── theme/               # Wear Material 3 theme
├── service/
│   ├── RunTrackingService.kt      # Foreground service — source of truth for run state
│   └── OngoingActivityManager.kt  # Ongoing Activity indicator on watch face
└── di/                      # Dependency injection
```

### Key Design Decisions

- **Foreground service owns run state**, not ViewModels — Activity can be destroyed when wrist drops
- **UiState sealed classes** per screen (MVI-lite) to prevent impossible states
- **Ambient mode support** required — reduce updates to 1/min, simplify UI when screen dims
- **OngoingActivity** keeps the app visible on watch face during active runs
- **Swipe-to-dismiss** with confirmation dialog on tracking screen to prevent accidental run loss

## Testing

### Strategy

- **Prefer fakes over mocks** — manually implement repository interfaces for tests; more readable, catches more bugs, survives refactors
- **Extract service logic** into a testable manager class; keep `RunTrackingService` as a thin lifecycle/notification wrapper

### Test Source Sets

```
app/src/
├── test/                    # Unit tests (JVM, no device)
│   └── java/.../
│       ├── presentation/    # ViewModel state transitions
│       ├── domain/          # Use cases, pace/distance calculations
│       └── data/            # Repository logic, mappers
├── androidTest/             # Instrumented tests (Wear emulator)
│   └── java/.../
│       ├── presentation/    # Compose UI on round/square screens
│       ├── data/local/      # Room DAO with in-memory DB
│       └── service/         # Service binding
└── testFixtures/            # Shared fakes for both test/ and androidTest/
    └── java/.../fake/
```

### Test Libraries

| Library | Purpose |
|---------|---------|
| `kotlinx-coroutines-test` | `runTest`, `TestDispatcher` for coroutine-based logic |
| `turbine` | Assert `Flow` emissions in sequence |
| `mockk` | Mock dependencies where fakes are impractical |
| `compose-ui-test-junit4` | `ComposeTestRule`, `onNodeWithText`, `performClick` |
| `room-testing` | In-memory Room DB for DAO tests |

### Gradle Commands

```bash
./gradlew testDebugUnitTest        # JVM unit tests (fast, no device)
./gradlew connectedAndroidTest     # Instrumented tests (needs Wear emulator)
```

## CI (GitHub Actions)

### Pipeline Structure

```
push/PR → [unit-tests + lint] ──┐
                                 ├── (all must pass)
         [build APK]       ─────┤
                                 │
         [instrumented-tests] ──┘  (Wear OS emulator matrix)
```

### Wear OS Emulator on CI

- **System image**: must use `android-wear` target, not phone images
- **KVM acceleration**: required — enable via udev rules before emulator boot
- **Device profiles**: test on `wearos_large_round` (most common) and `wearos_small_round` (catches edge clipping)
- **API matrix**: `[30, 33]` — min SDK and a recent API level
- **Caching**: cache Gradle build (`gradle/actions/setup-gradle`) and AVD snapshots to cut ~2 min per run
- **Emulator flags**: `-no-window -gpu swiftshader_indirect -no-snapshot -noaudio -no-boot-anim` with animations disabled

### Key Notes

- `concurrency` group with `cancel-in-progress: true` to avoid wasting CI minutes on superseded pushes
- Upload test reports as artifacts on failure for debugging

## Build Configuration

- **Gradle**: 9.3.1 with Kotlin DSL
- **AGP**: 9.1.0, **Kotlin**: 2.2.10
- **Compile/Target SDK**: 36, **Min SDK**: 30
- **Version catalog**: `gradle/libs.versions.toml` — all dependency versions managed here
- **Compose BOM**: 2024.09.00, **Wear Compose Material3/Foundation**: 1.5.6

## Wear OS Specifics

- Uses `wear-sdk` library and `com.google.android.wearable`
- Manifest declares `android.hardware.type.watch` feature
- Splash screen via `core-splashscreen` library with custom theme in `res/values/styles.xml`
- Wear-specific Compose previews: `@WearPreviewDevices`, `@WearPreviewFontScales`
