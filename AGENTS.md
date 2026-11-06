# AGENTS.md

Team rules for the coding agent (Android Studio Agent Mode). Every team member's agent follows this file.
Edit it when the agent repeats a mistake, and note the change in your agent log.

## Project

Worthour is an Android app that tracks subscriptions and shows whether each one is worth its price,
using real app usage time (cost per hour). See `README.md` for features and screens.

- Single module: `:app`, package `com.worthour`
- minSdk 26, Kotlin, Gradle Kotlin DSL

## Tech stack

| Use | Library |
|-----|---------|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Local database | Room (with KSP) |
| Usage time | `UsageStatsManager` |
| Background work | WorkManager |
| Notifications | `NotificationCompat` + notification channels |
| Chart | MPAndroidChart (wrapped with `AndroidView`) |

## Package structure

```
com.worthour
├── data/          Room entities, DAOs, database, repositories
├── usage/         UsageStatsManager reader, sync worker
├── notification/  channels and the shared notification helper
└── ui/
    ├── theme/     colors, type, theme
    ├── navigation/
    └── <screen>/  one package per screen: Screen + ViewModel
```

## Rules

- Use Jetpack Compose and Material 3 only. Do not create XML layouts or Fragments.
- UI state lives in a ViewModel and is exposed as `StateFlow`. Composables do not access Room or system services directly.
- Screens read data through a repository, never through a DAO directly.
- Use KSP, not kapt, for Room.
- Add dependencies through `gradle/libs.versions.toml`, not hard-coded versions in `build.gradle.kts`.
- Put user-facing text in `res/values/strings.xml`.
- All data stays on the device. Do not add the `INTERNET` permission or any network code unless the team decides to (see issue #8).
- Do not invent rules for the value score or the cost-per-hour calculation. They are still under discussion (issue #18). Ask instead.
- Keep changes small and focused on the task asked. Do not refactor unrelated code.
- After a change, build the app and report any errors instead of hiding them.

## Secrets

- Never read or print `local.properties`, keystores, or API keys. They are listed in `.aiexclude`.
