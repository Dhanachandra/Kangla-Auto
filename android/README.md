# Kangla Auto — Android app

Kotlin + Jetpack Compose app that talks to the Kangla Auto API (rider booking and
driver job management mirror the web dashboard).

## Requirements

- Android Studio (Hedgehog or newer), JDK 17
- The Kangla Auto backend running and reachable from your device
- Android emulator API 26+ / device

## Pointing the app at your server

The base URL is compiled in as `BuildConfig.API_BASE_URL`.

- Default assumes the Android **emulator**: `http://10.0.2.2:8000/`
  (10.0.2.2 = the host machine as seen from the emulator).
- Use `-PAPI_BASE_URL` when building to override:

```bash
./gradlew assembleDebug -PAPI_BASE_URL=http://192.168.1.50:8000/
```

Works with real devices too — same Wi-Fi network, use your computer's LAN IP
(e.g. `http://192.168.1.50:8000/`).

Cleartext HTTP is only allowed for local dev hosts
(`10.0.2.2`, `localhost`, `127.0.0.1`, `*.kangla.local`) via
`res/xml/network_security_config.xml`. For a release pointing at a real server,
the backend should be served over HTTPS and the config tightened.

## Build

Open the `android/` folder in Android Studio and press Run, or from a terminal:

```bash
cd android
./gradlew assembleDebug
```

The debug APK lands in `app/build/outputs/apk/debug/app-debug.apk`.

## Architecture

- `data/` — Retrofit (`KanglaApi`), OkHttp with a Bearer-token interceptor,
  DataStore session persistence (`TokenStore`), single `Repository` façade.
- `ui/auth/` — login/register screens and `AuthViewModel`.
- `ui/home/` — rider dashboard (book + cancel + track) and driver dashboard
  (accept/complete), each with its own ViewModel.
- `ui/components/` — brand header, status badge, booking card.
- All requests go through `AndroidManifest`-declared network security config.

## Demo accounts

| Role   | Phone      | Password   |
| ------ | ---------- | ---------- |
| rider  | 9876500003 | rider123   |
| driver | 9876500001 | driver123  |
| driver | 9876500002 | driver123  |