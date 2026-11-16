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
| UI | Jetpack Compose + Material 3, Navigation Compose |
| Local database | Room |
| Usage time | `UsageStatsManager` |
| Background work and alerts | WorkManager + `NotificationCompat` |
| Login and external database | Firebase Authentication + Cloud Firestore |

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
- Do not add libraries outside the tech stack above. Draw charts with Compose instead of a chart library.
- UI state lives in a ViewModel and is exposed as `StateFlow`. Composables do not access Room or system services directly.
- Screens read data through a repository, never through a DAO directly.
- Use KSP, not kapt, for Room.
- Add dependencies through `gradle/libs.versions.toml`, not hard-coded versions in `build.gradle.kts`.
- Put user-facing text in `res/values/strings.xml`.
- The team chose Firebase as the external database (issue #8). Use Firebase Authentication for login and Cloud Firestore for remote data. Network access is allowed only for Firebase.
- Do not invent rules for the value score or the cost-per-hour calculation. They are still under discussion (issue #18). Ask instead.
- Keep changes small and focused on the task asked. Do not refactor unrelated code.
- After a change, build the app and report any errors instead of hiding them.

## Secrets

- Never read or print `local.properties`, keystores, or API keys. They are listed in `.aiexclude`.
