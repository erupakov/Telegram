---
name: divo-dal-dao-layer
overview: Introduce a Kotlin-based DAL/DAO layer for the Divo mobile client that talks to the backend.divo.fashion REST API described by openapi.yaml, using Retrofit + OkHttp and Kotlin coroutines.
todos:
  - id: deps-config
    content: Add Retrofit, OkHttp, and Gson converter dependencies and create DivoApiConfig with base URL and timeouts.
    status: completed
  - id: http-client
    content: Implement DivoApiClient with OkHttp client, auth interceptor, and Retrofit builder.
    status: completed
  - id: retrofit-interfaces
    content: Define Retrofit service interfaces (AuthService, UserService, PublicationService, EventService, WalletService, SystemService, DictionaryService) based on openapi.yaml.
    status: completed
  - id: dto-models
    content: Create core DTOs for auth, user, feed/publication, event, wallet, and error responses under org.telegram.divo.dto.
    status: completed
  - id: result-and-safe-call
    content: Introduce DivoResult sealed class and a safeCall helper to standardize network error handling.
    status: completed
  - id: dao-implementations
    content: Implement AuthDao, UserDao, PublicationDao, EventDao, WalletDao using Retrofit services and safeCall.
    status: completed
  - id: api-singleton
    content: Create DivoApi singleton/object to expose initialized DAOs to ViewModels and other app layers.
    status: completed
  - id: ui-integration-minimal
    content: Wire DAL into at least one existing Divo ViewModel (e.g., login + user profile) to verify end-to-end behavior with the backend.
    status: completed
isProject: false
---

### High-level approach

- **Goal**: Add a cohesive, Kotlin DAL/DAO layer under `org.telegram.divo` that wraps the `https://backend.divo.fashion/api` REST API (as defined in `openapi.yaml`) using Retrofit + OkHttp and exposes coroutine-based suspend functions for use by Divo screens and view models.
- **Scope**: Implement shared API client/config, auth handling, and representative DAOs for core domains (Auth, User, Publication, Event, Wallet, Messenger dictionaries/feature flags), with clear extension points so additional endpoints can be added incrementally.
- **Key ideas**: Centralize base URL and interceptors, define typed DTOs for key endpoints, map network errors to a small sealed error hierarchy, and keep DAOs thin so view models stay clean.

### Directory and module layout

- **Place DAL in Divo package**:
  - Create `[TMessagesProj/src/main/java/org/telegram/divo/dal](TMessagesProj/src/main/java/org/telegram/divo/dal)` as the main data access package.
  - Add a subpackage `[org/telegram/divo/dal/dao](TMessagesProj/src/main/java/org/telegram/divo/dal/dao)` for domain-specific DAOs.
  - Add a subpackage `[org/telegram/divo/dto](TMessagesProj/src/main/java/org/telegram/divo/dto)` for request/response models used by Retrofit.
- **Keep wiring simple**:
  - Provide a singleton-style provider (e.g. `DivoApi` object in Kotlin) to expose initialized DAO instances to the rest of the app.

### Retrofit/OkHttp configuration

- **Dependencies** (Gradle)
  - Add Retrofit + Gson converter + OkHttp (and logging interceptor) to the main Android module `build.gradle`.
  - Ensure versions are compatible with existing `com.google.code.gson:gson` already in the project.
- **Config class**
  - Create `DivoApiConfig` under `[org/telegram/divo/dal/DivoApiConfig.kt](TMessagesProj/src/main/java/org/telegram/divo/dal/DivoApiConfig.kt)` with:
    - `const val BASE_URL = "https://backend.divo.fashion/api/"` (or derived from `BuildVars` if you prefer).
    - Timeouts and a flag for enabling network logging in debug builds.
- **Auth token provider**
  - Add an `AccessTokenProvider` interface under `[org/telegram/divo/dal/AccessTokenProvider.kt](TMessagesProj/src/main/java/org/telegram/divo/dal/AccessTokenProvider.kt)` with a `suspend fun getAccessToken(): String?`.
  - Implement a default provider (e.g. `SharedPrefsAccessTokenProvider`) that reads/writes JWT tokens from SharedPreferences or an existing auth helper class.
- **OkHttp client**
  - In `[org/telegram/divo/dal/DivoApiClient.kt](TMessagesProj/src/main/java/org/telegram/divo/dal/DivoApiClient.kt)`, build an `OkHttpClient` with:
    - Interceptor to add `Authorization: Bearer <token>` for all endpoints except those explicitly marked `security: []` in `openapi.yaml` (e.g. `/auth/login`, `/auth/registration`, `/geo/*`, `/crypto/webhook`, some file upload endpoints).
    - Logging interceptor (body level in debug, basic/none in release).
    - Sensible connect/read timeouts (e.g. 15–30 seconds).
