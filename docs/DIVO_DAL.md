# Divo DAL/DAO Layer – Usage Guide

This document describes the Data Access Layer (DAL) and Data Access Objects (DAOs) used to call the Divo backend REST API (`https://backend.divo.fashion/api`) from the Android app. The layer is built with **Kotlin coroutines**, **Retrofit**, and **OkHttp**, and exposes suspend functions that return a unified `DivoResult<T>` type.

---

## 1. Overview

| Component | Location | Purpose |
|-----------|----------|---------|
| **DivoApi** | `org.telegram.divo.dal.DivoApi` | Singleton entry point; exposes all DAOs and raw services |
| **DAOs** | `org.telegram.divo.dal.dao.*` | Domain-specific access: Auth, User, Publication, Event, Wallet |
| **Services** | `org.telegram.divo.dal.api.*` | Retrofit interfaces (used by DAOs and optionally by app code) |
| **DTOs** | `org.telegram.divo.dto.*` | Request/response models (auth, user, common) |
| **DivoResult** | `org.telegram.divo.dal.DivoResult` | Sealed result type: Success, NetworkError, HttpError, UnknownError |
| **Config** | `org.telegram.divo.dal.DivoApiConfig` | Base URL and timeouts |

All network calls are **suspend** functions and must be invoked from a coroutine (e.g. `viewModelScope.launch`).

---

## 2. Accessing the API

Use the `DivoApi` object to get DAO instances. They are lazily initialized.

```kotlin
import org.telegram.divo.dal.DivoApi

// DAOs (recommended for app code)
DivoApi.authDao
DivoApi.userDao
DivoApi.publicationDao
DivoApi.eventDao
DivoApi.walletDao

// Raw Retrofit services (for endpoints not yet wrapped in a DAO)
DivoApi.systemService
DivoApi.dictionaryService
```

---

## 3. Result type: DivoResult<T>

Every DAO method returns `DivoResult<T>`. Handle all cases to provide a good user experience.

```kotlin
when (val result = DivoApi.userDao.getCurrentUserInfo()) {
    is DivoResult.Success -> {
        val user = result.data  // e.g. UserInfoResponse
        // update UI with user
    }
    is DivoResult.NetworkError -> {
        // e.g. no internet, timeout
        val message = result.exception.localizedMessage ?: "Network error"
    }
    is DivoResult.HttpError -> {
        // 4xx/5xx; optional parsed body
        val code = result.code
        val message = result.body?.message ?: "HTTP $code"
    }
    is DivoResult.UnknownError -> {
        // unexpected exception
        result.throwable
    }
}
```

- **Success&lt;T&gt;** – request succeeded; `data` is the response body.
- **NetworkError** – `IOException` (e.g. connection failed, timeout).
- **HttpError** – non-2xx status; `code` and optional `body` (`ErrorResponse`: `message`, `errors`).
- **UnknownError** – any other throwable.

---

## 4. Usage by domain

### 4.1 Auth (`AuthDao`)

Used for login, registration, and logout. On successful login/registration, the **access token is stored automatically** via `AccessTokenProvider` (SharedPreferences by default). Logout clears it.

**Login**

```kotlin
import org.telegram.divo.dal.DivoApi
import org.telegram.divo.dto.auth.LoginRequest

viewModelScope.launch {
    val request = LoginRequest(
        email = "user@example.com",
        password = "secret",
        deviceId = "device-uuid",
        deviceType = "android"
    )
    when (val result = DivoApi.authDao.login(request)) {
        is DivoResult.Success -> {
            // Token is already saved; navigate to home or load user
        }
        is DivoResult.HttpError -> {
            val msg = result.body?.message ?: "Login failed"
            // show error
        }
        // handle NetworkError, UnknownError
    }
}
```

**Registration**

```kotlin
import org.telegram.divo.dto.auth.RegistrationRequest

val request = RegistrationRequest(
    email = "user@example.com",
    password = "secret",
    timezone = "Europe/Moscow",
    role = "new_face"  // or "model", "agency_employee", "customer"
)
when (val result = DivoApi.authDao.register(request)) {
    is DivoResult.Success -> { /* token stored */ }
    // ...
}
```

**Logout**

```kotlin
when (val result = DivoApi.authDao.logout()) {
    is DivoResult.Success -> { /* token cleared */ }
    // ...
}
```

