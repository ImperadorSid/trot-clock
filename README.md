<p align="center">
  <img src="https://img.shields.io/badge/Platform-Wear%20OS-4285F4?logo=wear-os&logoColor=white" alt="Wear OS"/>
  <img src="https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material%203-3DDC84?logo=jetpack-compose&logoColor=white" alt="Compose"/>
  <img src="https://img.shields.io/badge/Min%20SDK-30-brightgreen" alt="Min SDK"/>
  <img src="https://img.shields.io/badge/License-All%20Rights%20Reserved-red" alt="License"/>
</p>

# ⏱️ TrotClock

A **Wear OS walk/jog interval timer** built with Jetpack Compose for Wear OS and Material 3.

Create custom interval sessions, run them with a countdown timer on your wrist, and manage them in a persisted list — all without needing a phone.

---

## ✨ Features

| | Feature | Description |
|---|---------|-------------|
| 🏃 | **Custom intervals** | Define walk/jog durations and repetitions per session |
| ⏳ | **Live countdown** | Color-coded phases (warmup, walk, jog, cooldown) with MM:SS display |
| ⚡ | **Intervals-only mode** | Skip warmup and cooldown, jump straight to walk/jog |
| 🔋 | **Background execution** | Foreground service keeps the timer running when the screen is off |
| ⌚ | **Ongoing activity** | Session stays visible on the watch face while active |
| 📳 | **Haptic feedback** | Vibration on every interval transition |
| 👆 | **Swipe to delete** | Manage sessions with native Wear OS gestures |
| 💾 | **Persistent storage** | Sessions saved locally with Room database |

---

## 🏗️ Architecture

Single-module **MVVM + Clean Architecture** with a foreground service as the source of truth for timer state.

```
┌─────────────────────────────────────────────┐
│  📱 Presentation                            │
│  Compose Screens → ViewModels → UiState     │
├─────────────────────────────────────────────┤
│  ⚙️ Service                                 │
│  RunTrackingService (foreground)             │
│  Owns SessionTimer · Exposes StateFlow      │
├─────────────────────────────────────────────┤
│  🧩 Domain                                  │
│  Models · Timer · Repository Interfaces     │
├─────────────────────────────────────────────┤
│  🗄️ Data                                    │
│  Room DB · Repository Implementations       │
└─────────────────────────────────────────────┘
```

**Key design decisions:**

- 🔒 **Foreground service owns state** — survives activity destruction when the wrist drops
- 🛡️ **Sealed UI states** (`Loading`, `Success`, `Ready`, `Completed`) — no impossible states
- 🔗 **Join table for patterns** — proper relational model with cascade delete
- 💉 **Manual DI** via `AppContainer` — no framework overhead

---

## 🛠️ Tech Stack

| Category | Technology | Version |
|----------|-----------|---------|
| 🟣 Language | Kotlin | 2.2.10 |
| 🏗️ Build | AGP + Gradle KTS | 9.1.0 / 9.3.1 |
| 🎨 UI | Wear Compose Material 3 | 1.5.6 |
| 🧭 Navigation | Wear Compose Navigation | 1.5.6 |
| 🗄️ Database | Room + KSP | 2.7.1 |
| ⚡ Async | Kotlin Coroutines | 1.10.2 |
| 🔄 Lifecycle | ViewModel + Lifecycle Service | 2.9.1 |
| ⌚ Wear | Ongoing Activity | 1.1.0 |
| 🧪 Testing | JUnit 4 · Turbine · Compose UI Test | — |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio with Wear OS emulator or a physical Wear OS device
- JDK 17+

### Build & Install

```bash
# 🔨 Debug build
./gradlew assembleDebug

# 📲 Install on connected Wear device/emulator
./gradlew installDebug

# 📦 Release build
./gradlew assembleRelease
```

---

## 🧪 Testing

Built with a **TDD approach** using fakes over mocks. Shared test fakes live in `testFixtures/`.

```bash
# ✅ Unit tests (JVM — no device needed)
./gradlew testDebugUnitTest

# 📱 Instrumented tests (requires Wear emulator/device)
./gradlew connectedAndroidTest
```

**Test coverage includes:**

- ✅ Domain models, timer logic, and mappers
- ✅ Repository operations with fake DAOs
- ✅ ViewModel state transitions
- ✅ Room DAO with in-memory database
- ✅ Compose UI screens

---

## 📄 License

This project is not currently licensed for redistribution. All rights reserved.

---

<p align="center">
  Built with ❤️ for Wear OS
</p>