- **Retrofit instance**
  - Configure Retrofit with:
    - `baseUrl(DivoApiConfig.BASE_URL)`.
    - `client(okHttpClient)`.
    - `addConverterFactory(GsonConverterFactory.create(sharedGsonInstance))`.

### API interfaces based on openapi.yaml

- **Auth API** (`/auth/*`)
  - Under `[org/telegram/divo/dal/api/AuthService.kt](TMessagesProj/src/main/java/org/telegram/divo/dal/api/AuthService.kt)`, define a Retrofit interface with:
    - `suspend fun login(body: LoginRequest): Response<LoginResponse>` for `/auth/login`.
    - `suspend fun registration(body: RegistrationRequest): Response<RegistrationResponse>` for `/auth/registration`.
    - Other key endpoints like `loginSocial`, `registrationSocial`, `sendEmailCode`, `confirmEmail`, `resetPassword`, `logout`.
- **User API** (`/user/*`)
  - In `[org/telegram/divo/dal/api/UserService.kt](TMessagesProj/src/main/java/org/telegram/divo/dal/api/UserService.kt)`, map endpoints like `/user/info`, `/user/rating`, `/user/update-profile`, `/user/change-role`, `/user/list-with-wallets`.
- **Publication API** (`/publication/*`, `/feedline/*`, `/follower/*`, `/favorite/*`)
  - In `[org/telegram/divo/dal/api/PublicationService.kt](TMessagesProj/src/main/java/org/telegram/divo/dal/api/PublicationService.kt)`, cover `list`, `feed`, `create`, `like/unlike`, edit/delete by ID.
- **Event API** (`/event/*`)
  - In `[org/telegram/divo/dal/api/EventService.kt](TMessagesProj/src/main/java/org/telegram/divo/dal/api/EventService.kt)`, cover listings, CRUD, applies, likes.
- **Wallet API** (`/wallet/*`, `/user-paid-services/*`)
  - In `[org/telegram/divo/dal/api/WalletService.kt](TMessagesProj/src/main/java/org/telegram/divo/dal/api/WalletService.kt)`, map wallet list, individual wallets, operations, withdraw, send, and paid services.
- **System/Dictionary/Messenger subsets**
  - Create smaller interfaces like `SystemService` and `DictionaryService` for endpoints such as `/system/feature-flags`, `/dictionary/*`, and a minimal subset of `/messenger/*` if you plan to call them from the Android app.

### DTO design (request/response models)

- **Location for DTOs**
  - Use `[org/telegram/divo/dto](TMessagesProj/src/main/java/org/telegram/divo/dto)` for shared models, grouped by domain (e.g. `auth`, `user`, `feed`, `wallet`).
- **Auth DTOs**
  - `LoginRequest(email, password, deviceId, deviceType)`.
  - `LoginResponse(accessToken: String, /* optional user payload if backend returns it */)`.
  - `RegistrationRequest(email, password, timezone, role)` (role as an enum matching `new_face`, `model`, etc.).
  - DTOs for reset password, confirm email, social login where necessary.
- **User DTOs**
  - `UserInfoResponse` matching `/user/info` payload (fields inferred from backend or left as `Map<String, Any>` initially if the schema is not fully documented yet).
  - Simple request objects for update-profile/email/password as `Map` or dedicated data classes, depending on how strictly you want typing at this stage.
- **Publication/Event/Wallet DTOs**
  - Core list/filter request models for `/publication/list`, `/event/list`, `/wallet/list`, etc. (likely pagination, some filters: can start as `Map<String, Any?>` until fields are stabilized).
  - Basic response wrappers like `PagedResponse<T>` if the backend uses a consistent pattern; otherwise use endpoint-specific data classes.
- **Error model**
  - `ErrorResponse(message: String?, errors: Map<String, Any>?)` matching `components.schemas.Error` in `openapi.yaml`.

### DAO layer design

- **Common result type**
  - Define a sealed `DivoResult<T>` in `[org/telegram/divo/dal/DivoResult.kt](TMessagesProj/src/main/java/org/telegram/divo/dal/DivoResult.kt)`:
    - `Success<T>(data: T)`.
    - `NetworkError(ioe: IOException)`.
    - `HttpError(code: Int, body: ErrorResponse?)`.
    - `UnknownError(throwable: Throwable)`.
