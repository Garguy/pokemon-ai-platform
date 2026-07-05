# Pokémon AI Platform

A personality quiz that uses a Jungian-style questionnaire and an LLM to recommend your Pokémon match.

> ⚠️ **Legal disclaimer:** Pokémon is a registered trademark of Nintendo, Game Freak, and Creatures Inc. This is a personal portfolio project, not affiliated with or endorsed by the IP holders. Not intended for production or commercial use.

## Architecture

- Spring Boot 4 + GraphQL API
- PostgreSQL + pgvector
- Spring AI + Groq (llama-3.3-70b-versatile)
- Kotlin Multiplatform client with Compose Multiplatform UI (Android / iOS / Desktop)
- Apollo GraphQL client

## Prerequisites

- Java 21 (`.sdkmanrc` included — run `sdk env`)
- Docker + Docker Compose
- Android Studio (Android)
- Xcode on macOS (iOS)
- Free Groq API key (see below)

## Getting a Groq API key

Sign up free at https://console.groq.com, then create an API key. The model used is `llama-3.3-70b-versatile`, available on the free tier.

## Running the backend

```bash
cd backend
docker compose up -d
cp .env.example .env
# Run once without JWT_SECRET to generate one, add it to .env, then:
set -a && source .env && set +a && ./gradlew :app:bootRun
```

GraphiQL is available at http://localhost:8080/graphiql.

A dev admin account is seeded automatically — `admin@pokemonai.com` / `admin` — convenient for exploring recommendation history without registering.

## Running Android

Open `client/` in Android Studio. Run the following each time the emulator restarts:

```bash
adb reverse tcp:8080 tcp:8080
```

Then hit Run.

## Running iOS

```bash
cd client
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

Then open `client/iosApp/iosApp.xcodeproj` in Xcode and run on simulator.

## Running Desktop

```bash
cd client
./gradlew :desktopApp:run
```

## Contributing

Fork the repo, branch off `main`, open a PR. Backend modules live in `backend/modules/`: `identity`, `pokemon`, `questionnaire`, `recommendation`, `ai`, `shared`.

AI integration tests are tagged `integration-ai` and excluded from the default test run. To include them:

```bash
./gradlew testAiIntegration
```

Requires `GROQ_API_KEY` to be set.
