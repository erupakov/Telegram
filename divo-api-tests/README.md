# divo-api-tests

JVM tests for how the Android client talks to the Divo backend. They compile the app's real
network layer (`TMessagesProj/.../divo/dal` DTOs, Retrofit services, mappers, `DivoApiClient`,
`AuthRepository`) without the Android SDK or building the APK, and run in seconds.

```
./gradlew -p divo-api-tests test      # or: gradle -p divo-api-tests test
```

Report: `divo-api-tests/build/reports/tests/test/index.html`.

## What is covered

- **Response parsing** (`UserProfileParsingTest`, `BlockedUsersParsingTest`): fixtures shaped
  like real stage responses (`src/test/resources/fixtures`, personal data replaced) go through the
  same Gson + mappers as in the app. Every field set to null or missing in turn, unknown enum values,
  empty objects, numbers as strings, unicode and very long strings.
- **Requests and errors** (`RequestContractTest`, `ErrorHandlingTest`): the real OkHttp client and
  Retrofit against a local MockWebServer mounted like stage (`/api/`). Paths, bodies, the
  `Authorization` / platform / language headers, token handling in `AuthRepository`, and how 4xx/5xx,
  HTML instead of JSON, broken JSON, empty bodies, dropped connections and timeouts become `DivoResult`.

## Adding tests

- New response shape: drop a sanitized JSON into `fixtures/` and parse it like the existing tests.
- New endpoint: if its DTO/service only uses plain Kotlin, it's already compiled in; otherwise add
  the file to `include(...)` in `build.gradle.kts` and stub what it touches in `src/stubs`.
- `src/stubs` holds compile-time stand-ins for the few Android/Telegram classes the layer touches;
  the app always uses the real ones.
