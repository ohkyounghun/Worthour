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
(How to handle the first cycle and yearly plans is under review: [#18](https://github.com/ohkyounghun/Worthour/issues/18).)

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

| Feature | Description |
|---------|-------------|
| Add subscription | Presets or manual add, linked to one or more apps |
| Usage tracking | Daily app usage time per service |
| Value analysis | Cost per hour after each billing cycle, with a Good / OK / Low score |
| Payment alerts | Before each payment. A free trial's end date counts as the first payment |
| Review before renewal | Low-score services get a "review before you pay" note in the payment alert |

**Cut from the proposal** to keep the scope right for a two-person team:
the payment calendar (the Home screen already lists upcoming payments) and the home screen widget.

## Screens

| # | Screen | Contents |
|---|--------|----------|
| 01 | Home | Monthly total, value badge (Measuring / Good / OK / Low), upcoming payments with D-day |
| 02 | Add Subscription | Presets, billing info, analysis method (Time Tracking / Non-Time Tracking), linked apps |
| 03 | Usage Permission | Why usage access is needed, one-tap jump to Android settings, privacy note |
| 04 | Subscription Detail | Cost per hour, daily usage list, edit or delete |
| 05 | Insights | One cost-per-hour bar chart, sorted so the least-worth services come first |
| 06 | Alerts | Payment alert (free trials included) with a review note for low-score services |

## Tech stack

| Component | Used for | Category |
|-----------|----------|----------|
| UsageStatsManager | App usage time per service | Device feature |
| WorkManager | Daily usage sync, payment reminders | Android component |
| Notification | Payment alerts with review note | Android component |
| Room | Local database for subscriptions and usage | Jetpack library |
| MPAndroidChart | Cost-per-hour chart | 3rd-party library |

> **External database: under review.** The proposal keeps all data on the device (Room).
> Based on feedback, connecting an external database is being considered. See [#8](https://github.com/ohkyounghun/Worthour/issues/8).

## Schedule

| Week | Work |
|------|------|
| W8 – W9 | UI design & DB schema |
| W9 – W10 | Subscription CRUD & presets |
| W9 – W11 | Usage tracking & daily sync |
| W11 – W12 | Payment alerts |
| W12 – W13 | Value report & chart |
| W13 – W14 | Polish (+ external DB, if adopted in [#8](https://github.com/ohkyounghun/Worthour/issues/8)) |
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
- Insights screen with chart
- Review note in payment alerts
- External DB integration (if adopted in #8)

**이찬행: subscription & payment management**
- Figma design, app theme and navigation
- Add, edit, presets and analysis method
- Home screen and Subscription Detail screen
- Payment alerts (free trials included)

**Together (week 1):** data model, Room database, screen skeleton and notification helper.

> Roles were rebalanced after the scope cut. Proposal under discussion: [#19](https://github.com/ohkyounghun/Worthour/issues/19).
