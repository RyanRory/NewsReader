# News Reader

A small iOS app that shows top headlines and search results from the [GNews API](https://gnews.io), with category/language filters and locally-saved "starred" articles.

## Project Overview

The brief asked for the usual News Reader features (headlines, search, category/language filters, starring, opening articles in the browser) with one specific twist: everything below the `ViewModel` should live in **Kotlin Multiplatform (KMP)**, using **Ktor** for networking and **Koin** for dependency injection. The idea is to put all the business logic, networking, persistence, and data modeling in a shared module that could, in theory, also power an Android app — while the UI and presentation layer stay 100% native SwiftUI.

## Setup

Copy `ApiConfig.kt.example` to `ApiConfig.kt` and fill in your GNews API key:

```bash
cp sharedLogic/src/commonMain/kotlin/com/uptick/newsreader/ApiConfig.kt.example sharedLogic/src/commonMain/kotlin/com/uptick/newsreader/ApiConfig.kt
```

## Structure: What's in KMP vs. what stays iOS-native

```
KMP shared module (commonMain)
├── data/             — domain models (NewsArticle) + GNews DTOs + mapping
├── network/          — Ktor HttpClient + GNewsHttpRequests
├── repository/       — NewsRepository (interface) + NewsRepositoryImpl
├── service/          — FetchNewsService, ToggleStarService
├── db/               — SQLDelight schema + DataPersistence
└── di/               — Koin modules + KoinHelper
 
iOS app (Swift)
├── NewsViewModel          — @ObservableObject, debounce, pagination, starred state
├── iOSApp                 — Entry of the App
├── ContentView            — Main screen with 2 tabs of headlines and starred articles
├── Views/
│   ├── NewsListView       — headlines/search list, filters, pull-to-refresh, infinite scroll
│   ├── NewsItemRow        — single article cell, star button
│   ├── StarredView        — locally saved starred articles
│   └── FilterView         — category/language pickers
└── Utilities              — date formatting
```

### KMP layer

Everything below the `ViewModel` is shared Kotlin code:

- **`data/`** — `NewsArticle` is the domain model used everywhere (including by SwiftUI). `GNewsResponse`/`GNewsArticle`/`GNewsSource` are `@Serializable` DTOs that mirror the GNews JSON response exactly, with a mapper (`toNewsArticle()`) that converts them into the domain model.
- **`network/`** — a single `GNewsHttpRequests` class wraps the Ktor `HttpClient`. There's one `fetchNews()` function that handles both the `top-headlines` and `search` endpoints (see "API call merging" below).
- **`repository/`** — `NewsRepository` is the interface the rest of the app depends on; `NewsRepositoryImpl` is the only thing that knows it's talking to GNews over HTTP.
- **`service/`** — `FetchNewsService` and `ToggleStarService` are the public API the iOS app talks to. Functions are marked `@Throws` so Swift can use normal `try`/`await` instead of dealing with Kotlin's `Result` type across the interop boundary.
- **`db/`** — SQLDelight database for starred articles (see "Local persistence" below).
- **`di/`** — Koin modules wiring everything together, plus a small `KoinHelper` that gives Swift a clean entry point to grab services without needing to know about Koin's API.

### iOS layer

Everything above the service layer is plain SwiftUI:

- **`NewsViewModel`** — the only `ObservableObject` in the app. Owns all `@Published` state (articles, filters, loading/error flags, pagination), debounces search/filter changes via Combine, and merges starred state into fetched articles.
- **Views** — purely declarative, no business logic. `NewsListView` handles the headlines/search list with pull-to-refresh and infinite scroll; `StarredView` shows the saved articles; `FilterView` is the category/language pickers; `NewsItemRow` is the reusable article cell.
- Opening an article in the browser, image loading (`AsyncImage`), and accessibility are all handled natively — there was no reason to push these into KMP since they're inherently platform UI concerns.
  The `ViewModel` itself stays on the iOS side rather than in KMP. Early on I considered putting it in shared code with a Combine/Flow bridge, but decided against it — `@Published`/`@MainActor`/Combine are idiomatic SwiftUI patterns, and bridging Kotlin `StateFlow` into `ObservableObject` adds a layer of glue code for not much benefit in a single-platform app. The KMP boundary sits at the **service layer**: `FetchNewsService` and `ToggleStarService` are the contract, and the ViewModel consumes them like any other Swift dependency.

## Decisions and things I changed along the way

### Merging the headlines and search API calls

GNews has two separate endpoints — `/top-headlines` and `/search` — but they take almost identical parameters, differing only in whether a search query (`q`) is present. Rather than modeling these as two separate network calls, repository methods, and services, I collapsed them into a single `fetchNews(query, category, lang, page)`:

- If `query` is `null` or blank, it hits `/top-headlines`.
- If `query` has content, it hits `/search`.
  This meant `GetHeadlinesService` and `SearchNewsService` (which started as two near-identical classes) became one `FetchNewsService`. The "is this headlines or search" decision lives in exactly one place (the network layer), and the ViewModel doesn't need to call two different services depending on what the user typed — it just always calls `fetch(query: ..., ...)` and lets the empty/non-empty `query` decide the behaviour. This removed a fair amount of duplicated code without losing any clarity, since the call site (`if query.count >= 3 { search } else { headlines }`) is still obvious from context.

### `operator fun invoke` → renamed to `fetch`

The services originally used Kotlin's `operator fun invoke`, which is nice when both the caller and the service are Kotlin. Once the `ViewModel` moved to the iOS side, `operator fun invoke` doesn't translate to anything idiomatic in Swift (it just becomes `.invoke(...)`), so I renamed the public entry points to `fetch(...)`, `toggleStar(...)`, etc. — names that read naturally from both sides. The `invoke` functions are remained though, just for future work on Android side.

### Debouncing

The GNews docs ask for at least 300ms debounce on requests, and no request until the search query is 3+ characters. Initially I only debounced the search field, since that's the field that changes on every keystroke. But re-reading the requirement, it applies to **all** requests to the endpoint, so category and language picker changes should also be debounced (a user rapidly flipping through categories shouldn't fire a request per tap).