---

### 4.2 User (`UserDao`)

Requires the user to be authenticated. The shared OkHttp client adds `Authorization: Bearer <token>` automatically for these endpoints.

**Get current user**

```kotlin
import org.telegram.divo.dto.user.UserInfoResponse

when (val result = DivoApi.userDao.getCurrentUserInfo()) {
    is DivoResult.Success -> {
        val user: UserInfoResponse = result.data
        // user.id, user.email, user.role, user.name, user.avatarUrl, user.rating
    }
    is DivoResult.HttpError -> {
        if (result.code == 401) { /* not logged in or token expired */ }
    }
    // ...
}
```

**Update profile**

```kotlin
val fields = mapOf(
    "name" to "New Name",
    "avatarUrl" to "https://..."
)
when (val result = DivoApi.userDao.updateProfile(fields)) {
    is DivoResult.Success -> { val updated = result.data }
    // ...
}
```

---

### 4.3 Publication / Feed (`PublicationDao`)

Endpoints use generic request bodies (e.g. for filters or pagination). Responses are returned as raw `ResponseBody`; parse JSON in the caller or extend the layer with typed DTOs later.

```kotlin
// Feed list (e.g. empty map or pagination/filter keys as per backend)
when (val result = DivoApi.publicationDao.getFeed(emptyMap())) {
    is DivoResult.Success -> {
        val jsonString = result.data.string()
        // parse or pass to a parser
    }
    // ...
}

// Like / unlike (payload typically includes publication id)
DivoApi.publicationDao.like(mapOf("publicationId" to 123))
DivoApi.publicationDao.unlike(mapOf("publicationId" to 123))
```

---

### 4.4 Events (`EventDao`)

```kotlin
when (val result = DivoApi.eventDao.listEvents(emptyMap())) {
    is DivoResult.Success -> { val body = result.data }
    // ...
}

when (val result = DivoApi.eventDao.getEvent(id = 42L)) {
    is DivoResult.Success -> { val body = result.data }
    // ...
}
```

---

### 4.5 Wallet (`WalletDao`)

```kotlin
DivoApi.walletDao.listWallets(emptyMap())
DivoApi.walletDao.getWallet(walletId = 1L)
DivoApi.walletDao.getOperations(walletId = 1L, emptyMap())
```

---

### 4.6 System and dictionaries (raw services)

For endpoints not yet wrapped in a DAO, use the Retrofit services directly. You will get Retrofit `Response<T>` and must handle errors yourself (or wrap in `safeCall` if you add a small helper).

```kotlin
// Example: feature flags
val response = DivoApi.systemService.getFeatureFlags()
if (response.isSuccessful) {
    val body = response.body()?.string()
    // parse JSON
}

// Dictionaries
DivoApi.dictionaryService.getGenders()
DivoApi.dictionaryService.getAppearances()
DivoApi.dictionaryService.getPayments()
```

---

## 5. ViewModel integration example

The recommended pattern is to call DAOs inside `viewModelScope.launch`, then map `DivoResult` to UI state and one-off effects (e.g. navigation, toasts).

```kotlin
class AuthViewModel : BaseViewModel<AuthViewState, AuthViewIntent, AuthViewEffect>() {

    override fun handleIntent(intent: AuthViewIntent) {
        when (intent) {
            is AuthViewIntent.Login -> performLogin(
                intent.email, intent.password,
                intent.deviceId, intent.deviceType
            )
        }
    }

    private fun performLogin(
        email: String, password: String,
        deviceId: String, deviceType: String
    ) {
        viewModelScope.launch {
            setState { copy(isLoading = true, errorMessage = null) }

            val request = LoginRequest(email, password, deviceId, deviceType)
            when (val result = DivoApi.authDao.login(request)) {
                is DivoResult.Success -> {
                    setState { copy(isLoading = false, loginResponse = result.data) }
                    sendEffect(AuthViewEffect.LoginSuccess)
                }
                is DivoResult.HttpError -> {
                    val msg = result.body?.message ?: "HTTP ${result.code}"
                    setState { copy(isLoading = false, errorMessage = msg) }
                    sendEffect(AuthViewEffect.ShowError(msg))
                }
                is DivoResult.NetworkError -> {
                    val msg = result.exception.localizedMessage ?: "Network error"
                    setState { copy(isLoading = false, errorMessage = msg) }
                    sendEffect(AuthViewEffect.ShowError(msg))
                }
                is DivoResult.UnknownError -> {
                    val msg = result.throwable.localizedMessage ?: "Unknown error"
                    setState { copy(isLoading = false, errorMessage = msg) }
                    sendEffect(AuthViewEffect.ShowError(msg))
                }
            }
        }
    }
}
```

