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
  RunTrackingService (foreground) — owns SessionTimer, exposes StateFlow
       │
Domain Layer (domain/)
  Models (Session, IntervalPattern, TimerStep) + SessionTimer + Repository interfaces
       │
Data Layer (data/)
  Repositories → Room DB (session history)
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
│   ├── model/               # Domain models (Session, IntervalPattern, TimerStep, IntervalType, TimerPhase)
│   ├── timer/               # SessionTimer + TimerState sealed class
│   ├── repository/          # Repository interfaces
│   └── util/                # Clock abstraction
├── presentation/
│   ├── MainActivity.kt      # Entry point (ComponentActivity + setContent)
│   ├── navigation/          # Wear NavGraph (SwipeDismissableNavHost)
│   ├── screen/              # Feature screens with co-located ViewModels + UiState
│   ├── util/                # DurationFormat utility
│   └── theme/               # Wear Material 3 theme
├── service/
│   ├── RunTrackingService.kt      # Foreground service — thin lifecycle/notification wrapper
│   ├── SessionManager.kt         # Testable session logic: action handling, timer orchestration
│   ├── NotificationHelper.kt      # Notification channel + builder with pause/resume/stop actions
│   └── OngoingActivityManager.kt  # Ongoing Activity indicator on watch face
└── di/                      # Dependency injection
```

### Key Design Decisions

- **Foreground service owns run state**, not ViewModels — Activity can be destroyed when wrist drops
- **SessionManager** holds testable business logic; `RunTrackingService` is a thin lifecycle/notification wrapper
- **UiState sealed classes** per screen (MVI-lite) to prevent impossible states
- **Ambient mode support** required — reduce updates to 1/min, simplify UI when screen dims
- **OngoingActivity** keeps the app visible on watch face during active runs
- **Swipe-to-dismiss** with confirmation dialog on tracking screen to prevent accidental run loss

### Service Actions

`RunTrackingService` responds to intent actions:

| Action | Extra | Behavior |
|--------|-------|----------|
| `ACTION_START` | `EXTRA_SESSION_ID` (Long), `EXTRA_INTERVALS_ONLY` (Boolean) | Load session from repo, build timer steps, start foreground |
| `ACTION_PAUSE` | — | Pause the `SessionTimer` |
| `ACTION_RESUME` | — | Resume the `SessionTimer` |
| `ACTION_STOP` | — | Stop timer, call `stopSelf()` |

State flows: `RunTrackingService.timerState` (static `StateFlow<TimerState>`) and `RunTrackingService.intervalTransition` (static `SharedFlow<IntervalType>`).

## Testing

### Strategy

- **Prefer fakes over mocks** — manually implement repository interfaces for tests; more readable, catches more bugs, survives refactors
- **Service logic lives in `SessionManager`** — a testable class that handles action dispatching, timer orchestration, and session loading; `RunTrackingService` is a thin lifecycle/notification wrapper

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

## CI (GitHub Actions) — Planned

> **Note**: CI is not yet implemented. The configuration below is the target design for when GitHub Actions workflows are added.

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

### Wear OS UI Patterns

- **ScreenScaffold** wraps every screen for proper Wear layout with scroll indicators
- **SwipeDismissableNavHost** for gesture-based navigation (swipe right = back)
- **TransformingLazyColumn** for scrollable lists with Wear-appropriate transformations
- **SwipeToReveal** for swipe-to-delete gestures on list items
- **EdgeButton** for primary actions (create, save) at screen edges
- **FilledTonalButton** for secondary actions (toggle intervals-only, pause)
- **Vibration feedback** on interval transitions (`VibrationEffect.createOneShot(500)`)
- Version-aware vibrator access: `VibratorManager` on API 31+, legacy `VIBRATOR_SERVICE` below
- Keep text concise — watch screens are small; use `bodySmall`/`labelMedium` for secondary info

### Navigation

Three routes defined in `Screen.kt`:

| Route | Description | Arguments |
|-------|-------------|-----------|
| `session_list` | Start destination, shows saved sessions | None |
| `create_session` | Form to create a new session with patterns | None |
| `active_session/{sessionId}` | Timer screen for running a session | `sessionId: Long` |

Navigate with: `navController.navigate(Screen.activeSession(id))` — helper function builds the route string.

## Feature Development Workflow

When adding a new feature, follow this layer order:

1. **Domain model** — Add data classes in `domain/model/`
2. **Repository interface** — Add methods to `domain/repository/SessionRepository.kt`
3. **Room entities + mapper** — Add `@Entity` in `data/local/db/`, update `SessionMapper.kt`
4. **DAO methods** — Add `@Query`/`@Insert`/`@Delete` to `SessionDao.kt`
5. **Repository implementation** — Implement new methods in `data/repository/SessionRepositoryImpl.kt`
6. **Update test fakes** — Update `FakeSessionRepository` and `FakeSessionDao` in `testFixtures/`
7. **ViewModel** — Create in `presentation/screen/<feature>/` with sealed `UiState`
8. **Screen composable** — Create in same package, collect ViewModel state with `collectAsState()`
9. **Navigation** — Add route to `Screen.kt` and destination to `NavGraph.kt`
10. **Tests** — Unit test the ViewModel, instrumented test the screen, add domain model tests

### Adding a New Screen

```
presentation/screen/<feature>/
├── <Feature>Screen.kt          # @Composable with ScreenScaffold
├── <Feature>ViewModel.kt       # ViewModelProvider.Factory in companion
└── <Feature>UiState.kt         # sealed class with Loading/Ready/Error variants
```

## Anti-patterns

- **Don't put business logic in `@Composable` functions** — Move it to ViewModel or domain layer
- **Don't use `GlobalScope`** — Use `viewModelScope` in ViewModels, `lifecycleScope` in services
- **Don't skip sealed UiState classes** — Every screen must have a sealed class preventing impossible states
- **Don't use mocks when a fake exists** — Check `testFixtures/` first; only mock for Android framework classes
- **Don't call suspend functions from `@Composable` without `LaunchedEffect`** — Side effects need proper scoping
- **Don't store state in the Activity or Composables** — State lives in ViewModel (survives config changes) or Service (survives activity destruction)
- **Don't create new `SessionTimer` instances** — The service owns the single timer instance via companion object
- **Don't add dependencies without updating `libs.versions.toml`** — All versions go in the version catalog
- **Don't put logic in `RunTrackingService` directly** — Add it to `SessionManager` so it's testable without Android framework

## Code Conventions

- **Naming**: Kotlin standard — `PascalCase` classes, `camelCase` functions/properties, `UPPER_SNAKE_CASE` constants
- **File organization**: One public class per file; co-locate UiState + ViewModel + Screen per feature
- **Composable functions**: Capitalize names (`@Composable fun ActiveSessionScreen`), hoist state, accept lambdas for events
- **ViewModel factories**: Use `companion object { fun factory(...): ViewModelProvider.Factory }` pattern
- **Sealed classes**: Use for UiState (Loading/Ready/Error variants) and navigation routes
- **Flow collection**: Use `collectAsState()` in Composables, `stateIn()` with `WhileSubscribed(5000)` in ViewModels
- **Test names**: Use backtick syntax with descriptive sentences (`fun \`initial state is Loading\`()`)