The final implementation uses Combine's `CombineLatest3` over `searchQuery`, `selectedCategory`, and `selectedLang`, with `.debounce(300ms)` and `.removeDuplicates()`. A single `fetchNews()` function decides whether to call headlines or search based on the current query length. Manual user actions (pull-to-refresh and "load more" on scroll) bypass the debounce entirely, since those are explicit one-shot actions where the user expects an immediate response. A `guard !isLoading` / `guard !isLoadingMore` check prevents duplicate in-flight requests instead.

### Local persistence: UserDefaults → SQLDelight

My first pass at "starred" persistence used **Multiplatform Settings** (a KMP wrapper around `UserDefaults`/`SharedPreferences`), storing the list of starred articles as a JSON-encoded string under a single key. This is genuinely the right call for small amounts of simple key-value data, and it worked.

However, once I started thinking about this as a "production quality" decision rather than just "make it work":

- Every toggle meant decoding the *entire* starred list from a JSON string, mutating it, and re-encoding the whole thing back to a string, that is an O(n) read-modify-write for a single toggle.
- There's no good way to query ("is this one article starred?") without decoding everything.
- It doesn't scale gracefully if starred articles grow into the hundreds, or if I later wanted to store more structured data (e.g. a starred timestamp for sorting).

I switched to **SQLDelight** with a small `StarredArticle` table (one row per starred article, primary key on `id`). `DataPersistence` now does `INSERT OR REPLACE` / `DELETE` / `COUNT WHERE id = ?` — each operation is a real, indexed, type-safe SQL query rather than a full-collection round trip. The public interface of `DataPersistence` (`toggleStar`, `isStarred`, `getStarredArticles`) didn't change, so this swap only touched the implementation, the Koin wiring (`NewsDatabase` + a platform `SqlDriver` via `expect`/`actual`), and the test setup (an in-memory SQLite driver per platform instead of `MapSettings`).

This is also a better foundation if "starred" articles ever need to support more than a boolean, e.g. notes, tags, or sort order, which would be awkward to bolt onto a JSON blob in `UserDefaults`.

### Koin + Ktor setup notes

A couple of non-obvious things that cost time and are worth recording:

- **Ktor needs a platform engine.** `ktor-client-core` alone isn't enough. Without `ktor-client-darwin` in `iosMain`, Koin fails at runtime with `Failed to find HttpClientEngineContainer`, which surfaces as an opaque `InstanceCreationException` for anything downstream that depends on `HttpClient`.
- **SQLDelight's native driver needs `libsqlite3`.** The KMP framework doesn't automatically propagate the `sqlite3` C symbols to the iOS app target, producing ~30 "Undefined symbol: _sqlite3_*" linker errors. Fixed by adding `libsqlite3.tbd` to "Link Binary With Libraries" in Xcode.
- **Uncaught Kotlin exceptions crash Swift silently.** Any Koin-managed class whose methods can throw needs `@Throws(Exception::class)`, or the exception trips `Kotlin_ObjCExport_trapOnUndeclaredException` and the app crashes with a near-useless stack trace. Once `@Throws` is in place, Swift can `try`/`catch` and Koin's `printLogger(Level.DEBUG)` reveals the real underlying error (in practice, this is how the two issues above were diagnosed).

## What I'd do with more time

- **Android target** — the shared module is already structured so an Android app could reuse `service/`, `repository/`, `network/`, and `db/` directly. Only the `expect`/`actual` driver implementations and a thin Compose UI would be net-new.
- **Caching / offline support** — the brief doesn't require offline support, but since starred articles are already in SQLite, headline results could be cached there too with a simple TTL, so a cold launch isn't a blank screen while waiting on the network.
- **Better error handling/retry** — currently a failed fetch shows an error state. A retry button and distinguishing "no internet" vs. "API error" (e.g. Rate limit hit - GNews caps at 100 requests/day on the free tier) would be the next step.
- **iOS unit tests for `NewsViewModel`** — the KMP layer (`service`, `repository`, `db`) has unit test coverage using fakes/in-memory SQLite. The ViewModel's debounce and pagination logic would benefit from tests too, using a mock `FetchNewsService`/`ToggleStarService`.
- **Snapshot/UI tests** — particularly for Dynamic Type at larger accessibility sizes, where the current `HStack` based row layout could be tested against `.accessibility`+ sizes.
- **Localization** — language *filtering* is supported (en/zh/fr/ja/pl/ta/vi per the brief), but the UI strings themselves aren't localized. For a team that genuinely reads news in multiple languages, localizing the app shell would be a natural next step.