---

## 6. Configuration and auth

- **Base URL and timeouts** – Defined in `DivoApiConfig` (`BASE_URL`, `CONNECT_TIMEOUT_SECONDS`, `READ_TIMEOUT_SECONDS`). Base URL is `https://backend.divo.fashion/api/`.
- **Token storage** – By default the app uses `SharedPrefsAccessTokenProvider` (prefs name `divo_auth`, key `access_token`). The same `AccessTokenProvider` is used when building the OkHttp client and in `AuthDao` to persist/clear the token.
- **Auth header** – The OkHttp client adds `Authorization: Bearer <token>` for all requests **except** paths that are explicitly unauthenticated in the OpenAPI spec (e.g. `/auth/login`, `/auth/registration`, `/geo/*`, `/file/upload-file`, `/file/upload-files`, `/crypto/webhook`). No need to add the header in app code.

To use a custom token source (e.g. encrypted storage or another auth module), implement `AccessTokenProvider` and supply it when building the OkHttp client and AuthDao (currently this requires changing `DivoApi` to accept or construct that provider).

---

## 7. Adding a new endpoint

To add support for a new API operation:

1. **Retrofit interface** – Add a suspend function returning `Response<T>` in the appropriate `*Service` under `org.telegram.divo.dal.api`.
2. **DTOs** – If the request or response has a fixed shape, add data classes in `org.telegram.divo.dto` (e.g. under `auth`, `user`, or a new subpackage). Use `@SerializedName` if JSON keys differ from Kotlin names.
3. **DAO** – In the corresponding DAO under `org.telegram.divo.dal.dao`, add a method that calls the service inside `safeCall { ... }` and returns `DivoResult<T>`.
4. **DivoApi** – If you introduce a new service/DAO, expose it from `DivoApi` (lazy `val`).

Example: adding “get user by id” to UserDao would require a new method in `UserService`, then a `getUser(id: Long): DivoResult<UserInfoResponse>` (or similar) in `UserDao` that uses `safeCall { service.getUser(id) }`.

---

## 8. Dependencies

The DAL uses:

- **Retrofit** + **converter-gson** – HTTP client and JSON (de)serialization.
- **OkHttp** + **logging-interceptor** – underlying client; body logging in debug when `BuildVars.LOGS_ENABLED` is true.
- **Gson** – already used by the project; DTOs use it via Retrofit and in `DivoResult`/`safeCall` for error body parsing.

These are declared in `TMessagesProj/build.gradle`. The API contract is described in the project’s `openapi.yaml`.

---

## 9. File layout reference

```
org.telegram.divo/
├── dal/
│   ├── DivoApi.kt              # Entry point: DAOs and services
│   ├── DivoApiClient.kt        # OkHttp + Retrofit build, auth interceptor
│   ├── DivoApiConfig.kt        # Base URL, timeouts
│   ├── DivoResult.kt           # Sealed result + safeCall
│   ├── AccessTokenProvider.kt  # Token get/set interface
│   ├── SharedPrefsAccessTokenProvider.kt
│   ├── api/                    # Retrofit interfaces
│   │   ├── AuthService.kt
│   │   ├── UserService.kt
│   │   ├── PublicationService.kt
│   │   ├── EventService.kt
│   │   ├── WalletService.kt
│   │   ├── SystemService.kt
│   │   └── DictionaryService.kt
│   └── dao/
│       ├── AuthDao.kt
│       ├── UserDao.kt
│       ├── PublicationDao.kt
│       ├── EventDao.kt
│       └── WalletDao.kt
└── dto/
    ├── auth/                   # LoginRequest, LoginResponse, RegistrationRequest
    ├── user/                   # UserInfoResponse
    └── common/                 # ErrorResponse
```

For a minimal “login + load user” flow, use `DivoApi.authDao.login` and, on success, `DivoApi.userDao.getCurrentUserInfo()` from a ViewModel as shown above.