## Error Handling Patterns

Errors propagate through layers:

```
Room DAO (throws exception)
  → Repository (catches, returns null or empty)
    → ViewModel (maps to UiState.Error or handles gracefully)
      → Screen (renders error state from sealed class)
```

- **DAO layer**: Let Room exceptions propagate (suspend functions throw on failure)
- **Repository layer**: Wrap DAO calls if needed; return `null` for missing entities
- **ViewModel layer**: Use `UiState.Error` sealed variant; check for null returns from repository
- **Screen layer**: Render error states from UiState; never catch exceptions in Composables
- **Service layer**: Use null checks with early return (`repository ?: return`, `session ?: return@launch`)

## Environment Setup

### Prerequisites

- Android Studio (latest stable) with Wear OS module
- JDK 17+
- Wear OS emulator (API 30+) or physical Wear OS device

### First-Time Setup

```bash
# Clone and open in Android Studio
git clone <repo-url> && cd TrotClock

# Build
./gradlew assembleDebug

# Create Wear emulator: Tools → Device Manager → Create Device → Wear OS → Large Round → API 30+

# Run on emulator
./gradlew installDebug
```

### Release Signing

Create `keystore.properties` in project root (gitignored):

```properties
storeFile=release.keystore
storePassword=<your-password>
keyAlias=<your-alias>
keyPassword=<your-password>
```

If `keystore.properties` is missing, the build falls back to debug signing automatically.
