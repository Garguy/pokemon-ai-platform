# iosApp

Xcode project that consumes the KMP framework produced by `:shared`.

## Setup

1. Run `./gradlew :shared:linkReleaseFrameworkIosSimulatorArm64` from the `client/` directory.
2. Open `iosApp.xcodeproj` in Xcode.
3. The `PokemonAiFramework.xcframework` is output to `shared/build/bin/`.
4. Add it under *Frameworks, Libraries, and Embedded Content* in the Xcode target.

## TokenStorage

`PlatformTokenStorage` on iOS uses the system Keychain via Security framework.
The `expect/actual` is wired via the `iosMain` source set in `:shared`.

## Entry point

`ContentView.swift` instantiates the shared Koin graph via a thin Swift wrapper
and hosts the navigation stack. Each screen calls through to the shared
ViewModels over the KMP–Swift interop layer.