- **Base DAO helper**
  - Implement an internal helper function (e.g. `suspend fun <T> safeCall(block: suspend () -> Response<T>): DivoResult<T>`) to:
    - Catch IO/network errors as `NetworkError`.
    - Parse `ErrorResponse` from error body using Gson when HTTP code not in 200–299.
    - Wrap non-network exceptions as `UnknownError`.
- **AuthDao**
  - Under `[org/telegram/divo/dal/dao/AuthDao.kt](TMessagesProj/src/main/java/org/telegram/divo/dal/dao/AuthDao.kt)`, implement methods like:
    - `suspend fun login(request: LoginRequest): DivoResult<LoginResponse>`.
    - `suspend fun register(request: RegistrationRequest): DivoResult<RegistrationResponse>`.
    - `suspend fun logout(): DivoResult<Unit>`.
  - On successful login/registration, persist the `accessToken` via the `AccessTokenProvider` implementation.
- **UserDao**
  - Provide methods like `getCurrentUserInfo()`, `updateProfile(...)`, `changeRole(...)`, mapping directly to `UserService` calls and returning `DivoResult<...>`.
- **PublicationDao / EventDao / WalletDao**
  - For each domain, expose high-value operations needed by the UI first (feed list, like/unlike, create publication, list events, apply to event, list wallets, withdraw/send funds), and return typed `DivoResult` values.
- **Extensibility**
  - Structure DAOs so adding a new endpoint is just:
    - Add Retrofit method to appropriate `*Service`.
    - Add DTO(s) if needed.
    - Add a small wrapper method in the corresponding `*Dao` using `safeCall`.

### Wiring into the app / usage from ViewModels

- **Factory / singleton access**
  - Create an object `DivoApi` under `[org/telegram/divo/dal/DivoApi.kt](TMessagesProj/src/main/java/org/telegram/divo/dal/DivoApi.kt)` that lazily initializes Retrofit, services, and DAOs:

```kotlin
// Pseudocode sketch
object DivoApi {
    private val retrofit: Retrofit by lazy { /* build from DivoApiClient */ }

    val authDao: AuthDao by lazy { AuthDao(retrofit.create(AuthService::class.java), accessTokenProvider) }
    val userDao: UserDao by lazy { UserDao(retrofit.create(UserService::class.java)) }
    // ... other DAOs
}
```

- **ViewModel integration**
  - In existing Divo `ViewModel`s (e.g. under `[org/telegram/divo/screen](TMessagesProj/src/main/java/org/telegram/divo/screen)`), plan to replace any future ad-hoc HTTP calls with calls to DAOs inside `viewModelScope.launch { ... }` using suspend functions.
  - Handle `DivoResult` in ViewModels, mapping to UI states (loading, success, error with user-friendly message).

### Minimal initial implementation subset

To keep the first iteration focused and testable, start with:

- **Auth**
  - Implement full flow for `/auth/login`, `/auth/registration`, `/auth/send-email-code`, `/auth/confirm-email`, `/auth/reset-password`, and `/auth/logout`.
- **User**
  - Implement `/user/info` and `/user/update-profile` so you can show/update the logged-in user profile in the app.
- **Publication feed**
  - Implement `/publication/feed` and `/publication/like`/`/unlike` as the initial content feed integration.
- **System feature flags**
  - Implement `/system/feature-flags` to allow feature-flagging Divo UI functionality.

After verifying this subset in the UI, you can extend DAOs to events, wallet, messenger helpers, etc., following the same patterns.

### Error handling and logging strategy

- **HTTP codes**
  - Treat 2xx as success, 4xx/5xx as errors with parsed `ErrorResponse` when possible.
  - Special-case auth-related codes:
    - `401 Unauthorized`: trigger a logout/token refresh flow or surface a specific error type like `DivoResult.AuthError` if you want finer handling.
    - `403 Forbidden`: map to a `PermissionDenied` subtype of `DivoResult` or encode inside `HttpError` for now.
- **Logging**
  - Use OkHttp logging interceptor in debug builds.
  - Log unexpected exceptions via `FileLog.e()` so they appear in your existing logging pipeline.

### Testing and verification

- **Unit tests (optional but recommended)**
  - Add unit tests for `safeCall` and DAOs using a fake `Retrofit`/`CallAdapter` or by mocking `AuthService`/`UserService`.
- **Manual verification**
  - From a small test ViewModel or a simple screen, call `AuthDao.login` and assert correct behavior (token stored, `DivoResult.Success` returned).
  - Verify requests hit `https://backend.divo.fashion/api/...` with the correct paths and headers using logging interceptor output.

This plan sets up a modern, coroutine-based DAL/DAO layer that uses Retrofit + OkHttp and is tightly aligned with the `openapi.yaml` definition, while fitting naturally into the existing `org.telegram.divo` package structure.