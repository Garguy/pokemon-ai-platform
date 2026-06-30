# Pokémon AI Platform

## Project structure

- `backend/` — Spring Boot 4 + GraphQL + PostgreSQL API
- `client/` — Kotlin Multiplatform (KMP) app: Android, iOS, Desktop targets
- `client/shared/` — Shared KMP module (Apollo GraphQL, Koin DI, Compose Multiplatform UI)
- `client/androidApp/` — Android host app
- `client/iosApp/` — iOS Xcode project (consumes `shared.framework`)
- `client/desktopApp/` — JVM desktop app

## Running the backend

```bash
cd backend
set -a && source .env && set +a && ./gradlew :app:bootRun
```

## Running Android

```bash
# Each time the emulator restarts, tunnel port 8080:
~/Library/Android/sdk/platform-tools/adb reverse tcp:8080 tcp:8080
```

Then hit play in Android Studio. Server URL is `http://localhost:8080/graphql`.

## Building iOS framework

```bash
cd client
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Then open `client/iosApp/iosApp.xcodeproj` in Xcode and run on simulator.

## Running desktop

```bash
cd client
./gradlew :desktopApp:run
```

## Key decisions

- Android uses `adb reverse` (not `10.0.2.2`) due to macOS firewall blocking TCP to the emulator
- iOS simulator uses `localhost:8080` directly — no tunnel needed
- KMP framework produces a static `.framework` for iOS via `binaries.framework { isStatic = true }`
