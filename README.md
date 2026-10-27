# Worthour

**Is your subscription really worth it?**

Worthour is an Android app that tracks your subscriptions and tells you whether each one is worth what you pay. It measures how long you actually use each service on your phone and turns that into a **cost per hour**.

> Mobile Programming · Team 3
> 오경훈 (22102004) · 이찬행 (22102010)

---

## Why

- **They pile up.** Video, music, cloud, apps. Each fee is small, so the total is easy to miss.
- **They renew silently.** Auto-renewal and free trials become paid plans without anyone noticing.
- **Their value is invisible.** We know what we pay. We don't know how much we actually use.

Typical trackers show a list of subscriptions, payment reminders and a monthly total.
Worthour adds **real usage time**, a **cost-per-hour score** and a **review alert before you pay**.

## Core idea: cost per hour

```
cost per hour = monthly fee ÷ hours used in the last billing cycle
```

Usage time comes from Android's app usage data (`UsageStatsManager`).
A score is shown only after one full billing cycle has been measured.

| Score | Cost per hour |
|-------|---------------|
| Good  | under ₩1,000/h |
| OK    | ₩1,000 – ₩2,000/h |
| Low   | above ₩2,000/h |

Example: a ₩17,000 monthly plan used for 2 hours last cycle → **₩8,500 per hour**.

## How it works

1. **Register:** pick a preset or add a subscription manually.
2. **Link:** connect it to one or more apps on the phone.
3. **Measure:** read daily usage time from Android once a day.
4. **Analyze:** show the value score and alert before payment.

## Features

| Priority | Feature | Description |
|----------|---------|-------------|
| Core | Add subscription | Presets or manual add, linked to one or more apps |
| Core | Usage tracking | Daily app usage time per service |
| Core | Value analysis | Cost per hour after each billing cycle, with a Good / OK / Low score |
| Core | Payment alerts | Before each payment and before a free trial ends |
| Key idea | Review before renewal | Flags services you rarely use |
| Extra | Payment calendar | When money leaves, at a glance |
| Extra | Home widget | Monthly total and next payment on the home screen |

## Screens

| # | Screen | Contents |
|---|--------|----------|
| 01 | Home | Monthly total, value badge (Measuring / Good / OK / Low), upcoming payments with D-day |
| 02 | Add Subscription | Presets, billing info, analysis method (Time Tracking / Non-Time Tracking), linked apps |
| 03 | Usage Permission | Why usage access is needed, one-tap jump to Android settings, privacy note |
| 04 | Subscription Detail | Cost per hour, daily usage, edit or delete |
| 05 | Insights | Cost-per-hour chart, review-before-renewal hint, least-used list |
| 06 | Payment Calendar | Monthly view, daily detail, upcoming payments |
| 07 | Alerts & Widget | Payment alert, free-trial alert, home widget |

## Tech stack

| Component | Used for | Category |
|-----------|----------|----------|
| UsageStatsManager | App usage time per service | Device feature |
| WorkManager | Daily usage sync, payment reminders | Android component |
| Notification | Payment, free-trial and review alerts | Android component |
| App Widget | Monthly total on the home screen | Widget |
| Room | Local database for subscriptions and usage | Jetpack library |
| MPAndroidChart | Cost-per-hour and usage charts | 3rd-party library |
| Calendar view library | Payment calendar screen | 3rd-party library |

> **External database: under review.** The proposal keeps all data on the device (Room).
> Based on feedback, connecting an external database is being considered. See the related issue.

## Schedule

| Week | Work |
|------|------|
| W8 – W9 | UI design & DB schema |
| W9 – W10 | Subscription CRUD & presets |
| W9 – W11 | Usage tracking & daily sync |
| W11 – W12 | Alerts & payment calendar |
| W12 – W13 | Value report & charts |
| W13 – W14 | Widget & polish |
| W14 – W15 | Testing & demo video |

| Milestone | Date |
|-----------|------|
| Proposal | 10/20 |
| WIP | 11/9 – 11/13 |
| Final | 12/6 |
| Interview | 12/8 |

## Roles

**오경훈: usage & value analysis**
- Usage access permission screen
- Usage tracking and daily sync
- Cost-per-hour logic and value score
- Detail and Insights screens with charts
- Review-before-renewal alert

**이찬행: subscription & payment management**
- Figma design, app theme and navigation
- Add, edit, presets and analysis method
- Home screen and payment calendar
- Payment and free-trial alerts
- Home screen widget

**Together (week 1):** data model, Room database, screen skeleton and notification helper.
