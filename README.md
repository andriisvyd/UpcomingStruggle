# Upcoming Weather

An Android forecast app: today and the week, for the device's own position or any city you
name. Kotlin, coroutines, Compose, Clean Architecture over six modules.

## Running it

```
./gradlew installDebug
```

**No API key.** The provider is [Open-Meteo](https://open-meteo.com/), whose free tier needs
none, so the repository builds and runs as cloned. Java 21, `minSdk 26`, `targetSdk 36`.

Location is optional — every screen offers naming a city instead, and refusing the permission
degrades rather than blocks.

## The four features

| Requirement | Where |
|---|---|
| Forecast for the current day | Dashboard hero, hour strip and readings ledger |
| Forecast for the week | Seven-day list below them; a row opens that day in detail |
| Weather in the current city | Location button in the app bar, or the first row of search |
| Weather in any other city | Search, backed by Open-Meteo geocoding |

## Modules

```
:app                 navigation host, Koin started here, nothing else
:feature:forecast    dashboard + day details, view models, UI mapper
:feature:search      city search, view model
:core:designsystem   theme and domain-free primitives
:core:domain         pure Kotlin — models, repository interfaces, use cases
:core:data           Open-Meteo, DataStore, device location
```

`:core:domain` is a `kotlin-jvm` module with no Android dependency, so the layering is
structural rather than asserted — nothing in it *can* reach the platform. `:core:designsystem`
never depends on it: primitives take strings and enums from their caller, never a forecast.

Search writes the selected place, the forecast screen observes it. Neither feature knows the
other exists, and no city travels through a navigation route.

## Worth reading first

Five files carry most of the decisions:

| File | Why |
|---|---|
| `core/domain/…/model/` | Typed answers instead of empty ones — `SearchOutcome`, `PlaceLabel`, `ForecastUpdate`, `ForecastRead`, `DayUpdate` |
| `core/domain/…/usecase/ObserveForecast.kt` | How long a forecast stays good, and why a refresh overrides it but arriving somewhere new does not |
| `core/data/…/repository/DefaultForecastRepository.kt` | Answers from the store, then the provider |
| `feature/forecast/…/mapper/ForecastUiMapper.kt` | Two clocks: the reader's for the hour strip, the place's for sunrise and the day log |
| `core/data/…/di/DataModuleTest.kt` | Koin binds at runtime, so a hole in the wiring fails the build rather than the app |

## Tests

132, all JVM, no instrumentation:

```
./gradlew test
```

`:core:domain` 42 · `:core:data` 64 · `:feature:forecast` 25 · `:feature:search` 1

Written against payloads captured from the live API and a real DataStore on disk. Deliberate
mutations were run against both the response mapper and the UI mapper to check the suites have
teeth — each was caught by the test whose name describes it.

## Design

A typewriter theme in two fixed grades, day and night, chosen by the system dark-mode flag. No
dynamic colour, no elevation, no image assets — weather glyphs are typed characters in
condition ink. Deliberate, not incidental: the specification is in `design/` — written before
any code, used as the reference every module was built against, and kept current since.

Where the implementation diverged from it — the provider switch above all — the reasons are in
the pull request descriptions.

## What is not here

- No instrumentation or screenshot tests. The screens are covered by previews and by hand.
- `release` does not minify, and inline `@Preview` functions still ship. Mocks and preview
  screens are confined to `src/debug`.
- One place at a time. Storage keeps the last fifteen forecasts, but the app reports on one.
- The forecast runs seven days because that is what the provider gives; nothing paginates
  further.

## AI tools

Used throughout. Which ones, and where they helped and did not, is in
[`TOOLS.md`](TOOLS.md).
